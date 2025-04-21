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

public class tests_marks_add extends Fragment {

    private Spinner spinnerTests;
    private LinearLayout studentMarksContainer;
    private DatabaseReference studentDataRef, quizzesRef;
    private ArrayList<StudentData> studentNames;
    private ArrayList<QuizData> quizList;
    private QuizAdapter quizAdapter;
    private String selectedQuizId;

    public tests_marks_add() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tests_marks_add, container, false);

        // Initialize UI components
        spinnerTests = view.findViewById(R.id.spinnerTests);
        studentMarksContainer = view.findViewById(R.id.llStudentMarksContainer);
        Button btnSaveMarks = view.findViewById(R.id.btnSaveMarks);

        // Initialize Firebase Database references
        studentDataRef = FirebaseDatabase.getInstance().getReference().child("studentTuples");
        quizzesRef = FirebaseDatabase.getInstance().getReference().child("quizzes");

        // Initialize student names and quiz list
        studentNames = new ArrayList<>();
        quizList = new ArrayList<>();

        // Initialize Spinner and Adapter
        quizAdapter = new QuizAdapter(requireContext(), quizList);
        spinnerTests.setAdapter(quizAdapter);

        // Load data
        loadAllStudentNames();
        loadAllQuizzes();

        // Spinner item selection listener
        spinnerTests.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < quizList.size()) {
                    selectedQuizId = quizList.get(position).quizId;
                    loadMarksForSelectedQuiz(); // Load marks for the selected quiz
                } else {
                    selectedQuizId = null;
                }
                Log.d("tests_marks_add", "Selected quiz ID: " + selectedQuizId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedQuizId = null;
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
                Log.d("tests_marks_add", "Student names loaded: " + studentNames.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load students: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("tests_marks_add", "Failed to load students: " + databaseError.getMessage());
            }
        });
    }

    private void loadAllQuizzes() {
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                quizList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String quizId = snapshot.getKey(); // Get the quiz ID
                    String title = snapshot.child("title").getValue(String.class); // Get the quiz title
                    if (quizId != null && title != null) {
                        quizList.add(new QuizData(quizId, title));
                    }
                }
                Log.d("tests_marks_add", "Quiz list loaded: " + quizList.size());
                quizAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load quizzes: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("tests_marks_add", "Failed to load quizzes: " + databaseError.getMessage());
            }
        });
    }

    private void addStudentView(String studentName) {
        // Ensure getContext() is not null
        Context context = getContext();
        if (context == null) {
            Log.e("tests_marks_add", "Context is null in addStudentView");
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

        // Create an EditText for marks
        EditText marksEditText = new EditText(context);
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
        if (selectedQuizId == null) {
            Toast.makeText(requireContext(), "Please select a test", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(requireContext(), "Invalid marks for student: " + studentName, Toast.LENGTH_SHORT).show();
                }
            } else {
                allMarksEntered = false;
                Toast.makeText(requireContext(), "Please enter marks for all students", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (allMarksEntered) {
            DatabaseReference marksRef = quizzesRef.child(selectedQuizId).child("marks");
            marksRef.updateChildren(marksMap)
                    .addOnSuccessListener(aVoid -> Toast.makeText(requireContext(), "Marks saved for test: " + selectedQuizId, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(requireContext(), "Failed to save marks: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void loadMarksForSelectedQuiz() {
        if (selectedQuizId == null) return;

        DatabaseReference marksRef = quizzesRef.child(selectedQuizId).child("marks");
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

                    // Load marks from Firebase and set to EditText
                    Object marks = dataSnapshot.child(studentName).getValue();
                    if (marks != null) {
                        marksEditText.setText(String.valueOf(marks));
                    } else {
                        marksEditText.setText(""); // Clear if no marks are available
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(requireContext(), "Failed to load marks: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("tests_marks_add", "Failed to load marks: " + databaseError.getMessage());
            }
        });
    }

    // Class for storing student data
    static class StudentData {
        String name;

        StudentData(String name) {
            this.name = name;
        }
    }

    // Class for storing quiz data
    static class QuizData {
        String quizId;
        String title;

        QuizData(String quizId, String title) {
            this.quizId = quizId;
            this.title = title;
        }
    }

    // Custom adapter for quizzes
    private static class QuizAdapter extends ArrayAdapter<QuizData> {

        public QuizAdapter(@NonNull Context context, ArrayList<QuizData> quizzes) {
            super(context, 0, quizzes);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return initView(position, convertView, parent);
        }

        private View initView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_spinner_item, parent, false);
            }

            TextView textView = convertView.findViewById(android.R.id.text1);
            QuizData quiz = getItem(position);
            if (quiz != null) {
                textView.setText("ID: " + quiz.quizId + " | Title: " + quiz.title);
            }
            return convertView;
        }
    }
}
