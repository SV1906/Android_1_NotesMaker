package com.example.notesmaker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.rilixtech.widget.countrycodepicker.CountryCodePicker;
import java.util.concurrent.TimeUnit;

public class Register extends AppCompatActivity {
    EditText rCodeEnter, rEmailId, rPassword, rName;
    FirebaseAuth fAuth;
    Button Next;
    private static final String TAG = "Register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        fAuth = FirebaseAuth.getInstance();
        Next = findViewById(R.id.button_next);
        rEmailId = findViewById(R.id.edit_text_emailId);
        rPassword = findViewById(R.id.edit_text_password);
        rName = findViewById(R.id.edit_text_name);

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = rEmailId.getText().toString().trim();
                String password = rPassword.getText().toString().trim();
                String name = rName.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    rName.setError("Name is mandatory");
                    return;
                }
                if (TextUtils.isEmpty(email)) {
                    rEmailId.setError("Email Id is mandatory");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    rPassword.setError("Password is mandatory");
                    return;
                }
                if (password.length() < 6) {
                    rPassword.setError("Password should be longer than 6 characters");
                    return;
                }



                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = fAuth.getCurrentUser();
                            user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                  @Override
                                                                                  public void onSuccess(Void aVoid) {
                                                                                      Toast.makeText(Register.this, "Verification Message has been sent", Toast.LENGTH_LONG).show();
                                                                                  }
                                                                              }
                            ).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG, "On Failure: Message not sent" + e.getMessage());
                                    Toast.makeText(Register.this, "Verification Message not sent", Toast.LENGTH_LONG).show();
                                }
                            });
                            Toast.makeText(Register.this, "User Created", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        } else {
                            Toast.makeText(Register.this, "Error!" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });
    }
}










