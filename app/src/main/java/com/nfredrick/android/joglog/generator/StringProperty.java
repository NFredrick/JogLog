package com.nfredrick.android.joglog.generator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class StringProperty {

    private String mString;
    private PropertyChangeSupport mPropertyChangeSupport;

    public StringProperty() {
        mString = "";
        mPropertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    public String getString() {
        return mString;
    }

    public void setString(String s) {
        String oldString = mString;
        this.mString = s;
        mPropertyChangeSupport.firePropertyChange("mString", oldString, mString);
    }
}
