package com.heroku.java.bean;

public class Cash {
    private int paymentid;

    public Cash() {
    }

    public Cash(int paymentid) {
        this.paymentid = paymentid;
    }

    public int getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(int paymentid) {
        this.paymentid = paymentid;
    }

}
