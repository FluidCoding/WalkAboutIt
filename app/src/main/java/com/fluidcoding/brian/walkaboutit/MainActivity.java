package com.fluidcoding.brian.walkaboutit;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Login For Fire
 */
public class MainActivity extends AppCompatActivity {
    private Button btnSubmit;      // Login/Register
    SharedPreferences loginAuth;    // login token/name valid for 24 hours
    private CheckBox checkNewUser;  // Checked for New User
    private EditText txtRepeatPass; // Repeat Password Input
    private EditText txtPass;       // Password Input
    private EditText txtEmail;      // Email Input
    private String uName;   // Username for cache

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginAuth = getSharedPreferences("auth", 0);
        uName=loginAuth.getString("uName","nologin");

        initFireBase();
        initUI();

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
    }

    /**
     * Listener for new account checkbox - checked changed.
     */
    public class LoginOrRegisterChanged implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {
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
