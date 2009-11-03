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
package org.openengsb.prototype;

import java.io.IOException;

import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.xpath.CachedXPathAPI;
import org.openengsb.routingspike.Router;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * @org.apache.xbean.XBean element="prototypeEndpoint"
 *                         description="Prototype Component" The only
 *                         Prototype-Domain-Endpoint. This Endpoint is
 *                         responsible for forwarding all requests to an actual
 *                         connector, based on an id and a lookup-table
 */
public class PrototypeEndpoint extends ProviderEndpoint {

    private Log logger = LogFactory.getLog(getClass());

    private Router router = new Router();

    @Override
    protected void processInOnly(MessageExchange exchange, NormalizedMessage in) throws Exception {
        // simply forward to processInOutRequest; We'll distinguish between
        // InOut and InOnly later, based on the value of the out-message (i.e.
        // null or not-null)
        processInOut(exchange, in, null);
    }

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        NormalizedMessage result = router.route(in);

        if (out == null) {
            return;
        }

        InOut inOut = getExchangeFactory().createInOutExchange();
        inOut.setInMessage(result);

        out.setContent(result.getContent());
    }

    protected Node extractSingleNode(NormalizedMessage inMessage, String xPath) throws MessagingException,
            TransformerException, ParserConfigurationException, IOException, SAXException {
        Node rootNode = getRootNode(inMessage);
        if (rootNode == null) {
            return null;
        } else {
            CachedXPathAPI xpath = new CachedXPathAPI();
            return xpath.selectSingleNode(rootNode, xPath);
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
}
