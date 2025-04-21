package com.wlu.eduease.student;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class AssignmentScheduleFragment extends Fragment {

    private RecyclerView assignmentRecyclerView;
    private AssignmentAdapter assignmentAdapter;
    private List<Assignment> assignmentList = new ArrayList<>();
    private DatabaseReference assignmentsRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_assignment_schedule, container, false);
        assignmentRecyclerView = view.findViewById(R.id.assignmentRecyclerView);
        assignmentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        assignmentAdapter = new AssignmentAdapter(assignmentList);
        assignmentRecyclerView.setAdapter(assignmentAdapter);

        assignmentsRef = FirebaseDatabase.getInstance().getReference("assignments");

        loadAssignmentSchedule();

        return view;
    }

    private void loadAssignmentSchedule() {
        assignmentsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                assignmentList.clear(); // Clear previous data

                for (DataSnapshot assignmentSnapshot : dataSnapshot.getChildren()) {
                    String title = assignmentSnapshot.child("title").getValue(String.class);
                    String subject = assignmentSnapshot.child("subject").getValue(String.class);
                    String date = assignmentSnapshot.child("date").getValue(String.class);
                    String pdfUrl = assignmentSnapshot.child("pdfUrl").getValue(String.class);

                    Assignment assignment = new Assignment(title, subject, date, pdfUrl);
                    assignmentList.add(assignment);
                }

                assignmentAdapter.notifyDataSetChanged();
                Log.d("AssignmentScheduleFragment", "Total Assignment Schedule Items: " + assignmentList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("AssignmentScheduleFragment", "Failed to load assignment data: " + databaseError.getMessage());
            }
        });
    }

    private static class Assignment {
        String title, subject, date, pdfUrl;

        Assignment(String title, String subject, String date, String pdfUrl) {
            this.title = title;
            this.subject = subject;
            this.date = date;
            this.pdfUrl = pdfUrl;
        }
    }

    private static class AssignmentAdapter extends RecyclerView.Adapter<AssignmentAdapter.AssignmentViewHolder> {
        private List<Assignment> assignmentList;

        AssignmentAdapter(List<Assignment> assignmentList) {
            this.assignmentList = assignmentList;
        }

        @NonNull
        @Override
        public AssignmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assignment, parent, false);
            return new AssignmentViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AssignmentViewHolder holder, int position) {
            Assignment assignment = assignmentList.get(position);
            holder.assignmentTitle.setText(assignment.title);
            holder.assignmentSubject.setText(assignment.subject);
            holder.assignmentDate.setText(assignment.date);
            holder.assignmentPdfUrl.setText(assignment.pdfUrl);
            holder.assignmentPdfUrl.setOnClickListener(v -> {
                // Handle URL click
            });
        }

        @Override
        public int getItemCount() {
            return assignmentList.size();
        }

        static class AssignmentViewHolder extends RecyclerView.ViewHolder {
            TextView assignmentTitle, assignmentSubject, assignmentDate, assignmentPdfUrl;

            AssignmentViewHolder(@NonNull View itemView) {
                super(itemView);
                assignmentTitle = itemView.findViewById(R.id.assignmentTitle);
                assignmentSubject = itemView.findViewById(R.id.assignmentSubject);
                assignmentDate = itemView.findViewById(R.id.assignmentDate);
                assignmentPdfUrl = itemView.findViewById(R.id.assignmentPdfUrl);
            }
        }
    }
}
