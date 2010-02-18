package org.openengsb.drools.events;

import org.openengsb.core.model.Event;

public class EdbCommitEvent extends Event {

    private static final String AUTHOR = "author";

    public EdbCommitEvent() {
        super("Edb", "commit");
    }

    public void setAuthor(String value) {
        setValue(AUTHOR, value);
    }

    public String getAuthor() {
        return (String) getValue(AUTHOR);
    }

    public void setMessage(String value) {
        setValue("message", value);
    }

    public String getMessage() {
        return (String) getValue("message");
    }

}
