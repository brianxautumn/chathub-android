package edu.sfsu.csc780.chathub.model;

/**
 * Created by mac on 11/28/16.
 */
public class PrivateThread {

    //String user1;
    //String user2;
    //String name1;
    //String name2;
    User user1;
    User user2;

    public PrivateThread(){

    }

    public PrivateThread(User user1, User user2){
        this.user1 = user1;
        this.user2 = user2;
        //this.name1 = name1;
        //this.name2 = name2;
    }

    //Temporary
    public String getContactName(){
        return "test";
    }

}
