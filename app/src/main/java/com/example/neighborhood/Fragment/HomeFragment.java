package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.example.neighborhood.R;

public class HomeFragment extends Fragment {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Find the views within the fragment
        TextView appNameTextView = rootView.findViewById(R.id.app_name);
        ImageView searchIconImageView = rootView.findViewById(R.id.search_icon);
        ImageView messageIconImageView = rootView.findViewById(R.id.message_icon);

        // Set click listeners or perform other actions as needed

        return rootView;
    }
}
