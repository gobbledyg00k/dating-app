package com.pattern.acquaintances.model;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class DBManager {
    private DatabaseReference dataBase;
    private FirebaseAuth mAuth;
    OnCompleteListener<AuthResult> signUpOnComplete = null;
    OnCompleteListener<AuthResult> signInOnComplete = null;
    public DBManager(){
        mAuth = FirebaseAuth.getInstance();
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
        Log.println(Log.INFO, "Database", "SignUp completed");
    }
    public void signIn(String email, String password){
        if (this.signInOnComplete != null){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this.signInOnComplete);
        } else {
            mAuth.signInWithEmailAndPassword(email, password);
        }
        Log.println(Log.INFO, "Database", "SignIn completed");
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

}
