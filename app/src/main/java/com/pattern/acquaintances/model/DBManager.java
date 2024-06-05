package com.pattern.acquaintances.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.function.Function;

public class DBManager {
    private String logTag = "Database";
    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;
    private Account accountData = null;
    OnCompleteListener<Void> saveAccDataOnComplete = null;
    Function<Account, Void> onGetAccountData;

    public DBManager(Function<Account, Void> onGetAccountData_){
        dataBase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        onGetAccountData = onGetAccountData_;
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    setAccountDataChangeListener();
                }
            }
        });
    }
    /**
     * сохранить в базе данных объект Accaunt за uid авторизованного пользователя
     * если пользователь неавторизован ничего не произойдёт
     */
    public void saveAccountData(Account acc){
        DatabaseReference users = dataBase.getReference("users");
        String uid = mAuth.getUid();
        if (uid == null){
            Log.i(logTag, "User uid equals null/unauthorized.");
            return;
        }
        DatabaseReference user = users.child(uid);
        if (saveAccDataOnComplete != null){
            user.setValue(acc).addOnCompleteListener(saveAccDataOnComplete);
        } else {
            user.setValue(acc);
        }
        Log.i(logTag, "Saving account data");
    }

    /**
     * устанавливает listiner, который вызовется при сохранении Account
     * в listener-е можно прописать действия на ошибки исходя из результата task.getException()
     */
    public void setSaveAccDataOnComplete(OnCompleteListener<Void> saveAccDataOnComplete) {
        this.saveAccDataOnComplete = saveAccDataOnComplete;
    }
    private void setAccountDataChangeListener(){
        DatabaseReference users = dataBase.getReference("users");
        String uid = mAuth.getUid();
        if (uid == null){
            Log.i(logTag, "User uid equals null/unauthorized.");
            return;
        }
        DatabaseReference user = users.child(uid);
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accountData = snapshot.getValue(Account.class);
                Log.i(logTag, "Got new account data");
                onGetAccountData.apply(accountData);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                accountData = null;
                Log.e(logTag, "Error trying to get account data: " + error.getMessage());
            }
        });
    }
}
