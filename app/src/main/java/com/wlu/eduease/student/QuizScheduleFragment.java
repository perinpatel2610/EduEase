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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wlu.eduease.R;

import java.util.ArrayList;
import java.util.List;

public class QuizScheduleFragment extends Fragment {

    private RecyclerView quizRecyclerView;
    private QuizAdapter quizAdapter;
    private List<Quiz> quizList = new ArrayList<>();
    private DatabaseReference quizzesRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_quiz_schedule, container, false);
        quizRecyclerView = view.findViewById(R.id.quizRecyclerView);
        quizRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        quizAdapter = new QuizAdapter(quizList);
        quizRecyclerView.setAdapter(quizAdapter);

        quizzesRef = FirebaseDatabase.getInstance().getReference("quizzes");

        // Get the logged-in user's details
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            loadQuizSchedule();
        } else {
            Log.e("QuizScheduleFragment", "No user is logged in");
        }
        return view;
    }

    private void loadQuizSchedule() {
        quizzesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                quizList.clear(); // Clear previous data

                for (DataSnapshot quizSnapshot : dataSnapshot.getChildren()) {
                    String title = quizSnapshot.child("title").getValue(String.class);
                    String subject = quizSnapshot.child("subject").getValue(String.class);
                    String date = quizSnapshot.child("date").getValue(String.class);
                    String pdfUrl = quizSnapshot.child("pdfUrl").getValue(String.class);

                    Quiz quiz = new Quiz(title, subject, date, pdfUrl);
                    quizList.add(quiz);
                }

                quizAdapter.notifyDataSetChanged();
                Log.d("QuizScheduleFragment", "Total Quiz Schedule Items: " + quizList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("QuizScheduleFragment", "Failed to load quiz data: " + databaseError.getMessage());
            }
        });
    }

    private static class Quiz {
        String title, subject, date, pdfUrl;

        Quiz(String title, String subject, String date, String pdfUrl) {
            this.title = title;
            this.subject = subject;
            this.date = date;
            this.pdfUrl = pdfUrl;
        }
    }

    private static class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuizViewHolder> {
        private List<Quiz> quizList;

        QuizAdapter(List<Quiz> quizList) {
            this.quizList = quizList;
        }

        @NonNull
        @Override
        public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
            return new QuizViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
            Quiz quiz = quizList.get(position);
            holder.quizTitle.setText(quiz.title);
            holder.quizSubject.setText(quiz.subject);
            holder.quizDate.setText(quiz.date);
            holder.quizPdfUrl.setText(quiz.pdfUrl);
            holder.quizPdfUrl.setOnClickListener(v -> {
                // Handle URL click
            });
        }

        @Override
        public int getItemCount() {
            return quizList.size();
        }

        static class QuizViewHolder extends RecyclerView.ViewHolder {
            TextView quizTitle, quizSubject, quizDate, quizPdfUrl;

            QuizViewHolder(@NonNull View itemView) {
                super(itemView);
                quizTitle = itemView.findViewById(R.id.quizTitle);
                quizSubject = itemView.findViewById(R.id.quizSubject);
                quizDate = itemView.findViewById(R.id.quizDate);
                quizPdfUrl = itemView.findViewById(R.id.quizPdfUrl);
            }
        }
    }
}
