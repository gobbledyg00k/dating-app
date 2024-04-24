package com.pattern.acquaintances.login.entry;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.pattern.acquaintances.MainActivity;
import com.pattern.acquaintances.R;
import com.pattern.acquaintances.model.DBManager;

public class EntryActivity extends AppCompatActivity {

    EditText editTextTextEmailAddress;
    TextView textView;
    EditText editTextTextPassword;
    private DBManager db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
        textView = findViewById(R.id.textView);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);

        db = new DBManager();

        db.setSignInOnComplete(new OnCompleteListener<AuthResult>() { //TODO catching exceptions in right way
            @SuppressLint("SetTextI18n")
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    textView.setText(R.string.successfully);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                } else {
                    textView.setText(R.string.login_failed);
                }
            }
        });
    }

    public void onClick(View v){
        if((editTextTextEmailAddress.length() > 0) && (editTextTextPassword.length() > 0)){ //TODO здесь надо проверку пароля
            db.signIn(editTextTextEmailAddress.getText().toString(),
                    editTextTextPassword.getText().toString());
        } else{
            textView.setText(R.string.enter_the_data);
        }

        System.out.println("Button clicked");

    }
}