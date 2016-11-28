package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.model.ChatThread;
import edu.sfsu.csc780.chathub.ui.SignInActivity;

public class MessageThreadActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, ThreadUtil.ThreadLoadListener {
    private RecyclerView mThreadRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private String mUsername;
    private DrawerLayout mDrawerLayout;
    private android.support.v7.app.ActionBarDrawerToggle mDrawerToggle;
    private static final String TAG = "ThreadActivity";
    private Toolbar mToolBar;
    public static final String ANONYMOUS = "anonymous";
    static final int REQUEST_PUBLIC_THREAD = 1;
    static final int REQUEST_PRIVATE_THREAD = 2;
    static final int REQUEST_CONTACTS = 3;
    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    public ImageView mMyImage;
    public TextView mMyName;
    public TextView mMyEmail;

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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerRoot);
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                mToolBar,
                R.string.drawer_open,
                R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationView mNavigationView = (NavigationView) findViewById(R.id.navView);
        View hView =  mNavigationView.getHeaderView(0);
        mMyImage = (ImageView) hView.findViewById(R.id.myImage);
        mMyName = (TextView) hView.findViewById(R.id.myName);
        mMyEmail = (TextView) hView.findViewById(R.id.myEmail);
        mMyName.setText(mUser.getDisplayName());
        mMyEmail.setText(mUser.getEmail());
        Picasso.with(getApplicationContext()).load(mUser.getPhotoUrl()).into(mMyImage);


        NavigationView nv = (NavigationView) findViewById(R.id.navView);
        assert nv != null;
        nv.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {


            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                String txt;
                Intent i;
                switch (menuItem.getItemId()) {
                    case R.id.contacts:
                        txt = "Contacts Selected";
                        i = new Intent(getApplicationContext(), ContactActivity.class);
                        startActivityForResult(i, REQUEST_CONTACTS);
                        break;
                    case R.id.logOut:
                        //txt = "Contacts Selected";
                        mAuth.signOut();
                        Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                        mUsername = ANONYMOUS;
                        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
                        return true;
                        //break;

                    default:
                        txt = "Invalid Item Selected";
                }
                //Toast.makeText(getApplicationContext(), txt, Toast.LENGTH_LONG).show();
                mDrawerLayout.closeDrawers();
                return true;
            }
        });




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
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Handle action bar item clicks here. The action bar will
         * automatically handle clicks on the Home/Up button, so long
         * as you specify a parent activity in AndroidManifest.xml.
         */

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}
