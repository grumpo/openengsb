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

package org.openengsb.maven;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.openengsb.messages.maven.MavenResult;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.SerializationException;
import org.openengsb.util.serialization.Serializer;

public class SerializationTest {

	private Serializer serializer;
	
    @Test
    public void JibxSerializationOfMavenResultShouldSucceedWithValidInput()
            throws URISyntaxException, SerializationException, IOException {
        File validFile = new File(ClassLoader.getSystemResource("valid-mavenresult.xml")
                .toURI());

        MavenResult msg = new MavenResult();
        msg.setMavenOutput("mavenOutput");
        msg.setTask("task");
        msg.setErrorMessage("errorMessage");
        msg.setFile("file");
        msg.setDeployedFiles(new String[] { "deployedFile1", "deployedFile2", "deployedFile3" });
        
        StringWriter sw = new StringWriter();

        serializer = new JibxXmlSerializer();
        serializer.serialize(msg, sw);

        assertEquals(getWhitespaceAdjustedTextFromFile(validFile), sw.toString());
    }
    
    @Test
    public void JibxSerializationOfMavenResultShouldSucceedWithNullValueForEverything()
            throws URISyntaxException, SerializationException, IOException {
        File validFile = new File(ClassLoader.getSystemResource("valid-mavenresult-witheverythingisnull.xml")
                .toURI());

        MavenResult msg = new MavenResult();
        msg.setMavenOutput(null);
        msg.setTask(null);
        msg.setErrorMessage(null);
        msg.setFile(null);
        msg.setDeployedFiles(null);
        
        StringWriter sw = new StringWriter();

        serializer = new JibxXmlSerializer();
        serializer.serialize(msg, sw);

        assertEquals(getWhitespaceAdjustedTextFromFile(validFile), sw.toString());
    }
    
    @Test
    public void JibxDeserializationOfMavenResultShouldSucceedWithValidInput()
            throws URISyntaxException, FileNotFoundException, SerializationException {
        File validFile = new File(ClassLoader.getSystemResource("valid-mavenresult.xml").toURI());

        serializer = new JibxXmlSerializer();

        MavenResult msg = serializer.deserialize(MavenResult.class,
                new FileReader(validFile));

        assertEquals("mavenOutput", msg.getMavenOutput());
        assertEquals("task", msg.getTask());
        assertEquals("errorMessage", msg.getErrorMessage());
        assertEquals("file", msg.getFile());
        assertEquals(3, msg.getDeployedFiles().length);
        assertEquals("deployedFile1", msg.getDeployedFiles()[0]);
        assertEquals("deployedFile2", msg.getDeployedFiles()[1]);
        assertEquals("deployedFile3", msg.getDeployedFiles()[2]);
    }
	
    private String getWhitespaceAdjustedTextFromFile(File file) throws IOException {
        return FileUtils.readFileToString(file).replaceAll("\n", "").replaceAll(">\\s*<", "><").replaceAll("<!--.*-->",
                "");
    }
    
}
