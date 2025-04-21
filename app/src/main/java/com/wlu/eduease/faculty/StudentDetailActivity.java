package com.wlu.eduease.faculty;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;
import com.wlu.eduease.R;

import java.util.Map;

public class StudentDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("EduEase");
            actionBar.setDisplayHomeAsUpEnabled(true);// Ensure you have this drawable
        }

        String studentJson = getIntent().getStringExtra("student");
        Gson gson = new Gson();
        MyResponse.Student student = gson.fromJson(studentJson, MyResponse.Student.class);

        TextView studentIdView = findViewById(R.id.studentId);
        TextView avgAssignmentMarksView = findViewById(R.id.avgAssignmentMarks);
        TextView avgQuizMarksView = findViewById(R.id.avgQuizMarks);
        LinearLayout assignmentsContainer = findViewById(R.id.assignmentsContainer);
        LinearLayout quizzesContainer = findViewById(R.id.quizzesContainer);

        studentIdView.setText("Student ID: " + student.student_id);
        avgAssignmentMarksView.setText("Average Assignment Marks: " + student.avg_assignment_marks);
        avgQuizMarksView.setText("Average Quiz Marks: " + student.avg_quiz_marks);

        if (student.assignments != null) {
            for (Map.Entry<String, MyResponse.Assignment> entry : student.assignments.entrySet()) {
                TextView assignmentView = new TextView(this);
                assignmentView.setText(entry.getKey() + ": Subject: " + entry.getValue().subject + ", Marks: " + entry.getValue().marks);
                assignmentsContainer.addView(assignmentView);
            }
        }

        if (student.quizzes != null) {
            for (Map.Entry<String, MyResponse.Quiz> entry : student.quizzes.entrySet()) {
                TextView quizView = new TextView(this);
                quizView.setText(entry.getKey() + ": Subject: " + entry.getValue().subject + ", Marks: " + entry.getValue().marks);
                quizzesContainer.addView(quizView);
            }
        }
    }   

    private static class MyResponse {
        private static class Student {
            String student_id;
            boolean at_risk;
            double avg_assignment_marks;
            double avg_quiz_marks;
            Map<String, Assignment> assignments;
            Map<String, Quiz> quizzes;
        }

        private static class Assignment {
            int marks;
            String subject;
        }

        private static class Quiz {
            int marks;
            String subject;
        }
    }
}
