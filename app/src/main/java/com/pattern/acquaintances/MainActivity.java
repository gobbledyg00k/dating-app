package com.pattern.acquaintances;

import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.pattern.acquaintances.databinding.ActivityMainBinding;
import com.pattern.acquaintances.model.Account;
import com.pattern.acquaintances.model.DBManager;
import com.pattern.acquaintances.model.DayOfBirth;
import com.pattern.acquaintances.model.Sex;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DBManager db = new DBManager();
        db.signIn("m.pomogaev@g.nsu.ru", "123123");
        Account acc = new Account();
        acc.setLastName("Крутой");
        acc.setFirstName("Мистер");
        acc.setSex(Sex.male);
        acc.setLocation("Семёрка");
        acc.setDayOfBirth(new DayOfBirth(2002,12, 10));
        db.saveAccountData(acc);
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

}