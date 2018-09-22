package com.edu.bupt.new_account.model;

public class User {
    private Integer id;

    private String openid;

    public User(Integer id, String openid) {
        this.id = id;
        this.openid = openid;
    }

    public User() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid == null ? null : openid.trim();
    }
}