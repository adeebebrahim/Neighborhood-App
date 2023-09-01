package com.example.neighborhood.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.neighborhood.OnboardingActivity;
import com.example.neighborhood.R;

public class SlideFragment extends Fragment {

    private static final String ARG_POSITION = "position";
    private int position;

    public SlideFragment() {

    }

    public static SlideFragment newInstance(int position) {
        SlideFragment fragment = new SlideFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            position = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_slide, container, false);

        TextView textView = view.findViewById(R.id.textView);
        TextView moretextView = view.findViewById(R.id.moretextView);


        if (position == 0) {
            textView.setText("Welcome to the Neighborhood App!");
            textView.setVisibility(View.VISIBLE);
        } else if (position == 1) {
            textView.setText("Learn about your neighborhood and stay connected!");
            moretextView.setText("• Discover local businesses and services in your area." +
                    "\n• Stay informed about community events and activities." +
                    "\n• Connect with neighbors to share recommendations and tips." +
                    "\n• Join discussions on local topics and issues.");

        } else if (position == 2) {
            textView.setText("Practice good digital citizenship and follow app guidelines.");
            moretextView.setText("• Respect others' privacy and personal information." +
                    "\n• Avoid sharing sensitive or inappropriate content." +
                    "\n• Be respectful and considerate in your interactions." +
                    "\n• Follow community guidelines and rules.");

            final Button finishButton = view.findViewById(R.id.finishButton);
            finishButton.setVisibility(View.VISIBLE);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
            );
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            int margin = (int) getResources().getDimension(R.dimen.button_margin);
            layoutParams.setMargins(0, 0, margin, margin);
            finishButton.setLayoutParams(layoutParams);
            finishButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    OnboardingActivity activity = (OnboardingActivity) getActivity();
                    if (activity != null) {
                        activity.onSlideCompleted();
                    }
                }
            });
        }
        return view;
    }
}

