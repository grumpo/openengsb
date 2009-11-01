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
package org.openengsb.connector.svn.commands;

import org.openengsb.scm.common.commands.AbstractScmCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;


/**
 * An abstract implementation of the Command-interface to be used by all
 * SVN-Commands. It holds svn-specific members (with their getters and setters),
 * along with some convenience-methods and default implementations of
 * svnKit-specific handlers.
 * 
 * @param <Returntype> The type the command shall return form it's execute-call.
 */
public abstract class AbstractSvnCommand<ReturnType> extends AbstractScmCommand<ReturnType> {
    protected static final String HEAD_KEYWORD = "HEAD";
    protected static final String TRUNK_KEYWORD = "TRUNK";
    protected static final String BRANCHES = "branches";
    protected static final String TRUNK = "trunk";
    
    private ISVNClientAdapter client = null;
    
    // TODO inject
    protected String clientType = "javahl";
    
    /* getters and setters */

    protected ISVNClientAdapter getClient() {
    	if(this.client == null) {
    		this.client = SVNClientAdapterFactory.createSVNClient(this.clientType);
    	}
        return this.client;
    }

    /* end getters and setters */

    /* helpers */

    /**
     * Helper that determines the Repository's URL solely from the working copy
     * 
     * @return The retreived URL
     * @throws ScmException
     */
    protected SVNUrl getRepositoryUrl() throws ScmException {
        try {
            return getClient().getInfo(getWorkingCopy()).getRepository();

        } catch (SVNClientException exception) {
            throw new ScmException(exception);
        }
    }

    /**
     * Helper that determines the Repository's URL honoring the checkout-path.
     * That is, if not the whole repository but a sub-path within it was checked
     * out to create this working-copy, the repository's URL + the subpath is
     * returned
     * 
     * @return The requested URL + subpath
     * @throws ScmException
     */
    protected SVNUrl getRepositoryUrlRelativeToWorkigCopy() throws ScmException {
        try {
            return getClient().getInfo(getWorkingCopy()).getUrl();

        } catch (SVNClientException exception) {
            throw new ScmException(exception);
        }
    }

    /* end helpers */

}
