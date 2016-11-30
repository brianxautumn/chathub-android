package edu.sfsu.csc780.chathub.ui;

import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.sfsu.csc780.chathub.model.PrivateThread;
import edu.sfsu.csc780.chathub.model.User;

/**
 * Created by mac on 11/28/16.
 */
public class PrivateChatUtil {

    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();

    public static String setupPrivateChat(User fromUser, User toUser){
        //String uid1, String uid2, String name1, String name2

        //PrivateThread privateThread = new PrivateThread(fromUser, toUser);
        int compare = fromUser.getUid().compareTo(toUser.getUid());
        String combinedPrivateThreadKey = fromUser.getUid() + toUser.getUid();
        if (compare > 0) {
            combinedPrivateThreadKey = toUser.getUid() + fromUser.getUid();
        }

        //Make an extra reference to avoid having to do complicated lookups since restricted to json
        //Also avoid overwrites if already exists
        sFirebaseDatabaseReference.child("private-messages").child(combinedPrivateThreadKey).child("to-user").setValue(toUser);
        sFirebaseDatabaseReference.child("private-messages").child(combinedPrivateThreadKey).child("from-user").setValue(fromUser);

        //Have to flatten data, not sure why embeded objects dont work...
        sFirebaseDatabaseReference.child("users").child(fromUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("name").setValue(toUser.getName());
        sFirebaseDatabaseReference.child("users").child(fromUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("uid").setValue(toUser.getUid());
        sFirebaseDatabaseReference.child("users").child(fromUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("email").setValue(toUser.getEmail());
        sFirebaseDatabaseReference.child("users").child(fromUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("photoUrl").setValue(toUser.getPhotoUrl());

        sFirebaseDatabaseReference.child("users").child(toUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("name").setValue(fromUser.getName());
        sFirebaseDatabaseReference.child("users").child(toUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("uid").setValue(fromUser.getUid());
        sFirebaseDatabaseReference.child("users").child(toUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("email").setValue(fromUser.getEmail());
        sFirebaseDatabaseReference.child("users").child(toUser.getUid()).child("chats").child(combinedPrivateThreadKey).child("photoUrl").setValue(fromUser.getPhotoUrl());



        return combinedPrivateThreadKey;
    }


}
