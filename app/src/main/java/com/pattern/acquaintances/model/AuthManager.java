package com.pattern.acquaintances.model;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AuthManager {
    private String logTag = "Authentication";
    private FirebaseAuth mAuth;
    OnCompleteListener<AuthResult> signUpOnComplete = null;
    OnCompleteListener<AuthResult> signInOnComplete = null;
    OnCompleteListener<Void> resetPasswordOnComplete = null;
    public AuthManager(){
        mAuth = FirebaseAuth.getInstance();
    }
    /**
     * проверяет авторизован ли уже пользователь, получает данные об аккаунте
     * !!может не успеть достать данные до вызова getAccountData!!
     */
    public boolean isSignedIn(){
        if (mAuth.getCurrentUser() != null){
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
        Log.i(logTag, "SigningUp of " + email);
    }
    /**
     * авторизация, достаёт данные об аккаунте, новые данные хранятся DBManager
     * !!может не успеть достать данные до вызова getAccountData!!
     */
    public void signIn(String email, String password){
        if (this.signInOnComplete != null){
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this.signInOnComplete);
        } else {
            mAuth.signInWithEmailAndPassword(email, password);
        }
        Log.i(logTag, "SigningIn of " + email);
    }
    /**
     *  выйти из аккаунта
     */
    public void signOut(){
        mAuth.signOut();
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
        Log.i(logTag, "Sending password reset to " + email);
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
     * устанавливает listiner, который вызовется после отправки на почту письма с восстановлением пароля
     * в listener-е можно прописать действия на ошибки исходя из результата task.getException()
     */
    public void setResetPasswordOnComplete(OnCompleteListener<Void> resetPasswordOnComplete) {
        this.resetPasswordOnComplete = resetPasswordOnComplete;
    }
}