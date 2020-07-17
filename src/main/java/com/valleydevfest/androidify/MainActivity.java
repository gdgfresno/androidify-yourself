package com.valleydevfest.androidify;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity {

    public static final String ANONYMOUS = "anonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().hide();

        String mUsername = ANONYMOUS;

        // Initialize Firebase Auth
        // Firebase instance variables
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                String mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }

//        // FragmentActivity, OnConnectionFailedListener
//        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .enableAutoManage(this, this) // FragmentActivity, OnConnectionFailedListener
//                .addApi(Auth.GOOGLE_SIGN_IN_API)
//                .build();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_website) {
            Uri uriUrl = Uri.parse(getResources().getString(R.string.website_url));
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            startActivity(launchBrowser);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
