package com.wlu.eduease.faculty;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AssignmentsMarksAddFragment extends Fragment {

    private Spinner spinnerAssignments;
    private LinearLayout studentMarksContainer;
    private DatabaseReference studentDataRef, assignmentsRef;
    private ArrayList<StudentData> studentNames;
    private ArrayList<AssignmentData> assignmentList;
    private AssignmentAdapter assignmentAdapter;
    private String selectedAssignmentId;

    public AssignmentsMarksAddFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_assignments_marks_add, container, false);

        // Initialize UI components
        spinnerAssignments = view.findViewById(R.id.spinnerAssignments);
        studentMarksContainer = view.findViewById(R.id.llStudentMarksContainer);

        Button btnSaveMarks = view.findViewById(R.id.btnSaveMarks);

        // Initialize Firebase Database references
        studentDataRef = FirebaseDatabase.getInstance().getReference().child("studentTuples");
        assignmentsRef = FirebaseDatabase.getInstance().getReference().child("assignments");

        // Initialize student names and assignment list
        studentNames = new ArrayList<>();
        assignmentList = new ArrayList<>();

        // Initialize Spinner and Adapter
        assignmentAdapter = new AssignmentAdapter(getActivity(), assignmentList);
        spinnerAssignments.setAdapter(assignmentAdapter);

        // Load data
        loadAllStudentNames();
        loadAllAssignments();

        // Spinner item selection listener
        spinnerAssignments.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < assignmentList.size()) {
                    selectedAssignmentId = assignmentList.get(position).assignmentId;
                    loadMarksForSelectedAssignment(); // Load marks for the selected assignment
                } else {
                    selectedAssignmentId = null;
                }
                Log.d("AssignmentsMarksAddFragment", "Selected assignment ID: " + selectedAssignmentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedAssignmentId = null;
            }
        });

        // Save marks button click listener
        btnSaveMarks.setOnClickListener(v -> saveMarks());

        return view;
    }

    private void loadAllStudentNames() {
        studentDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentMarksContainer.removeAllViews();
                studentNames.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String studentName = snapshot.child("Name").getValue(String.class);
                    if (studentName != null) {
                        studentNames.add(new StudentData(studentName));
                        addStudentView(studentName);
                    }
                }
                Log.d("AssignmentsMarksAddFragment", "Student names loaded: " + studentNames.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load students: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AssignmentsMarksAddFragment", "Failed to load students: " + databaseError.getMessage());
            }
        });
    }

    private void loadAllAssignments() {
        assignmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                assignmentList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String assignmentId = snapshot.getKey(); // Get the assignment ID
                    String title = snapshot.child("title").getValue(String.class); // Get the assignment title
                    if (assignmentId != null && title != null) {
                        assignmentList.add(new AssignmentData(assignmentId, title));
                    }
                }
                Log.d("AssignmentsMarksAddFragment", "Assignment list loaded: " + assignmentList.size());
                assignmentAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load assignments: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("AssignmentsMarksAddFragment", "Failed to load assignments: " + databaseError.getMessage());
            }
        });
    }

    private void addStudentView(String studentName) {
        // Create a horizontal LinearLayout
        LinearLayout studentLayout = new LinearLayout(getActivity());
        studentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        studentLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Create a TextView for student name
        TextView nameTextView = new TextView(getActivity());
        nameTextView.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        nameTextView.setText(studentName);
        nameTextView.setPadding(8, 8, 8, 8);

        // Create an EditText for marks
        EditText marksEditText = new EditText(getActivity());
        marksEditText.setLayoutParams(new LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f));
        marksEditText.setHint("Marks");
        marksEditText.setPadding(8, 8, 8, 8);

        // Add views to horizontal LinearLayout
        studentLayout.addView(nameTextView);
        studentLayout.addView(marksEditText);

        // Add horizontal LinearLayout to container
        studentMarksContainer.addView(studentLayout);
    }

    private void saveMarks() {
        if (selectedAssignmentId == null) {
            Toast.makeText(getActivity(), "Please select an assignment", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean allMarksEntered = true;
        Map<String, Object> marksMap = new HashMap<>();

        for (int i = 0; i < studentMarksContainer.getChildCount(); i++) {
            // Get the student layout at the current index
            LinearLayout studentLayout = (LinearLayout) studentMarksContainer.getChildAt(i);

            // Get the student name from the TextView
            TextView nameTextView = (TextView) studentLayout.getChildAt(0);
            String studentName = nameTextView.getText().toString();

            // Get the marks from the EditText
            EditText marksEditText = (EditText) studentLayout.getChildAt(1);
            String marksString = marksEditText.getText().toString().trim();

            if (!marksString.isEmpty()) {
                try {
                    int marks = Integer.parseInt(marksString);
                    marksMap.put(studentName, marks);
                } catch (NumberFormatException e) {
                    allMarksEntered = false;
                    Toast.makeText(getActivity(), "Invalid marks for student: " + studentName, Toast.LENGTH_SHORT).show();
                }
            } else {
                allMarksEntered = false;
                Toast.makeText(getActivity(), "Please enter marks for all students", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (allMarksEntered) {
            DatabaseReference marksRef = assignmentsRef.child(selectedAssignmentId).child("marks");
            marksRef.updateChildren(marksMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getActivity(), "Marks saved for assignment: " + selectedAssignmentId, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getActivity(), "Failed to save marks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadMarksForSelectedAssignment() {
        if (selectedAssignmentId == null) return;

        DatabaseReference marksRef = assignmentsRef.child(selectedAssignmentId).child("marks");
        marksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (int i = 0; i < studentMarksContainer.getChildCount(); i++) {
                    // Get the student layout at the current index
                    LinearLayout studentLayout = (LinearLayout) studentMarksContainer.getChildAt(i);

                    // Get the student name from the TextView
                    TextView nameTextView = (TextView) studentLayout.getChildAt(0);
                    String studentName = nameTextView.getText().toString();

                    // Get the marks EditText
                    EditText marksEditText = (EditText) studentLayout.getChildAt(1);

                    // Load marks from Firebase if available
                    Integer marks = dataSnapshot.child(studentName).getValue(Integer.class);
                    if (marks != null) {
                        marksEditText.setText(String.valueOf(marks));
                    } else {
                        marksEditText.setText(""); // Clear the EditText if no marks are found
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load marks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Class to hold student data
    private static class StudentData {
        public String studentName;

        public StudentData(String studentName) {
            this.studentName = studentName;
        }
    }

    // Class to hold assignment data
    private static class AssignmentData {
        public String assignmentId;
        public String title;

        public AssignmentData(String assignmentId, String title) {
            this.assignmentId = assignmentId;
            this.title = title;
        }
    }

    // Adapter for assignment Spinner
    private static class AssignmentAdapter extends ArrayAdapter<AssignmentData> {

        public AssignmentAdapter(Context context, ArrayList<AssignmentData> assignmentList) {
            super(context, android.R.layout.simple_spinner_item, assignmentList);
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
            }
            AssignmentData assignment = getItem(position);
            if (assignment != null) {
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(assignment.title);
            }
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false);
            }
            AssignmentData assignment = getItem(position);
            if (assignment != null) {
                TextView textView = convertView.findViewById(android.R.id.text1);
                textView.setText(assignment.title);
            }
            return convertView;
        }
    }
}
