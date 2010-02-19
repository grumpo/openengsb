/**

   Copyright 2010 OpenEngSB Division, Vienna University of Technology

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
package org.openengsb.edb.jbi.endpoints;

import java.util.HashMap;
import java.util.Map;

import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.xml.transform.Source;

import org.apache.servicemix.jbi.jaxp.StringSource;
import org.openengsb.core.EventHelper;
import org.openengsb.core.EventHelperImpl;
import org.openengsb.core.MessageProperties;
import org.openengsb.core.model.Event;
import org.openengsb.drools.events.EdbCommitEvent;
import org.openengsb.edb.core.api.EDBHandler;
import org.openengsb.edb.jbi.endpoints.commands.EDBCommit;
import org.openengsb.edb.jbi.endpoints.commands.EDBEndpointCommand;
import org.openengsb.edb.jbi.endpoints.commands.EDBQuery;
import org.openengsb.edb.jbi.endpoints.commands.EDBRegisterLink;
import org.openengsb.edb.jbi.endpoints.commands.EDBRequestLink;
import org.openengsb.edb.jbi.endpoints.commands.EDBReset;
import org.openengsb.edb.jbi.endpoints.commands.EDBEndpointCommand.CommandResult;
import org.openengsb.edb.jbi.endpoints.responses.DefaultAcmResponseBuilder;
import org.openengsb.edb.jbi.endpoints.responses.EDBEndpointResponseBuilder;
import org.openengsb.edb.jbi.endpoints.responses.LinkRegisteredResponseBuilder;
import org.openengsb.edb.jbi.endpoints.responses.LinkRequestResponseBuilder;

import edu.emory.mathcs.backport.java.util.Collections;

/**
 * @org.apache.xbean.XBean element="edb" The Endpoint to the commit-feature
 *
 */
public class EdbEndpoint extends AbstractEndpoint {
    /*
     * Operations
     *
     * Strings to identify an operation. {@link
     * javax.jbi.messaging.MessageExchange} requires a QName as operation.
     *
     * setOperation(new QName(OPERATION_COMMIT))
     *
     * The namespace is ignored in the operation-check
     */

    public static final String DEFAULT_USER = "EDB";
    public static final String DEFAULT_EMAIL = "EDB@openengsb.org";

    public static final String COMMIT_OPERATION_TAG_NAME = "operation";
    public static final String QUERY_ELEMENT_NAME = "query";
    public static final int DEFAULT_DEPTH = 1;

    public static final int LINK_TARGET_ELEMENT = 1;

    // should be set via spring ?
    private Map<EDBOperationType, EDBEndpointCommand> commands;
    private Map<EDBOperationType, EDBEndpointResponseBuilder> reponses;

    @SuppressWarnings("unchecked")
    private static Map<EDBOperationType, Class<? extends Event>> makeEventMap() {
        Map<EDBOperationType, Class<? extends Event>> map;
        map = new HashMap<EDBOperationType, Class<? extends Event>>();
        map.put(EDBOperationType.COMMIT, EdbCommitEvent.class);
        // TODO add Events for other operations
        /*
         * map.put(EDBOperationType.QUERY, Event.class);
         * map.put(EDBOperationType.REGISTER_LINK, Event.class);
         * map.put(EDBOperationType.REQUEST_LINK, Event.class);
         * map.put(EDBOperationType.RESET, Event.class);
         */
        return Collections.unmodifiableMap(map);
    }

    // maps operation types to corresponding events
    private static final Map<EDBOperationType, Class<? extends Event>> eventMap = makeEventMap();

    @Override
    protected void processInOutRequest(final MessageExchange exchange, final NormalizedMessage in,
            final NormalizedMessage out) throws Exception {
        getLog().info("init handler from factory");

        final EDBHandler handler = this.fullConfig.getFactory().loadDefaultRepository();
        final EDBHandler linksHandler = this.fullConfig.getFactory().loadRepository(this.fullConfig.getLinkStorage());

        // see issue #84
        init(handler, linksHandler);
        initReponseBuilder();

        getLog().info("parsing message");
        /*
         * Only check the local part. Don't care about the namespace of the
         * operation
         */
        final EDBOperationType op = XmlParserFunctions.getMessageType(in);// exchange.getOperation().getLocalPart();
        CommandResult cmdResult = this.commands.get(op).execute(in);
        String body = cmdResult.responseString;
        body = this.reponses.get(op).wrapIntoResponse(body);

        final Source response = new StringSource(body);
        this.logger.info(body);
        out.setContent(response);

        Map<String, Object> eventAttributes = cmdResult.eventAttributes;
        Class<? extends Event> eventClass = eventMap.get(op);
        if (eventClass != null) {
            Event event = eventClass.newInstance();
            for (String key : eventAttributes.keySet()) {
                Object value = eventAttributes.get(key);
                event.setValue(key, value);
            }
            MessageProperties msgProperties = readProperties(in);
            EventHelper helper = new EventHelperImpl(this, msgProperties);
            // TODO figure out why context /Edb cannot be found
            // helper.sendEvent(event);
            helper.sendEvent(event, "urn:openengsb:drools", "droolsService");
        }
    }

    /**
     * see issue #84
     */
    private void init(final EDBHandler handler, final EDBHandler linksHandler) {
        this.commands = new HashMap<EDBOperationType, EDBEndpointCommand>();
        this.commands.put(EDBOperationType.COMMIT, new EDBCommit(handler, this.logger));
        this.commands.put(EDBOperationType.QUERY, new EDBQuery(handler, this.logger));
        this.commands.put(EDBOperationType.RESET, new EDBReset(handler, this.logger));
        this.commands.put(EDBOperationType.REGISTER_LINK, new EDBRegisterLink(linksHandler, this.logger));
        this.commands.put(EDBOperationType.REQUEST_LINK, new EDBRequestLink(linksHandler, this.logger));
    }

    /**
     * see issue #84
     */
    private void initReponseBuilder() {
        this.reponses = new HashMap<EDBOperationType, EDBEndpointResponseBuilder>();
        this.reponses.put(EDBOperationType.COMMIT, new DefaultAcmResponseBuilder());
        this.reponses.put(EDBOperationType.QUERY, new DefaultAcmResponseBuilder());
        this.reponses.put(EDBOperationType.RESET, new DefaultAcmResponseBuilder());
        this.reponses.put(EDBOperationType.REGISTER_LINK, new LinkRegisteredResponseBuilder());
        this.reponses.put(EDBOperationType.REQUEST_LINK, new LinkRequestResponseBuilder());
    }
}
