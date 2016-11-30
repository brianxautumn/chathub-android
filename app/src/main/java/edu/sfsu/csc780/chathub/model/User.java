package edu.sfsu.csc780.chathub.model;

import java.util.HashMap;

/**
 * Created by mac on 11/26/16.
 */
public class User {
    private String name;
    private String uid;
    private String email;
    private String photoUrl;

    private HashMap<String, User> contacts;

    public  User(){

    }

    public User(String name, String email){
        this.name = name;
        this.email = email;
    }

    public User(String name, String email, String photoUrl){
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
    }

    public User(String name, String email, String photoUrl, String uid){
        this.name = name;
        this.email = email;
        this.photoUrl = photoUrl;
        this.uid = uid;
    }


    public void setPhotoUrl(String photoUrl){
        this.photoUrl = photoUrl;
    }

    public void setUid(String uid){
        this.uid = uid;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getName(){
        return this.name;
    }

    public String getUid(){
        return this.uid;
    }

    public String getEmail(){
        return this.email;
    }

    public String getPhotoUrl(){return this.photoUrl;}

}
