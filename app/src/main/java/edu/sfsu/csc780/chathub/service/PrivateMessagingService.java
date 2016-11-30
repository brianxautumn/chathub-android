package edu.sfsu.csc780.chathub.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.model.ChatThread;
import edu.sfsu.csc780.chathub.model.PrivateThread;
import edu.sfsu.csc780.chathub.ui.InboxActivity;
import edu.sfsu.csc780.chathub.ui.InboxUtil;


public class PrivateMessagingService extends Service {

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private DatabaseReference mActiveThreads;
    private long startTime;


    @Override
    public int onStartCommand (Intent intent,  int flags, int startId){

        //Initialize Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        startTime = new Date().getTime();


        //Now set up a listener for the user's contacts
        mFirebaseDatabaseReference =
                FirebaseDatabase.getInstance().getReference();
        mActiveThreads = mFirebaseDatabaseReference.child("users").child(mUser.getUid()).child("chats");


        mActiveThreads.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {

                     DatabaseReference chatRef = postSnapshot.getRef();
                        chatRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            PrivateThread threadData = dataSnapshot.getValue(PrivateThread.class);
                            if(threadData.getTimestamp() > startTime){
                                createNotification(threadData.getMessage(), threadData.getName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                //createNotification(post.getMessage(), "test");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotification(String message, String name){
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_message_white_24px)
                        .setContentTitle(name)
                        .setContentText(message)
                        .setLights(Color.BLUE, 500, 500)
                        .setStyle(new NotificationCompat.InboxStyle())
                        .setSound(alarmSound);

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, InboxActivity.class);


        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(InboxActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(0, mBuilder.build());
    }
}
