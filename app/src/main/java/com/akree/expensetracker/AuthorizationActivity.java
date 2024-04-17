package com.akree.expensetracker;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.akree.expensetracker.serialization.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.NotNull;

import java.util.List;

public class AuthorizationActivity extends AppCompatActivity {

    private EditText username, email, password;
    private Button signUP;
    private TextView logIn;
    private TextInputLayout inputLayout;

    private boolean isSigningUp = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        password = findViewById(R.id.passwrd);
        signUP = findViewById(R.id.signupbtn);
        logIn = findViewById(R.id.logintxt);
        inputLayout = findViewById(R.id.inputLayout);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {

            startActivity(new Intent(AuthorizationActivity.this, NavActivity.class));
            finish();

        }

        signUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (email.getText().toString().isEmpty() || password.getText().toString().isEmpty()) {

                    if (isSigningUp && username.getText().toString().isEmpty()) {

                        Toast.makeText(AuthorizationActivity.this, "Invalid input", Toast.LENGTH_SHORT).show();
                        return;

                    }

                }

                if (isSigningUp) {

                    handleSignUp();

                } else {

                    handleLogIn();

                }

            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isSigningUp) {

                    isSigningUp = false;
                    username.setVisibility(View.GONE);
                    inputLayout.setVisibility(View.GONE);
                    signUP.setText("Log In");
                    logIn.setText("Don`t have an account? Sign Up");

                } else {

                    isSigningUp = true;
                    inputLayout.setVisibility(View.VISIBLE);
                    username.setVisibility(View.VISIBLE);
                    signUP.setText("Sign Up");
                    logIn.setText("Already have an account? Log in");

                }

            }
        });

    }

    private void handleSignUp() {

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    FirebaseDatabase.getInstance().getReference("user/" + FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(new User(username.getText().toString(), email.getText().toString(), "", 0, List.of("Food", "Clothes", "Other", "Transport")));

                    startActivity(new Intent(AuthorizationActivity.this, NavActivity.class));

                    Toast.makeText(AuthorizationActivity.this, "Signed Up successfully", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(AuthorizationActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }
            }
        });

    }

    private void handleLogIn() {

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {

                if (task.isSuccessful()) {

                    startActivity(new Intent(AuthorizationActivity.this, NavActivity.class));

                    Toast.makeText(AuthorizationActivity.this, "Logged In successfully", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(AuthorizationActivity.this, task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }

            }
        });

    }

}