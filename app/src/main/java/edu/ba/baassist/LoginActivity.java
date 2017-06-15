package edu.ba.baassist;

 //Login to get the userdata and submit them to www.campus-dual.de

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;


public class LoginActivity extends AppCompatActivity {

    //params
    private UserLoginTask mAuthTask = null;

    // UI elements
    private AutoCompleteTextView mUsername;
    private EditText mHashView;
    private View mProgressView;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        mUsername = (AutoCompleteTextView) findViewById(R.id.username);

        mHashView = (EditText) findViewById(R.id.hash);
        mHashView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void attemptLogin() {
        if (mAuthTask != null) return;

        // Reset errors.
        mUsername.setError(null);
        mHashView.setError(null);

        // Store values at the time of the login attempt.
        String username = mUsername.getText().toString();
        String hash = mHashView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid hash, if the user entered one.
        if (!TextUtils.isEmpty(hash) && !isPasswordValid(hash)) {
            mHashView.setError(getString(R.string.error_invalid_hash));
            focusView = mHashView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            mUsername.setError(getString(R.string.error_field_required));
            focusView = mUsername;
            cancel = true;
        } else if (!isUserValid(username)) {
            mUsername.setError(getString(R.string.error_invalid_email));
            focusView = mUsername;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, hash);
            mAuthTask.execute((Void) null);
        }
    }

    //checks for possibility based on www.campus-dual.de
    //username needs to be integer only

    private boolean isUserValid(String username) {
        return (!username.matches(".*[a-z].*"));
    }

    //checks for possibility based on www.campus-dual.de
    //hash need to be 32 digits
    private boolean isPasswordValid(String password) {
        return (password.length() == 32);
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressView.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }



    private class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String mHash;


        UserLoginTask(String email, String hash) {
            username = email;
            mHash = hash;
        }

        //public get method for userhash
        public String getuserhash(){
            return (mHash);
        }


        //public get method for userid
        public String getuserid(){
             return (username);
        }


        @Override
        protected Boolean doInBackground(Void... params) {
            connAdapter.trustAllCertificates();
            boolean reachable = false;
            try {
                reachable = connAdapter.isReachable("https://erp.campus-dual.de/sap/bc/webdynpro/sap/zba_initss?uri=https%3a%2f%2fselfservice.campus-dual.de%2findex%2flogin&sap-client=100&sap-language=DE#");
                } catch (IOException e) {
                e.printStackTrace();
            }

            boolean UserCheck =connAdapter.logInconnection(getuserid(),getuserhash());

            return (reachable && UserCheck);

        }


        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {

                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                //go-to main activity and register account

            } else {

                //after wrong connection show dialog
                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                alertDialog.setTitle("Fehler bei der Verbindung");
                alertDialog.setMessage("Es ist ein Fehler bei der Verbindung zu Campus-Dual aufgetreten!" +
                        " Bitte überprüfe deine Userid und deinen Userhash." +
                        " Für weitere Fragen nutze bitte:");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }});
                alertDialog.show();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}