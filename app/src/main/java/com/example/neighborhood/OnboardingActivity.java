package com.example.neighborhood;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.example.neighborhood.Adapter.SlidePagerAdapter;

public class OnboardingActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private SlidePagerAdapter slidePagerAdapter;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPreferences.getBoolean("completedOnboarding", false)) {
            startLoginActivity(); // Skip onboarding if completed before
        } else {
            viewPager = findViewById(R.id.viewPager);
            slidePagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
            viewPager.setAdapter(slidePagerAdapter);
        }
    }

    public void onSlideCompleted() {
        markOnboardingAsCompleted();
    }

    private void markOnboardingAsCompleted() {
        sharedPreferences.edit().putBoolean("completedOnboarding", true).apply();
        startLoginActivity();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish(); // Finish the onboarding activity
    }
}

