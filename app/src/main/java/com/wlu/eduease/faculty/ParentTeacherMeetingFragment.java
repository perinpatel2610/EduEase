package com.wlu.eduease.faculty;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ParentTeacherMeetingFragment extends Fragment {

    private TextView facultyNameText;
    private Spinner subjectSpinner;
    private EditText dateEditText;
    private EditText timeEditText;
    private EditText roomEditText;
    private Button saveButton;
    private RecyclerView meetingsRecyclerView;

    private DatabaseReference ptmDatabaseReference;
    private DatabaseReference usersDatabaseReference;
    private DatabaseReference facultyDataReference;
    private MeetingAdapter meetingAdapter;
    private List<Meeting> meetingList;

    private String currentMeetingId; // Track the currently selected meeting ID for updates

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent_teacher_meeting, container, false);

        facultyNameText = view.findViewById(R.id.facultyNameText);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        dateEditText = view.findViewById(R.id.dateEditText);
        timeEditText = view.findViewById(R.id.timeEditText);
        roomEditText = view.findViewById(R.id.roomEditText);
        saveButton = view.findViewById(R.id.saveButton);
        meetingsRecyclerView = view.findViewById(R.id.meetingsRecyclerView);

        ptmDatabaseReference = FirebaseDatabase.getInstance().getReference("ptm_schedule");
        usersDatabaseReference = FirebaseDatabase.getInstance().getReference("users");
        facultyDataReference = FirebaseDatabase.getInstance().getReference("faculty_data");

        meetingList = new ArrayList<>();
        meetingAdapter = new MeetingAdapter(meetingList);
        meetingsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        meetingsRecyclerView.setAdapter(meetingAdapter);

        setFacultyName();
        populateSubjects();
        loadMeetings();

        dateEditText.setOnClickListener(v -> showDatePicker());
        timeEditText.setOnClickListener(v -> showTimePicker());

        saveButton.setOnClickListener(v -> saveMeetingDetails());

        return view;
    }

    private void setFacultyName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            usersDatabaseReference.child(userId).child("fullname").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String currentUserName = dataSnapshot.getValue(String.class);
                    if (currentUserName != null) {
                        facultyNameText.setText(currentUserName);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
        } else {
            facultyNameText.setText("Not Logged In");
        }
    }

    private void populateSubjects() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            facultyDataReference.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    List<String> subjectsList = new ArrayList<>();
                    for (DataSnapshot subjectSnapshot : dataSnapshot.getChildren()) {
                        String subject = subjectSnapshot.getValue(String.class);
                        if (subject != null) {
                            subjectsList.add(subject);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subjectsList);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    subjectSpinner.setAdapter(adapter);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    databaseError.toException().printStackTrace();
                }
            });
        }
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year1, month1, dayOfMonth) -> {
            dateEditText.setText(String.format("%d-%02d-%02d", year1, month1 + 1, dayOfMonth));
        }, year, month, day);

        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view, hourOfDay, minute1) -> {
            timeEditText.setText(String.format("%02d:%02d", hourOfDay, minute1));
        }, hour, minute, true);
        timePickerDialog.show();
    }

    private void saveMeetingDetails() {
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();
        String roomNumber = roomEditText.getText().toString();
        String selectedSubject = subjectSpinner.getSelectedItem().toString();
        String facultyName = facultyNameText.getText().toString();

        if (TextUtils.isEmpty(date) || TextUtils.isEmpty(time) || TextUtils.isEmpty(roomNumber) || selectedSubject.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String, String> meetingData = new HashMap<>();
        meetingData.put("date", date);
        meetingData.put("time", time);
        meetingData.put("room_number", roomNumber);
        meetingData.put("subject", selectedSubject);
        meetingData.put("faculty_name", facultyName);

        DatabaseReference meetingRef;
        if (currentMeetingId != null) {
            meetingRef = ptmDatabaseReference.child(currentMeetingId);
            saveButton.setText("Save Meeting"); // Reset button text to "Save" after update
        } else {
            meetingRef = ptmDatabaseReference.push();
        }

        meetingRef.setValue(meetingData).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Meeting saved successfully", Toast.LENGTH_SHORT).show();
                dateEditText.setText("");
                timeEditText.setText("");
                roomEditText.setText("");
                loadMeetings();
            } else {
                Toast.makeText(getContext(), "Failed to save meeting", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadMeetings() {
        ptmDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                meetingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Meeting meeting = snapshot.getValue(Meeting.class);
                    if (meeting != null) {
                        meeting.setId(snapshot.getKey()); // Set the ID of the meeting
                        meetingList.add(meeting);
                    }
                }
                meetingAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    public void populateFieldsForUpdate(Meeting meeting) {
        dateEditText.setText(meeting.date);
        timeEditText.setText(meeting.time);
        roomEditText.setText(meeting.room_number);
        subjectSpinner.setSelection(((ArrayAdapter<String>) subjectSpinner.getAdapter()).getPosition(meeting.subject));
        facultyNameText.setText(meeting.faculty_name);
        currentMeetingId = meeting.getId(); // Save the ID of the meeting to be updated
        saveButton.setText("Update Meeting"); // Change button text for update
    }

    public static class Meeting {
        public String date;
        public String time;
        public String room_number;
        public String subject;
        public String faculty_name;
        private String id; // Add this field

        public Meeting() {
            // Default constructor required for calls to DataSnapshot.getValue(Meeting.class)
        }

        public Meeting(String date, String time, String room_number, String subject, String faculty_name) {
            this.date = date;
            this.time = time;
            this.room_number = room_number;
            this.subject = subject;
            this.faculty_name = faculty_name;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    private class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.MeetingViewHolder> {

        private final List<Meeting> meetings;

        public MeetingAdapter(List<Meeting> meetings) {
            this.meetings = meetings;
        }

        @NonNull
        @Override
        public MeetingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meeting, parent, false);
            return new MeetingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MeetingViewHolder holder, int position) {
            Meeting meeting = meetings.get(position);
            holder.dateTextView.setText("Date: " + meeting.date);
            holder.timeTextView.setText("Time: " + meeting.time);
            holder.roomTextView.setText("Room: " + meeting.room_number);
            holder.subjectTextView.setText("Subject: " + meeting.subject);
            holder.facultyNameTextView.setText("Faculty: " + meeting.faculty_name);

            holder.itemView.setOnClickListener(v -> {
                // Populate fields for updating
                populateFieldsForUpdate(meeting);
            });

            holder.deleteButton.setOnClickListener(v -> {
                // Remove the meeting from the database
                DatabaseReference meetingRef = ptmDatabaseReference.child(meeting.getId());
                meetingRef.removeValue().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Meeting deleted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to delete meeting", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return meetings.size();
        }

        public class MeetingViewHolder extends RecyclerView.ViewHolder {
            public TextView dateTextView;
            public TextView timeTextView;
            public TextView roomTextView;
            public TextView subjectTextView;
            public TextView facultyNameTextView;
            public Button deleteButton;

            public MeetingViewHolder(@NonNull View itemView) {
                super(itemView);
                dateTextView = itemView.findViewById(R.id.dateTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                roomTextView = itemView.findViewById(R.id.roomTextView);
                subjectTextView = itemView.findViewById(R.id.subjectTextView);
                facultyNameTextView = itemView.findViewById(R.id.facultyNameTextView);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}
