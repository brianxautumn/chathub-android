package edu.sfsu.csc780.chathub.model;

/**
 * Created by mac on 11/29/16.
 */
public class Contact{

    private User user;

    public Contact(){

    }

    public Contact(User user){
        this.user = user;
    }

    public User getUser(){
        return this.user;
    }

    public void setUser(User user){
        this.user = user;
    }

}