package com.heroku.java.bean;

public class Business {
    private String ownerName;
    private String businessType;
    private String businessID;  // Note the capital "ID"

    // Constructor
    public Business(String ownerName, String businessType, String businessID) {
        this.ownerName = ownerName;
        this.businessType = businessType;
        this.businessID = businessID;
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

    public String getBusinessID() {
        return businessID;
    }

    public void setBusinessID(String businessID) {
        this.businessID = businessID;
    }

   
}
