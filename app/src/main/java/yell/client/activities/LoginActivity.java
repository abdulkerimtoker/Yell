package yell.client.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import yell.client.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import yell.client.gcm.GcmRegistrationIntentService;
import yell.client.util.HttpRequester;

public class LoginActivity extends Activity {

    @Bind(R.id.btLogin) Button buttonLogin;
    @Bind(R.id.tvEmail) EditText tvEmail;
    @Bind(R.id.tvPassword) EditText tvPassword;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("private_preferences", MODE_PRIVATE);
    }

    @OnClick(R.id.btLogin)
    public void Login(){

        String username = tvEmail.getText().toString();
        String password = tvPassword.getText().toString();;

        if (username.isEmpty() || password.isEmpty()){
            Toast.makeText(LoginActivity.this, "Username veya pass boş.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() > 16 || username.length() < 4){
            Toast.makeText(LoginActivity.this, "Max 16 char.", Toast.LENGTH_SHORT).show();
            return;
        }

        new LoginTask().execute(username, password);
    }

    @OnClick(R.id.tvCreateAcc)
    public void Register(){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    class LoginTask extends AsyncTask<String, Void, Void> {

        private String key = null;
        private String username;
        private String password;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(LoginActivity.this, "Login", "Please wait..");
        }

        @Override
        protected Void doInBackground(String... params) {
            username = params[0];
            password = params[1];

            try {
                key = HttpRequester.login(username, password);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            if (key.equals("wrong")) {
                Toast.makeText(LoginActivity.this, "Incorrect username or password", Toast.LENGTH_SHORT).show();
            } else if (key.equals("error")) {
                Toast.makeText(LoginActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            } else if (key.length() == 36) {
                Log.i("Session Key", key);

                SharedPreferences sharedPreferences = getSharedPreferences("private_preferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();

                editor.putString("session_key", key);
                editor.putString("me", username);
                editor.commit();

                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                startService(new Intent(LoginActivity.this, GcmRegistrationIntentService.class)
                        .putExtra("session_key", key));
                LoginActivity.this.finish();
            }
        }
    }

}
