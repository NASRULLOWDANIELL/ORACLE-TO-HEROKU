package com.heroku.java.bean;

public class Payment {
    private int paymentId;
    private String paymentType;
    private Double paymentTotal;
    private int bookingId;
    private Card card;
    private Cash cash;

    public Payment() {
    }

    public Payment(int paymentId, String paymentType, Double paymentTotal, int bookingId, Card card, Cash cash) {
        this.paymentId = paymentId;
        this.paymentType = paymentType;
        this.paymentTotal = paymentTotal;
        this.bookingId = bookingId;
        this.card = card;
        this.cash = cash;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Double getPaymentTotal() {
        return paymentTotal;
    }

    public void setPaymentTotal(Double paymentTotal) {
        this.paymentTotal = paymentTotal;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Cash getCash() {
        return cash;
    }

    public void setCash(Cash cash) {
        this.cash = cash;
    }

}