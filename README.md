
# **Project Directory Structure**

```
src/
└── main/
    └── java/
        └── com/
            └── yourapp/
                ├── repository/
                │   ├── AuthRepository.kt
                │
                ├── viewmodel/
                │   ├── AuthViewModel.kt
                │   
                ├── ui/
                │   ├── activities/
                │   │   ├── AuthActivity.kt
                │   │
                │   ├── fragments/
                │       ├── LoginFragment.kt
                │       ├── SignupFragment.kt
                │   
                ├── navigation/
                │   ├── auth_navigation.xml
                │
                ├── res/
                    ├── layout/
                    │   ├── activity_auth.xml
                    │   ├── fragment_login.xml
                    │   ├── fragment_signup.xml
                    │
                    ├── values/
                        ├── colors.xml
```


# **Sequence of Operations**

## **Login Flow**
1. **User Input:**
   - User enters email and password in `LoginFragment`.
   
2. **ViewModel Invocation:**
   - `LoginFragment` calls `AuthViewModel.login()`.

3. **Repository Communication:**
   - `AuthViewModel` forwards the login request to `AuthRepository.login()`.

4. **Authentication:**
   - `AuthRepository` queries Firestore to verify the password.
   - Firebase Authentication handles user sign-in.

5. **State Update:**
   - `AuthRepository` updates `_authState`.

6. **UI Update:**
   - `LoginFragment` observes the result and updates the UI accordingly.

---

## **Signup Flow**
1. **User Input:**
   - User enters name, email, and password in `SignupFragment`.

2. **ViewModel Invocation:**
   - `SignupFragment` calls `AuthViewModel.signup()`.

3. **Repository Communication:**
   - `AuthViewModel` forwards the signup request to `AuthRepository.signup()`.

4. **Data Storage and Authentication:**
   - `AuthRepository` hashes the password and stores user data in Firestore.
   - Firebase Authentication creates the user account.

5. **State Update:**
   - `AuthRepository` updates `_authState`.

6. **UI Update:**
   - `SignupFragment` observes the result and updates the UI accordingly.

