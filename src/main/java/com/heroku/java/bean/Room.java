package com.heroku.java.bean;

public class Room {
    private int roomid;
    private String roomname;
    private String roomtype;
    private String roomdesc;
    private Double roomprice;

    public Room() {
    }

    public Room(int roomid, String roomname, String roomtype, String roomdesc, Double roomprice) {
        this.roomid = roomid;
        this.roomname = roomname;
        this.roomtype = roomtype;
        this.roomdesc = roomdesc;
        this.roomprice = roomprice;
    }

    public int getRoomid() {
        return roomid;
    }

    public void setRoomid(int roomid) {
        this.roomid = roomid;
    }

    public String getRoomname() {
        return roomname;
    }

    public void setRoomname(String roomname) {
        this.roomname = roomname;
    }

    public String getRoomtype() {
        return roomtype;
    }

    public void setRoomtype(String roomtype) {
        this.roomtype = roomtype;
    }

    public String getRoomdesc() {
        return roomdesc;
    }

    public void setRoomdesc(String roomdesc) {
        this.roomdesc = roomdesc;
    }

    public Double getRoomprice() {
        return roomprice;
    }

    public void setRoomprice(Double roomprice) {
        this.roomprice = roomprice;
    }
}
