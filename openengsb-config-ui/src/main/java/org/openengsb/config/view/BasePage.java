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
package org.openengsb.config.view;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.openengsb.config.jbi.ComponentsRetriever;
import org.openengsb.config.jbi.component.ComponentDescriptor;

public class BasePage extends WebPage
{
	@SpringBean
	protected ComponentsRetriever retriever;

	public BasePage() {
		add(new NavigationPanel("navigationBar", retriever.lookupComponents()));
	}

	protected ComponentDescriptor getComponent(String name) {
		for (ComponentDescriptor c : retriever.lookupComponents()) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}
}