package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Debug;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.R;

public class MessageUtil {
    private static final String LOG_TAG = MessageUtil.class.getSimpleName();
    public static final String MESSAGES_CHILD = "messages";
    public static final String THREAD_KEY = "threads";
    private static final String TAG = "MessageUtil";
    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();
    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    private static MessageLoadListener sAdapterListener;
    private static FirebaseAuth sFirebaseAuth;
    public interface MessageLoadListener { public void onLoadComplete(); }
    public static View.OnClickListener sMessageClickListener;
    public static View.OnLongClickListener sMessageLongClickListener;

    public static void send(final ChatMessage chatMessage) {

        final String threadId = chatMessage.getThreadId();
        //final int messageCount = 0;
        final String messageRef = THREAD_KEY + "/" + threadId + "/messages";
        Log.d("messagePushing" , threadId);
        sFirebaseDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(snapshot.hasChild(THREAD_KEY + "/" + threadId + "/count")){
                    int messageCount = snapshot.child( THREAD_KEY + "/" + threadId + "/count" ).getValue(Integer.class);
                    Log.d(LOG_TAG, "threadcount was " + messageCount);
                    sFirebaseDatabaseReference.child(THREAD_KEY + "/" + threadId + "/count").setValue(messageCount+1);
                }else{
                    sFirebaseDatabaseReference.child(THREAD_KEY + "/" + threadId + "/count").setValue(1);
                }

                sFirebaseDatabaseReference.child(messageRef).push().setValue(chatMessage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //sFirebaseDatabaseReference.child(messageRef).push().setValue(chatMessage);
        //sFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(chatMessage);
    }

    public static void sendPrivate(final ChatMessage chatMessage) {

        final String threadId = chatMessage.getThreadId();
        //final int messageCount = 0;


        sFirebaseDatabaseReference.child("private-messages").child(threadId).child("messages").push().setValue(chatMessage);
        String recieverUid = PrivateChatUtil.getReceiverUid(chatMessage.getUid(), threadId);

        //Now we must push the message preview to the other user
        sFirebaseDatabaseReference.child("users").child(recieverUid).child("chats").child(threadId).child("message").setValue(chatMessage.getText());
        sFirebaseDatabaseReference.child("users").child(recieverUid).child("chats").child(threadId).child("timestamp").setValue(chatMessage.getTimestamp());
        Log.d(TAG, recieverUid);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView messageTextView;
        public ImageView messageImageView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;
        public TextView timestampTextView;
        public View messageLayout;
        public String userUid;
        //public View view;


        public MessageViewHolder(View v) {
            super(v);
            //this.view = v;
            v.setOnClickListener(sMessageClickListener);
            v.setOnLongClickListener(sMessageLongClickListener);
            //v.setTag(0 , userUid);


            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            timestampTextView = (TextView) itemView.findViewById(R.id.timestampTextView);
            messageLayout = (View) itemView.findViewById(R.id.messageLayout);
        }

    }

    public static class MessageMeta{
        String name;
        String email;
        String uid;
        String photoUrl;


        public MessageMeta(String name, String email, String uid, String photoUrl){
            this.name = name;
            this.email = email;
            this.uid = uid;
            this.photoUrl = photoUrl;
        }
    }

    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             MessageLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView,
                                                             final View.OnClickListener messageClicklistener,
                                                             final View.OnLongClickListener messageLongClickListener,
                                                             final String threadId,
                                                             final boolean isPrivate) {
        sMessageClickListener = messageClicklistener;
        sMessageLongClickListener = messageLongClickListener;
        String messageRef = null;
        if(isPrivate){
            messageRef = "private-messages/" + threadId + "/messages";

        }else{
            messageRef = THREAD_KEY + "/" + threadId + "/messages";
        }


        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);
        sAdapterListener = listener;
        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<ChatMessage,
                MessageViewHolder>(
                ChatMessage.class,
                R.layout.item_message,
                MessageViewHolder.class,
                sFirebaseDatabaseReference.child(messageRef)) {
            @Override
            protected void populateViewHolder(final MessageViewHolder viewHolder,
                                              ChatMessage chatMessage, int position) {
                sAdapterListener.onLoadComplete();
                viewHolder.messageTextView.setText(chatMessage.getText());
                viewHolder.messengerTextView.setText(chatMessage.getName());
                viewHolder.messageLayout.setTag(new MessageMeta(chatMessage.getName(), chatMessage.getEmail(), chatMessage.getUid(), chatMessage.getPhotoUrl()));

                //viewHolder.setUserUid(chatMessage.getUid());
                //viewHolder.setName(chatMessage.getText());
                if (chatMessage.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(activity,
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    SimpleTarget target = new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation glideAnimation) {
                            viewHolder.messengerImageView.setImageBitmap(bitmap);
                            final String palettePreference = activity.getString(R.string
                                    .auto_palette_preference);

                            if (preferences.getBoolean(palettePreference, false)) {
                                DesignUtils.setBackgroundFromPalette(bitmap, viewHolder
                                        .messageLayout);
                            } else {
                                viewHolder.messageLayout.setBackground(
                                        activity.getResources().getDrawable(
                                                R.drawable.message_background));
                            }

                        }
                    };
                    Glide.with(activity)
                            .load(chatMessage.getPhotoUrl())
                            .asBitmap()
                            .into(target);
                }

                if (chatMessage.getImageUrl() != null) {

                    viewHolder.messageImageView.setVisibility(View.VISIBLE);
                    viewHolder.messageTextView.setVisibility(View.GONE);

                    try {
                        final StorageReference gsReference =
                                sStorage.getReferenceFromUrl(chatMessage.getImageUrl());
                        gsReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Glide.with(activity)
                                        .load(uri)
                                        .into(viewHolder.messageImageView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Log.e(LOG_TAG, "Could not load image for message", exception);
                            }
                        });
                    } catch (IllegalArgumentException e) {
                        viewHolder.messageTextView.setText("Error loading image");
                        Log.e(LOG_TAG, e.getMessage() + " : " + chatMessage.getImageUrl());
                    }
                } else {
                    viewHolder.messageImageView.setVisibility(View.GONE);
                    viewHolder.messageTextView.setVisibility(View.VISIBLE);
                }

                long timestamp = chatMessage.getTimestamp();
                if (timestamp == 0 || timestamp == chatMessage.NO_TIMESTAMP ) {
                    viewHolder.timestampTextView.setVisibility(View.GONE);
                } else {
                    viewHolder.timestampTextView.setText(DesignUtils.formatTime(activity,
                            timestamp));
                    viewHolder.timestampTextView.setVisibility(View.VISIBLE);
                }

            }
        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                Log.d(LOG_TAG, "Item was inserted");
                int messageCount = adapter.getItemCount();
                int lastVisiblePosition = linearManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (messageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);
                }
            }
        });
        return adapter;
    }

    public static StorageReference getImageStorageReference(FirebaseUser user, Uri uri) {
        //Create a blob storage reference with path : bucket/userId/timeMs/filename
        long nowMs = Calendar.getInstance().getTimeInMillis();

        return sStorage.getReference().child(user.getUid() + "/" + nowMs + "/" + uri
                .getLastPathSegment());
    }

}
