package com.fluidcoding.brian.walkaboutit;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.instantapps.PackageManagerWrapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

/**
 * Login For Fire
 */
public class MainActivity extends AppCompatActivity {
    final String TAG = "MainActivity";

    // UI Elements
    private Button btnSubmit;       // Login/Register
    private CheckBox checkNewUser;  // Checked for New User
    private EditText txtRepeatPass; // Repeat Password Input
    private EditText txtPass;       // Password Input
    private EditText txtEmail;      // Email Input

    // Firebase
    final String FIREBASE_URL = "https://walkaboutit-d5df6.firebaseio.com/";
    private FirebaseAuth mAuth;
    private DatabaseReference userFBRef;     // Firenase reference to user accounts
    private FirebaseAuth.AuthStateListener mAuthListener;

    private int formMASK;
    private Intent mapAct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hook all UI Components
        initUI();

        mapAct = new Intent(this, MapActivity.class);

        // Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("SignIn", "onAuthStateChanged:signed_in:" + user.getUid());
                    startActivity(mapAct);
                } else {
                    // User is signed out
                    Log.d("SignOut", "onAuthStateChanged:signed_out");
                }
            }
        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /**
     * Attempt to log user in or register
     * Show relevant Snackbar on error
     * @param v the view in static context that triggered this call
     */
    public void loginOrRegister(final View v){
        String email = txtEmail.getText().toString();
        String pass = txtPass.getText().toString();
        String pass2 = txtRepeatPass.getText().toString();
        final boolean isNewAccount = checkNewUser.isChecked();

        if(!validateForm(email, pass, pass2, isNewAccount)) return;

        // Create a New Account
        if(isNewAccount){
            mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete( Task<AuthResult> task) {
                        Log.d("Status", "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.d("Err","Account Creation Error");
                            Snackbar.make(v, "Account Creation Error", Snackbar.LENGTH_INDEFINITE).show();
                        }else{
                            Snackbar.make(v, "Account Creation Successful, Please Log In..", Snackbar.LENGTH_INDEFINITE).show();
                        }
                    }
                });
        }else{// Login
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete( Task<AuthResult> task) {
                            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());

                            // If sign in fails, display a message to the user. If sign in succeeds
                            // the auth state listener will be notified and logic to handle the
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "signInWithEmail", task.getException());
                                Toast.makeText(MainActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }else{
                                startActivity(mapAct);
                            }
                        }
                    });
        }
    }

    /**
     *
     * @param email Email Field Entry
     * @param pass  Password text
     * @param pass2 Password Repeat text(if needed)
     * @param newAcc checked state of New Account
     * @return true if form is ready to submit
     */
    public boolean validateForm(String email, String pass, String pass2, boolean newAcc){
        formMASK = 0;
        if(!email.matches(".+@.+\\..+"))    formMASK = formMASK | 1;
        if(pass.length()<6)                 formMASK = formMASK | 2;
        if(newAcc & (!pass2.equals(pass)))  formMASK = formMASK | 4;
        if(formMASK == 0)                   return true;
        else                                return false;
    }

    /**
     * Just hooking up ui elements here...that is all
     * TODO: Hook onChange for all elements to toggle submit button if valid.
     */
    public void initUI(){
        // Hook components from the view
        checkNewUser = (CheckBox)findViewById(R.id.checkNewUser);
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPass = (EditText)findViewById(R.id.txtPassword);
        txtRepeatPass = (EditText)findViewById(R.id.txtPasswordRepeat);
        btnSubmit = (Button)findViewById(R.id.btnLogin);

        // Event Hooks
        checkNewUser.setOnCheckedChangeListener(new LoginOrRegisterChanged());
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginOrRegister(v);
            }
        });
    }

    /**
     * Listener for new account checkbox - checked changed.
     */
    public class LoginOrRegisterChanged implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(!isChecked) {
                txtRepeatPass.setVisibility(View.GONE);
                btnSubmit.setText(R.string.app_login);
            }
            else{
                txtRepeatPass.setVisibility(View.VISIBLE);
                btnSubmit.setText(R.string.app_register);
            }
        }
    }

}
