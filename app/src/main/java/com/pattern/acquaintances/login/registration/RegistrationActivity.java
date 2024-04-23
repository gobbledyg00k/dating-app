package com.pattern.acquaintances.login.registration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pattern.acquaintances.R;

public class RegistrationActivity extends AppCompatActivity {

    private EditText editTextTextEmailAddress;
    private TextView textView;
    private EditText editTextTextPassword;
    private EditText editTextTextPassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextTextPassword2 = findViewById(R.id.editTextTextPassword2);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        textView = findViewById(R.id.textView);
        editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
    }

    public void onClick(View v){                         //TODO здесь надо проверку пароля
        String x = editTextTextPassword.getText().toString();
        String y = editTextTextPassword2.getText().toString();
        if(x.equals(y)){
            textView.setText(R.string.acc_create_succ);
            Intent intent = new Intent(this, PersonalDataActivity.class);
            startActivity(intent);
        } else{
            textView.setText(R.string.pass_dont_match);
        }
        System.out.println("Button clicked");
    }
}