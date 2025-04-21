from flask import Flask, request, jsonify
import joblib

app = Flask(__name__)

# Load the trained model
model = joblib.load('at_risk_model.pkl')

@app.route('/predict', methods=['POST'])
def predict():
    try:
        data = request.get_json()
        print("Received data:", data)

        if 'students' not in data:
            return jsonify({"error": "Missing students data"}), 400

        students = data['students']

        if not students:
            return jsonify({"error": "No students data available"}), 400

        student_predictions = []

        for student in students:
            assignments = student.get('assignments', {})
            quizzes = student.get('quizzes', {})

            # Initialize sums and counts
            total_assignment_marks = 0
            total_quiz_marks = 0
            assignment_count = 0
            quiz_count = 0

            # Calculate total assignment marks and count
            for assignment in assignments.values():
                total_assignment_marks += assignment.get('marks', 0)
                assignment_count += 1

            # Calculate total quiz marks and count
            for quiz in quizzes.values():
                total_quiz_marks += quiz.get('marks', 0)
                quiz_count += 1

            # Calculate averages
            avg_assignment_marks = total_assignment_marks / assignment_count if assignment_count > 0 else 0
            avg_quiz_marks = total_quiz_marks / quiz_count if quiz_count > 0 else 0

            # Perform prediction
            at_risk = bool(model.predict([[avg_assignment_marks, avg_quiz_marks]])[0])

            student_predictions.append({
                "student_id": student.get('student_id'),
                "at_risk": at_risk,
                "avg_assignment_marks": avg_assignment_marks,
                "avg_quiz_marks": avg_quiz_marks,
                "assignments": assignments,
                "quizzes": quizzes
            })

        # Return a JSON response with the prediction results
        return jsonify({"students": student_predictions})

    except Exception as e:
        print("Exception occurred:", str(e))
        return jsonify({"error": "Internal server error"}), 500

if __name__ == '__main__':
    app.run(host="192.168.0.153", port=5000, debug=True)
