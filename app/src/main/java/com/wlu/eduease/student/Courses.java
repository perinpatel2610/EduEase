package com.wlu.eduease.student;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.util.ArrayList;

public class Courses extends Fragment {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> courseList;

    private DatabaseReference facultyDataRef;

    public Courses() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_courses, container, false);

        listView = view.findViewById(R.id.listView);
        courseList = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, courseList);
        listView.setAdapter(adapter);

        // Initialize Firebase reference
        facultyDataRef = FirebaseDatabase.getInstance().getReference().child("faculty_data");

        // Load all courses from faculty_data
        loadAllCourses();

        return view;
    }

    private void loadAllCourses() {
        // Add listener to fetch all courses
        facultyDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                courseList.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot courseSnapshot : userSnapshot.child("name").getChildren()) {
                        String courseName = courseSnapshot.getValue(String.class);
                        courseList.add(courseName);
                    }
                }
                adapter.notifyDataSetChanged();
                Log.d("CoursesFragment", "Number of courses loaded: " + courseList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load courses: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("CoursesFragment", "Failed to load courses: " + databaseError.getMessage());
            }
        });
    }
}
