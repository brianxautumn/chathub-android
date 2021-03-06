package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.daimajia.swipe.SwipeLayout;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatThread;
import edu.sfsu.csc780.chathub.model.User;

/**
 * Created by mac on 11/27/16.
 */
public class ContactUtil {

    public static View.OnClickListener sMessageClickListener;
    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();
    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    private static ThreadLoadListener sAdapterListener;
    private static FirebaseAuth sFirebaseAuth;
    public interface ThreadLoadListener { public void onLoadComplete(); }
    private static final String TAG = "Contacts";

    public static View.OnClickListener sContactClickListener;

    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        public TextView name;
        public TextView email;
        public String uid;
        public View contactLayout;
        private SwipeLayout layout;
        public String photoUrl;

        public ContactViewHolder(View v) {
            super(v);

            itemView.findViewById(R.id.contactDetails).setOnClickListener(sContactClickListener);
            itemView.findViewById(R.id.contactDetails).setTag(this);
            itemView.findViewById(R.id.deleteContact).setOnClickListener(sContactClickListener);
            itemView.findViewById(R.id.deleteContact).setTag(this);
            itemView.findViewById(R.id.startChat).setOnClickListener(sContactClickListener);
            itemView.findViewById(R.id.startChat).setTag(this);
            //layout.setShowMode(SwipeLayout.ShowMode.LayDown);
            //layout.addDrag(SwipeLayout.DragEdge.Right, layout.findViewWithTag("Bottom2"));

            name = (TextView) itemView.findViewById(R.id.contactName);
            email = (TextView) itemView.findViewById(R.id.contactEmail) ;
            contactLayout = itemView.findViewById(R.id.contactLayout);
        }

        public void setPhotoUrl(String photoUrl){
            this.photoUrl = photoUrl;
        }

        public void setUid(String uid){

            this.uid = uid;
        }

        public String getUid(){
            return this.uid;
        }

        public String getPhotoUrl(){
            return this.photoUrl;
        }


    }


    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             ContactUtil.ThreadLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView,
                                                             final View.OnClickListener contactClickListener,
                                                             final String uid) {

        sContactClickListener = contactClickListener;

        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);

        sAdapterListener = listener;
        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<User,
                ContactViewHolder>(
                User.class,
                R.layout.contact_layout,
                ContactViewHolder.class,
                sFirebaseDatabaseReference.child("users").child(uid).child("contacts")) {

            @Override
            protected void populateViewHolder(final ContactViewHolder viewHolder,
                                              User user, int position) {



                viewHolder.name.setText(user.getName());
                viewHolder.email.setText(user.getEmail());
                viewHolder.setUid(user.getUid());
                viewHolder.setPhotoUrl(user.getPhotoUrl());
                sAdapterListener.onLoadComplete();
            }

        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

            }
        });

        return adapter;
    }

    public static void addContact(User user){

    }

}
