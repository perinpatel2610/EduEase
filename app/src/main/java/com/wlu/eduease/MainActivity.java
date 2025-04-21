package com.wlu.eduease;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.faculty.FacultyHome;
import com.wlu.eduease.parent.ParentHome;
import com.wlu.eduease.student.StudentHome;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private FirebaseAuth auth;
    private TextView textView;
    private FirebaseUser user;
    private DatabaseReference usersRef;
    private String userRole;
    private NavigationView navigationView;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth and User
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();

        if (user == null) {
            navigateToLogin();
            return;
        }

        // Initialize Firebase Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users").child(user.getUid());
        databaseReference = database.getReference();

        // Set up Navigation Drawer and Toolbar
        setupNavigationDrawer();

        // Load user data and set up the UI
        loadUserData();

        // Start data migration
        startMigration();
    }

    private void setupNavigationDrawer() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_help) {
            showHelpDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showHelpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Help");

        // Set the dialog content
        String message = "Author: Rudri Jardosh\n" +
                "              Perin Patel\n" +
                "              Fenil Patel\n" +
                "              Maitri Patel\n" +
                "Version: 1.0\n\n" +
                "Instructions:\n" +
                "\nFaculty:\n" +
                "1. Uploading Materials, Tests, Assignments, Quizs.\n" +
                "2. Posting Grades of Tests, Assignments, Quizs.\n" +
                "3. Viewing Student Performance.\n\n" +
                "Students:\n" +
                "1. Viewing Materials, Tests, Assignments, Quizs.\n" +
                "2. Checking Grades of Tests, Assignments, Quizs.\n" +
                "3. Participating in Quizzes.\n\n" +
                "Parents:\n" +
                "1. Viewing Student Progress.\n" +
                "2. Scheduling PTMs.\n" +
                "3. Receiving Notifications.\n\n";
        builder.setMessage(message);

        // Add an OK button
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private void loadUserData() {
        navigationView = findViewById(R.id.nav_view);
        textView = navigationView.getHeaderView(0).findViewById(R.id.user_details);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String fullName = dataSnapshot.child("fullname").getValue(String.class);
                    userRole = dataSnapshot.child("role").getValue(String.class);
                    String welcomeMessage = getString(R.string.welcome_message, fullName);
                    textView.setText(welcomeMessage);

                    updateMenuVisibility(userRole);
                    loadDefaultFragment();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadDefaultFragment() {
        if (userRole == null) return;

        if ("student".equalsIgnoreCase(userRole)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentHome()).commit();
            navigationView.setCheckedItem(R.id.nav_student_home);
        } else if ("parent".equalsIgnoreCase(userRole)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ParentHome()).commit();
            navigationView.setCheckedItem(R.id.nav_parent_home);
        } else if ("faculty".equalsIgnoreCase(userRole)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FacultyHome()).commit();
            navigationView.setCheckedItem(R.id.nav_faculty_home);
        }
    }

    private void updateMenuVisibility(String role) {
        Menu menu = navigationView.getMenu();
        MenuItem studentHome = menu.findItem(R.id.nav_student_home);
        MenuItem facultyHome = menu.findItem(R.id.nav_faculty_home);
        MenuItem parentHome = menu.findItem(R.id.nav_parent_home);

        studentHome.setVisible("student".equalsIgnoreCase(role));
        facultyHome.setVisible("faculty".equalsIgnoreCase(role));
        parentHome.setVisible("parent".equalsIgnoreCase(role));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_student_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new StudentHome()).commit();
        } else if (itemId == R.id.nav_faculty_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FacultyHome()).commit();
        } else if (itemId == R.id.nav_parent_home) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ParentHome()).commit();
        } else if (itemId == R.id.nav_about) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new AboutFragment()).commit();
        } else if (itemId == R.id.nav_logout) {
            handleLogout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLogout() {
        Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, user_login.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    private void startMigration() {
        migrateData();
    }

    private void migrateData() {
        DatabaseReference assignmentsRef = databaseReference.child("assignments");
        DatabaseReference quizzesRef = databaseReference.child("quizzes");

        assignmentsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot assignmentSnapshot : dataSnapshot.getChildren()) {
                    String assignmentId = assignmentSnapshot.getKey();
                    String date = assignmentSnapshot.child("date").getValue(String.class);
                    String pdfUrl = assignmentSnapshot.child("pdfUrl").getValue(String.class);
                    String subject = assignmentSnapshot.child("subject").getValue(String.class);
                    String title = assignmentSnapshot.child("title").getValue(String.class);

                    DataSnapshot marksSnapshot = assignmentSnapshot.child("marks");
                    for (DataSnapshot studentSnapshot : marksSnapshot.getChildren()) {
                        String studentId = studentSnapshot.getKey();
                        int marks = studentSnapshot.getValue(Integer.class);
                        saveAssignment(studentId, assignmentId, date, marks, pdfUrl, subject, title);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error without showing a Toast
            }
        });

        quizzesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String quizId = quizSnapshot.getKey();
                    String date = quizSnapshot.child("date").getValue(String.class);
                    String pdfUrl = quizSnapshot.child("pdfUrl").getValue(String.class);
                    String subject = quizSnapshot.child("subject").getValue(String.class);
                    String title = quizSnapshot.child("title").getValue(String.class);

                    DataSnapshot marksSnapshot = quizSnapshot.child("marks");
                    for (DataSnapshot studentSnapshot : marksSnapshot.getChildren()) {
                        String studentId = studentSnapshot.getKey();
                        int marks = studentSnapshot.getValue(Integer.class);
                        saveQuiz(studentId, quizId, date, marks, pdfUrl, subject, title);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error without showing a Toast
            }
        });
    }

    private void saveAssignment(String studentId, String assignmentId, String date, int marks, String pdfUrl, String subject, String title) {
        Map<String, Object> assignmentData = new HashMap<>();
        assignmentData.put("marks", marks);
        assignmentData.put("subject", subject);

        databaseReference.child("students").child(studentId).child("assignments").child(assignmentId).setValue(assignmentData)
                .addOnSuccessListener(aVoid -> {
                    // Success handling without Toast
                })
                .addOnFailureListener(e -> {
                    // Failure handling without Toast
                });
    }

    private void saveQuiz(String studentId, String quizId, String date, int marks, String pdfUrl, String subject, String title) {
        Map<String, Object> quizData = new HashMap<>();
        quizData.put("marks", marks);
        quizData.put("subject", subject);

        databaseReference.child("students").child(studentId).child("quizzes").child(quizId).setValue(quizData)
                .addOnSuccessListener(aVoid -> {
                    // Success handling without Toast
                })
                .addOnFailureListener(e -> {
                    // Failure handling without Toast
                });
    }

}
