package com.example.neighborhood;

import android.content.Context;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.neighborhood.Fragment.AddFragment;
import com.example.neighborhood.Fragment.CommunityFragment;
import com.example.neighborhood.Fragment.EventFragment;
import com.example.neighborhood.Fragment.HomeFragment;
import com.example.neighborhood.Fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavigationListener implements BottomNavigationView.OnNavigationItemSelectedListener {

    private final Context context;

    public BottomNavigationListener(Context context) {
        this.context = context;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment selectedFragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                selectedFragment = new HomeFragment();
                break;
            case R.id.navigation_community:
                selectedFragment = new CommunityFragment();
                break;
            case R.id.navigation_add:
                selectedFragment = new AddFragment();
                break;
            case R.id.navigation_event:
                selectedFragment = new EventFragment();
                break;
            case R.id.navigation_profile:
                selectedFragment = new ProfileFragment();
                break;
        }

        if (selectedFragment != null) {
            ((MainActivity) context).getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .addToBackStack(null)
                    .commit();
        }

        return true;
    }
}
