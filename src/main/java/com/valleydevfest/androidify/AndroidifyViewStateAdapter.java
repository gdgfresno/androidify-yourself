package com.valleydevfest.androidify;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class AndroidifyViewStateAdapter extends FragmentStateAdapter {

    private List<Integer> imageIds;

    public AndroidifyViewStateAdapter(FragmentActivity fragmentActivity, List<Integer> imageIds) {
        super(fragmentActivity);
        this.imageIds = imageIds;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        AndroidifyViewPagerItemFragment fragment = new AndroidifyViewPagerItemFragment();
        fragment.setImgId(imageIds.get(position));
        return fragment;
    }

    @Override
    public int getItemCount() {
        return imageIds.size();
    }
}
