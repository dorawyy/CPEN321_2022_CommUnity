package com.example.community.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.community.MainActivity;
import com.example.community.R;
import com.example.community.VolleyCallBack;
import com.example.community.classes.GlobalUtil;
import com.example.community.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "LOGIN_ACTIVITY";
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        GlobalUtil.setAppContext(this);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

        ActivityLoginBinding binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);


        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_login);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
    }

    protected void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    private void finishLogin(GoogleSignInAccount account) {
        GlobalUtil.setAccount(account);
        GlobalUtil.FetchUser();
        Intent mainActivityIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainActivityIntent);
        finish();
    }

    private void handleLoginError() {
        // TODO Pretty-ify toast
        Toast errorToast = Toast.makeText(this, "Failed to create user", Toast.LENGTH_SHORT);
        errorToast.show();
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            userDoesExist(account.getId(), new LoginCallback() {
                public void onSuccess(boolean exists) {
                    Log.d(TAG, "onSuccess: " + exists);
                    if (!exists) {
                        createUser(account, new LoginCallback() {
                            @Override
                            public void onError(VolleyError error) {
                                handleLoginError();
                            }

                            @Override
                            public void onSuccess() {
                                finishLogin(account);
                            }
                        });
                    } else {
                        finishLogin(account);
                    }
                }

                @Override
                public void onError(VolleyError error) {
                    handleLoginError();
                }
            });

        }
    }

    private void createUser(GoogleSignInAccount account, VolleyCallBack volleyCallBack) {
        Log.d(TAG, "createUser: Start");
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = GlobalUtil.USER_URL + "/user";
        JSONObject data = new JSONObject();
        try {
            data.put("userId", account.getId());
            data.put("firstName", account.getGivenName());
            data.put("lastName", account.getFamilyName());
            data.put("email", account.getEmail());
            data.put("profilePicture", account.getPhotoUrl());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, data,
                response -> {
                    Log.d(TAG, "createUser: " + response);
                    volleyCallBack.onSuccess();
                },
                error -> {
                    Log.e(TAG, "createUser: " + error);
                    volleyCallBack.onError();
                    // TODO: Fail sign in
                }
        );
        queue.add(request);

    }

    private void userDoesExist(String uid, VolleyCallBack callback) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = GlobalUtil.USER_URL + "/user" + "/" + uid;
        JsonObjectRequest sr = new JsonObjectRequest(Request.Method.GET, url,
                null,
                response -> {
                    Log.d(TAG, "userDoesExist: successResponse");
                    callback.onSuccess(!response.isNull("user"));
                },
                error -> {
                    Log.e(TAG, "userDoesExist: " + error);
                    callback.onError(error);
                });
        queue.add(sr);
    }

}


