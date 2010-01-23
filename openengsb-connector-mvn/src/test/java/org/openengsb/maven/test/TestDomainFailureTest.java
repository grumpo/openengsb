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

package org.openengsb.maven.test;

import java.io.File;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.codehaus.plexus.util.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.se.endpoints.MavenTestEndpoint;
import org.openengsb.maven.test.constants.TestMvnTestConstants;
import org.openengsb.messages.maven.MavenResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class TestDomainFailureTest extends TestCase {

    @Resource(name = "unit_constants_test")
    private TestMvnTestConstants CONSTANTS;

    private File pom;

    private File baseDirectory;

    private File testDir;

    private ClassPathResource res;

    @Before
    @Override
    public void setUp() throws Exception {
        this.res = new ClassPathResource(this.CONSTANTS.getTest_unit_fail());
        this.baseDirectory = this.res.getFile();

        this.pom = new File(this.baseDirectory, "/pom.xml");
        this.testDir = new File(this.baseDirectory, "/testDir");

        // create testDir and copy old pom.xml
        FileUtils.mkdir(this.testDir.getAbsolutePath());
        FileUtils.copyFileToDirectory(this.pom, this.testDir);
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        // Delete modified pom
        new File(this.baseDirectory, "pom.xml").delete();
        FileUtils.copyFileToDirectory(new File(this.testDir, "pom.xml"), this.baseDirectory);

        // delete copy and testDir
        new File(this.testDir, "pom.xml").delete();
        FileUtils.deleteDirectory(this.testDir);

    }

    @Test
    public void shouldFailBecauseOfAFailureByOneTest() throws Exception {
        final MavenTestEndpoint mavenTester = new MavenTestEndpoint();

        mavenTester.setBaseDirectory(this.baseDirectory);
        mavenTester.setStopAtFailuresOrErrors(false);

        mavenTester.configureSystem();

        mavenTester.getDom().writeInXMLFile(mavenTester.getDom().getDocument(),
                new File(this.baseDirectory, "pom-modified.xml"));

        // there are some whitespaces in the configuration tag, so you have to
        // navigate one node further
        assertEquals(mavenTester.getBaseDirectory().getAbsolutePath(), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getTextContent());

        assertEquals(String.valueOf(mavenTester.isStopAtFailuresOrErrors()), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getNextSibling().getTextContent());

        final MavenResult result = mavenTester.executeTests();
        assertEquals(MavenResult.FAILURE, result.getMavenOutput());
    }

    @Test
    public void test_shouldNotFailBecauseOfTheAttribute() throws Exception {
        // It is not necessary that the tests are successful

        final MavenTestEndpoint mavenTester = new MavenTestEndpoint();

        mavenTester.setBaseDirectory(this.baseDirectory);
        mavenTester.setStopAtFailuresOrErrors(true);
        mavenTester.configureSystem();

        mavenTester.getDom().writeInXMLFile(mavenTester.getDom().getDocument(),
                new File(this.baseDirectory, "pom-modified.xml"));

        // there are some whitespaces in the configuration tag, so you have to
        // navigate one node further
        assertEquals(mavenTester.getBaseDirectory().getAbsolutePath(), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getTextContent());

        assertEquals(String.valueOf(mavenTester.isStopAtFailuresOrErrors()), mavenTester.getDom()
                .getSurefireConfigurationNode().getFirstChild().getNextSibling().getNextSibling().getTextContent());

        final MavenResult result = mavenTester.executeTests();
        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

}
