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
package org.openengsb.core;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.openengsb.contextcommon.ContextHelper;

public abstract class OpenEngSBForwardEndpoint<T> extends OpenEngSBEndpoint<T> {

    public OpenEngSBForwardEndpoint() {
        super();
    }

    public OpenEngSBForwardEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public OpenEngSBForwardEndpoint(ServiceUnit serviceUnit, QName service, String endpoint) {
        super(serviceUnit, service, endpoint);
    }

    @Override
    protected T getImplementation(ContextHelper contextHelper) {
        return null;
    }

    @Override
    protected void inOut(MessageExchange exchange, NormalizedMessage in, NormalizedMessage out,
            ContextHelper contextHelper) throws Exception {
        QName defaultConnector = getForwardTarget(contextHelper);
        forwardMessage(exchange, in, out, defaultConnector);
    }

}