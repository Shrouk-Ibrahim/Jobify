# Jobify - Job Search and Application Tracker

## Project Overview
Jobify is an Android application designed to help users search for job listings, apply for positions, and track their application progress.
The app integrates Firebase Authentication, Firestore, a job search API from a freelancer platform, and the Unsplash API for fetching images by category.

## Graduation Project Information
- **Department:** Depi in Track Mobile Application Development
- **Team Members:**
   - Shrouk Ibrahim (Team Leader)
   - Rodayna Mohamed
- **Group Code:** ALX2_SWD4_G1 Mobile Application Developer

## Key Features
- **User Authentication:** Email/password authentication using Firebase.
- **Job Listings:** Integration with a freelancer platform API to fetch job listings.
- **Job Application Tracking:** Save, track, and manage job applications.
- **Image Fetching:** Unsplash API integration for category-based images.
- **Notifications:** Push notifications for application status updates and reminders.

## Technologies Used
- **Frontend:** Android (XML for UI design)
- **Backend:** Kotlin (Application Logic)
- **Database:** Firebase Firestore
- **Authentication:** Firebase Authentication (Email/Password)
- **APIs:** Freelancer Platform API (Job Listings), Unsplash API (Images)
- **Notifications:** Firebase Cloud Messaging (FCM)

## API Integrations
### Job Search API (Freelancer Platform)
- Fetch job listings via REST API.
- Display results dynamically in the app.
- Provide filters based on category, location, and type.

### Unsplash API (Image Fetching by Category)
- Fetch relevant images for job categories.
- Enhance UI with high-quality images.


## Push Notifications (Firebase Cloud Messaging)
- Users receive notifications for status updates on their job applications.
- Custom notifications for reminders and deadlines.

## Navigation Flow
1. **Splash Screen** → Login/Signup
2. **Login/Signup** → MainActivity
3. **Main Navigation (Bottom Navigation Bar):**
   - Home → Job Listings
   - Jobs Fragment → Job Saved / Job Tracking (Switch between them)
   - Job Details → When clicking on a job
   - Profile → User Information & Settings
   - Notifications → Application Updates & Reminders