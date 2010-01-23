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

package org.openengsb.maven.se;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.apache.maven.embedder.Configuration;
import org.apache.maven.embedder.DefaultConfiguration;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderConsoleLogger;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.reactor.MavenExecutionException;
import org.apache.servicemix.common.endpoints.ProviderEndpoint;
import org.openengsb.maven.common.exceptions.MavenException;
import org.openengsb.maven.common.pojos.LogLevelMaven;
import org.openengsb.maven.common.pojos.Options;
import org.openengsb.maven.common.pojos.ProjectConfiguration;
import org.openengsb.messages.maven.MavenResult;
import org.openengsb.util.serialization.JibxXmlSerializer;
import org.openengsb.util.serialization.Serializer;

public class AbstractMavenEndpoint extends ProviderEndpoint {

    // Container for extensions
    // if container support features for all domains, this could managed with
    // setter and getter in the <tt>AbstractMavenEndpoint</tt>

    protected Options options = new Options();
    protected ProjectConfiguration projectConfiguration = new ProjectConfiguration();
    protected LogLevelMaven loglevel = new LogLevelMaven();

    // values for events
    protected Date buildStartTime;
    protected Date buildEndTime;
    protected boolean settingsDefinied;

    protected Serializer serializer;

    public AbstractMavenEndpoint() {
        this.serializer = new JibxXmlSerializer();
    }

    /**
     * Executes the given Maven goals in the given directory.
     * 
     * @param file Directory in which the pom.xml is expected
     * @param goals Goals to execute for the pom.xml in the directory specified
     *        by parameter 'file'
     * @return Result of the Maven goal execution.
     * @throws MavenException
     */
    protected MavenResult execute(String file, List<String> goals) throws MavenException {
        return execute(file, goals, new Properties());
    }

    /**
     * Executes the given Maven goals in the given directory.
     * 
     * @param file Directory in which the pom.xml is expected
     * @param goals Goals to execute for the pom.xml in the directory specified
     *        by parameter 'file'
     * @return Result of the Maven goal execution.
     * @throws MavenException
     */
    @SuppressWarnings("unchecked")
    protected MavenResult execute(String file, List<String> goals, Properties properties) throws MavenException {
        MavenResult mavenResult = new MavenResult();

        // set up maven call
        File baseDirectory = new File(file);
        Configuration configuration = new DefaultConfiguration().setClassLoader(Thread.currentThread()
                .getContextClassLoader());

        if (this.options.getSettings() != null) {
            configuration.setUserSettingsFile(this.options.getSettings());
        } else {
            configuration.setUserSettingsFile(MavenEmbedder.DEFAULT_USER_SETTINGS_FILE);
        }

        MavenEmbedder embedder = null;
        MavenExecutionRequest request = null;

        if (configuration.getUserSettingsFile().exists()) {
            this.settingsDefinied = true;
        }

        try {
            embedder = new MavenEmbedder(configuration);
        } catch (MavenEmbedderException exception) {
            throw new MavenException(exception);
        }

        // further set up call
        request = new DefaultMavenExecutionRequest().setBaseDirectory(baseDirectory).setGoals(goals).setProperties(
                properties);

        request.setPom(new File(this.projectConfiguration.getBaseDirectory(), "/pom.xml"));

        // default logging is info level
        MavenEmbedderConsoleLogger consoleLogger = new MavenEmbedderConsoleLogger();
        consoleLogger.setThreshold(LogLevelMaven.ACTUAL_LEVEL);

        embedder.setLogger(consoleLogger);
        this.buildStartTime = new Date();

        MavenExecutionResult executionResult = embedder.execute(request);

        if (executionResult.hasExceptions()) {
            mavenResult.setExceptions(executionResult.getExceptions());
            mavenResult.setMavenOutput(MavenResult.ERROR);

            // construct errormessage
            StringBuilder stringBuilder = new StringBuilder();

            for (Object exceptionObject : executionResult.getExceptions()) {
                String exceptionMessage;
                if (exceptionObject instanceof Exception) {
                    exceptionMessage = ((Exception) exceptionObject).getMessage();
                } else {
                    exceptionMessage = exceptionObject.toString();
                }

                stringBuilder.append(exceptionMessage);
                stringBuilder.append("\n");
            }

            mavenResult.setErrorMessage(stringBuilder.toString());

            ReactorManager reactor = executionResult.getReactorManager();

            if (reactor != null && reactor.hasBuildFailures()) {
                MavenProject mavenProject;
                try {
                    mavenProject = embedder.readProject(new File(this.projectConfiguration.getBaseDirectory(),
                            "/pom.xml"));

                    if (reactor.hasBuildFailures()) {
                        BuildFailure buildFailure = executionResult.getReactorManager().getBuildFailure(mavenProject);
                        mavenResult.setTask(buildFailure.getTask());
                        mavenResult.setMavenOutput(MavenResult.FAILURE);
                    }

                } catch (ProjectBuildingException e) {
                    e.printStackTrace();
                } catch (MavenExecutionException e) {
                    e.printStackTrace();
                }
            }
        } else {
            mavenResult.setMavenOutput(MavenResult.SUCCESS);
        }

        this.buildEndTime = new Date();
        mavenResult.setTimestamp(new Date().getTime());

        return mavenResult;
    }
}
