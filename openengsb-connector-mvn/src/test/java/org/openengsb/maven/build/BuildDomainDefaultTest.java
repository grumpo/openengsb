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

package org.openengsb.maven.build;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import junit.framework.TestCase;

import org.apache.maven.embedder.MavenEmbedderException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openengsb.maven.build.constants.BuildMvnTestConstants;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.openengsb.maven.se.endpoints.MavenBuildEndpoint;
import org.openengsb.messages.maven.MavenResult;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/testbeans.xml" })
public class BuildDomainDefaultTest extends TestCase {

    @Resource(name = "unit_constants_build")
    private BuildMvnTestConstants CONSTANTS;

    private File baseDirectory;

    private File settings;

    private File installedFile;

    private ClassPathResource res;

    @Before
    @Override
    public void setUp() throws Exception {
        this.res = new ClassPathResource(this.CONSTANTS.getTest_project());
        this.baseDirectory = this.res.getFile();

        this.res = new ClassPathResource(this.CONSTANTS.getTest_settings_file());
        this.settings = this.res.getFile();

        this.installedFile = new File(this.settings.getParentFile().getParentFile(),
                "testRepo/org/test/project/build-test/1.0/build-test-1.0.jar");
        if (this.installedFile.exists()) {
            this.installedFile.delete();
        }
    }

    @Test
    public void shouldCleanProject() throws MavenEmbedderException, IOException, MavenException {
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setGoals(Arrays.asList(new String[] { "clean" }));
        projectConfig.setBaseDirectory(this.baseDirectory);

        Options options = new Options();
        options.setSettings(this.settings);

        MavenBuildEndpoint maven = new MavenBuildEndpoint();
        maven.setOptions(options);
        maven.setProjectConfiguration(projectConfig);
        MavenResult result = maven.executeBuild();

        if (result.getExceptions() != null) {
            List<Exception> list = result.getExceptions();

            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }

            assertEquals(0, result.getExceptions().size());
        }

        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
    }

    @Test
    public void shouldBuildSuccesful_JAR_clean_package() throws MavenEmbedderException, IOException, MavenException {
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setGoals(Arrays.asList(new String[] { "clean", "package" }));
        projectConfig.setBaseDirectory(this.baseDirectory);

        Options options = new Options();
        options.setSettings(this.settings);

        MavenBuildEndpoint maven = new MavenBuildEndpoint();
        maven.setProjectConfiguration(projectConfig);
        maven.setOptions(options);
        MavenResult result = maven.executeBuild();

        if (result.getExceptions() != null) {
            ArrayList<Exception> list = (ArrayList<Exception>) result.getExceptions();

            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }

            assertEquals(0, result.getExceptions().size());
        }

        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
        assertNotNull(maven.buildStartTime());
        assertNotNull(maven.buildEndTime());
        assertTrue(maven.settingsDefined());
    }

    @Test
    public void shouldBuildSuccesful_JAR_clean_install() throws MavenEmbedderException, IOException, MavenException {
        ProjectConfiguration projectConfig = new ProjectConfiguration();
        projectConfig.setGoals(Arrays.asList(new String[] { "clean", "install" }));
        projectConfig.setBaseDirectory(this.baseDirectory);

        Options options = new Options();
        options.setSettings(this.settings);

        MavenBuildEndpoint maven = new MavenBuildEndpoint();
        maven.setProjectConfiguration(projectConfig);
        maven.setOptions(options);
        MavenResult result = maven.executeBuild();

        if (result.getExceptions() != null) {
            ArrayList<Exception> list = (ArrayList<Exception>) result.getExceptions();

            for (int i = 0; i < list.size(); i++) {
                System.out.println(list.get(i));
            }

            assertEquals(0, result.getExceptions().size());
        }

        assertEquals(MavenResult.SUCCESS, result.getMavenOutput());
        assertTrue(this.installedFile.exists());
    }
}
