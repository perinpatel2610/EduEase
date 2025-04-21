import firebase_admin
from firebase_admin import credentials, db
import pandas as pd

# Initialize Firebase connection
cred = credentials.Certificate("credentials.json")
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://android-final-project-d655d-default-rtdb.firebaseio.com/'
})

# Fetch data from Firebase Realtime Database
ref_students = db.reference('students')

students = ref_students.get()

# Process and save data to CSV
data = []

if students:
    for student_id, student_data in students.items():
        student_assignments = student_data.get('assignments', {})
        student_quizzes = student_data.get('quizzes', {})

        for subject in ['Maths', 'English', 'Science']:
            assignment_marks = [v['marks'] for k, v in student_assignments.items() if v['subject'] == subject]
            quiz_marks = [v['marks'] for k, v in student_quizzes.items() if v['subject'] == subject]

            if assignment_marks or quiz_marks:
                avg_assignment_marks = sum(assignment_marks) / len(assignment_marks) if assignment_marks else 0
                avg_quiz_marks = sum(quiz_marks) / len(quiz_marks) if quiz_marks else 0

                data.append({
                    'student_id': student_id,
                    'subject': subject,
                    'avg_assignment_marks': avg_assignment_marks,
                    'avg_quiz_marks': avg_quiz_marks
                })

df = pd.DataFrame(data)

# Check if data is not empty before saving
if not df.empty:
    df.to_csv('student_data.csv', index=False)
    print("Data successfully written to student_data.csv")
else:
    print("No data available to write to student_data.csv")
