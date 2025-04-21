package com.wlu.eduease.student;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.wlu.eduease.R;
import java.util.ArrayList;
import java.util.List;

public class StudentScheduleFragment extends Fragment {

    private LinearLayout scheduleContainer;
    private DatabaseReference databaseReference;
    private static final String TAG = "StudentScheduleFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_schedule, container, false);

        scheduleContainer = view.findViewById(R.id.scheduleContainer);
        databaseReference = FirebaseDatabase.getInstance().getReference("class_schedules");

        // Fetch all schedule data when the fragment is created
        fetchAllClassSchedules();

        return view;
    }

    private void fetchAllClassSchedules() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                scheduleContainer.removeAllViews(); // Clear previous views

                if (snapshot.exists()) {
                    Log.d(TAG, "Data exists.");
                    boolean dataFound = false;
                    for (DataSnapshot scheduleSnapshot : snapshot.getChildren()) {
                        String facultyName = scheduleSnapshot.child("faculty_name").getValue(String.class);
                        String subjectName = scheduleSnapshot.child("subject").getValue(String.class);
                        String time = scheduleSnapshot.child("time").getValue(String.class);
                        String room = scheduleSnapshot.child("room").getValue(String.class);
                        String day = scheduleSnapshot.child("day").getValue(String.class); // Get day

                        Log.d(TAG, "Faculty Name: " + facultyName);
                        Log.d(TAG, "Subject Name: " + subjectName);
                        Log.d(TAG, "Time: " + time);
                        Log.d(TAG, "Room: " + room);
                        Log.d(TAG, "Day: " + day);

                        // Inflate the schedule item view and set data
                        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.schedule_item_view, scheduleContainer, false);

                        TextView dayTextView = itemView.findViewById(R.id.dayTextView);
                        TextView facultyNameTextView = itemView.findViewById(R.id.facultyNameTextView);
                        TextView subjectTextView = itemView.findViewById(R.id.subjectTextView);
                        TextView timeTextView = itemView.findViewById(R.id.timeTextView);
                        TextView roomTextView = itemView.findViewById(R.id.roomTextView);

                        dayTextView.setText(day != null ? day : "No Data");
                        facultyNameTextView.setText(facultyName != null ? facultyName : "No Data");
                        subjectTextView.setText(subjectName != null ? subjectName : "No Data");
                        timeTextView.setText(time != null ? time : "No Data");
                        roomTextView.setText(room != null ? room : "No Data");

                        scheduleContainer.addView(itemView);
                        dataFound = true;
                    }
                    if (!dataFound) {
                        // Optionally add a "No Data" message directly to the container
                        TextView noDataTextView = new TextView(getContext());
                        noDataTextView.setText("No Schedule Available");
                        noDataTextView.setTextSize(16);
                        noDataTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                        noDataTextView.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));
                        noDataTextView.setGravity(Gravity.CENTER);
                        scheduleContainer.addView(noDataTextView);
                    }
                } else {
                    // Optionally add a "No Data" message directly to the container
                    TextView noDataTextView = new TextView(getContext());
                    noDataTextView.setText("No Schedule Available");
                    noDataTextView.setTextSize(16);
                    noDataTextView.setTextColor(getResources().getColor(android.R.color.darker_gray));
                    noDataTextView.setLayoutParams(new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    ));
                    noDataTextView.setGravity(Gravity.CENTER);
                    scheduleContainer.addView(noDataTextView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
            }
        });
    }




}
