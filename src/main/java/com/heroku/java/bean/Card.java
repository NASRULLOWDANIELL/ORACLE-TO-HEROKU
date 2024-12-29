package com.heroku.java.bean;

public class Card {
    private int paymentid;
    private String cardtype;
    private String cardnumber;
    private String cvv;

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }
    public Card() {
    }

    public Card(int paymentid, String cardtype, String cardnumber) {
        this.paymentid = paymentid;
        this.cardtype = cardtype;
        this.cardnumber = cardnumber;
    }

    public int getPaymentid() {
        return paymentid;
    }

    public void setPaymentid(int paymentid) {
        this.paymentid = paymentid;
    }

    public String getCardType() {
        return cardtype;
    }

    public void setCardType(String cardtype) {
        this.cardtype = cardtype;
    }

    public String getCardNumber() {
        return cardnumber;
    }

    public void setCardNumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }
}
