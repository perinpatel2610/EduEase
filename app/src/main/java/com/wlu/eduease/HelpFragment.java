package com.wlu.eduease;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HelpFragment extends Fragment {

    public HelpFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help, container, false);

        // Setup the help content
        TextView textViewHelp = view.findViewById(R.id.text_view_help);
        String authorName = "John Doe"; // Replace with actual author's name
        String versionNumber = "1.0"; // Replace with actual version number
        String instructions = "1. Use the menu to navigate through the app.\n" +
                "2. Click on items to view details.\n" +
                "3. Use the settings to customize your experience.";

        String message = "Author: " + authorName + "\n" +
                "Version: " + versionNumber + "\n\n" +
                "Instructions:\n" + instructions;

        textViewHelp.setText(message);

        return view;
    }
}
