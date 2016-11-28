package edu.sfsu.csc780.chathub.model;

import java.util.HashMap;

/**
 * Created by mac on 11/26/16.
 */
public class User {
    private String name;
    private String uid;
    private String email;

    private HashMap<String, User> contacts;

    public  User(){

    }

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
