package edu.sfsu.csc780.chathub.ui;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.sfsu.csc780.chathub.model.PrivateThread;

/**
 * Created by mac on 11/28/16.
 */
public class PrivateChatUtil {

    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();

    public static void setupPrivateChat(String uid1, String uid2, String name1, String name2){
        PrivateThread privateThread = new PrivateThread(uid1, uid2, name1, name2);
        DatabaseReference newChatRef = sFirebaseDatabaseReference.child("private-messages").push();
        String newChatKey = newChatRef.getKey();
        newChatRef.setValue(privateThread);


        //Make an extra reference to avoid having to do complicated lookups since restricted to json
        sFirebaseDatabaseReference.child("users").child(uid1).child("chats").child(newChatKey).setValue(privateThread);
        sFirebaseDatabaseReference.child("users").child(uid2).child("chats").child(newChatKey).setValue(privateThread);

    }

}
