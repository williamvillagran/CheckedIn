package edu.utsa.checkedin.model;

public class Friend {
    public String Uid;
    public String email;

    public Friend () {}

    public Friend(String Uid, String email) {
        this.email = email;
        this.Uid = Uid;
    }

    public String getUid() {
        return Uid;
    }

    public String getEmail() {
        return email;
    }

    public void setUid(String Uid) {
        this.Uid = Uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
