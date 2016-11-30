package edu.sfsu.csc780.chathub.model;

/**
 * Created by mac on 11/28/16.
 */
public class PrivateThread {

    String name;
    String uid;
    String message;
    String photoUrl;
    String email;
    long timestamp;

    public void setName(String name) {
        this.name = name;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }



    public PrivateThread(){

    }


    public String getName(){
        return this.name;
    }


}
