package com.valleydevfest.androidify;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

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
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    public static final String PARTICIPANT_CHILD = "participants";
    private EditText mNameEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ViewPager viewPagerHead = (ViewPager) rootView.findViewById(R.id.viewPagerHead);
        final ViewPager viewPagerBody = (ViewPager) rootView.findViewById(R.id.viewPagerBody);
        final ViewPager viewPagerLegs = (ViewPager) rootView.findViewById(R.id.viewPagerLegs);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        viewPagerHead.setAdapter(new AndroidifyViewPagerAdapter(fm, AndroidDrawables.getHeads()));
        viewPagerBody.setAdapter(new AndroidifyViewPagerAdapter(fm, AndroidDrawables.getBodies()));
        viewPagerLegs.setAdapter(new AndroidifyViewPagerAdapter(fm, AndroidDrawables.getLegs()));

        initNameEdit(rootView);
        initSubmitButton(rootView, viewPagerHead, viewPagerBody, viewPagerLegs);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        Log.i(TAG, "FB User: " + mFirebaseAuth.getUid());

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mFirebaseDatabaseReference.child(PARTICIPANT_CHILD).child(mFirebaseUser.getUid()).getRef()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Avatar avatar = dataSnapshot.getValue(Avatar.class);
                        Log.i(TAG, "Got Avatar");
                        if (avatar != null) {
                            Log.i(TAG, "Got Avatar2 " + avatar.name);
                            mNameEditText.setText(avatar.name);
                            viewPagerHead.setCurrentItem(avatar.head - 1);
                            viewPagerBody.setCurrentItem(avatar.body - 1);
                            viewPagerLegs.setCurrentItem(avatar.legs - 1);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "loadAvatar:onCancelled", databaseError.toException());
                    }
                });

        return rootView;
    }

    private void initNameEdit(View rootView) {
        mNameEditText = (EditText) rootView.findViewById(R.id.androidName);
    }

    private void initSubmitButton(View rootView, final ViewPager viewPagerHead, final ViewPager viewPagerBody, final ViewPager viewPagerLegs) {
        View submitButton = rootView.findViewById(R.id.button_submit);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Avatar avatar = new Avatar(
                        mNameEditText.getText().toString(),
                        viewPagerHead.getCurrentItem() + 1,
                        viewPagerBody.getCurrentItem() + 1,
                        viewPagerLegs.getCurrentItem() + 1);

                mFirebaseDatabaseReference.child(PARTICIPANT_CHILD).child(mFirebaseUser.getUid())
                        .setValue(avatar, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError,
                                                   DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.w(TAG, "Unable to write to database.",
                                            databaseError.toException());
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}