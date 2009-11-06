/**

 Copyright 2009 OpenEngSB Division, Vienna University of Technology

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 */

package org.openengsb.routingspike;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.jbi.messaging.NormalizedMessageImpl;
import org.apache.xpath.CachedXPathAPI;
import org.openengsb.routingspike.MagicContext.ContextObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Router {

    private final static String SCRIPT_PATH = "scripts/";

    private MagicContext magicContext = new MagicContext();

    public NormalizedMessage route(NormalizedMessage inMessage) {
        String id = getIdFromMessage(inMessage);
        ContextObject ctx = magicContext.resolve(id);

        if (ctx == null) {
            throw new RuntimeException(String.format("No context found for ID '%s'", id));
        }

        String script = ctx.filename;
        String config = ctx.configuration;

        Pattern scalaPattern = Pattern.compile(".*\\.scala$", Pattern.CASE_INSENSITIVE);
        if (!scalaPattern.matcher(script).matches()) {
            throw new RuntimeException(String.format("File type not supported. Script '%s' cannot be processed.",
                    script));
        }

        ScriptResult scriptResult = new ScriptResult();
        ScalaExecutor scala = new ScalaExecutor();
        scala.bind("id", String.class, id);
        scala.bind("config", String.class, config);
        scala.bind("scriptResult", ScriptResult.class, scriptResult);
        scala.execute(new File(SCRIPT_PATH + "openengsb-dsl.scala"));
        scala.execute(new File(SCRIPT_PATH + script));

        NormalizedMessage outMessage = new NormalizedMessageImpl();
        try {
            outMessage.setContent(new StringSource(scriptResult.getResult()));
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }

        return outMessage;
    }

    private String getIdFromMessage(NormalizedMessage inMessage) {
        Node idNode = extractSingleNode(inMessage, "./@id");

        if (idNode == null) {
            throw new RuntimeException("ID not found");
        }

        String id = idNode.getNodeValue();
        return id;
    }

    protected Node extractSingleNode(NormalizedMessage inMessage, String xPath) {
        CachedXPathAPI xpath = new CachedXPathAPI();

        try {
            Node rootNode = getRootNode(inMessage);

            if (rootNode == null) {
                return null;
            }

            return xpath.selectSingleNode(rootNode, xPath);

        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SAXException e) {
            throw new RuntimeException(e);
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    private Node getRootNode(NormalizedMessage message) throws ParserConfigurationException, IOException, SAXException,
            TransformerException {
        SourceTransformer sourceTransformer = new SourceTransformer();
        DOMSource messageXml = sourceTransformer.toDOMSource(message.getContent());

        Node rootNode = messageXml.getNode();

        if (rootNode instanceof Document) {
            return rootNode.getFirstChild();
        } else {
            return rootNode;
        }
    }

    public static void main(String[] args) throws IOException {
        Router router = new Router();

        NormalizedMessageImpl inMessage = new NormalizedMessageImpl();
        inMessage.setContent(new StringSource(
                "<message id=\"42\"><payload><timer><name>Foo</name><group>Bar</group></timer></payload></message>"));

        NormalizedMessage outMessage = router.route(inMessage);
        System.out.println("Result is: " + outMessage);
        System.out.println(outMessage.getContent());
    }
}
