package com.wlu.eduease;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

public class user_register extends AppCompatActivity {

    // Firebase database reference
    DatabaseReference databaseReference;
    DatabaseReference studentTuplesRef;

    private RadioGroup radioGroup;
    private RadioButton radioButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_register);

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");
        studentTuplesRef = FirebaseDatabase.getInstance().getReference().child("studentTuples");

        final EditText fullname = findViewById(R.id.fullname);
        final EditText phone = findViewById(R.id.phone);
        final EditText email = findViewById(R.id.email);
        final EditText password = findViewById(R.id.password);
        final EditText conPassword = findViewById(R.id.conPassword);

        final Button btn_register = findViewById(R.id.btn_register);
        final TextView loginNow = findViewById(R.id.loginNow);

        radioGroup = findViewById(R.id.radioGroup);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String fullnameTxt = fullname.getText().toString();
                final String phoneTxt = phone.getText().toString();
                final String emailTxt = email.getText().toString();
                final String passwordTxt = password.getText().toString();
                final String conPasswordTxt = conPassword.getText().toString();
                final int selectedId = radioGroup.getCheckedRadioButtonId();
                radioButton = findViewById(selectedId);
                final String selectedRole = radioButton.getText().toString();

                if (fullnameTxt.isEmpty() || phoneTxt.isEmpty() || emailTxt.isEmpty() || passwordTxt.isEmpty()) {
                    Toast.makeText(user_register.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                } else if (!passwordTxt.equals(conPasswordTxt)) {
                    Toast.makeText(user_register.this, "Password and Confirm Password do not match", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(emailTxt, passwordTxt)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                        if (firebaseUser != null) {
                                            String userId = firebaseUser.getUid();

                                            // Save additional user data to Firebase Realtime Database
                                            DatabaseReference userRef = databaseReference.child(userId);
                                            userRef.child("fullname").setValue(fullnameTxt);
                                            userRef.child("email").setValue(emailTxt);
                                            userRef.child("phone").setValue(phoneTxt);
                                            userRef.child("role").setValue(selectedRole);
                                            userRef.child("password").setValue(passwordTxt);

                                            if ("Student".equals(selectedRole)) {
                                                // Add student to the tuples node in Firebase
                                                saveStudentTupleToFirebase(fullnameTxt);
                                            }

                                            Toast.makeText(user_register.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent = new Intent(getApplicationContext(), user_login.class);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(user_register.this, "Failed to authenticate user", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(user_register.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });

        loginNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), user_login.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void saveStudentTupleToFirebase(final String fullname) {
        studentTuplesRef.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                long studentCount = mutableData.getChildrenCount(); // Get current count
                String studentId = String.format("%02d", studentCount + 1); // Generate new ID

                // Create the new student entry
                MutableData studentData = mutableData.child(studentId);
                studentData.child("Name").setValue(fullname);
                studentData.child("StudentId").setValue(studentId);

                return Transaction.success(mutableData); // Transaction success
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean committed, @Nullable DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    Log.e("StudentTupleUpdate", "Error adding student: " + databaseError.getMessage());
                } else {
                    Log.d("StudentTupleUpdate", "Successfully added student: " + fullname);
                }
            }
        });
    }
}
