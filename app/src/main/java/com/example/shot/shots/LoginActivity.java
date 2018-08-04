package com.example.shot.shots;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.FirebaseFunctionsException;
import com.google.firebase.functions.HttpsCallableResult;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    private CallbackManager callbackManager;
    private LoginButton fblogin;
    private FirebaseAuth mauth;
    private ProgressBar bar;
    private DatabaseReference mdatabase,ref;
    private FirebaseFunctions mfirebaseFunctions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mauth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();
        fblogin = findViewById(R.id.fblogin);
        bar = findViewById(R.id.progressBar1);
        mfirebaseFunctions = FirebaseFunctions.getInstance();
        fblogin.setReadPermissions("email","public_profile");
        fblogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
    }

    private void handleFacebookAccessToken(AccessToken token) {
        bar.setVisibility(View.VISIBLE);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mauth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("LoginActivity","signIn:success");
                    FirebaseUser user = mauth.getCurrentUser();
                    sendFirstMessage(user.getUid());
                    if (user != null) {
                        writeinfo(user);
                    }
                    startActivity(new Intent(LoginActivity.this,MainActivity.class));
                    finish();
                    //updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("LoginActivity", "signInWithCredential:failure", task.getException());
                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    //updateUI(null);
                }

                // [START_EXCLUDE]
                bar.setVisibility(View.GONE);
            }
        });
    }

    private Task<Void> firstMessage(String uid){
        return mfirebaseFunctions.getHttpsCallable("firstMessage")
                .call(uid)
                .continueWith(new Continuation<HttpsCallableResult, Void>() {
                    @Override
                    public Void then(@NonNull Task<HttpsCallableResult> task) throws Exception {
                        return null;
                    }
                });
    }
    private void sendFirstMessage(String uid){
        firstMessage(uid).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()){
                    Exception e = task.getException();
                    if (e instanceof FirebaseFunctionsException){
                        FirebaseFunctionsException ffe = (FirebaseFunctionsException) e;
                        FirebaseFunctionsException.Code code = ffe.getCode();
                        Object details = ffe.getDetails();
                    }
                    Log.w("LoginActivity","addMessage:onFailure",e);
                    Toast.makeText(LoginActivity.this,"An error occured",Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(LoginActivity.this,"Task Completed successfully! ",Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void writeinfo(FirebaseUser user){
        mdatabase = FirebaseDatabase.getInstance().getReference();
        ref = mdatabase.child(user.getUid());
        final String name = user.getDisplayName();
        final String email = user.getEmail();
        final String purl = String.valueOf(user.getPhotoUrl());
        final boolean logged = true;
        final Map<String,Object> data = new HashMap<>();
        data.put("name",name);
        data.put("email",email);
        data.put("purl",purl);
        data.put("logged",logged);
        Log.d("LoginActivity","writing data: "+name+email+purl+logged);
        ref.child("userdetail").updateChildren(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                Toast.makeText(LoginActivity.this,"Logged In",Toast.LENGTH_SHORT).show();
                Log.d("LoginActivity","data added: "+name+email+purl+logged);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result back to the Facebook SDK
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
