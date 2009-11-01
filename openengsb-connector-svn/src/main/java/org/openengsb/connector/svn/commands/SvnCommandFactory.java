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

import org.openengsb.scm.common.commands.AbstractScmCommandFactory;
import org.openengsb.scm.common.commands.AddCommand;
import org.openengsb.scm.common.commands.BlameCommand;
import org.openengsb.scm.common.commands.BranchCommand;
import org.openengsb.scm.common.commands.CheckoutCommand;
import org.openengsb.scm.common.commands.CommitCommand;
import org.openengsb.scm.common.commands.DeleteCommand;
import org.openengsb.scm.common.commands.DiffCommand;
import org.openengsb.scm.common.commands.ExportCommand;
import org.openengsb.scm.common.commands.ImportCommand;
import org.openengsb.scm.common.commands.ListBranchesCommand;
import org.openengsb.scm.common.commands.LogCommand;
import org.openengsb.scm.common.commands.MergeCommand;
import org.openengsb.scm.common.commands.SwitchBranchCommand;
import org.openengsb.scm.common.commands.UpdateCommand;


/**
 * Actual implementation of the AbstractCommandFactory-template. All
 * SVN-specific Command-implementations are instantiated here and filled with
 * SVN-specific parameters. Parameters, that are specific to a certain Command
 * are passed in AbstractCommandFactory.
 */
public class SvnCommandFactory extends AbstractScmCommandFactory {

    /* AbstractCommandFactory implementation */

    @Override
    public AddCommand createAddCommand() {
        // create and set up command
        SvnAddCommand command = new SvnAddCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public BlameCommand createBlameCommand() {
        // create and set up command
        SvnBlameCommand command = new SvnBlameCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public BranchCommand createBranchCommand() {
        // create and set up command
        SvnBranchCommand command = new SvnBranchCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public CheckoutCommand createCheckoutCommand() {
        // create and set up command
        SvnCheckoutCommand command = new SvnCheckoutCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public CommitCommand createCommitCommand() {
        // create and set up command
        SvnCommitCommand command = new SvnCommitCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public DeleteCommand createDeleteCommand() {
        // create and set up command
        SvnDeleteCommand command = new SvnDeleteCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public DiffCommand createDiffCommand() {
        // create and set up command
        SvnDiffCommand command = new SvnDiffCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public ExportCommand createExportCommand() {
        // create and set up command
        SvnExportCommand command = new SvnExportCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public ImportCommand createImportCommand() {
        // create and set up command
        SvnImportCommand command = new SvnImportCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public ListBranchesCommand createListBranchesCommand() {
        // create and set up command
        SvnListBranchesCommand command = new SvnListBranchesCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public LogCommand createLogCommand() {
        // create and set up command
        SvnLogCommand command = new SvnLogCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public MergeCommand createMergeCommand() {
        // create and set up command
        SvnMergeCommand command = new SvnMergeCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public SwitchBranchCommand createSwitchBranchCommand() {
        // create and set up command
        SvnSwitchBranchCommand command = new SvnSwitchBranchCommand();
        setUpCommand(command);

        return command;
    }

    @Override
    public UpdateCommand createUpdateCommand() {
        // create and set up command
        SvnUpdateCommand command = new SvnUpdateCommand();
        setUpCommand(command);

        return command;
    }

    /* end AbstractCommandFactory implementation */

    /* helpers */

    /**
     * Initializes the library to work with a repository via different
     * protocols.
     */
    private static void setupLibrary() {
        // TODO implement via client adapter
    }

    protected void init() {
        setupLibrary();
    }

    private void setUpCommand(AbstractSvnCommand<?> command) {
        // set scm-specific parameters
        setScmParameters(command);
    }

    /* end helpers */
}
