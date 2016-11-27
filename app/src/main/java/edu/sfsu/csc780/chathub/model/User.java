package edu.sfsu.csc780.chathub.model;

/**
 * Created by mac on 11/26/16.
 */
public class User {
    private String name;
    private String uid;
    private String email;

    public User(String name, String email){
        this.name = name;
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

}
