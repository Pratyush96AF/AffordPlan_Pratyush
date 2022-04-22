package com.affordplan.Model;

public class UriProperties {

    String uri;

    int counter;

    int phaseCounter;



    int maxCounter;



    public UriProperties(String uri, int counter, int maxCounter,int phaseCounter){
        this.uri=uri;
        this.counter=counter;
        this.maxCounter=maxCounter;
        this.phaseCounter=phaseCounter;

    }


    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public int getMaxCounter() {
        return maxCounter;
    }

    public void setMaxCounter(int maxCounter) {
        this.maxCounter = maxCounter;
    }

    public int getPhaseCounter() {
        return phaseCounter;
    }

    public void setPhaseCounter(int phaseCounter) {
        this.phaseCounter = phaseCounter;
    }
}
