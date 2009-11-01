package org.openengsb.connector.svn.commands;

import java.io.File;

import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;
import org.tigris.subversion.svnclientadapter.SVNNodeKind;

/**
 * Default implementation of ISVNNotifyListener. Overwrite individual methods for specific logging needs.
 */
public class DefaultNotifyListener implements ISVNNotifyListener {

	@Override
	public void logCommandLine(String arg0) {
		// no action
	}

	@Override
	public void logCompleted(String arg0) {
		// no action
	}

	@Override
	public void logError(String arg0) {
		// no action
	}

	@Override
	public void logMessage(String arg0) {
		// no action
	}

	@Override
	public void logRevision(long arg0, String arg1) {
		// no action
	}

	@Override
	public void onNotify(File arg0, SVNNodeKind arg1) {
		// no action
	}

	@Override
	public void setCommand(int arg0) {
		// no action
	}

}
