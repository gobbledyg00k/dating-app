package com.pattern.acquaintances.model;

import java.util.Date;

public class Message {
    public Message(){}
    public Message(String uid_, Date date_, String msg_){
        uid = uid_;
        date = date_;
        msg = msg_;
    }
    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDateMili() {
        return date;
    }

    public void setDateMili(Date data) {
        this.date = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    private String uid;
    private Date date;
    private String msg;

}
