package edu.utsa.checkedin.model;

public class Friend {
    private String uid;
    private String email;

    // REQUIRED no-arg constructor
    public Friend() {}

    public Friend(String uid, String email) {
        this.uid = uid;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
