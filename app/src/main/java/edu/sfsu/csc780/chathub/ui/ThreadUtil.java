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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.model.ChatThread;



/**
 * Created by mac on 11/14/16.
 */
public class ThreadUtil {

    public static View.OnClickListener sMessageClickListener;
    private static DatabaseReference sFirebaseDatabaseReference =
            FirebaseDatabase.getInstance().getReference();
    private static FirebaseStorage sStorage = FirebaseStorage.getInstance();
    private static ThreadLoadListener sAdapterListener;
    private static FirebaseAuth sFirebaseAuth;
    public static final String THREADS_CHILD = "threads";
    public interface ThreadLoadListener { public void onLoadComplete(); }
    private static final String TAG = "Thread";

    public static View.OnClickListener sThreadClickListener;


    public static class ThreadViewHolder extends RecyclerView.ViewHolder{
        public TextView messageTextView;
        public View threadLayout;
        public String threadKey;
        public ThreadViewHolder(View v) {
            super(v);
            v.setOnClickListener(sThreadClickListener);
            v.setTag(this);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            threadLayout = (View)  itemView.findViewById(R.id.threadLayout);
        }

        public String getThreadKey(){
            return this.threadKey;
        }

        public void setThreadKey(String threadKey){
            this.threadKey = threadKey;
        }

    }


    public static FirebaseRecyclerAdapter getFirebaseAdapter(final Activity activity,
                                                             ThreadUtil.ThreadLoadListener listener,
                                                             final LinearLayoutManager linearManager,
                                                             final RecyclerView recyclerView,
                                                             final View.OnClickListener threadClicklistener) {

        sThreadClickListener = threadClicklistener;

        final SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(activity);

        sAdapterListener = listener;
        final FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<ChatThread,
                ThreadViewHolder>(
                ChatThread.class,
                R.layout.thread_layout,
                ThreadViewHolder.class,
                sFirebaseDatabaseReference.child(THREADS_CHILD)) {

            @Override
            protected void populateViewHolder(final ThreadViewHolder viewHolder,
                                              ChatThread chatThread, int position) {


                //final String key = this.getRef(position).getKey();
                //Log.d("FirebaseTest" , key);
                viewHolder.messageTextView.setText(chatThread.getLabel());
                viewHolder.setThreadKey(this.getRef(position).getKey());
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
