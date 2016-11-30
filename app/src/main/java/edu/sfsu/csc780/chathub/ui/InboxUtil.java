package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatThread;
import edu.sfsu.csc780.chathub.model.PrivateThread;

/**
 * Created by mac on 11/29/16.
 */
public class InboxUtil {
    public static View.OnClickListener sMessageClickListener;
    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();

    private static FirebaseAuth sAuth = FirebaseAuth.getInstance();

    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    private static ThreadLoadListener sAdapterListener;
    private static FirebaseAuth sFirebaseAuth;
    public static final String THREADS_CHILD = "threads";
    public interface ThreadLoadListener { public void onLoadComplete(); }
    private static final String TAG = "Thread";

    public static View.OnClickListener sThreadClickListener;


    public static class ThreadViewHolder extends RecyclerView.ViewHolder{
        public TextView contactName;
        public View threadLayout;
        public String threadKey;
        public ThreadViewHolder(View v) {
            super(v);
            v.setOnClickListener(sThreadClickListener);
            v.setTag(this);
            contactName = (TextView) itemView.findViewById(R.id.contactName);

        }

        public String getThreadKey(){
            return this.threadKey;
        }

        public void setThreadKey(String threadKey){
            this.threadKey = threadKey;
        }

    }


    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             InboxUtil.ThreadLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView,
                                                             final View.OnClickListener threadClicklistener) {

        sThreadClickListener = threadClicklistener;

        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);

        sAdapterListener = listener;
        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<PrivateThread,
                ThreadViewHolder>(
                PrivateThread.class,
                R.layout.private_thread,
                ThreadViewHolder.class,
                sFirebaseDatabaseReference.child("users").child(InboxUtil.sAuth.getCurrentUser().getUid()).child("chats")) {

            @Override
            protected void populateViewHolder(final ThreadViewHolder viewHolder,
                                              PrivateThread chatThread, int position) {


                //final String key = this.getRef(position).getKey();
                //Log.d("FirebaseTest" , key);
                viewHolder.contactName.setText(chatThread.getName());
                //viewHolder.setThreadKey(this.getRef(position).getKey());
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

}
