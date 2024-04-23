package com.pattern.acquaintances.login.entry;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.pattern.acquaintances.MainActivity;
import com.pattern.acquaintances.R;

public class EntryActivity extends AppCompatActivity {

    EditText editTextTextEmailAddress;
    TextView textView;
    EditText editTextTextPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        editTextTextEmailAddress = findViewById(R.id.editTextTextEmailAddress);
        textView = findViewById(R.id.textView);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
    }

    public void onClick(View v){
        if((editTextTextEmailAddress.length() > 0) && (editTextTextPassword.length() > 0)){ //TODO здесь надо проверку пароля
            textView.setText(R.string.successfully);
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else{
            textView.setText(R.string.enter_the_data);
        }

        System.out.println("Button clicked");

    }
}