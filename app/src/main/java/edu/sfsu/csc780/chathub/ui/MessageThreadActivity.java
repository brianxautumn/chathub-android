package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.model.ChatThread;

public class MessageThreadActivity extends AppCompatActivity implements ThreadUtil.ThreadLoadListener {
    private RecyclerView mThreadRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private String mUsername;
    private static final String TAG = "ThreadActivity";

    static final int REQUEST_PUBLIC_THREAD = 1;
    static final int REQUEST_PRIVATE_THREAD = 2;


    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseRecyclerAdapter<ChatThread, ThreadUtil.ThreadViewHolder>
            mFirebaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_thread);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mUser.getDisplayName();

        }

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.threadRecyclerView);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseAdapter = ThreadUtil.getFirebaseAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mMessageRecyclerView,
                mThreadClickListener);

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request=" + requestCode + ", result=" + resultCode);

        if (requestCode == REQUEST_PUBLIC_THREAD && resultCode == Activity.RESULT_OK) {

        }
    }

    private View.OnClickListener mThreadClickListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            ThreadUtil.ThreadViewHolder threadViewHolder = (ThreadUtil.ThreadViewHolder) v.getTag();
            //int position  =   mFirebaseAdapter.getAdapterPosition();
            String threadKey = mFirebaseAdapter.getRef(threadViewHolder.getLayoutPosition()).getKey();
            //String threadLabel = mFirebaseAdapter.getRef(threadViewHolder.getLayoutPosition()).getRoot();
            String threadLabel = (String) threadViewHolder.messageTextView.getText();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("THREAD", threadKey);
            i.putExtra("LABEL", threadLabel);
            startActivityForResult(i, REQUEST_PUBLIC_THREAD);
        }

    };

    @Override
    public void onLoadComplete() {

    }
}
