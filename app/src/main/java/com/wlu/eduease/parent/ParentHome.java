package com.wlu.eduease.parent;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.wlu.eduease.R;
import com.wlu.eduease.faculty.AddAttendanceFragment;
import com.wlu.eduease.faculty.AtRiskStudent;

import java.util.Locale;

public class ParentHome extends Fragment {

    private CalendarView calendarView;
    private CardView scheduleCardView;
    private TextView facultyNameTextView;
    private TextView subjectTextView;
    private TextView timeTextView;
    private TextView roomTextView;
    private Button gradesButton,atRiskButton,viewAttendanceButton;

    private DatabaseReference databaseReference;

    public ParentHome() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_parent, container, false);

        calendarView = view.findViewById(R.id.calendarView);
        scheduleCardView = view.findViewById(R.id.ptmScheduleCardView);
        facultyNameTextView = view.findViewById(R.id.facultyNameTextView);
        subjectTextView = view.findViewById(R.id.subjectTextView);
        timeTextView = view.findViewById(R.id.timeTextView);
        roomTextView = view.findViewById(R.id.roomTextView);
        gradesButton = view.findViewById(R.id.gradesButton);
        atRiskButton = view.findViewById(R.id.btnAtRiskStudents);
        viewAttendanceButton = view.findViewById(R.id.viewAttendanceButton);

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
        databaseReference = FirebaseDatabase.getInstance().getReference("ptm_schedule");

        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            String selectedDate = String.format(Locale.getDefault(), "%d-%02d-%02d", year, month + 1, dayOfMonth);
            fetchPTMSchedule(selectedDate);
        });

        gradesButton.setOnClickListener(v -> openGradesFragment());

        return view;
    }

    private void fetchPTMSchedule(String date) {
        databaseReference.orderByChild("date").equalTo(date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                        String facultyName = scheduleSnapshot.child("faculty_name").getValue(String.class);
                        String subjectName = scheduleSnapshot.child("subject").getValue(String.class);
                        String time = scheduleSnapshot.child("time").getValue(String.class);
                        String room = scheduleSnapshot.child("room_number").getValue(String.class);

                        facultyNameTextView.setText(facultyName);
                        subjectTextView.setText(subjectName);
                        timeTextView.setText(time);
                        roomTextView.setText(room);
                    }
                } else {
                    facultyNameTextView.setText("No Data");
                    subjectTextView.setText("No Data");
                    timeTextView.setText("No Data");
                    roomTextView.setText("No Data");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });
    }

    private void openGradesFragment() {
        Fragment gradesFragment = new StudentGradesFragment();
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, gradesFragment); // Make sure R.id.fragment_container is the correct ID
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
