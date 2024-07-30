package com.heroku.java.bean;

public class Business {
    private String ownerName;
    private String businessType;

    // Constructors
    public Business() {}

    public Business(String ownerName, String businessType) {
        this.ownerName = ownerName;
        this.businessType = businessType;
    }

    public Business(String ownerName2, String businessType2, String businessId) {
        //TODO Auto-generated constructor stub
    }

    // Getters and setters
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }
}
