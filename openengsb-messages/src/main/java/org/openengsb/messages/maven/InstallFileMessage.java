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

package org.openengsb.messages.maven;

public class InstallFileMessage {
    private InstallFileDescriptor fileDescriptor;

    public InstallFileMessage() {

    }

    public InstallFileMessage(InstallFileDescriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    public InstallFileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    public void setFileDescriptor(InstallFileDescriptor fileDescriptor) {
        this.fileDescriptor = fileDescriptor;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof InstallFileMessage)) {
            return false;
        }

        InstallFileMessage other = (InstallFileMessage) obj;

        return this.fileDescriptor.equals(other.getFileDescriptor());
    }
}