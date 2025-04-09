package com.template.programsToBenchmark;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Test {

    int n;

    @JsonCreator
    public Test(@JsonProperty("n")int n) {this.n=n;}
    
}