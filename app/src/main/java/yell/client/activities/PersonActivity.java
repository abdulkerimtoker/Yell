package yell.client.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlDownloader;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import yell.client.R;
import yell.client.type.Person;
import yell.client.util.CircleBitmap;
import yell.client.util.DateFormat;
import yell.client.util.Friends;
import yell.client.util.HttpRequester;

public class PersonActivity extends AppCompatActivity {

    @Bind(R.id.ivProfile)
    ImageView ivProfile;

    @Bind(R.id.twName)
    TextView twName;
    @Bind(R.id.twDesc)
    TextView twDesc;
    @Bind(R.id.twDate)
    TextView twDate;

    private Toolbar toolbar;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        ButterKnife.bind(this);

        username = getIntent().getStringExtra("username");

        // Initialize the toolbar and set it as the action bar of the app
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(" " + username);
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        new LoadProfileTask().execute(username);
    }

    public void sendMessage() {
        Intent intent = new Intent(this, MessagingActivity.class);
        intent.putExtra("username", twName.getText().toString());
        startActivity(intent);
    }


    public void addFriend() {
        if (Friends.addFriend(this, username)) {
            Toast.makeText(this, username + " is added as friend", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.people_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                addFriend();
                return true;
            case R.id.action_message:
                sendMessage();
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

    class LoadProfileTask extends AsyncTask<String, Void, Void> {

        private ProgressDialog progressDialog;
        private JSONObject response;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(PersonActivity.this, "Loading", "Please wait...");
        }

        @Override
        protected Void doInBackground(String... params) {
            String username = params[0];

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
                    twName.setText(response.getString("username"));
                    twDesc.setText(response.getString("description"));


                    Date signupDate = DateFormat.parse(response.getString("sign_up_date"));
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(signupDate);

                    twDate.setText(String.format("Sign Up Date: %d/%d/%d",
                            calendar.get(Calendar.DAY_OF_MONTH),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.YEAR)));

                    if (response.has("image_id")) {
                        UrlImageViewHelper.setUrlDrawable(ivProfile,
                                HttpRequester.WEB_CONTEXT + "/Images/" + response.getString("image_id"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(PersonActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
