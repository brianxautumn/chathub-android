package edu.sfsu.csc780.chathub.ui;

import android.content.Intent;
import android.os.Debug;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.PrivateThread;
import edu.sfsu.csc780.chathub.model.User;

public class ContactDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar mToolBar;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String uid;
    private TextView mContactName;
    private TextView mContactEmail;
    public ImageView mContactImage;
    private Button mStartChatButton;

    private FirebaseUser firebaseContact;
    public static final String TAG = "ContactActivity";
    private DatabaseReference mFirebaseDatabaseReference;

    static final int REQUEST_PUBLIC_THREAD = 1;
    static final int REQUEST_PRIVATE_THREAD = 2;

    static final int PRIVATE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contact_detail);
        setTitle(getString(R.string.contact_detail));
        mAuth = FirebaseAuth.getInstance();


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uid = extras.getString("UID");


        }

        mFirebaseDatabaseReference =
                FirebaseDatabase.getInstance().getReference();



        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mContactName =(TextView) findViewById(R.id.contactName);
        mContactEmail =(TextView) findViewById(R.id.contactEmail);
        mContactImage = (ImageView) findViewById(R.id.contactImage);
        mStartChatButton = (Button) findViewById(R.id.startChat);
        mStartChatButton.setOnClickListener(this);



        mFirebaseDatabaseReference.child("users").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Post post = dataSnapshot.getValue(Post.class);
                mContactName.setText(dataSnapshot.child("name").getValue(String.class));
                mContactEmail.setText(dataSnapshot.child("email").getValue(String.class));
                //mContactImage.set
                //Log.d(TAG, dataSnapshot.child("photoUrl").getValue(String.class));
                String photoUrl = dataSnapshot.child("photoUrl").getValue(String.class);
                Picasso.with(getApplicationContext()).load(photoUrl).into(mContactImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.startChat:
                Log.d(TAG, "Should start chat");
                String name2 = (String) mContactName.getText();//TODO:: could be better, maybe use tag
                String threadKey = PrivateChatUtil.setupPrivateChat(mAuth.getCurrentUser().getUid(), uid, mAuth.getCurrentUser().getDisplayName(), name2);

                //Now launch the chat activity
                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.putExtra("THREAD", threadKey);
                i.putExtra("LABEL", name2);
                i.putExtra("MODE", true);
                startActivityForResult(i, REQUEST_PRIVATE_THREAD);

                break;
            default:
                //do nothing
                break;
        }
    }

}
