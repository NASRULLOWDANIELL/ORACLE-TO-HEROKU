package com.heroku.java.bean;

public class Customer {
    private Integer custid;
    private String custname;
    private String custemail;
    private String custpassword;
    private String custphoneno;

    public Customer() {

    }

    public Customer(Integer custid, String custname, String custemail, String custpassword, String custphoneno) {
        this.custid = custid;
        this.custname = custname;
        this.custemail = custemail;
        this.custpassword = custpassword;
        this.custphoneno = custphoneno;
    }

    public Integer getCustid() {
        return this.custid;
    }

    public void setCustid(Integer custid) {
        this.custid = custid;
    }

    public String getCustname() {
        return this.custname;
    }

    public void setCustname(String custname) {
        this.custname = custname;
    }

    public String getCustemail() {
        return this.custemail;
    }

    public void setCustemail(String custemail) {
        this.custemail = custemail;
    }

    public String getCustpassword() {
        return this.custpassword;
    }

    public void setCustpassword(String custpassword) {
        this.custpassword = custpassword;
    }

    public String getCustphoneno() {
        return this.custphoneno;
    }

    public void setCustphoneno(String custphoneno) {
        this.custphoneno = custphoneno;
    }

}
