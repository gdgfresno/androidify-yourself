package com.valleydevfest.androidify;

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
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class PlaceholderFragment extends Fragment {

    private static final String TAG = "PlaceholderFragment";
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseUser mFirebaseUser;
    public static final String PARTICIPANT_CHILD = "participants";
    private EditText mNameEditText;
    private ViewPager mViewPagerHead;
    private ViewPager mViewPagerBody;
    private ViewPager mViewPagerLegs;

    private static final int WRITE_EXTERNAL_STORAGE = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mViewPagerHead = rootView.findViewById(R.id.viewPagerHead);
        mViewPagerBody = rootView.findViewById(R.id.viewPagerBody);
        mViewPagerLegs = rootView.findViewById(R.id.viewPagerLegs);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        mViewPagerHead.setAdapter(new AndroidifyViewPagerAdapter(fm, AndroidDrawables.getHeads()));
        mViewPagerBody.setAdapter(new AndroidifyViewPagerAdapter(fm, AndroidDrawables.getBodies()));
        mViewPagerLegs.setAdapter(new AndroidifyViewPagerAdapter(fm, AndroidDrawables.getLegs()));

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
                mViewPagerLegs.getCurrentItem() + 1);

        mFirebaseDatabaseReference.child(PARTICIPANT_CHILD).child(mFirebaseUser.getUid())
                .setValue(avatar, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           @NonNull DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Log.w(TAG, "Unable to write to database.",
                                    databaseError.toException());
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(),
                                    getResources().getString(R.string.figure_saved),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initSubmitButtons(View rootView) {
        View submitButton = rootView.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitAvatar();
            }
        });

        View submitButton2 = rootView.findViewById(R.id.button_submit2);
        submitButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitAvatar();
            }
        });
    }

    private void initWebsiteButton(View rootView) {
        View wbesiteButton = rootView.findViewById(R.id.button_wbesite);
        wbesiteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uriUrl = Uri.parse(getResources().getString(R.string.website_url));
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });
    }

    private void initShareButton(View rootView) {
        View shareButton = rootView.findViewById(R.id.button_share);
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int permissionCheck = ContextCompat.checkSelfPermission(Objects.requireNonNull(getActivity()),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            new String[] { android.Manifest.permission.WRITE_EXTERNAL_STORAGE },
                            WRITE_EXTERNAL_STORAGE);
                } else {
                    share();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case WRITE_EXTERNAL_STORAGE:
                if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    share();
                }
                break;

            default:
                break;
        }
    }

    private void share() {
        Integer head = AndroidDrawables.getHeads().get(mViewPagerHead.getCurrentItem());
        Integer body = AndroidDrawables.getBodies().get(mViewPagerBody.getCurrentItem());
        Integer legs = AndroidDrawables.getLegs().get(mViewPagerLegs.getCurrentItem());

        Bitmap bitmap = BitmapUtils.combineDrawables(getResources(), head, body, legs);

        String imagePath = MediaStore.Images.Media.insertImage(
                Objects.requireNonNull(getActivity()).getContentResolver(), bitmap,
                getResources().getString(R.string.android_avatar), null);
        Uri imageURI = Uri.parse(imagePath);
        startShareActivity(imageURI);
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