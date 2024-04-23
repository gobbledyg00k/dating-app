package com.pattern.acquaintances.login;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.pattern.acquaintances.R;
import com.pattern.acquaintances.login.entry.EntryActivity;
import com.pattern.acquaintances.login.registration.RegistrationActivity;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void createAcc(View v){
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void useAcc(View v){
        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);
    }
}