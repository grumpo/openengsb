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

package org.openengsb.routingspike;

import java.util.Map;

import org.openengsb.util.Hash;

public class MagicContext {

    private Map<String, ContextObject> contexts = Hash.newHashMap();

    public MagicContext() {
        contexts.put("42", new ContextObject("test.scala", "config1"));
        contexts.put("bar", new ContextObject("test2.scala", "config2"));
    }

    public ContextObject resolve(String context) {
        return contexts.get(context);
    }

    public class ContextObject {
        String filename;
        String configuration;

        public ContextObject(String filename, String configuration) {
            this.filename = filename;
            this.configuration = configuration;
        }
    }
}
