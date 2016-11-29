package edu.sfsu.csc780.chathub.ui;

import android.content.Intent;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatThread;

public class InboxActivity extends AppCompatActivity implements InboxUtil.ThreadLoadListener {
    private RecyclerView mThreadRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private String mUsername;
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    static final int REQUEST_PUBLIC_THREAD = 1;
    static final int REQUEST_PRIVATE_THREAD = 2;
    private FirebaseRecyclerAdapter<ChatThread, InboxUtil.ThreadViewHolder>
            mFirebaseAdapter;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        setTitle("Inbox");

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.threadRecyclerView);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseAdapter = InboxUtil.getFirebaseAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mMessageRecyclerView,
                mThreadClickListener);

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLoadComplete() {

    }

    private View.OnClickListener mThreadClickListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            InboxUtil.ThreadViewHolder threadViewHolder = (InboxUtil.ThreadViewHolder) v.getTag();
            //int position  =   mFirebaseAdapter.getAdapterPosition();
            String threadKey = mFirebaseAdapter.getRef(threadViewHolder.getLayoutPosition()).getKey();
            Intent i = new Intent(getApplicationContext(), MainActivity.class);
            i.putExtra("THREAD", threadKey);
            i.putExtra("LABEL", "test label");
            i.putExtra("MODE", true);
            startActivityForResult(i, REQUEST_PRIVATE_THREAD);
        }

    };
}
