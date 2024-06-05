package com.pattern.acquaintances.ui.login.registration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.pattern.acquaintances.R;
import com.pattern.acquaintances.model.Account;
import com.pattern.acquaintances.model.DBManager;
import com.pattern.acquaintances.model.AuthManager;

import java.util.function.Function;


public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextTextEmailAddress;
    private TextView textView;
    private EditText editTextTextPassword;
    private EditText editTextTextPassword2;
    private DBManager db;
    private AuthManager auth;
    private String email;
    private String pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextTextPassword2 = findViewById(R.id.editTextTextPassword2);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        textView = findViewById(R.id.textView);
        editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
        db = new DBManager(new Function<Account, Void>() {
            @Override
            public Void apply(Account account) {
                return null;
            }
        });
        auth = new AuthManager();

        auth.setSignUpOnComplete(new OnCompleteListener<AuthResult>() { //TODO catching exceptions in right way
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    textView.setText(R.string.acc_create_succ);
                    Intent intent = new Intent(getApplicationContext(), PersonalDataActivity.class);
                    intent.putExtra("email", email);
                    intent.putExtra("password", pass);
                    startActivity(intent);
                } else {
                    textView.setText(R.string.register_failed);
                }
            }
        });
    }

    public void onClick(View v){    //TODO здесь надо проверку пароля

        pass = editTextTextPassword.getText().toString();
        String pass_confirm = editTextTextPassword2.getText().toString();
        email = editTextTextEmailAddress.getText().toString();
        if(pass.equals(pass_confirm)){
            auth.signUp(email, pass);
        } else{
            textView.setText(R.string.pass_dont_match);
        }
        System.out.println("Button clicked");
    }
}