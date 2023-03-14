package com.example.excel2fx;

import java.math.BigDecimal;

public class CodeAndPrice {

    BigDecimal code, price;

    public CodeAndPrice(BigDecimal code, BigDecimal price) {
        this.code = code;
        this.price = price;
    }


    public BigDecimal getCode() {
        return code;
    }

    public void setCode(BigDecimal code) {
        this.code = code;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return code.toBigInteger() + "   |   " + price ;
    }
}
