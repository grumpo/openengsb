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

import java.io.File;
import java.util.ArrayList;

import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.commands.CommitCommand;
import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.scm.common.pojos.MergeResult;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;


/**
 * A Command that commits all changes within the whole working copy or a
 * sub-path thereof and submits them to the remote repository. Implements the
 * general contracts of <code>{@link Command}</code> and
 * <code>{@link CommitCommand}</code>.
 */
public class SvnCommitCommand extends AbstractSvnCommand<MergeResult> implements CommitCommand {
    private String subPath = null;
    private String author = null;
    private String message = null;

    @Override
    public MergeResult execute() throws ScmException {
        if (!canWriteToRepository()) {
            throw new ScmException("Must not write to repository (set developerConnection to be able to do so)");
        }

        // set up parameters
        File[] paths = new File[] { getWorkingCopy() };
        if ((this.subPath != null) && (!this.subPath.isEmpty())) {
            paths[0] = new File(paths[0], this.subPath);
        }

        String commitMessage = (this.author.isEmpty() ? "" : this.author + ":\n") + this.message;

        // set up intermediate lists for result
        final ArrayList<String> addedFiles = new ArrayList<String>();
        final ArrayList<String> mergedFiles = new ArrayList<String>();
        final ArrayList<String> deletedFiles = new ArrayList<String>();

        // set up client
        final ISVNNotifyListener listener = new DefaultNotifyListener() {
        	 
        	/* (non-Javadoc)
			 * @see org.openengsb.connector.svn.commands.DefaultNotifyListener#onNotify(java.io.File, org.tigris.subversion.svnclientadapter.SVNNodeKind)
			 */
			@Override
			public void onNotify(File file, SVNNodeKind kind) {
				super.onNotify(file, kind);
				
				// TODO distinct add, delete and merge
				if(kind.equals(SVNNodeKind.FILE)) {
					mergedFiles.add(file.getPath());
				}
			}
        };
        getClient().addNotifyListener(listener);
        
        try {
            // perform call
            long revisionId = getClient().commit(paths, commitMessage, true);
            
            // set up and fill result
            MergeResult result = new MergeResult();
            result.setAdds(addedFiles.toArray(new String[addedFiles.size()]));
            result.setDeletions(deletedFiles.toArray(new String[deletedFiles.size()]));
            result.setMerges(mergedFiles.toArray(new String[mergedFiles.size()]));
            result.setRevision(String.valueOf(revisionId));

            // TODO find out how to collect conflicting files...
            // conflicting files are reported in errormessages and therefore
            // should be treated as error in SVN
            return result;
            
        } catch (SVNClientException exception) {
            throw new ScmException(exception);
        } finally {
            // unregister listener
            getClient().removeNotifyListener(listener);
        }
    }

    @Override
    public void setSubPath(String subPath) {
        this.subPath = subPath;
    }

    @Override
    public void setAuthor(String author) {
        this.author = author;
    }

    @Override
    public void setMessage(String message) {
        this.message = message;
    }

}
