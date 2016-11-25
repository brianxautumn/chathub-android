package edu.sfsu.csc780.chathub.model;

import java.util.Date;

/**
 * Created by mac on 11/14/16.
 */
public class ChatThread {
    private String label;
    private String key;
    private String messageCount;

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey(){
        return this.key;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    public ChatThread() {

    }

    public ChatThread(String label) {
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }

    public void setMessageCount(String count){
        this.messageCount = count;
    }

    public String getMessageCount(){
        return this.messageCount;
    }

}
