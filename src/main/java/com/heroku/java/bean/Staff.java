package com.heroku.java.bean;

public class Staff {
    private int staffid;
    private String staffname;
    private String staffemail;
    private String staffpassword;
    private String staffphoneno;
    private String staffrole;
    private Integer managerid;

    public Staff() {

    }

    public Staff(int staffid, String staffname, String staffemail, String staffpassword, String staffphoneno,
            String staffrole, int managerid) {
        this.staffid = staffid;
        this.staffname = staffname;
        this.staffemail = staffemail;
        this.staffpassword = staffpassword;
        this.staffphoneno = staffphoneno;
        this.staffrole = staffrole;
        this.managerid = managerid;
    }

    public int getStaffid() {
        return staffid;
    }

    public void setStaffid(int staffid) {
        this.staffid = staffid;
    }

    public String getStaffname() {
        return staffname;
    }

    public void setStaffname(String staffname) {
        this.staffname = staffname;
    }

    public String getStaffemail() {
        return staffemail;
    }

    public void setStaffemail(String staffemail) {
        this.staffemail = staffemail;
    }

    public String getStaffpassword() {
        return staffpassword;
    }

    public void setStaffpassword(String staffpassword) {
        this.staffpassword = staffpassword;
    }

    public String getStaffphoneno() {
        return staffphoneno;
    }

    public void setStaffphoneno(String staffphoneno) {
        this.staffphoneno = staffphoneno;
    }

    public String getStaffrole() {
        return staffrole;
    }

    public void setStaffrole(String staffrole) {
        this.staffrole = staffrole;
    }

    public Integer getManagerid() {
        return managerid;
    }

    public void setManagerid(Integer managerid) {
        this.managerid = managerid;
    }
}
