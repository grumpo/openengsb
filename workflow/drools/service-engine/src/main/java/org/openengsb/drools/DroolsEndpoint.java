/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE\-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package org.openengsb.drools;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.jbi.servicedesc.ServiceEndpoint;
import javax.xml.namespace.QName;

import org.apache.servicemix.common.DefaultComponent;
import org.apache.servicemix.common.ServiceUnit;
import org.drools.RuleBase;
import org.drools.agent.RuleAgent;
import org.openengsb.contextcommon.ContextHelper;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.endpoints.SimpleEventEndpoint;
import org.openengsb.core.model.Event;

/**
 * @org.apache.xbean.XBean element="droolsEndpoint"
 *                         description="Drools Component"
 */
public class DroolsEndpoint extends SimpleEventEndpoint {

    private RuleBase ruleBase;

    /**
     * List of global variables for rules to use.
     */
    private Map<String, Object> globals = new HashMap<String, Object>();

    public DroolsEndpoint() {
    }

    public DroolsEndpoint(DefaultComponent component, ServiceEndpoint endpoint) {
        super(component, endpoint);
    }

    public DroolsEndpoint(ServiceUnit su, QName service, String endpoint) {
        super(su, service, endpoint);
    }

    @Override
    protected void handleEvent(Event e, ContextHelper contextHelper, MessageProperties msgProperties) {
        if (ruleBase == null) {
            init();
        }
        drools(e, msgProperties);
    }

    private void init() {
        Properties config = new Properties();
        config.put("url", "http://localhost:8081/drools-guvnor/org.drools.guvnor.Guvnor/package/org.openengsb/LATEST");
        RuleAgent agent = RuleAgent.newRuleAgent(config);
        RuleBase ruleBase = agent.getRuleBase();
        setRuleBase(ruleBase);
    }

    public RuleBase getRuleBase() {
        return this.ruleBase;
    }

    public void setRuleBase(RuleBase ruleBase) {
        this.ruleBase = ruleBase;
    }

    public Map<String, Object> getGlobals() {
        return this.globals;
    }

    public void setGlobals(Map<String, Object> variables) {
        this.globals = variables;
    }

    /**
     * handle the MessageExchange with drools.
     * 
     * @param e2 exchange to handle
     */
    protected void drools(Event e, MessageProperties msgProperties) {
        Collection<Object> objects = Arrays.asList(new Object[] { e });
        DroolsExecutionContext drools = new DroolsExecutionContext(this, objects, msgProperties);
        try {
            drools.start();
        } finally {
            shutdown(drools);
        }
    }

    private void shutdown(DroolsExecutionContext drools) {
        drools.stop();
        this.ruleBase = null;
    }

}