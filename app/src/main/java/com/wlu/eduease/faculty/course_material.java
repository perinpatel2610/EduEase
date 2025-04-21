package com.wlu.eduease.faculty;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.wlu.eduease.R;

import java.util.ArrayList;

public class course_material extends Fragment {

    private static final String TAG = "course_material";

    private EditText etCourseMaterial;
    private Button btnSaveCourseMaterial;
    private ListView lvCourseMaterials;

    private DatabaseReference facultyDataRef;
    private FirebaseUser currentUser;
    private ArrayList<String> courseMaterialList;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> courseMaterialKeys; // To store Firebase keys

    // Variables for editing mode
    private boolean isEditMode = false;
    private String editingKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_course_material, container, false);

        // Initialize Firebase references
        facultyDataRef = FirebaseDatabase.getInstance().getReference().child("faculty_data");

        etCourseMaterial = view.findViewById(R.id.etCourseMaterial);
        btnSaveCourseMaterial = view.findViewById(R.id.btnSaveCourseMaterial);
        lvCourseMaterials = view.findViewById(R.id.lvCourseMaterials);

        // Get current user from Firebase Authentication
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Initialize lists for course materials and keys
        courseMaterialList = new ArrayList<>();
        courseMaterialKeys = new ArrayList<>();
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, courseMaterialList);
        lvCourseMaterials.setAdapter(adapter);

        // Handle item click for editing
        lvCourseMaterials.setOnItemClickListener((parent, view1, position, id) -> {
            String courseMaterial = courseMaterialList.get(position);
            etCourseMaterial.setText(courseMaterial);
            isEditMode = true;
            editingKey = courseMaterialKeys.get(position); // Get the Firebase key for editing
            btnSaveCourseMaterial.setText("Update");
        });

        // Handle long press for deleting
        lvCourseMaterials.setOnItemLongClickListener((parent, view12, position, id) -> {
            String keyToDelete = courseMaterialKeys.get(position);
            showDeleteConfirmationDialog(keyToDelete);
            return true;
        });

        // Save button click listener
        btnSaveCourseMaterial.setOnClickListener(v -> {
            String courseMaterial = etCourseMaterial.getText().toString().trim();
            if (!courseMaterial.isEmpty()) {
                if (isEditMode) {
                    updateCourseMaterial(editingKey, courseMaterial); // Update existing material
                } else {
                    saveCourseMaterial(courseMaterial); // Save new material
                }
            } else {
                Toast.makeText(getActivity(), "Please enter course material", Toast.LENGTH_SHORT).show();
            }
        });

        // Load course materials from Firebase
        loadCourseMaterials();

        return view;
    }

    private void saveCourseMaterial(String courseMaterial) {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Push new course material to Firebase under "name"
        DatabaseReference userFacultyDataRef = facultyDataRef.child(uid).child("name").push();
        userFacultyDataRef.setValue(courseMaterial)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Course material saved", Toast.LENGTH_SHORT).show();
                    etCourseMaterial.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to save course material: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to save course material", e);
                });
    }

    private void updateCourseMaterial(String key, String courseMaterial) {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Update course material in Firebase under "name"
        DatabaseReference userFacultyDataRef = facultyDataRef.child(uid).child("name").child(key);
        userFacultyDataRef.setValue(courseMaterial)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Course material updated", Toast.LENGTH_SHORT).show();
                    etCourseMaterial.setText("");
                    isEditMode = false;
                    btnSaveCourseMaterial.setText("Save");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to update course material: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to update course material", e);
                });
    }

    private void deleteCourseMaterial(String key) {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Delete course material from Firebase under "name"
        DatabaseReference userFacultyDataRef = facultyDataRef.child(uid).child("name").child(key);
        userFacultyDataRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Course material deleted", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to delete course material: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Failed to delete course material", e);
                });
    }

    private void loadCourseMaterials() {
        if (currentUser == null) {
            Toast.makeText(getActivity(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        String uid = currentUser.getUid();

        // Reference to user's course materials in Firebase under "name"
        DatabaseReference userFacultyDataRef = facultyDataRef.child(uid).child("name");

        // Add listener to handle initial load and real-time updates
        userFacultyDataRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                String key = dataSnapshot.getKey();
                String courseMaterial = dataSnapshot.getValue(String.class);
                courseMaterialList.add(courseMaterial); // Add course material to list
                courseMaterialKeys.add(key); // Add Firebase key to list
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                String key = dataSnapshot.getKey();
                String courseMaterial = dataSnapshot.getValue(String.class);
                int index = courseMaterialKeys.indexOf(key);
                if (index != -1) {
                    courseMaterialList.set(index, courseMaterial); // Update course material in list
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String key = dataSnapshot.getKey();
                int index = courseMaterialKeys.indexOf(key);
                if (index != -1) {
                    courseMaterialList.remove(index); // Remove course material from list
                    courseMaterialKeys.remove(index); // Remove Firebase key from list
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                // Not used for this scenario
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showDeleteConfirmationDialog(String keyToDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this course material?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCourseMaterial(keyToDelete);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
