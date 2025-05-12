package com.template.programsToBenchmark;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class TestTwoInputs {

    int n1,n2;

    @JsonCreator
    public TestTwoInputs(@JsonProperty("n1")int n1, @JsonProperty("n2")int n2) {
        this.n1 = n1;
        this.n2 = n2;
    }

    public void doNothing(int x,int y) {}

}
