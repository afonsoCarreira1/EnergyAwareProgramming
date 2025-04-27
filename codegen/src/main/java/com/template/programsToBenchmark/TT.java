package com.template.programsToBenchmark;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TT {

    Long n;

    @JsonCreator
    public TT(@JsonProperty("n")long n) {this.n=n;}
}