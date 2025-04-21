package com.wlu.eduease.faculty;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.wlu.eduease.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AtRiskStudent extends AppCompatActivity {

    private static final String TAG = "AtRiskStudent";
    private DatabaseReference mDatabase;
    private ListView listView;
    private OkHttpClient client;
    private Gson gson;
    private AtRiskStudentAdapter adapter;
    private List<MyResponse.Student> studentsList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_at_risk_student);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("EduEase");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        listView = findViewById(R.id.listView);
        studentsList = new ArrayList<>();
        adapter = new AtRiskStudentAdapter(this, studentsList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            MyResponse.Student student = studentsList.get(position);
            Intent intent = new Intent(AtRiskStudent.this, StudentDetailActivity.class);
            intent.putExtra("student", gson.toJson(student));
            startActivity(intent);
        });

        client = new OkHttpClient();
        gson = new Gson();
        mDatabase = FirebaseDatabase.getInstance().getReference("students");

        fetchDataFromFirebase();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchDataFromFirebase() {
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Map<String, Object>> studentsList = new ArrayList<>();
                for (DataSnapshot studentSnapshot : dataSnapshot.getChildren()) {
                    Map<String, Object> studentData = (Map<String, Object>) studentSnapshot.getValue();
                    studentData.put("student_id", studentSnapshot.getKey());
                    studentsList.add(studentData);
                }
                Map<String, List<Map<String, Object>>> requestData = new HashMap<>();
                requestData.put("students", studentsList);
                String jsonData = gson.toJson(requestData);
                sendDataToFlaskServer(jsonData);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG, "Failed to read data from Firebase", databaseError.toException());
            }
        });
    }

    private void sendDataToFlaskServer(String jsonData) {
        Log.d(TAG, "Sending JSON data to server: " + jsonData);  // Log the JSON data

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonData, JSON);
        Request request = new Request.Builder()
                .url("http://192.168.0.153:5000/predict")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Failed to connect to server", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e(TAG, "Server error: " + response);
                    return;
                }

                String responseData = response.body().string();
                Log.d(TAG, "Server response: " + responseData);

                // Parse the response
                final MyResponse parsedResponse = gson.fromJson(responseData, MyResponse.class);

                // Update UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        displayResults(parsedResponse);
                    }
                });
            }
        });
    }

    private void displayResults(MyResponse response) {
        studentsList.clear();

        // Check if the response or students list is null
        if (response == null || response.students == null) {
            Log.e(TAG, "Response or students list is null");
            return;
        }

        for (MyResponse.Student student : response.students) {
            // Check if student data is null
            if (student != null && student.at_risk) {
                studentsList.add(student);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private static class MyResponse {
        List<Student> students;

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

    private class AtRiskStudentAdapter extends ArrayAdapter<MyResponse.Student> {

        public AtRiskStudentAdapter(Context context, List<MyResponse.Student> students) {
            super(context, 0, students);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyResponse.Student student = getItem(position);

            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_at_risk_student, parent, false);
            }

            TextView studentIdView = convertView.findViewById(R.id.studentId);
            TextView avgAssignmentMarksView = convertView.findViewById(R.id.avgAssignmentMarks);
            TextView avgQuizMarksView = convertView.findViewById(R.id.avgQuizMarks);

            studentIdView.setText("Student ID: " + student.student_id);
            avgAssignmentMarksView.setText("Average Assignment Marks: " + student.avg_assignment_marks);
            avgQuizMarksView.setText("Average Quiz Marks: " + student.avg_quiz_marks);

            return convertView;
        }
    }
}
