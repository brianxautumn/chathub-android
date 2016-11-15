package edu.sfsu.csc780.chathub.model;

import java.util.Date;

/**
 * Created by mac on 11/14/16.
 */
public class ChatThread {
    private String label;
    private String name;
    private String photoUrl;

    public long getTimestamp() {
        return timestamp;
    }

    private long timestamp;
    public static final long NO_TIMESTAMP = -1;



    public ChatThread() {
    }

    public ChatThread(String label) {
        this.label = label;
    }

    public String getLabel(){
        return this.label;
    }

}
