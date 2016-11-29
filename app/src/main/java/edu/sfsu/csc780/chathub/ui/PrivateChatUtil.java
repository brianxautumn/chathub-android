package edu.sfsu.csc780.chathub.ui;

import android.content.Intent;

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

    public static String setupPrivateChat(String uid1, String uid2, String name1, String name2){
        PrivateThread privateThread = new PrivateThread(uid1, uid2, name1, name2);
        int compare = uid1.compareTo(uid2);
        String combinedPrivateThreadKey = uid1 + uid2;
        if (compare > 0) {
            combinedPrivateThreadKey = uid2 + uid1;
        }


        //Make an extra reference to avoid having to do complicated lookups since restricted to json
        //Also avoid overwrites if already exists
        sFirebaseDatabaseReference.child("private-messages").child(combinedPrivateThreadKey).setValue(privateThread);

        sFirebaseDatabaseReference.child("users").child(uid1).child("chats").child(combinedPrivateThreadKey).child("name1").setValue(name1);
        sFirebaseDatabaseReference.child("users").child(uid1).child("chats").child(combinedPrivateThreadKey).child("name2").setValue(name2);

        sFirebaseDatabaseReference.child("users").child(uid2).child("chats").child(combinedPrivateThreadKey).child("name1").setValue(name1);
        sFirebaseDatabaseReference.child("users").child(uid2).child("chats").child(combinedPrivateThreadKey).child("name2").setValue(name2);

        return combinedPrivateThreadKey;
    }

}
