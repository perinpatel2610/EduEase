# EduEase - Education Management Solution

EduEase is a comprehensive Android application aimed at improving academic management for students, teachers, and parents. It provides tools for managing schedules, assignments, grades, and performance analytics, with seamless integration of Firebase and machine learning for advanced features.

# Features

# General Features

Role-based access for Students, Teachers, and Parents.

User-friendly interface for navigation and usability.

Secure login and role-based access control.

Real-time updates using Firebase.

# Student Features

Access course materials and grades.

View and manage class schedules and assignment deadlines.

Monitor academic progress through personalized dashboards.

# Faculty Features

Upload course materials and assignments.

Manage attendance, grades, and performance tracking.

Identify at-risk students using ML-powered analytics.

# Parent Features

Monitor student grades and attendance.

Access parent-teacher meeting schedules.

View lists of at-risk students and performance metrics.

# How to Run:

Step 1. Start the Server:
	
	Run firebase_config.py 
		command: Python firebase_config.py
	
	Run ml_model.py
		command: Python ml_model.py
	
	Run app.py 
		command: Python app.py
		
Note: Firebase realtime database provides credentials to authenticate. So credentials.json is needed in the directory.

Step 2. Run Android Application.

# Login Credentials: 

	For Faculty Module: 
		Username: f1@edu.com
		Password: 121212

	For Student Module: 
		Username: s1@edu.com
		Password: 111111

	For Parent Module: 
		Username: p1@edu.com
		Password: 131313
