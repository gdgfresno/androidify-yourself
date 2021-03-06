package com.valleydevfest.androidify;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class PlaceholderFragment extends Fragment {

    private static final String TAG = "PlaceholderFragment";
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseUser mFirebaseUser;
    public static final String PARTICIPANT_CHILD = "participants";
    private EditText mNameEditText;
    private ViewPager2 mViewPagerHead;
    private ViewPager2 mViewPagerBody;
    private ViewPager2 mViewPagerLegs;

    private static final int WRITE_EXTERNAL_STORAGE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mViewPagerHead = rootView.findViewById(R.id.viewPagerHead);
        mViewPagerBody = rootView.findViewById(R.id.viewPagerBody);
        mViewPagerLegs = rootView.findViewById(R.id.viewPagerLegs);

        FragmentActivity fragmentActivity = getActivity();
        mViewPagerHead.setAdapter(
                new AndroidifyViewStateAdapter(fragmentActivity, AndroidDrawables.getHeads()));
        mViewPagerBody.setAdapter(
                new AndroidifyViewStateAdapter(fragmentActivity, AndroidDrawables.getBodies()));
        mViewPagerLegs.setAdapter(
                new AndroidifyViewStateAdapter(fragmentActivity, AndroidDrawables.getLegs()));

        initNameEdit(rootView);
        initSubmitButtons(rootView);
        initWebsiteButton(rootView);
        initShareButton(rootView);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        Log.i(TAG, "FB User: " + mFirebaseAuth.getUid());

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseDatabaseReference.child(PARTICIPANT_CHILD).child(mFirebaseUser.getUid()).getRef()
            .addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Avatar avatar = dataSnapshot.getValue(Avatar.class);
                    Log.i(TAG, "Got Avatar");
                    if (avatar != null) {
                        Log.i(TAG, "Got Avatar2 " + avatar.name);
                        mNameEditText.setText(avatar.name);
                        mViewPagerHead.setCurrentItem(avatar.head - 1);
                        mViewPagerBody.setCurrentItem(avatar.body - 1);
                        mViewPagerLegs.setCurrentItem(avatar.legs - 1);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.w(TAG, "loadAvatar:onCancelled", databaseError.toException());
                }
            });

        return rootView;
    }

    private void initNameEdit(View rootView) {
        mNameEditText = (EditText) rootView.findViewById(R.id.androidName);
    }

    private void submitAvatar() {
        Avatar avatar = new Avatar(
            mNameEditText.getText().toString(),
            mViewPagerHead.getCurrentItem() + 1,
            mViewPagerBody.getCurrentItem() + 1,
            mViewPagerLegs.getCurrentItem() + 1
        );

        mFirebaseDatabaseReference.child(PARTICIPANT_CHILD).child(mFirebaseUser.getUid())
            .setValue(avatar, (databaseError, databaseReference) -> {
                if (databaseError != null) {
                    Log.w(TAG, "Unable to write to database.",
                            databaseError.toException());
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.figure_saved),
                            Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void initSubmitButtons(View rootView) {
        View submitButton = rootView.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(view -> submitAvatar());

        View submitButton2 = rootView.findViewById(R.id.button_submit2);
        submitButton2.setOnClickListener(view -> submitAvatar());
    }

    private void initWebsiteButton(View rootView) {
        View websiteButton = rootView.findViewById(R.id.button_website);
        websiteButton.setOnClickListener(view -> {
            Uri uriUrl = Uri.parse(MainActivity.WEBSITE_URL);
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
            try {
                startActivity(launchBrowser);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_browser),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initShareButton(View rootView) {
        View shareButton = rootView.findViewById(R.id.button_share);
        shareButton.setOnClickListener(view -> {
            int permissionCheck = ContextCompat.checkSelfPermission(requireActivity(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                        WRITE_EXTERNAL_STORAGE);
            } else {
                share();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                share();
            }
        }
    }

    private void share() {
        Integer head = AndroidDrawables.getHeads().get(mViewPagerHead.getCurrentItem());
        Integer body = AndroidDrawables.getBodies().get(mViewPagerBody.getCurrentItem());
        Integer legs = AndroidDrawables.getLegs().get(mViewPagerLegs.getCurrentItem());

        Bitmap bitmap = BitmapUtils.combineDrawables(getResources(), head, body, legs);

        String imagePath = MediaStore.Images.Media.insertImage(
            requireActivity().getContentResolver(), bitmap,
            getResources().getString(R.string.android_avatar), null
        );
        if (imagePath == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.could_not_save),
                    Toast.LENGTH_SHORT).show();
        } else {
            Uri imageURI = Uri.parse(imagePath);
            startShareActivity(imageURI);
        }
    }

    private void startShareActivity(Uri imageURI) {
        final Intent shareIntent = new Intent(Intent.ACTION_SEND);

        shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shareIntent.putExtra(Intent.EXTRA_STREAM, imageURI);
        shareIntent.setType("image/png");

        startActivity(shareIntent);
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}