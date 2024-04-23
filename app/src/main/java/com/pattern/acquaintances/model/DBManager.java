package com.pattern.acquaintances.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DBManager {
    private String logTag = "Database";
    private FirebaseAuth mAuth;
    private FirebaseDatabase dataBase;
    private Account accountData = null;
    OnCompleteListener<AuthResult> signUpOnComplete = null;
    OnCompleteListener<AuthResult> signInOnComplete = null;

    OnCompleteListener<Void> resetPasswordOnComplete = null;
    OnCompleteListener<Void> saveAccDataOnComplete = null;

    public DBManager(){
        dataBase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }
    /**
     * сохранить в базе данных объект Accaunt за uid авторизованного пользователя
     * если пользователь неавторизован ничего не произойдёт
     */
    public void saveAccountData(Account acc){
        DatabaseReference users = dataBase.getReference("users");
        String uid = mAuth.getUid();
        if (uid == null){
            return;
        }
        DatabaseReference user = users.child(uid);
        if (saveAccDataOnComplete != null){
            user.setValue(acc).addOnCompleteListener(saveAccDataOnComplete);
        } else {
            user.setValue(acc);
        }
        Log.i(logTag, "Account data saved");
    }

    /**
     * проверяет авторизован ли уже пользователь, получает данные об аккаунте
     * !!может не успеть достать данные до вызова getAccountData!!
     */
    public boolean isSignedIn(){
        if (mAuth.getCurrentUser() != null){
            setAccountDataChangeListener();
            return true;
        }
        return false;
    }
    /**
     * регистрациия, !!может не удасться, если пароль короткий!!
     */
    public void signUp(String email, String password){
        if (this.signUpOnComplete != null){
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this.signUpOnComplete);
        } else {
            mAuth.createUserWithEmailAndPassword(email, password);
        }
        Log.i(logTag, "SignUp of " + email + " completed");
    }
    /**
     * авторизация, достаёт данные об аккаунте, лежащие в accountData
     * !!может не успеть достать данные до вызова getAccountData!!
     */
    public void signIn(String email, String password){
        if (this.signInOnComplete != null){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this.signInOnComplete);
        } else {
            mAuth.signInWithEmailAndPassword(email, password);
        }
        setAccountDataChangeListener();
        Log.i(logTag, "SignIn of " + email + " completed");
    }
    /**
     *  выйти из аккаунта
     */
    public void signOut(){
        mAuth.signOut();
        accountData = null;
        Log.i(logTag, "User signed out");
    }
    /**
     * отправить письмо на email для восстановления пароля
     */
    public void resetPassword(String email){
        if (resetPasswordOnComplete != null) {
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener(resetPasswordOnComplete);
        }
        else {
            mAuth.sendPasswordResetEmail(email);
        }
        Log.i(logTag, "Sent password reset to " + email);
    }
    /**
     * устанавливает listiner, который вызовется при регистрации
     * в listener-е можно прописать действия на ошибки исходя из результата task.getException()
     */
    public void setSignUpOnComplete(OnCompleteListener<AuthResult> signUpOnComplete) {
        this.signUpOnComplete = signUpOnComplete;
    }
    /**
     * устанавливает listiner, который вызовется при авторизации
     * в listener-е можно прописать действия на ошибки исходя из результата task.getException()
     */
    public void setSignInOnComplete(OnCompleteListener<AuthResult> signInOnComplete) {
        this.signInOnComplete = signInOnComplete;
    }
    /**
     * устанавливает listiner, который вызовется при сохранении Account
     * в listener-е можно прописать действия на ошибки исходя из результата task.getException()
     */
    public void setSaveAccDataOnComplete(OnCompleteListener<Void> saveAccDataOnComplete) {
        this.saveAccDataOnComplete = saveAccDataOnComplete;
    }
    /**
     * устанавливает listiner, который вызовется после отправки на почту письма с восстановлением пароля
     * в listener-е можно прописать действия на ошибки исходя из результата task.getException()
     */
    public void setResetPasswordOnComplete(OnCompleteListener<Void> resetPasswordOnComplete) {
        this.resetPasswordOnComplete = resetPasswordOnComplete;
    }
    /**
     * возвращает данные об аккаунте в объекте аккаунт
     * данные обновляются в реальном времени, то есть при изменении данных в БД, вернёт последнюю версию
     * если данные ещё не пришли вернёт null
     */
    public Account getAccountData() {
        return accountData;
    }

    private void setAccountDataChangeListener(){
        DatabaseReference users = dataBase.getReference("users");
        String uid = mAuth.getUid();
        if (uid == null){
            return;
        }
        DatabaseReference user = users.child(uid);
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                accountData = snapshot.getValue(Account.class);
                Log.i(logTag, "Got new account data");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                accountData = null;
                Log.e(logTag, "Error trying to get account data: " + error.getMessage());
            }
        });
    }
}
