package com.wlu.eduease.faculty;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class test_add extends Fragment {

    private static final String TAG = "test_add";
    private Spinner spinnerSubject;
    private EditText editTextTitle, editTextDate, editTextPdfUrl;
    private Button buttonSaveQuiz;
    private TableLayout tableLayoutQuizzes;
    private DatabaseReference facultyDataRef, quizzesRef;
    private List<String> subjects = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String selectedSubject;
    private String editingQuizId; // Store the ID of the quiz being edited

    public test_add() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tests_add, container, false);

        // Initialize Firebase Database references
        facultyDataRef = FirebaseDatabase.getInstance().getReference("faculty_data");
        quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");

        // Initialize UI components
        spinnerSubject = view.findViewById(R.id.spinnerSubject);
        editTextTitle = view.findViewById(R.id.editTextTitle);
        editTextDate = view.findViewById(R.id.editTextDate);
        editTextPdfUrl = view.findViewById(R.id.editTextPdfUrl);
        buttonSaveQuiz = view.findViewById(R.id.buttonSaveQuiz);
        tableLayoutQuizzes = view.findViewById(R.id.tableLayoutQuizzes);

        // Initialize Spinner and Adapter
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, subjects);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSubject.setAdapter(adapter);

        // Load subjects into Spinner
        loadAllSubjects();

        // Spinner item selection listener
        spinnerSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSubject = subjects.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedSubject = null;
            }
        });

        // Set OnClickListener for the date EditText
        editTextDate.setOnClickListener(v -> showDatePickerDialog());

        // Save quiz button click listener
        buttonSaveQuiz.setOnClickListener(v -> {
            if (editingQuizId == null) {
                saveQuiz(); // Save new quiz
            } else {
                updateQuiz(); // Update existing quiz
            }
        });

        // Load quizzes into TableLayout
        loadQuizzes();

        return view;
    }

    private void loadAllSubjects() {
        facultyDataRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                subjects.clear();
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    for (DataSnapshot courseSnapshot : userSnapshot.child("name").getChildren()) {
                        String courseName = courseSnapshot.getValue(String.class);
                        subjects.add(courseName);
                    }
                }
                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
                Log.d(TAG, "Number of subjects loaded: " + subjects.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to load subjects: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Failed to load subjects: " + databaseError.getMessage());
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
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
                    editTextDate.setText(date);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveQuiz() {
        String title = editTextTitle.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String pdfUrl = editTextPdfUrl.getText().toString().trim();

        if (selectedSubject == null || title.isEmpty() || date.isEmpty() || pdfUrl.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields and provide a PDF URL", Toast.LENGTH_SHORT).show();
            return;
        }

        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Find the next available ID
                int nextId = getNextAvailableId(dataSnapshot);

                // Create and add the new quiz with the next ID
                addQuiz(nextId, selectedSubject, title, date, pdfUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Failed to save quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int getNextAvailableId(DataSnapshot dataSnapshot) {
        int maxId = 0;

        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
            String idStr = snapshot.getKey();
            try {
                int id = Integer.parseInt(idStr);
                if (id > maxId) {
                    maxId = id;
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "Skipping invalid ID: " + idStr);
            }
        }

        return maxId + 1;
    }

    private void addQuiz(int id, String subject, String title, String date, String pdfUrl) {
        String idStr = new DecimalFormat("00").format(id); // Format ID with leading zeroes
        Quiz quiz = new Quiz(subject, title, date, pdfUrl);
        quizzesRef.child(idStr).setValue(quiz).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Quiz added successfully with ID: " + idStr, Toast.LENGTH_SHORT).show();
                clearInputFields();
                loadQuizzes(); // Reload quizzes after saving
            } else {
                Log.e(TAG, "Failed to save quiz: " + task.getException().getMessage());
                Toast.makeText(getContext(), "Failed to save quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearInputFields() {
        editTextTitle.setText("");
        editTextDate.setText("");
        editTextPdfUrl.setText("");
        editingQuizId = null; // Clear the editing ID
    }

    private void loadQuizzes() {
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableLayoutQuizzes.removeAllViews(); // Clear existing rows
                // Add header row
                TableRow headerRow = new TableRow(getActivity());
                addTextViewToRow(headerRow, "Quiz ID");
                addTextViewToRow(headerRow, "Subject");
                addTextViewToRow(headerRow, "Title");
                addTextViewToRow(headerRow, "Date");
                addTextViewToRow(headerRow, "PDF URL");
                // No Update button in the header row
                tableLayoutQuizzes.addView(headerRow);

                // Add data rows
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.getKey();
                    Quiz quiz = snapshot.getValue(Quiz.class);

                    TableRow row = new TableRow(getActivity());
                    addTextViewToRow(row, id);
                    addTextViewToRow(row, quiz.subject);
                    addTextViewToRow(row, quiz.title);
                    addTextViewToRow(row, quiz.date);
                    addTextViewToRow(row, quiz.pdfUrl);

                    // Add Delete button
                    addButtonToRow(row, "Delete", v -> deleteQuiz(id));

                    // Click listener to set data in the EditText fields for updating
                    row.setOnClickListener(v -> {
                        editTextTitle.setText(quiz.title);
                        editTextDate.setText(quiz.date);
                        editTextPdfUrl.setText(quiz.pdfUrl);
                        editingQuizId = id; // Set ID of the quiz being edited
                        buttonSaveQuiz.setText("Update Quiz");
                    });

                    tableLayoutQuizzes.addView(row);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to load quizzes: " + databaseError.getMessage());
            }
        });
    }

    private void addTextViewToRow(TableRow row, String text) {
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        textView.setPadding(8, 8, 8, 8);
        textView.setText(text);
        row.addView(textView);
    }

    private void addButtonToRow(TableRow row, String text, View.OnClickListener listener) {
        Button button = new Button(getActivity());
        button.setText(text);
        button.setOnClickListener(listener);
        row.addView(button);
    }

    private void updateQuiz() {
        String newTitle = editTextTitle.getText().toString().trim();
        String newDate = editTextDate.getText().toString().trim();
        String newPdfUrl = editTextPdfUrl.getText().toString().trim();

        if (selectedSubject == null || newTitle.isEmpty() || newDate.isEmpty() || newPdfUrl.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields and provide a PDF URL", Toast.LENGTH_SHORT).show();
            return;
        }

        Quiz updatedQuiz = new Quiz(selectedSubject, newTitle, newDate, newPdfUrl);
        quizzesRef.child(editingQuizId).setValue(updatedQuiz).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(getContext(), "Quiz updated successfully", Toast.LENGTH_SHORT).show();
                clearInputFields();
                loadQuizzes(); // Reload quizzes after updating
                buttonSaveQuiz.setText("Save Quiz"); // Reset button text
            } else {
                Log.e(TAG, "Failed to update quiz: " + task.getException().getMessage());
                Toast.makeText(getContext(), "Failed to update quiz", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Inside your test_add class
    private Quiz deletedQuiz; // Store the deleted quiz data

    private void deleteQuiz(String id) {
        // Fetch the quiz data before deleting
        quizzesRef.child(id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    deletedQuiz = dataSnapshot.getValue(Quiz.class); // Save the quiz data

                    // Proceed with deletion
                    quizzesRef.child(id).removeValue().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Snackbar.make(getView(), "Quiz deleted successfully", Snackbar.LENGTH_LONG)
                                    .setAction("Undo", v -> {
                                        // Restore the deleted quiz
                                        if (deletedQuiz != null) {
                                            quizzesRef.child(id).setValue(deletedQuiz).addOnCompleteListener(restoreTask -> {
                                                if (restoreTask.isSuccessful()) {
                                                    Toast.makeText(getContext(), "Quiz restored successfully", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getContext(), "Failed to restore quiz", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    })
                                    .show();
                            loadQuizzes(); // Reload quizzes after deleting
                        } else {
                            Log.e(TAG, "Failed to delete quiz: " + task.getException().getMessage());
                            Toast.makeText(getContext(), "Failed to delete quiz", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to fetch quiz data: " + databaseError.getMessage());
            }
        });
    }

    private static class Quiz {
        public String subject;
        public String title;
        public String date;
        public String pdfUrl;

        public Quiz(String subject, String title, String date, String pdfUrl) {
            this.subject = subject;
            this.title = title;
            this.date = date;
            this.pdfUrl = pdfUrl;
        }

        public Quiz() { // Default constructor required for Firebase
        }
    }
}
