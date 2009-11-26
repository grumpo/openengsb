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

package org.openengsb.maven.se.endpoints;

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;

import org.apache.maven.wagon.PathUtils;
import org.openengsb.maven.common.domains.InstallFileDomain;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.messages.InstallFileMessage;
import org.openengsb.maven.common.pojos.InstallFileDescriptor;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.result.MavenResult;
import org.openengsb.maven.common.serializer.MavenResultSerializer;
import org.openengsb.maven.se.AbstractMavenEndpoint;
import org.openengsb.util.serialization.SerializationException;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Endpoint implementing maven's install:install-file functionality.
 * 
 * @org.apache.xbean.XBean element="mavenFileInstaller"
 */
public class MavenInstallFileEndpoint extends AbstractMavenEndpoint implements InstallFileDomain {

    @Override
    protected void processInOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out) throws Exception {
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        List<MavenResult> resultList = new ArrayList<MavenResult>();
        try {
            InstallFileMessage msg = serializer.deserialize(InstallFileMessage.class, new StringReader(
                    transformMessageToString(in)));
            InstallFileDescriptor descriptor = msg.getFileDescriptor();

            MavenResult installResult = installFile(descriptor);

            resultList.add(installResult);
        } catch (SerializationException ex) {
            MavenResult result = new MavenResult();
            result.setErrorMessage("An error occurred deserializing the incoming message.");

            List<Exception> exceptions = new ArrayList<Exception>();
            exceptions.add(ex);
            result.setExceptions(exceptions);

            result.setMavenOutput(MavenResult.ERROR);
            resultList.add(result);
        }

        out.setContent(MavenResultSerializer.serialize(out.getContent(), resultList));
        getChannel().send(exchange);
    }

    @SuppressWarnings("unchecked")
    @Override
    public MavenResult installFile(InstallFileDescriptor fileDescriptor) throws MavenException {
        if (fileDescriptor == null) {
            MavenResult result = new MavenResult();
            result.setMavenOutput(MavenResult.ERROR);
            result.setErrorMessage("Cannot install file. The given file descriptor is null.");
            return result;
        }
        if (!fileDescriptor.validate()) {
            MavenResult result = new MavenResult();
            result.setMavenOutput(MavenResult.ERROR);
            result.setErrorMessage("Cannot install file. The given file descriptor is invalid.");
            return result;
        }

        String fileName = PathUtils.filename(fileDescriptor.getFilePath());
        String pathToFile = PathUtils.dirname(fileDescriptor.getFilePath());

        Properties props = new Properties();
        props.setProperty("file", fileName);
        props.setProperty("groupId", fileDescriptor.getGroupId());
        props.setProperty("artifactId", fileDescriptor.getArtifactId());
        props.setProperty("version", fileDescriptor.getVersion());
        props.setProperty("packaging", fileDescriptor.getPackaging());

        this.projectConfiguration.setBaseDirectory(new File(pathToFile));

        return execute(pathToFile, new ArrayList<String>(Arrays.asList(new String[] { "install:install-file" })), props);
    }

    public void setOptions(Options options) {
        this.options = options;
    }

    private String transformMessageToString(NormalizedMessage msg) throws TransformerFactoryConfigurationError,
            TransformerException {
        Transformer messageTransformer = TransformerFactory.newInstance().newTransformer();
        StringWriter stringWriter = new StringWriter();
        messageTransformer.transform(msg.getContent(), new StreamResult(stringWriter));
        return stringWriter.toString();
    }
}
