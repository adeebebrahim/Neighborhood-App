package com.example.neighborhood;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

public class Register extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword, editTextConfirmPassword, editTextDateOfBirth, editTextName, editTextUsername, editTextMobileNo;
    Spinner spinnerGender;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;
    DatabaseReference databaseUsers;
    FirebaseDatabase database;
    Calendar myCalendar;
    HashMap<String, Boolean> registeredEmails;
    HashMap<String, Boolean> registeredMobileNumbers;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        databaseUsers = FirebaseDatabase.getInstance().getReference("Users");
        database = FirebaseDatabase.getInstance();
        registeredEmails = new HashMap<>();
        registeredMobileNumbers = new HashMap<>();

        editTextName = findViewById(R.id.name);
        editTextUsername = findViewById(R.id.username);
        editTextMobileNo = findViewById(R.id.mobileNo);
        spinnerGender = findViewById(R.id.gender);
        editTextDateOfBirth = findViewById(R.id.dateOfBirth);
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        editTextConfirmPassword = findViewById(R.id.confirmPassword);
        buttonReg = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);

        ArrayAdapter<CharSequence> genderAdapter = ArrayAdapter.createFromResource(this,
                R.array.genders, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(genderAdapter);

        myCalendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dateOfBirthPicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateDateOfBirthLabel();
            }
        };

        editTextDateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(Register.this, dateOfBirthPicker,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String name = editTextName.getText().toString().trim();
                String username = editTextUsername.getText().toString().trim();
                String mobileNo = editTextMobileNo.getText().toString().trim();
                String gender = spinnerGender.getSelectedItem().toString();
                String dateOfBirth = editTextDateOfBirth.getText().toString().trim();
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String confirmPassword = editTextConfirmPassword.getText().toString().trim();
                String bio = ""; // Get the bio from the user input
                String image = ""; // Get the bio from the user input
                int followerCount = 0; // Initialize the follower count
                int followingCount = 0; // Initialize the following count

                try {
                    validateField(name, "Please enter name");
                    validateField(mobileNo, "Please enter mobile number");
                    validateField(gender, "Please select gender");
                    validateField(dateOfBirth, "Please select date of birth");
                    validateField(email, "Please enter email");
                    validateField(password, "Please enter password");
                    validateField(confirmPassword, "Please confirm password");
                    validatePasswordMatch(password, confirmPassword);
                    validateDateOfBirthFormat(dateOfBirth);
                    validateEmailFormat(email);
                    validateMobileNumberFormat(mobileNo);
                    checkUniqueMobileNumber(mobileNo);

                } catch (Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(Register.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check unique email only if mobile number check is successful
                checkUniqueEmail(email);
            }
        });
    }

    private void validateField(String value, String errorMessage) throws Exception {
        if (TextUtils.isEmpty(value)) {
            throw new Exception(errorMessage);
        }
    }

    private void validatePasswordMatch(String password, String confirmPassword) throws Exception {
        if (!password.equals(confirmPassword)) {
            throw new Exception("Passwords do not match");
        }
    }

    private void validateDateOfBirthFormat(String dateOfBirth) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        sdf.setLenient(false);
        try {
            Date dob = sdf.parse(dateOfBirth);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dob);
            int year = calendar.get(Calendar.YEAR);
            if (year < 1900) {
                throw new Exception("Invalid date of birth");
            }
        } catch (ParseException e) {
            throw new Exception("Invalid date format");
        }
    }

    private void validateEmailFormat(String email) throws Exception {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw new Exception("Invalid email address");
        }
    }

    private void validateMobileNumberFormat(String mobileNumber) throws Exception {
        if (mobileNumber.length() != 10) {
            throw new Exception("Invalid mobile number");
        }
    }

    private void checkUniqueEmail(final String email) {
        if (registeredEmails.containsKey(email)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(Register.this, "Email already exists", Toast.LENGTH_SHORT).show();
        } else {
            createUserWithEmailAndPassword();
        }
    }

    private void checkUniqueMobileNumber(final String mobileNumber) {
        if (registeredMobileNumbers.containsKey(mobileNumber)) {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(Register.this, "Mobile number already exists", Toast.LENGTH_SHORT).show();
        } else {
            registeredMobileNumbers.put(mobileNumber, true);
        }
    }

    private void createUserWithEmailAndPassword() {
        progressBar.setVisibility(View.VISIBLE);
        String name = editTextName.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String mobileNo = editTextMobileNo.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();
        String dateOfBirth = editTextDateOfBirth.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String bio = ""; // Get the bio from the user input
        String image = ""; // Get the bio from the user input
        int followerCount = 0; // Initialize the follower count
        int followingCount = 0; // Initialize the following count

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                // Create a HashMap to store user information
                                HashMap<String, Object> userInfo = new HashMap<>();
                                userInfo.put("userId", userId); // Add the userId
                                userInfo.put("name", name);
                                userInfo.put("username", username);
                                userInfo.put("email", email);
                                userInfo.put("mobileNo", mobileNo);
                                userInfo.put("gender", gender);
                                userInfo.put("dateOfBirth", dateOfBirth);
                                userInfo.put("bio", bio);
                                userInfo.put("image", image);
                                userInfo.put("followerCount", followerCount);
                                userInfo.put("followingCount", followingCount);

                                DatabaseReference reference = database.getReference("Users");
                                // Save user info to the database
                                databaseUsers.child(userId).setValue(userInfo);

                                Toast.makeText(Register.this, "Registration Successful.",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                                finish();
                            }
                        } else {
                            Toast.makeText(Register.this, "Registration Failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void updateDateOfBirthLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        editTextDateOfBirth.setText(sdf.format(myCalendar.getTime()));
    }
}
