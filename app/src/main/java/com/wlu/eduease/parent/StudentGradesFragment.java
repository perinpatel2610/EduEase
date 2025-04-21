// StudentGradesFragment.java
package com.wlu.eduease.parent;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.util.ArrayList;
import java.util.List;

public class StudentGradesFragment extends Fragment {

    private Spinner studentSpinner;
    private RecyclerView gradesRecyclerView;
    private DatabaseReference databaseReference;
    private List<String> studentList;
    private List<String> studentIdList; // Stores StudentIds for marks retrieval
    private GradeAdapter gradeAdapter;
    private List<String> gradesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_grades, container, false);
        studentSpinner = view.findViewById(R.id.studentSpinner);
        gradesRecyclerView = view.findViewById(R.id.gradesRecyclerView);

        studentList = new ArrayList<>();
        studentIdList = new ArrayList<>();
        gradesList = new ArrayList<>();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        gradeAdapter = new GradeAdapter(gradesList);
        gradesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        gradesRecyclerView.setAdapter(gradeAdapter);

        fetchStudents();

        studentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String studentName = studentList.get(position);
                fetchGrades(studentName);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                gradesList.clear();
                gradeAdapter.notifyDataSetChanged();
            }
        });

        return view;
    }

    private void fetchStudents() {
        databaseReference.child("studentTuples").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                studentList.clear();
                studentIdList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String studentName = snapshot.child("Name").getValue(String.class);
                    String studentId = snapshot.child("StudentId").getValue(String.class);
                    if (studentName != null && studentId != null) {
                        studentList.add(studentName);
                        studentIdList.add(studentId);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, studentList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                studentSpinner.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void fetchGrades(String studentName) {
        gradesList.clear();
        fetchQuizGrades(studentName);
        fetchAssignmentGrades(studentName);
    }

    private void fetchQuizGrades(final String studentName) {
        databaseReference.child("quizzes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey();
                    Long mark = quizSnapshot.child("marks").child(studentName).getValue(Long.class);
                    if (mark != null) {
                        gradesList.add("Quiz " + quizId + ": " + mark);
                    }
                }
                gradeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void fetchAssignmentGrades(final String studentName) {
        databaseReference.child("assignments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot assignmentSnapshot : dataSnapshot.getChildren()) {
                    String assignmentId = assignmentSnapshot.getKey();
                    Long mark = assignmentSnapshot.child("marks").child(studentName).getValue(Long.class);
                    if (mark != null) {
                        gradesList.add("Assignment " + assignmentId + ": " + mark);
                    }
                }
                gradeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.GradeViewHolder> {

        private List<String> gradesList;

        public GradeAdapter(List<String> gradesList) {
            this.gradesList = gradesList;
        }

        @NonNull
        @Override
        public GradeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_grade_card, parent, false);
            return new GradeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GradeViewHolder holder, int position) {
            String gradeEntry = gradesList.get(position);
            // Assuming your gradeEntry is formatted as "Title: Marks"
            String[] parts = gradeEntry.split(": ");
            if (parts.length == 2) {
                holder.titleTextView.setText(parts[0]); // Title part
                holder.marksTextView.setText(parts[1]); // Marks part
            }
        }

        @Override
        public int getItemCount() {
            return gradesList.size();
        }

        class GradeViewHolder extends RecyclerView.ViewHolder {

            TextView titleTextView;
            TextView marksTextView;

            public GradeViewHolder(@NonNull View itemView) {
                super(itemView);
                titleTextView = itemView.findViewById(R.id.titleTextView);
                marksTextView = itemView.findViewById(R.id.marksTextView);
            }
        }
    }

}
