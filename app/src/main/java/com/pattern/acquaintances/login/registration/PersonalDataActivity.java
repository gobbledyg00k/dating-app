package com.pattern.acquaintances.login.registration;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.pattern.acquaintances.MainActivity;
import com.pattern.acquaintances.R;

import com.pattern.acquaintances.model.Account;
import com.pattern.acquaintances.model.DBManager;
import com.pattern.acquaintances.model.DayOfBirth;

import java.util.Calendar;
import java.util.Objects;

public class PersonalDataActivity extends AppCompatActivity {

    private ImageView imageView;
    private EditText name;
    private EditText surname;
    private TextView age;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Integer birthDate;
    private Integer birthMonth;
    private Integer birthYear;
    private RadioGroup radioGroup;
    private String gender;
    private DBManager db;
    private  Account acc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_data);

        name = findViewById(R.id.name);
        surname = findViewById(R.id.surname);
        age = findViewById(R.id.age);
        //Choosing date from scrolling calendar as extar window
        age.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        PersonalDataActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                birthDate = day;
                birthMonth = month + 1;
                birthYear = year;
                String date = day + "." + month + "." + year;
                age.setText(date);
            }
        };

        radioGroup = findViewById(R.id.radioGroup);

        imageView = findViewById(R.id.imageView);
        Button button = findViewById(R.id.add_photo);

        //Auto auth to edit user data
        //TODO check signIn here
        Bundle arguments = getIntent().getExtras();
        assert arguments != null;
        String email = Objects.requireNonNull(arguments.get("email")).toString();
        String pass = Objects.requireNonNull(arguments.get("password")).toString();
        db = new DBManager();
        db.signIn(email, pass);
        acc = new Account();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iGallery = new Intent(Intent.ACTION_PICK);
                iGallery.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                galleryActivityResultLauncher.launch(iGallery);
            }
        });
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult( //я надеюсь это работает
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        assert data != null;
                        Uri imageUri = data.getData();

                        imageView.setImageURI(imageUri); //TODO надо ее еще как-то скачать в БД
                    }
                    else {
                        Toast.makeText(PersonalDataActivity.this, "Cancelled...", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    public void nextPage(View v){ //TODO обработать не введение данных
        int answerRadioButtonId = radioGroup.getCheckedRadioButtonId();
        if (answerRadioButtonId != -1) {
            RadioButton answer = findViewById(answerRadioButtonId);
            gender = answer.getText().toString();
        } else{
            //TODO обработать не выбор пола
        }
        acc.setFirstName(name.getText().toString());
        acc.setLastName(surname.getText().toString());
        acc.setSex(gender);
        acc.setDayOfBirth(new DayOfBirth(birthYear,birthMonth, birthDate));
        db.saveAccountData(acc);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        System.out.println("Button clicked");
    }
}