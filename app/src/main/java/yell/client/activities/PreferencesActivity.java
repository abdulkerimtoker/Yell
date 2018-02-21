package yell.client.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import yell.client.R;
import yell.client.util.HttpRequester;

public class PreferencesActivity extends AppCompatActivity {

    @Bind(R.id.ivProfile)
    ImageView ivProfile;

    @Bind(R.id.etDesc)
    EditText etDesc;

    private String sessionKey;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        // Initialize the toolbar and set it as the action bar of the app
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Preferences");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ButterKnife.bind(this);

        SharedPreferences preferences = getSharedPreferences("private_preferences", MODE_PRIVATE);

        sessionKey = preferences.getString("session_key", null);

        new LoadProfileTask().execute(preferences.getString("me", ""));
    }

    public void saveChanges() {
        String description = etDesc.getText().toString();
        new ChangeDescriptionTask().execute(sessionKey, description);
    }

    public void logout() {
        SharedPreferences.Editor editor = getSharedPreferences("private_preferences", MODE_PRIVATE).edit();

        editor.remove("session_key");
        editor.remove("conversations");
        editor.remove("friends");
        editor.commit();

        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @OnClick(R.id.btUpload)
    public void uploadImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            new UploadImageTask().execute(sessionKey, getRealPathFromURI(data.getData()));
        }
    }

    public String getRealPathFromURI(Uri selectedImage) {
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String picturePath = cursor.getString(columnIndex);

        return picturePath;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preferences, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveChanges();
                return true;
            case R.id.action_logout:
                logout();
                return true;
            case android.R.id.home:
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    class ChangeDescriptionTask extends AsyncTask<String, Void, Void> {

        private String sessionKey;
        private String description;
        private String response;

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PreferencesActivity.this, "Save", "Please wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            sessionKey = params[0];
            description = params[1];

            try {
                response = HttpRequester.changeDescription(sessionKey, description);
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
                Toast.makeText(PreferencesActivity.this, "Changes are saved", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PreferencesActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class UploadImageTask extends AsyncTask<String, Void, Void> {

        private String response;
        private String path;
        private String sessionKey;

        private ProgressDialog progressDialog;


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PreferencesActivity.this, "Upload Image", "Please Wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            sessionKey = params[0];
            path = params[1];

            try {
                response = HttpRequester.uploadImage(sessionKey, path);
            } catch (Exception e) {
                response = "error";
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (progressDialog != null) {
                progressDialog.dismiss();
            }

            if (response.length() >= 32) {
                String imageId = response;
                Log.i("IMAGE ID", imageId);
                UrlImageViewHelper.setUrlDrawable(ivProfile, HttpRequester.WEB_CONTEXT + "/Images/" + imageId);
            } else {
                Toast.makeText(PreferencesActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class LogOutTask extends AsyncTask<String, Void, Void> {

        private String sessionKey;
        private String response;

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PreferencesActivity.this, "Log Out", "Please wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            sessionKey = params[0];

            try {
                response = HttpRequester.logout(sessionKey);
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

            PreferencesActivity.this.finish();

            startActivity(new Intent(PreferencesActivity.this, MainActivity.class));
        }
    }

    class LoadProfileTask extends AsyncTask<String, Void, Void> {

        JSONObject response;
        String username;

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PreferencesActivity.this, "Loading", "Please Wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            username = params[0];

            try {
                response = HttpRequester.profile(username);
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

            if (response != null) {
                try {
                    etDesc.setText(response.getString("description"));

                    if (response.has("image_id")) {
                        String imageId = response.getString("image_id");
                        UrlImageViewHelper.setUrlDrawable(ivProfile, HttpRequester.WEB_CONTEXT + "/Images/" + imageId);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(PreferencesActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
