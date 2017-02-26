package com.fluidcoding.brian.walkaboutit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Login For Fire
 */
public class MainActivity extends AppCompatActivity {
    private Button btnSubmit;       // Login/Register
    SharedPreferences loginAuth;    // login token/name valid for 24 hours
    private CheckBox checkNewUser;  // Checked for New User
    private EditText txtRepeatPass; // Repeat Password Input
    private EditText txtPass;       // Password Input
    private EditText txtEmail;      // Email Input
    private String uName;           // Username for cache
    private FirebaseAuth mAuth;
    private DatabaseReference userFBRef;     // Firenase reference to user accounts
    private FirebaseAuth.AuthStateListener mAuthListener;

    Intent mapAct;
    final String TAG = "MainActivity";
    final String FIREBASE_URL = "https://walkaboutit-d5df6.firebaseio.com/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Saved Data
        loginAuth = getSharedPreferences("auth", 0);
        uName=loginAuth.getString("uName","nologin");




        // Intents
        mapAct = new Intent(this, MapActivity.class);

        // Firebase setup
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
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

        initFireBase();
        initUI();
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
     * Show snackbar on error
     * @param v the view in static context that triggered this call
     */
    public void loginOrRegister(final View v){
        if(!validForm()) return;
        // Create a New Account
        if(checkNewUser.isChecked()){
            mAuth.createUserWithEmailAndPassword(txtEmail.getText().toString(), txtPass.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
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
        }else{
            mAuth.signInWithEmailAndPassword(txtEmail.getText().toString(), txtPass.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
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

    /*
        TODO: Basic form validation
     */
    public boolean validForm(){
        return true;
    }
    public void initUI(){

        // Hook Components
        checkNewUser = (CheckBox)findViewById(R.id.checkNewUser);
        checkNewUser.setOnCheckedChangeListener(new LoginOrRegisterChanged());
        txtEmail = (EditText)findViewById(R.id.txtEmail);
        txtPass = (EditText)findViewById(R.id.txtPassword);
        txtRepeatPass = (EditText)findViewById(R.id.txtPasswordRepeat);
        btnSubmit = (Button)findViewById(R.id.btnLogin);
        // Remember username by default
        if(!uName.equals("nologin"))    txtEmail.setText(uName);

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

    public void initFireBase(){
    }

}
