package com.affordplan.Model;

public class UriProperties {

    String uri;

    int counter;





    public UriProperties(String uri, int counter){
        this.uri=uri;
        this.counter=counter;

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

}
