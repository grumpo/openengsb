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

package org.openengsb.maven.installfile;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.InOut;
import javax.jbi.messaging.MessagingException;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;
import org.apache.servicemix.tck.SpringTestSupport;
import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.messages.maven.InstallFileDescriptor;
import org.openengsb.messages.maven.InstallFileMessage;
import org.openengsb.messages.maven.MavenResult;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.Serializer;
import org.springframework.context.support.AbstractXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class MavenInstallFileIntegrationTest extends SpringTestSupport {

    private static final String validFilePath = "target/test-classes/installfile-valid/installfile-sample.jar";
    private static final String invalidFilePath = "target/test-classes/installfile-invalid/installfile-sample.jar";
    private static final String groupId = "com.installfilevalidgroup";
    private static final String artifactId = "installfilevalidartifact";
    private static final String version = "installfilevalidversion-1.0";
    private static final String packaging = "jar";
    private static final String xbean = "spring-test-xbean-installfile.xml";
    private static final String testNamespace = "urn:test";
    private static final String serviceName = "fileInstallerService";
    private static Serializer serializer = new JibxXmlSerializer();

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected AbstractXmlApplicationContext createBeanFactory() {
        return new ClassPathXmlApplicationContext(xbean);
    }

    private InOut createInOutMessage(DefaultServiceMixClient client, String service, Source source)
            throws MessagingException {
        InOut inOut = client.createInOutExchange();
        inOut.setService(new QName(testNamespace, service));
        inOut.getInMessage().setContent(source);

        return inOut;
    }

    @Test
    public void successfulRunShouldReturnPositiveResult() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(jbi);
        StringWriter sw = new StringWriter();
        serializer.serialize(new InstallFileMessage(new InstallFileDescriptor(validFilePath, groupId, artifactId,
                version, packaging)), sw);
        InOut messageExchange = createInOutMessage(client, serviceName, new StringSource(sw.toString()));
        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        MavenResult result = serializer.deserialize(MavenResult.class, new StringReader(transformMessageToString(messageExchange.getOutMessage())));
        client.done(messageExchange);

        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    public void runWithNonExistingFilePathShouldReturnError() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(jbi);
        StringWriter sw = new StringWriter();
        serializer.serialize(new InstallFileMessage(new InstallFileDescriptor(invalidFilePath, groupId, artifactId,
                version, packaging)), sw);
        InOut messageExchange = createInOutMessage(client, serviceName, new StringSource(sw.toString()));
        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        MavenResult result = serializer.deserialize(MavenResult.class, new StringReader(transformMessageToString(messageExchange.getOutMessage())));
        client.done(messageExchange);

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
    }

    @Test
    public void runWithInvalidMessageFormatShouldReturnError() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(jbi);
        InOut messageExchange = createInOutMessage(client, serviceName, new StringSource(
                "<invalidmessage invalidattribute=\"invalid\" />"));
        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        MavenResult result = serializer.deserialize(MavenResult.class, new StringReader(transformMessageToString(messageExchange.getOutMessage())));
        client.done(messageExchange);

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertEquals("An error occurred deserializing the incoming message.", result.getErrorMessage());
        //assertEquals(1, result.getExceptions().size());
        //assertEquals("Error deserializing from reader.", result.getExceptions().get(0).getMessage());
    }

    @Test
    public void runWithInvalidParametersShouldReturnError() throws Exception {
        DefaultServiceMixClient client = new DefaultServiceMixClient(jbi);

        StringWriter sw = new StringWriter();
        serializer.serialize(new InstallFileMessage(new InstallFileDescriptor("", "", "", "", "")), sw);
        InOut messageExchange = createInOutMessage(client, serviceName, new StringSource(sw.toString()));

        client.sendSync(messageExchange);

        validateReturnMessage(messageExchange);

        MavenResult result = serializer.deserialize(MavenResult.class, new StringReader(transformMessageToString(messageExchange.getOutMessage())));
        client.done(messageExchange);

        assertEquals(MavenResult.ERROR, result.getMavenOutput());
        assertEquals("Cannot install file. The given file descriptor is invalid.", result.getErrorMessage());
    }

    // TODO Refactor this, since this method is also used by other maven tests
    // ... pull it into separate maven test base class
    private void validateReturnMessage(InOut message) throws Exception {
        if (message.getStatus() == ExchangeStatus.ERROR) {
            if (message.getError() != null) {
                throw message.getError();
            } else {
                fail("Received ERROR status");
            }
        } else if (message.getFault() != null) {
            fail("Received fault: " + new SourceTransformer().toString(message.getFault().getContent()));
        }
    }
    
    private String transformMessageToString(NormalizedMessage msg) throws TransformerFactoryConfigurationError,
	    TransformerException {
		Transformer messageTransformer = TransformerFactory.newInstance().newTransformer();
		StringWriter stringWriter = new StringWriter();
		messageTransformer.transform(msg.getContent(), new StreamResult(stringWriter));
		return stringWriter.toString();
	}

}