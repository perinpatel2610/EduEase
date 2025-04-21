package com.wlu.eduease.parent;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewAttendanceFragment extends Fragment {

    private LinearLayout attendanceContainer;
    private DatabaseReference attendanceReference;
    private DatabaseReference studentTuplesReference;

    public ViewAttendanceFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_view_attendance, container, false);

        attendanceContainer = view.findViewById(R.id.attendanceContainer);
        attendanceReference = FirebaseDatabase.getInstance().getReference("attendance");
        studentTuplesReference = FirebaseDatabase.getInstance().getReference("studentTuples");

        loadAttendanceData();

        return view;
    }

    private void loadAttendanceData() {
        studentTuplesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot studentSnapshot) {
                attendanceReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot attendanceSnapshot) {
                        attendanceContainer.removeAllViews();
                        Map<String, String> studentNames = new HashMap<>();
                        for (DataSnapshot snapshot : studentSnapshot.getChildren()) {
                            String studentId = snapshot.child("StudentId").getValue(String.class);
                            String studentName = snapshot.child("Name").getValue(String.class);
                            if (studentId != null && studentName != null) {
                                studentNames.put(studentId, studentName);
                            }
                        }

                        for (Map.Entry<String, String> entry : studentNames.entrySet()) {
                            String studentId = entry.getKey();
                            String studentName = entry.getValue();
                            long presentDays = 0;
                            long totalDays = 0;
                            for (DataSnapshot dateSnapshot : attendanceSnapshot.child(studentId).getChildren()) {
                                totalDays++;
                                Boolean isPresent = dateSnapshot.getValue(Boolean.class);
                                if (Boolean.TRUE.equals(isPresent)) {
                                    presentDays++;
                                }
                            }
                            double attendancePercentage = (totalDays > 0) ? (double) presentDays / totalDays * 100 : 0;
                            addAttendanceView(studentName, attendancePercentage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getContext(), "Failed to load attendance: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load students: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addAttendanceView(String studentName, double attendancePercentage) {
        Context context = getContext();
        if (context == null) {
            return;
        }

        LinearLayout attendanceLayout = new LinearLayout(context);
        attendanceLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        attendanceLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView nameTextView = new TextView(context);
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        nameTextView.setText(studentName);
        nameTextView.setPadding(8, 8, 8, 8);

        TextView attendanceTextView = new TextView(context);
        attendanceTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        attendanceTextView.setText(String.format(Locale.getDefault(), "%.2f%%", attendancePercentage));
        attendanceTextView.setPadding(8, 8, 8, 8);

        attendanceLayout.addView(nameTextView);
        attendanceLayout.addView(attendanceTextView);

        attendanceContainer.addView(attendanceLayout);
    }
}
