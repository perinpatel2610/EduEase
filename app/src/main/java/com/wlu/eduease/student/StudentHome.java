package com.wlu.eduease.student;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wlu.eduease.R;

public class StudentHome extends Fragment {



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student, container, false);

        Button btnCourseMaterial = view.findViewById(R.id.btnCourseMaterial);
        Button btnGrades = view.findViewById(R.id.btnGrades);
        Button btnTestSchedule = view.findViewById(R.id.btnTestSchedule);
        Button btnClassSchedule = view.findViewById(R.id.btnClassSchedule);
        Button btnAssignments = view.findViewById(R.id.btnAssignments);
        Button viewAttendanceButton = view.findViewById(R.id.viewAttendanceButton);

        viewAttendanceButton.setOnClickListener(v ->  {
                com.wlu.eduease.parent.ViewAttendanceFragment viewAttendanceFragment = new com.wlu.eduease.parent.ViewAttendanceFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, viewAttendanceFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            });


        btnCourseMaterial.setOnClickListener(v -> {
            // Create the new fragment
            Courses coursesFragment = new Courses();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, coursesFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnGrades.setOnClickListener(v -> {
            // Create the new fragment
            StudentMarksFragment studentMarksFragment = new StudentMarksFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, studentMarksFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });


        btnTestSchedule.setOnClickListener(v -> {
            // Create the new fragment
            QuizScheduleFragment quizScheduleFragment = new QuizScheduleFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, quizScheduleFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnClassSchedule.setOnClickListener(v -> {
            // Create the new fragment
            StudentScheduleFragment studentScheduleFragment = new StudentScheduleFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, studentScheduleFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnAssignments.setOnClickListener(v -> {
            // Create the new fragment
            AssignmentScheduleFragment assignmentScheduleFragment = new AssignmentScheduleFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, assignmentScheduleFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
