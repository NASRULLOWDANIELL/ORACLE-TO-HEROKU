package com.heroku.java.bean;

public class Business {
    private String ownerName;
    private String businessType;
    private String businessId;

    // Constructors
    public Business() {}

    public Business(String ownerName, String businessType, String businessId) {
        this.ownerName = ownerName;
        this.businessType = businessType;
        this.businessId = businessId;

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
    
    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }
}
