package com.noelchew.firebaseshoutout.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.noelchew.firebaseshoutout.R;
import com.noelchew.firebaseshoutout.data.FirstTimeRunningData;
import com.noelchew.firebaseshoutout.util.AnalyticsUtil;
import com.noelchew.ncutils.AlertDialogUtil;


/**
 * Created by noelchew on 24/11/2016.
 */

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 9001;

    private Context context;

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount googleSignInAccount;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;

    private Button btnGoogleSignIn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = LoginActivity.this;
        setContentView(R.layout.activity_login);

        AnalyticsUtil.sendAnalyticsScreenTrack(context, "LoginActivity");

        setView();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, onConnectionFailedListener /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        // Initialize FirebaseAuth
        mFirebaseAuth = FirebaseAuth.getInstance();

        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser != null) {
            // user is logged in
            // start MainActivity
            startMainActivity();
        }
    }

    private void setView() {
        btnGoogleSignIn = (Button) findViewById(R.id.button_sign_in);
        btnGoogleSignIn.setOnClickListener(btnSignInOnClickListener);

        if (FirstTimeRunningData.isFirstTimeRunning(context)) {
            FirstTimeRunningData.setIfFirstTimeRunning(context, false);
            AlertDialogUtil.showAlertDialogMessage(context, R.string.disclaimer_title, R.string.disclaimer_message, null);
        }
    }

    private View.OnClickListener btnSignInOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            signIn();
        }
    };

    private GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
// An unresolvable error has occurred and Google APIs (including Sign-In) will not
            // be available.
            Log.d(TAG, "onConnectionFailed:" + connectionResult);
            Toast.makeText(context, R.string.google_play_service_error, Toast.LENGTH_SHORT).show();
        }
    };

    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                googleSignInAccount = result.getSignInAccount();
                firebaseAuthWithGoogle(googleSignInAccount);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed. " + result.getStatus());
                Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.removeAuthStateListener(authStateListener);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(context, R.string.sign_in_failed,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            startMainActivity();
                        }
                    }
                });
    }

    public void startMainActivity() {
        AnalyticsUtil.sendAnalyticsEventTrack(context, "User", "Login");
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            if (firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isAnonymous()) {
                startMainActivity();
            }
        }
    };

}
