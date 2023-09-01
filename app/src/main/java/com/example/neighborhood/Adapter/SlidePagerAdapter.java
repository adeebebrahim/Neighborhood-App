package com.example.neighborhood.Adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.neighborhood.Fragment.SlideFragment;

public class SlidePagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_PAGES = 3;

    public SlidePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) {
        return SlideFragment.newInstance(position);
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
