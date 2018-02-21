package yell.client.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import yell.client.R;
import yell.client.sqlite.ConversationSqlHelper;
import yell.client.type.Message;
import yell.client.util.CircleBitmap;
import yell.client.util.Conversations;
import yell.client.util.DateFormat;
import yell.client.util.HttpRequester;
import yell.client.util.LastMessages;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MessagingActivity extends AppCompatActivity {

    private MessagingAdapter adapter;

    private ConversationSqlHelper sqlHelper;

    private BroadcastReceiver broadcastReceiver;

    private String conversationName;
    private String sessionKey;

    @Bind(R.id.btSend)
    Button btSend;

    @Bind(R.id.etMessage)
    EditText etMessage;

    @Bind(R.id.lvMessaging)
    ListView lvMessaging;

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messaging);
        ButterKnife.bind(this);

        conversationName = getIntent().getStringExtra("username");
        sessionKey       = getSharedPreferences("private_preferences", MODE_PRIVATE).getString("session_key", null);

        // Initialize the toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(conversationName);
        toolbar.setTitleTextColor(Color.WHITE);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MessagingActivity.this, PersonActivity.class).putExtra("username", conversationName));
            }
        });

        adapter = new MessagingAdapter(getLayoutInflater());
        lvMessaging.setAdapter(adapter);
        lvMessaging.setDivider(null);
        lvMessaging.setDividerHeight(0);

        // Load all messages from SQLite database to the ListView
        sqlHelper = new ConversationSqlHelper(this, conversationName);
        adapter.addAllMessageRows(sqlHelper.getAllMessages(true));
        adapter.notifyDataSetChanged();

        lvMessaging.setSelection(lvMessaging.getCount() - 1);

        // Register a BroadcastReceiver to receive incoming messages
        // When a message is received, add it to the ListView
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent data) {
                String sender = data.getStringExtra("sender");

                if (sender.equals(conversationName)) {
                    String message = data.getStringExtra("message");
                    String messageType = data.getStringExtra("message_type");
                    String date = data.getStringExtra("date");

                    adapter.addMessageRow(new Message(false, message, messageType, date));
                    adapter.notifyDataSetChanged();

                    lvMessaging.setSelection(lvMessaging.getCount() - 1);
                }
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("receive_message"));

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        new LoadLogoTask().execute(conversationName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences.Editor editor = getSharedPreferences("private_preferences", MODE_PRIVATE).edit();
        editor.putBoolean(conversationName + "_active", false);
        editor.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = getSharedPreferences("private_preferences", MODE_PRIVATE).edit();
        editor.putBoolean(conversationName + "_active", true);
        editor.commit();
    }

    @OnClick(R.id.btSend)
    public void onSendButtonClick(View view) {
        String messageText = etMessage.getText().toString();

        if (!messageText.equals("")) {
            etMessage.setText("");

            Message message = new Message(true,
                    messageText,
                    "txt",
                    DateFormat.format(new Date()));

            new SendMessageTask().execute(message);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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

    class SendMessageTask extends AsyncTask<Message, Void, Void> {

        private String response = null;
        private Message message;

        @Override
        protected Void doInBackground(Message... params) {
            message = params[0];

            try {
                response = HttpRequester.sendMessage(MessagingActivity.this.sessionKey,
                        MessagingActivity.this.conversationName,
                        message.message,
                        message.messageType);
            } catch (Exception e) {
                response = "error";
                e.printStackTrace();
            }

            if (response.equals("done")) {
                sqlHelper.insertMessage(message);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (response.equals("done")) {
                adapter.addMessageRow(message);
                adapter.notifyDataSetChanged();
                lvMessaging.setSelection(lvMessaging.getCount() - 1);

                Conversations.addConversation(MessagingActivity.this, MessagingActivity.this.conversationName);

                LastMessages.setLastMessage(MessagingActivity.this, conversationName, "You: " + message.message, false);
            } else {
                Toast.makeText(MessagingActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class LoadLogoTask extends AsyncTask<String, Void, Void> {

        private JSONObject response;

        @Override
        protected Void doInBackground(String... params) {
            try {
                response = HttpRequester.profile(params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (response != null) {
                if (response.has("image_id")) {
                    try {
                        Bitmap bitmap = UrlImageViewHelper.getCachedBitmap(
                                HttpRequester.WEB_CONTEXT + "/Images/" + response.getString("image_id"));

                        if (bitmap != null) {
                            BitmapDrawable logo = new BitmapDrawable(getResources(),
                                    CircleBitmap.getCircleBitmap(Bitmap.createScaledBitmap(bitmap, 100, 100, true)));

                            toolbar.setLogo(logo);
                        }
                    } catch (Exception e) {
                        Toast.makeText(MessagingActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            } else {
                Toast.makeText(MessagingActivity.this, "An error occured", Toast.LENGTH_SHORT).show();
            }
        }
    }

    class MessagingAdapter extends BaseAdapter {

        private LayoutInflater inflater;
        private ArrayList<View> rowViews;

        public MessagingAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
            this.rowViews = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return rowViews.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return rowViews.get(position);
        }

        public void addMessageRow(Message message) {
            View rowView = inflater.inflate(R.layout.message_row, null);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.tvMessage = (TextView) rowView.findViewById(R.id.tvMessage);
            viewHolder.tvTime    = (TextView) rowView.findViewById(R.id.tvTime);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateFormat.parse(message.date));

            viewHolder.tvMessage.setText(message.message);

            int hour   = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String hourText   = hour < 10 ? ("0" + String.valueOf(hour)) : String.valueOf(hour);
            String minuteText = minute < 10 ? ("0" + String.valueOf(minute)) : String.valueOf(minute);

            viewHolder.tvTime.setText(hourText + ":" + minuteText);

            LinearLayout wrapper = (LinearLayout) rowView.findViewById(R.id.wrapper);

            if (message.isSentByThis) {
                wrapper.setGravity(Gravity.RIGHT);
                viewHolder.tvMessage.setBackgroundResource(R.drawable.bubble_green);
            } else {
                wrapper.setGravity(Gravity.LEFT);
                viewHolder.tvMessage.setBackgroundResource(R.drawable.bubble_yellow);
            }

            rowView.setTag(viewHolder);

            rowViews.add(rowView);
        }

        public void addAllMessageRows(List<Message> messages) {
            for (Message message : messages) {
                addMessageRow(message);
            }
        }

        class ViewHolder {
            TextView tvMessage;
            TextView tvTime;
        }
    }
}

