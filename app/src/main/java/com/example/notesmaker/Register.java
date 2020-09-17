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
    String verificationID;
    PhoneAuthProvider.ForceResendingToken token;
    EditText rPhoneNo, rCodeEnter, rEmailId, rPassword;
    FirebaseAuth fAuth;
    Button Next;
    ProgressBar progressBar;
    TextView rState;
    CountryCodePicker codePicker;
    Boolean VerificationInProgress = false;
    private static final String TAG = "Register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //   rPhoneNo= findViewById(R.id.edit_text_Pno)  ;
        setContentView(R.layout.activity_register);
        fAuth = FirebaseAuth.getInstance();
        codePicker = findViewById(R.id.ccp);
        rPhoneNo = findViewById(R.id.edit_text_phoneNo);
        rCodeEnter = findViewById(R.id.edit_text_codeEnter);
        Next = findViewById(R.id.button_next);
        progressBar = findViewById(R.id.progressBar);
        rState = findViewById(R.id.state);
        rEmailId= findViewById(R.id.edit_text_emailId);
        rPassword = findViewById(R.id.edit_text_password) ;

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = rEmailId.getText().toString().trim();
                String password = rPassword.getText().toString().trim();

                if(TextUtils.isEmpty(email)){
                    rEmailId.setError("Email Id is mandatory");
                    return;
                }
                if(TextUtils.isEmpty(password)){
                    rPassword.setError("Password is mandatory");
                    return;
                }
                if(password.length() < 6) {
                    rPassword.setError("Password should be longer than 6 characters");
                    return;
                }
                if(rPhoneNo.getText().toString().isEmpty() && rPhoneNo.getText().toString().length() != 10){
                    rPhoneNo.setError("Phone no is not valid");
                    return;
                }
                if (!VerificationInProgress) {
                    String phoneNum = "+" + codePicker.getSelectedCountryCode() + rPhoneNo.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    rState.setText("Sending OTP...");
                    rState.setVisibility(View.VISIBLE);
                    requestOTP(phoneNum);
                }
                else{
                    String userOTP= rCodeEnter.getText().toString();
                    if(!userOTP.isEmpty() && userOTP.length()==6){
                        PhoneAuthCredential credential =PhoneAuthProvider.getCredential(verificationID,userOTP);
                        verifyAuth(credential);
                    }
                    else {
                        rCodeEnter.setError("Valid OTP is required");
                    }

                }
            }
        });
    }
    private void verifyAuth(PhoneAuthCredential credential) {
        fAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful() ){
                    Toast.makeText(Register.this, "Authentication is successful", Toast.LENGTH_SHORT ).show();
                    String email = rEmailId.getText().toString().trim();
                    String password = rPassword.getText().toString().trim();

                    fAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = fAuth.getCurrentUser();
                                user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                                      @Override
                                                                                      public void onSuccess(Void aVoid) {
                                                                                          Toast.makeText(Register.this, "Verification Message - Email has been sent", Toast.LENGTH_LONG).show();
                                                                                      }
                                                                                  }
                                ).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG , "On Failure: Message not sent"+e.getMessage() );
                                        Toast.makeText(Register.this, "Verification Message not sent", Toast.LENGTH_LONG).show();
                                    }
                                });
                                Toast.makeText(Register.this, "User Created", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            }
                            else{
                                Toast.makeText(Register.this, "Error!"+ task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(Register.this, "Authentication failed", Toast.LENGTH_SHORT ).show();
                }
            }
        });
    }
    private void requestOTP(String phone) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(phone, 60L, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                progressBar.setVisibility(View.GONE);
                rCodeEnter.setVisibility(View.VISIBLE);
                rState.setVisibility(View.GONE);
                verificationID = s;
                token = forceResendingToken;
                Next.setText("Verify");
                VerificationInProgress = true;
            }
            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
            }
            @Override
            public void onVerificationFailed(FirebaseException e) {
                Toast.makeText(Register.this, "Can't create Account" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }
}
