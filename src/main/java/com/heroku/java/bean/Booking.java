package com.heroku.java.bean;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Booking {
    private Integer bookingid;
    private Date bookingCheckInDate;
    private Date bookingCheckOutDate;
    private BigDecimal bookingPrice;
    private Integer catid;
    private String catIdsString;
    private String catNamesString;
    private Integer roomid;
    private String paymentstatus;
    private Integer feedbackId;
    private String paymentType;
    private String custname;
    private String catname;
    private String bookingstatus;

    public String getBookingstatus() {
        return bookingstatus;
    }

    public void setBookingstatus(String bookingstatus) {
        this.bookingstatus = bookingstatus;
    }

    public String getCustname() {
        return custname;
    }

    public void setCustname(String custname) {
        this.custname = custname;
    }

    public String getCatname() {
        return catname;
    }

    public void setCatname(String catname) {
        this.catname = catname;
    }

    public Booking() {

    }

    public Booking(Integer bookingid, Date bookingCheckInDate, Date bookingCheckOutDate, BigDecimal bookingPrice,
            Integer catid,
            Integer roomid, String paymentstatus, Integer feedbackid, String bookingstatus) {
        this.bookingid = bookingid;
        this.bookingCheckInDate = bookingCheckInDate;
        this.bookingCheckOutDate = bookingCheckOutDate;
        this.bookingPrice = bookingPrice;
        this.catid = catid;
        this.roomid = roomid;
        this.paymentstatus = paymentstatus;
        this.feedbackId = feedbackid;
        this.bookingstatus = bookingstatus;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Integer getFeedbackId() {
        return feedbackId;
    }

    public void setFeedbackId(Integer feedbackId) {
        this.feedbackId = feedbackId;
    }

    public Integer getBookingid() {
        return bookingid;
    }

    public void setBookingid(Integer bookingid) {
        this.bookingid = bookingid;
    }

    public Date getBookingCheckInDate() {
        return bookingCheckInDate;
    }

    public void setBookingCheckInDate(Date bookingCheckInDate) {
        this.bookingCheckInDate = bookingCheckInDate;
    }

    public Date getBookingCheckOutDate() {
        return bookingCheckOutDate;
    }

    public void setBookingCheckOutDate(Date bookingCheckOutDate) {
        this.bookingCheckOutDate = bookingCheckOutDate;
    }

    public BigDecimal getBookingPrice() {
        return bookingPrice;
    }

    public void setBookingPrice(BigDecimal bookingPrice) {
        this.bookingPrice = bookingPrice;
    }

    public Integer getCatid() {
        return catid;
    }

    public void setCatid(Integer catid) {
        this.catid = catid;
    }

    public Integer getRoomid() {
        return roomid;
    }

    public void setRoomid(Integer roomid) {
        this.roomid = roomid;
    }

    public String getPaymentstatus() {
        return paymentstatus;
    }

    public void setPaymentstatus(String paymentstatus) {
        this.paymentstatus = paymentstatus;
    }

    public void setCatNamesString(String catNamesString) {
        this.catNamesString = catNamesString;
    }

    public List<String> getCatNames() {
        if (catNamesString == null || catNamesString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(catNamesString.split(","));
    }

    public String getCatIdsString() {
        return catIdsString;
    }

    public void setCatIdsString(String catIdsString) {
        this.catIdsString = catIdsString;
    }

    public List<Integer> getCatIds() {
        if (catIdsString == null || catIdsString.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(catIdsString.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
