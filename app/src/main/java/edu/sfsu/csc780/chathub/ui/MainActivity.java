/**
 * Copyright Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.sfsu.csc780.chathub.ui;

import android.app.Activity;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;


import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.sfsu.csc780.chathub.R;
import edu.sfsu.csc780.chathub.model.ChatMessage;
import edu.sfsu.csc780.chathub.model.User;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        MessageUtil.MessageLoadListener {

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";
    private static final int REQUEST_INVITE = 1;
    public static final int REQUEST_PREFERENCES = 2;
    public static final int MSG_LENGTH_LIMIT = 64;
    private static final double MAX_LINEAR_DIMENSION = 500.0;
    public static final String ANONYMOUS = "anonymous";
    private static final int REQUEST_PICK_IMAGE = 1;
    static final int REQUEST_TAKE_PHOTO = 3;
    static final int PRIVATE = 2;
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    private FloatingActionButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private boolean isPrivate = false;

    // Firebase instance variables
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    private FirebaseRecyclerAdapter<ChatMessage, MessageUtil.MessageViewHolder>
            mFirebaseAdapter;
    private  DatabaseReference mFirebaseDatabaseReference;

    private ImageButton mImageButton;
    private int mSavedTheme;
    private ImageButton mLocationButton;
    private ImageButton mCameraButton;
    private String threadKey;
    private String threadLabel;
    private String mUserUid;
    private Toolbar mToolBar;
    private DataSnapshot contactSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            threadKey = extras.getString("THREAD");
            threadLabel = extras.getString("LABEL");
            isPrivate = extras.getBoolean("MODE");
            //The key argument here must match that used in the other activity
        }



        Log.d(TAG , "MODE IS : " + isPrivate + "");

        DesignUtils.applyColorfulTheme(this);
        setContentView(R.layout.activity_main);

        //Setup toolbar
        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setTitle(threadLabel);


        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;
        //Initialize Auth
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mUserUid = mUser.getUid();

        if (mUser == null) {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mUser.getDisplayName();
            if (mUser.getPhotoUrl() != null) {
                mPhotoUrl = mUser.getPhotoUrl().toString();
            }
        }

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        //mLinearLayoutManager.setReverseLayout(true);

        //Log.d(TAG, "Layout reverse is : " + mLinearLayoutManager.getReverseLayout());
        //Log.d(TAG, "Layout stack from end is : " + mLinearLayoutManager.getStackFromEnd());
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);


            mFirebaseAdapter = MessageUtil.getFirebaseAdapter(this,
                    this,  /* MessageLoadListener */
                    mLinearLayoutManager,
                    mMessageRecyclerView,
                    mImageClickListener,
                    mLongClickListener,
                    threadKey,
                    isPrivate);



        mMessageRecyclerView.setAdapter(mFirebaseAdapter);


        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(MSG_LENGTH_LIMIT)});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (FloatingActionButton) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send messages on click.
                mMessageRecyclerView.scrollToPosition(0);
                ChatMessage chatMessage = new
                        ChatMessage(
                        mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl
                        );
                chatMessage.setThreadId(threadKey);
                chatMessage.setUid(mUserUid);
                chatMessage.setEmail(mUser.getEmail());
                if(isPrivate)
                    MessageUtil.sendPrivate(chatMessage);
                else
                    MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
            }
        });


        mImageButton = (ImageButton) findViewById(R.id.shareImageButton);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });

        mLocationButton = (ImageButton) findViewById(R.id.locationButton);
        mLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMap();
            }
        });

        mCameraButton = (ImageButton) findViewById(R.id.cameraButton);
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePhotoIntent();
            }
        });



        //Now set up a listener for the user's contacts
        mFirebaseDatabaseReference =
                FirebaseDatabase.getInstance().getReference();

        final FirebaseUser firebaseUser = mAuth.getCurrentUser();
        mFirebaseDatabaseReference.child("users").child(firebaseUser.getUid()).child("contacts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                contactSnapshot = snapshot;
                Log.d(TAG, "Added info");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void dispatchTakePhotoIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocationUtils.startLocationUpdates(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        boolean isGranted = (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED);
        if (isGranted && requestCode == LocationUtils.REQUEST_CODE) {
            LocationUtils.startLocationUpdates(this);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            case R.id.preferences_menu:
                mSavedTheme = DesignUtils.getPreferredTheme(this);
                Intent i = new Intent(this, PreferencesActivity.class);
                startActivityForResult(i, REQUEST_PREFERENCES);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoadComplete() {
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private void pickImage() {
        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened"
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        intent.setType("image/*");

        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: request=" + requestCode + ", result=" + resultCode);

        if (requestCode == REQUEST_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            // Process selected image here
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            if (data != null) {
                Uri uri = data.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                // Resize if too big for messaging
                Bitmap bitmap = getBitmapForUri(uri);
                Bitmap resizedBitmap = scaleImage(bitmap);
                if (bitmap != resizedBitmap) {
                    uri = savePhotoImage(resizedBitmap);
                }

                createImageMessage(uri);
            } else {
                Log.e(TAG, "Cannot get image for uploading");
            }
        } else if (requestCode == REQUEST_PREFERENCES) {
            if (DesignUtils.getPreferredTheme(this) != mSavedTheme) {
                DesignUtils.applyColorfulTheme(this);
                this.recreate();
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK){

            if (data != null) {

                Bundle extras = data.getExtras();
                if (extras != null){
                    Log.d( "camera", "extras is not null");
                    Bitmap bitmap = (Bitmap) extras.get("data");
                    Bitmap resizedBitmap = scaleImage(bitmap);
                    Uri uri  = savePhotoImage(resizedBitmap);
                    createImageMessage(uri);

                }

            }
        }
    }

    private void createImageMessage(Uri uri) {
        if (uri == null) {
            Log.e(TAG, "Could not create image message with null uri");
            return;
        }

        final StorageReference imageReference = MessageUtil.getImageStorageReference(mUser, uri);
        UploadTask uploadTask = imageReference.putFile(uri);

        // Register observers to listen for when task is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "Failed to upload image message");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ChatMessage chatMessage = new
                        ChatMessage(mMessageEditText.getText().toString(),
                        mUsername,
                        mPhotoUrl,
                        imageReference.toString()
                        );
                chatMessage.setThreadId(threadKey);
                chatMessage.setUid(mUserUid);
                if(isPrivate)
                    MessageUtil.sendPrivate(chatMessage);
                else
                    MessageUtil.send(chatMessage);
                mMessageEditText.setText("");
            }
        });
    }

    private Bitmap getBitmapForUri(Uri imageUri) {
        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap scaleImage(Bitmap bitmap) {
        int originalHeight = bitmap.getHeight();
        int originalWidth = bitmap.getWidth();
        double scaleFactor =  MAX_LINEAR_DIMENSION / (double)(originalHeight + originalWidth);

        // We only want to scale down images, not scale upwards
        if (scaleFactor < 1.0) {
            int targetWidth = (int) Math.round(originalWidth * scaleFactor);
            int targetHeight = (int) Math.round(originalHeight * scaleFactor);
            return Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        } else {
            return bitmap;
        }
    }

    private Uri savePhotoImage(Bitmap imageBitmap) {
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (photoFile == null) {
            Log.d(TAG, "Error creating media file");
            return null;
        }

        try {
            FileOutputStream fos = new FileOutputStream(photoFile);
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return Uri.fromFile(photoFile);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String imageFileNamePrefix = "chathub-" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File imageFile = File.createTempFile(
                imageFileNamePrefix,    /* prefix */
                ".jpg",                 /* suffix */
                storageDir              /* directory */
        );
        return imageFile;
    }


    private void loadMap() {
        Loader<Bitmap> loader = getSupportLoaderManager().initLoader(0, null, new LoaderManager
                .LoaderCallbacks<Bitmap>() {
            @Override
            public Loader<Bitmap> onCreateLoader(final int id, final Bundle args) {
                return new MapLoader(MainActivity.this);
            }

            @Override
            public void onLoadFinished(final Loader<Bitmap> loader, final Bitmap result) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                mLocationButton.setEnabled(true);

                if (result == null) return;
                // Resize if too big for messaging
                Bitmap resizedBitmap = scaleImage(result);
                Uri uri = null;
                if (result != resizedBitmap) {
                    uri = savePhotoImage(resizedBitmap);
                } else {
                    uri = savePhotoImage(result);
                }
                createImageMessage(uri);

            }

            @Override
            public void onLoaderReset(final Loader<Bitmap> loader) {
            }

        });

        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        mLocationButton.setEnabled(false);
        loader.forceLoad();
    }

    private View.OnClickListener mImageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ImageView photoView = (ImageView) v.findViewById(R.id.messageImageView);
            // Only show the larger view in dialog if there's a image for the message
            if (photoView.getVisibility() == View.VISIBLE) {
                Bitmap bitmap = ((GlideBitmapDrawable) photoView.getDrawable()).getBitmap();
                showPhotoDialog(ImageDialogFragment.newInstance(bitmap));
            }
        }

    };

    private View.OnLongClickListener mLongClickListener = new View.OnLongClickListener() {


        @Override
        public boolean onLongClick(View view) {
            MessageUtil.MessageMeta messageMeta  = (MessageUtil.MessageMeta) view.findViewById(R.id.messageLayout).getTag();
            String name =  messageMeta.name;
            String uid = messageMeta.uid;
            String email = messageMeta.email;
            String photoUrl = messageMeta.photoUrl;
            User newContact = new User(name, email, photoUrl, uid);
            final FirebaseUser firebaseUser = mAuth.getCurrentUser();
            Log.d(TAG, uid);
            Log.d(TAG, firebaseUser.getUid());
            if(uid.equals(firebaseUser.getUid())){
                showSelfDetails(name);
            }else{
                showContactAddDialog(newContact);
            }

            return false;
        }

    };

    void showSelfDetails(final String name){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(name + " (me)")

                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    void showContactAddDialog(final User newContact){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Add " + newContact.getName() + " to contacts?")

                .setCancelable(false)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //User newContact = new User(name, email);
                        //newContact.setUid(uid);
                        mFirebaseDatabaseReference.child("users").child(mUser.getUid()).child("contacts").child(newContact.getUid()).setValue(newContact);

                        Context context = getApplicationContext();
                        CharSequence text = "Added " + newContact.getName() + " to contacts.";
                        int duration = Toast.LENGTH_LONG;

                        Toast toast = Toast.makeText(context, text, duration);
                        toast.show();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    void showPhotoDialog(DialogFragment dialogFragment) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction. We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        android.support.v4.app.Fragment prev =
                getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) { ft.remove(prev); }
        ft.addToBackStack(null);
        dialogFragment.show(ft , "dialog");




    }

}
