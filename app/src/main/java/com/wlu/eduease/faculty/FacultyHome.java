package com.wlu.eduease.faculty;

import android.content.Intent;
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

public class FacultyHome extends Fragment {

    private FirebaseAuth auth;
    private TextView textView;
    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faculty, container, false);

        Button btnCourseMaterial = view.findViewById(R.id.btnCourseMaterial);
        Button btnTestsQuizzes = view.findViewById(R.id.btnTestsQuizzes);
        Button btnTestSchedule = view.findViewById(R.id.btnTestSchedule);
        Button btnClassSchedule = view.findViewById(R.id.btnClassSchedule);
        Button btnAssignments = view.findViewById(R.id.btnAssignments);
        Button btnGrades = view.findViewById(R.id.btnGrades);
        Button btnPTMSchedule = view.findViewById(R.id.btnPTMSchedule);
        Button atRiskButton = view.findViewById(R.id.btnAtRiskStudents);
        Button btnAttendance = view.findViewById(R.id.addAttendanceButton);
        Button viewAttendanceButton = view.findViewById(R.id.viewAttendanceButton);

        btnAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAttendanceFragment addAttendanceFragment = new AddAttendanceFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, addAttendanceFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        viewAttendanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                com.wlu.eduease.parent.ViewAttendanceFragment viewAttendanceFragment = new com.wlu.eduease.parent.ViewAttendanceFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, viewAttendanceFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        atRiskButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AtRiskStudent.class);
            startActivity(intent);
        });

        btnCourseMaterial.setOnClickListener(v -> {
            // Create the new fragment
            course_material courseMaterialFragment = new course_material();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, courseMaterialFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnTestsQuizzes.setOnClickListener(v -> {
            // Create the new fragment
            tests_marks_add testsMarksAddFragment = new tests_marks_add();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, testsMarksAddFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnTestSchedule.setOnClickListener(v -> {
            // Create the new fragment
            test_add testsAddFragment = new test_add();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, testsAddFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnClassSchedule.setOnClickListener(v -> {
            // Create the new fragment
            ClassScheduleFragment classScheduleFragment = new ClassScheduleFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, classScheduleFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnAssignments.setOnClickListener(v -> {
            // Create the new fragment
            AssignmentAddFragment assignmentAddFragment = new AssignmentAddFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, assignmentAddFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnGrades.setOnClickListener(v -> {
            // Create the new fragment
            AssignmentsMarksAddFragment assignmentsMarksAddFragment = new AssignmentsMarksAddFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, assignmentsMarksAddFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        btnPTMSchedule.setOnClickListener(v -> {
            // Create the new fragment
            ParentTeacherMeetingFragment parentTeacherMeetingFragment = new ParentTeacherMeetingFragment();

            // Replace the current fragment with the new fragment
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, parentTeacherMeetingFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        });

        return view;
    }
}
