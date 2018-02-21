package yell.client.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import yell.client.R;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import yell.client.util.HttpRequester;

public class RegisterActivity extends Activity {

    @Bind(R.id.tvRegLogin) EditText login;
    @Bind(R.id.tvRegPassword) EditText pass;
    @Bind(R.id.cbTerms) CheckBox terms;

    ProgressDialog progressDialog;

    int selectedGender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
    }

    public String getGender(int position){
        switch (position) {
            case 0:
                return "Male";
            case 1:
                return "Female";
            default:
                return "Error";
        }
    }

    @OnClick(R.id.btRegister)
    public void Login(){

        String username = login.getText().toString();
        String password = pass.getText().toString();;

        if (!terms.isChecked()) {
            Toast.makeText(RegisterActivity.this, "Terms must be accepted", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() > 16 || username.length() < 4){
            Toast.makeText(RegisterActivity.this, "Max 16 and min 4 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        new RegisterTask().execute(username, password);
    }

    class RegisterTask extends AsyncTask<String, Void, Void> {

        private String response = null;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(RegisterActivity.this, "Register", "Please wait..");
        }

        @Override
        protected Void doInBackground(String... params) {
            try {
                response = HttpRequester.register(params[0], params[1]);
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

            if (response.equals("done")) {
                Toast.makeText(RegisterActivity.this, "Registered successfully!", Toast.LENGTH_SHORT).show();
                RegisterActivity.this.finish();
            } else if (response.equals("wrong")) {
                Toast.makeText(RegisterActivity.this, "This username is already in use", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
