package com.nfredrick.android.joglog.generator;

import com.google.android.gms.maps.model.LatLng;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

public class ListProperty {

    private ArrayList<LatLng> lst;

    private PropertyChangeSupport mPropertyChangeSupport;

    public ListProperty() {
        lst = new ArrayList<>();
        mPropertyChangeSupport = new PropertyChangeSupport(this);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        mPropertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        mPropertyChangeSupport.removePropertyChangeListener(listener);
    }

    public ArrayList<LatLng> getLst() {
        return lst;
    }

    public void setLst(ArrayList<LatLng> lst) {
        this.lst = lst;
    }
}
