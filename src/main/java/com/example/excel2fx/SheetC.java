package com.example.excel2fx;

import java.math.BigInteger;

public class SheetC {

    String name;
    Integer numOfPairs;

    public SheetC(String name, Integer numOfPairs) {
        this.name = name;
        this.numOfPairs = numOfPairs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getNumOfPairs() {
        return numOfPairs;
    }

    public void setNumOfPairs(Integer numOfPairs) {
        this.numOfPairs = numOfPairs;
    }


    @Override
    public String toString() {
        return "SheetC{" +
                "name='" + name + '\'' +
                ", numOfPairs=" + numOfPairs +
                '}';
    }
}
