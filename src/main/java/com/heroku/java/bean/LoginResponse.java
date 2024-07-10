package com.heroku.java.bean;

public class LoginResponse {
    
    private boolean success;
    private String message;
    private String email;
    private Integer id;
    private String password;

    public LoginResponse(boolean success, String message, String email, Integer id, String password) {
        this.success = success;
        this.message = message;
        this.email = email;
        this.id = id;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public Integer getId() {
        return id;
    }


    public void setId(Integer id) {
        this.id = id;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
