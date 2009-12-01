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

package org.openengsb.maven.serializer;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.messages.maven.InstallFileDescriptor;
import org.openengsb.messages.maven.InstallFileMessage;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;
import org.openengsb.util.serialization.Serializer;

public class InstallFileMessageSerializerTest {

    final String validMessageTemplate = "<mavenFileInstaller fileToInstall=\"%s\" groupId=\"%s\" artifactId=\"%s\" version=\"%s\" packaging=\"%s\"/>";
    final String invalidMessageTemplate = "<mavenFileInstallerInvalid fileToInstallInvalid=\"\" groupIdInvalid=\"\" artifactIdInvalid=\"\" versionInvalid=\"\" packagingInvalid=\"\"/>";

    final String filePath = "myfilepath";
    final String groupId = "mygroupId";
    final String artifactId = "myartifactId";
    final String version = "myversion1.0";
    final String packaging = "jar";

    private Serializer serializer;

    @Before
    public void setup() {
        this.serializer = new JibxXmlSerializer();
    }

    @Test
    public void serializeValidDescriptorShouldSucceed() throws TransformerException, SerializationException,
            URISyntaxException, IOException {
        File validFile = new File(ClassLoader.getSystemResource("valid-installfilemessage.xml").toURI());
        InstallFileDescriptor descriptor = new InstallFileDescriptor(this.filePath, this.groupId, this.artifactId,
                this.version, this.packaging);
        InstallFileMessage msg = new InstallFileMessage(descriptor);

        StringWriter sw = new StringWriter();
        serializer.serialize(msg, sw);

        assertEquals(getWhitespaceAdjustedTextFromFile(validFile), sw.toString());
    }

    @Test(expected = SerializationException.class)
    public void serializeInvalidDescriptorShouldThrowSerializationException() throws SerializationException {
        InstallFileDescriptor descriptor = new InstallFileDescriptor(null, this.groupId, this.artifactId, this.version,
                this.packaging);
        InstallFileMessage msg = new InstallFileMessage(descriptor);
        serializer.serialize(msg, new StringWriter());
    }

    @Test
    public void deserializeValidInputShouldSucceed() throws SerializationException, URISyntaxException,
            FileNotFoundException {
        File validFile = new File(ClassLoader.getSystemResource("valid-installfilemessage.xml").toURI());

        InstallFileMessage msg = serializer.deserialize(InstallFileMessage.class, new FileReader(validFile));

        InstallFileDescriptor descriptor = msg.getFileDescriptor();

        Assert.assertNotNull(descriptor);
        Assert.assertEquals(this.filePath, descriptor.getFilePath());
        Assert.assertEquals(this.groupId, descriptor.getGroupId());
        Assert.assertEquals(this.artifactId, descriptor.getArtifactId());
        Assert.assertEquals(this.version, descriptor.getVersion());
        Assert.assertEquals(this.packaging, descriptor.getPackaging());
    }

    @Test(expected = SerializationException.class)
    public void deserializeInvalidSourceShouldThrowSerializationException() throws SerializationException {
        serializer.deserialize(InstallFileMessage.class, new StringReader(this.invalidMessageTemplate));
    }

    private String getWhitespaceAdjustedTextFromFile(File file) throws IOException {
        return FileUtils.readFileToString(file).replaceAll("\n", "").replaceAll(">\\s*<", "><").replaceAll("<!--.*-->",
                "");
    }

}
