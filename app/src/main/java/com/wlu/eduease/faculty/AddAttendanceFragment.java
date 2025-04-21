package com.wlu.eduease.faculty;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddAttendanceFragment extends Fragment {

    private LinearLayout studentsContainer;
    private DatabaseReference attendanceReference;
    private DatabaseReference studentTuplesReference;
    private String currentDate;
    private ArrayList<StudentData> studentNames;

    public AddAttendanceFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_attendance, container, false);

        studentsContainer = view.findViewById(R.id.studentsContainer);
        attendanceReference = FirebaseDatabase.getInstance().getReference("attendance");
        studentTuplesReference = FirebaseDatabase.getInstance().getReference("studentTuples");

        currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ||
                calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            Toast.makeText(getContext(), "Attendance cannot be marked on weekends.", Toast.LENGTH_SHORT).show();

            // Navigate back to the faculty_home fragment
            if (getFragmentManager() != null) {
                Fragment facultyHomeFragment = new FacultyHome();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, facultyHomeFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }

            return view;
        }


        TextView dateTextView = view.findViewById(R.id.dateTextView);
        dateTextView.setText(currentDate);

        studentNames = new ArrayList<>();
        loadAllStudentNames();

        Button btnSaveAttendance = view.findViewById(R.id.btnSaveAttendance);
        btnSaveAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveAttendance();
            }
        });

        return view;
    }

    private void loadAllStudentNames() {
        studentTuplesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentsContainer.removeAllViews();
                studentNames.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String studentName = snapshot.child("Name").getValue(String.class);
                    String studentId = snapshot.child("StudentId").getValue(String.class);
                    if (studentName != null && studentId != null) {
                        studentNames.add(new StudentData(studentName, studentId));
                        addStudentView(studentName, studentId);
                    }
                }
                Log.d("AddAttendanceFragment", "Student names loaded: " + studentNames.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load students: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AddAttendanceFragment", "Failed to load students: " + databaseError.getMessage());
            }
        });
    }

    private void addStudentView(String studentName, String studentId) {
        // Ensure getContext() is not null
        Context context = getContext();
        if (context == null) {
            Log.e("AddAttendanceFragment", "Context is null in addStudentView");
            return;
        }

        // Create a horizontal LinearLayout
        LinearLayout studentLayout = new LinearLayout(context);
        studentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        studentLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create a TextView for student name
        TextView nameTextView = new TextView(context);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        nameTextView.setText(studentName);
        nameTextView.setPadding(8, 8, 8, 8);

        // Create a CheckBox for attendance
        CheckBox attendanceCheckBox = new CheckBox(context);
        attendanceCheckBox.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        attendanceCheckBox.setTag(studentId);
        attendanceCheckBox.setPadding(8, 8, 8, 8);

        // Add views to horizontal LinearLayout
        studentLayout.addView(nameTextView);
        studentLayout.addView(attendanceCheckBox);

        // Add horizontal LinearLayout to container
        studentsContainer.addView(studentLayout);
    }

    private void saveAttendance() {
        Map<String, Object> attendanceMap = new HashMap<>();

        for (int i = 0; i < studentsContainer.getChildCount(); i++) {
            View view = studentsContainer.getChildAt(i);
            if (view instanceof LinearLayout) {
                LinearLayout studentLayout = (LinearLayout) view;
                CheckBox checkBox = (CheckBox) studentLayout.getChildAt(1);
                String studentId = (String) checkBox.getTag();
                boolean isPresent = checkBox.isChecked();
                attendanceMap.put(studentId + "/" + currentDate, isPresent);
            }
        }

        attendanceReference.updateChildren(attendanceMap)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Attendance saved", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to save attendance", Toast.LENGTH_SHORT).show());
    }

    // Class for storing student data
    static class StudentData {
        String name;
        String id;

        StudentData(String name, String id) {
            this.name = name;
            this.id = id;
        }
    }
}
