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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.openengsb.scm.common.commands.CheckoutCommand;
import org.openengsb.scm.common.commands.Command;
import org.openengsb.scm.common.exceptions.ScmException;
import org.openengsb.scm.common.pojos.MergeResult;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;
import org.tigris.subversion.svnclientadapter.utils.Depth;

/**
 * A Command that checks out the remote repository's content into the folder
 * supplied in the SU-configuration. Implements the general contracts of
 * <code>{@link Command}</code> and <code>{@link CheckoutCommand}</code>.
 */
public class SvnCheckoutCommand extends AbstractSvnCommand<MergeResult>
		implements CheckoutCommand {
	@SuppressWarnings("unused")
	private String author = null; // TODO use

	@Override
	public MergeResult execute() throws ScmException {

		// set up parameters
		SVNUrl svnUrl;
		try {
			svnUrl = new SVNUrl(new URI(getRepositoryUri().getScheme(),
					getRepositoryUri().getUserInfo(), getRepositoryUri()
							.getHost(), getRepositoryUri().getPort(),
					getRepositoryUri().getPath(), "", "").toString());

		} catch (MalformedURLException e) {
			throw new ScmException(e.getMessage(), e);
		} catch (URISyntaxException e) {
			throw new ScmException(e.getMessage(), e);
		}

		// set up client
		final ArrayList<String> checkedOutFiles = new ArrayList<String>();
		final ISVNNotifyListener listener = new DefaultNotifyListener() {

			/*
			 * (non-Javadoc)
			 * @see	org.openengsb.connector.svn.commands.DefaultNotifyListener#onNotify(java.io.File, org.tigris.subversion.svnclientadapter.SVNNodeKind)
			 */
			@Override
			public void onNotify(File file, SVNNodeKind kind) {
				super.onNotify(file, kind);

				if (kind.equals(SVNNodeKind.FILE)) {
					checkedOutFiles.add(file.getPath());
				}
			}
		};
		getClient().addNotifyListener(listener);

		try {
			// call checkout
			getClient().checkout(svnUrl, getWorkingCopy(), SVNRevision.HEAD,
					Depth.infinity, true, true);

			MergeResult result = new MergeResult();
			result.setAdds(checkedOutFiles.toArray(new String[checkedOutFiles
					.size()]));
			result.setRevision(String.valueOf(getClient().getInfo(
					getWorkingCopy()).getCopyRev().getNumber()));
			return result;
		} catch (SVNClientException e) {
			throw new ScmException(e.getMessage(), e);
		} finally {
			// unregister listener
			getClient().removeNotifyListener(listener);
		}

	}

	@Override
	public void setAuthor(String author) {
		this.author = author;
	}
}
