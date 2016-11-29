package edu.sfsu.csc780.chathub.ui;

import android.content.Intent;
import android.os.Debug;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.sfsu.csc780.chathub.R;

public class ContactActivity extends AppCompatActivity implements ContactUtil.ThreadLoadListener {

    private DrawerLayout mDrawerLayout;
    private Toolbar mToolBar;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private RecyclerView mContactRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter mFirebaseAdapter;
    private static String TAG = "ContactActivity";

    private static int REQUEST_CONTACT_DETAIL = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        setTitle(getString(R.string.contacts));

        //mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerRoot);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        mContactRecyclerView = (RecyclerView) findViewById(R.id.contactRecyclerView);


        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(false);
        mContactRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseAdapter = ContactUtil.getFirebaseAdapter(this,
                this,  /* MessageLoadListener */
                mLinearLayoutManager,
                mContactRecyclerView,
                mContactClickListener,
                mUser.getUid());

        mContactRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }


    private View.OnClickListener mContactClickListener = new View.OnClickListener() {


        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.contactDetails:
                    ContactUtil.ContactViewHolder contactViewHolder = (ContactUtil.ContactViewHolder) v.getTag();
                    //int position  =   mFirebaseAdapter.getAdapterPosition();
                    String uid = mFirebaseAdapter.getRef(contactViewHolder.getLayoutPosition()).getKey();
                    Log.d(TAG, uid);
                    Intent i = new Intent(getApplicationContext(), ContactDetailActivity.class);
                    i.putExtra("UID", uid);

                    startActivityForResult(i, REQUEST_CONTACT_DETAIL);
                    break;
                default:
                    Log.d(TAG, "Unrecognized click");
                    break;
            }
        }

    };

    @Override
    public void onLoadComplete() {

    }
}
