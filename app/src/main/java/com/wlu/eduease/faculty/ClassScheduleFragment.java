package com.wlu.eduease.faculty;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClassScheduleFragment extends Fragment {

    private EditText roomEditText,dayTextView, timeTextView;
    private Spinner subjectSpinner;
    private TextView facultyNameText;
    private Button saveButton;
    private RecyclerView scheduleRecyclerView;
    private DatabaseReference classSchedulesRef;
    private DatabaseReference userRef;
    private DatabaseReference facultyDataRef;
    private String currentScheduleId;
    private List<String> subjectsList;
    private List<Schedule> scheduleList;
    private ScheduleAdapter scheduleAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_schedule, container, false);

        facultyNameText = view.findViewById(R.id.facultyNameText);
        subjectSpinner = view.findViewById(R.id.subjectSpinner);
        dayTextView = view.findViewById(R.id.dayEditText);
        timeTextView = view.findViewById(R.id.timeEditText);
        roomEditText = view.findViewById(R.id.roomEditText);
        saveButton = view.findViewById(R.id.saveButton);
        scheduleRecyclerView = view.findViewById(R.id.scheduleRecyclerView);

        classSchedulesRef = FirebaseDatabase.getInstance().getReference("class_schedules");
        userRef = FirebaseDatabase.getInstance().getReference("users");
        facultyDataRef = FirebaseDatabase.getInstance().getReference("faculty_data");

        subjectsList = new ArrayList<>();
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(scheduleList, new ScheduleAdapter.OnScheduleClickListener() {
            @Override
            public void onEditClick(Schedule schedule) {
                populateFieldsForEdit(schedule);
            }

            @Override
            public void onDeleteClick(Schedule schedule) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Schedule")
                        .setMessage("Are you sure you want to delete this schedule?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteSchedule(schedule.getId()))
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        setFacultyName();
        populateSubjects();

        dayTextView.setOnClickListener(v -> showDatePicker());
        timeTextView.setOnClickListener(v -> showTimePicker());

        saveButton.setOnClickListener(v -> saveClassSchedule());

        loadClassSchedules();

        return view;
    }

    private void setFacultyName() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            userRef.child(userId).child("fullname").addListenerForSingleValueEvent(new ValueEventListener() {
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
            facultyDataRef.child(userId).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    subjectsList.clear();
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
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // Format month as MM
                    String monthStr = String.format("%02d", selectedMonth + 1);
                    // Format day as DD
                    String dayStr = String.format("%02d", selectedDay);
                    // Set date in YYYY-MM-DD format
                    String date = selectedYear + "-" + monthStr + "-" + dayStr;
                    dayTextView.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR); // Use HOUR for 12-hour format
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                (view, selectedHour, selectedMinute) -> {
                    String amPm = (selectedHour >= 12) ? "PM" : "AM";
                    int displayHour = (selectedHour % 12 == 0) ? 12 : selectedHour % 12; // Convert to 12-hour format
                    String time = String.format("%02d:%02d %s", displayHour, selectedMinute, amPm);
                    timeTextView.setText(time);
                }, hour, minute, false); // false for 12-hour format
        timePickerDialog.show();
    }

    private void saveClassSchedule() {
        String facultyName = facultyNameText.getText().toString().trim();
        String subject = (String) subjectSpinner.getSelectedItem();
        String day = dayTextView.getText().toString().trim();
        String time = timeTextView.getText().toString().trim();
        String room = roomEditText.getText().toString().trim();

        if (!facultyName.isEmpty() && !subject.isEmpty() && !day.isEmpty() && !time.isEmpty() && !room.isEmpty()) {
            if (currentScheduleId == null) {
                // Create a new schedule
                String scheduleId = UUID.randomUUID().toString(); // Generate a unique ID for the schedule
                Map<String, Object> schedule = new HashMap<>();
                schedule.put("faculty_name", facultyName);
                schedule.put("subject", subject);
                schedule.put("day", day);
                schedule.put("time", time);
                schedule.put("room", room);

                classSchedulesRef.child(scheduleId).setValue(schedule)
                        .addOnSuccessListener(aVoid -> {
                            // Successfully saved
                            showSnackbar("Schedule saved successfully");
                            clearFields();
                        })
                        .addOnFailureListener(e -> {
                            // Failed to save
                            showSnackbar("Failed to save schedule");
                            e.printStackTrace();
                        });
            } else {
                // Update an existing schedule
                Schedule updatedSchedule = new Schedule();
                updatedSchedule.setId(currentScheduleId); // Set the ID
                updatedSchedule.setFaculty_name(facultyName);
                updatedSchedule.setSubject(subject);
                updatedSchedule.setDay(day);
                updatedSchedule.setTime(time);
                updatedSchedule.setRoom(room);

                updateSchedule(currentScheduleId, updatedSchedule);
            }
        } else {
            showSnackbar("Please fill all the fields");
        }
    }

    private void updateSchedule(String scheduleId, Schedule updatedSchedule) {
        classSchedulesRef.child(scheduleId).setValue(updatedSchedule)
                .addOnSuccessListener(aVoid -> {
                    showSnackbar("Schedule updated successfully");
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    showSnackbar("Failed to update schedule");
                    e.printStackTrace();
                });
    }

    private void deleteSchedule(String scheduleId) {
        classSchedulesRef.child(scheduleId).removeValue()
                .addOnSuccessListener(aVoid -> showSnackbar("Schedule deleted successfully"))
                .addOnFailureListener(e -> {
                    showSnackbar("Failed to delete schedule");
                    e.printStackTrace();
                });
    }

    private void showSnackbar(String message) {
        View rootView = getView();
        if (rootView != null) {
            Snackbar.make(rootView, message, Snackbar.LENGTH_SHORT).show();
        }
    }


    private void clearFields() {
        subjectSpinner.setSelection(0);
        dayTextView.setText("");
        timeTextView.setText("");
        roomEditText.setText("");

        saveButton.setText("Save Schedule");
        currentScheduleId = null;
    }

    private void loadClassSchedules() {
        classSchedulesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                scheduleList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Schedule schedule = snapshot.getValue(Schedule.class);
                    if (schedule != null) {
                        schedule.setId(snapshot.getKey()); // Set ID from the key
                        scheduleList.add(schedule);
                    }
                }
                scheduleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                databaseError.toException().printStackTrace();
            }
        });
    }

    private void populateFieldsForEdit(Schedule schedule) {
        facultyNameText.setText(schedule.getFaculty_name());
        subjectSpinner.setSelection(subjectsList.indexOf(schedule.getSubject()));
        dayTextView.setText(schedule.getDay());
        timeTextView.setText(schedule.getTime());
        roomEditText.setText(schedule.getRoom());

        saveButton.setText("Update Schedule");
        currentScheduleId = schedule.getId();
    }

    private static class Schedule {
        private String id;
        private String faculty_name;
        private String subject;
        private String day;
        private String time;
        private String room;

        public Schedule() {}

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFaculty_name() {
            return faculty_name;
        }

        public void setFaculty_name(String faculty_name) {
            this.faculty_name = faculty_name;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getDay() {
            return day;
        }

        public void setDay(String day) {
            this.day = day;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public String getRoom() {
            return room;
        }

        public void setRoom(String room) {
            this.room = room;
        }
    }

    private static class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

        private final List<Schedule> scheduleList;
        private final OnScheduleClickListener listener;

        public ScheduleAdapter(List<Schedule> scheduleList, OnScheduleClickListener listener) {
            this.scheduleList = scheduleList;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_schedule, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Schedule schedule = scheduleList.get(position);
            holder.facultyNameTextView.setText(schedule.getFaculty_name());
            holder.subjectTextView.setText(schedule.getSubject());
            holder.dayTextView.setText(schedule.getDay());
            holder.timeTextView.setText(schedule.getTime());
            holder.roomTextView.setText(schedule.getRoom());

            holder.editButton.setOnClickListener(v -> listener.onEditClick(schedule));
            holder.deleteButton.setOnClickListener(v -> listener.onDeleteClick(schedule));
        }

        @Override
        public int getItemCount() {
            return scheduleList.size();
        }

        public interface OnScheduleClickListener {
            void onEditClick(Schedule schedule);
            void onDeleteClick(Schedule schedule);
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView facultyNameTextView;
            TextView subjectTextView;
            TextView dayTextView;
            TextView timeTextView;
            TextView roomTextView;
            Button editButton;
            Button deleteButton;

            ViewHolder(View itemView) {
                super(itemView);
                facultyNameTextView = itemView.findViewById(R.id.facultyNameTextView);
                subjectTextView = itemView.findViewById(R.id.subjectTextView);
                dayTextView = itemView.findViewById(R.id.dayTextView);
                timeTextView = itemView.findViewById(R.id.timeTextView);
                roomTextView = itemView.findViewById(R.id.roomTextView);
                editButton = itemView.findViewById(R.id.editButton);
                deleteButton = itemView.findViewById(R.id.deleteButton);
            }
        }
    }
}
