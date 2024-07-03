package com.heroku.java.bean;

public class Cat {
    private int catid;
    private String catname;
    private String catbreed;
    private int catage;
    private int custid;
    private String custname;

    public void setCustname(String custname) {
        this.custname = custname;
    }

    public String getCustname() {
        return custname;
    }

    public Cat() {

    }

    public Cat(int catid, String catname, String catbreed, int catage, int custid) {
        this.catid = catid;
        this.catname = catname;
        this.catbreed = catbreed;
        this.catage = catage;
        this.custid = custid;
    }

    public int getCatid() {
        return catid;
    }

    public void setCatid(int catid) {
        this.catid = catid;
    }

    public String getCatname() {
        return catname;
    }

    public void setCatname(String catname) {
        this.catname = catname;
    }

    public String getCatbreed() {
        return catbreed;
    }

    public void setCatbreed(String catbreed) {
        this.catbreed = catbreed;
    }

    public int getCatage() {
        return catage;
    }

    public void setCatage(int catage) {
        this.catage = catage;
    }

    public int getCustid() {
        return custid;
    }

    public void setCustid(int custid) {
        this.custid = custid;
    }
}