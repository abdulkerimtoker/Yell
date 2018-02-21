package yell.client.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import yell.client.R;
import yell.client.activities.MessagingActivity;
import yell.client.sqlite.ConversationSqlHelper;
import yell.client.type.Message;
import yell.client.util.DateFormat;
import yell.client.util.HttpRequester;
import yell.client.util.LastMessages;

public class ConversationFragment extends Fragment implements AdapterView.OnItemClickListener {

    private BroadcastReceiver broadcastReceiver;

    private ConversationListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ListView listView = (ListView) inflater.inflate(R.layout.conversation_listview, container, false);

        adapter = new ConversationListAdapter(inflater);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(this);

        SharedPreferences preferences = getContext().getSharedPreferences("private_preferences", Context.MODE_PRIVATE);

        String conversationJson = preferences.getString("conversations", null);

        if (conversationJson != null) {
            try {
                JSONArray conversations = new JSONArray(conversationJson);

                for (int i = 0; i < conversations.length(); i++) {
                    String username = conversations.getString(i);

                    String messageJson = LastMessages.getLastMessage(getContext(), username);

                    if (messageJson != null) {
                        JSONObject lastMessage = new JSONObject(messageJson);

                        Message message = new Message(lastMessage.getBoolean("is_sent_by_this"),
                                lastMessage.getString("message"),
                                "",
                                lastMessage.getString("date"));

                        adapter.addOrUpdateConversation(username, message);
                    }
                }

                new UpdateImagesTask().execute(conversations);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String sender      = intent.getStringExtra("sender");
                String message     = intent.getStringExtra("message");
                String messageType = intent.getStringExtra("message_type");
                String date        = intent.getStringExtra("date");

                adapter.addOrUpdateConversation(sender, new Message(false, message, messageType, date));
                adapter.notifyDataSetChanged();
            }
        };

        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                new IntentFilter("receive_message"));

        return listView;
    }

    @Override
    public void onDestroyView() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ConversationListAdapter.ViewHolder viewHolder = (ConversationListAdapter.ViewHolder) view.getTag();
        String username = viewHolder.name;

        startActivity(new Intent(getContext(), MessagingActivity.class)
            .putExtra("username", username));
    }

    class ConversationListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private ArrayList<View> rowViews;

        public ConversationListAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
            rowViews = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return rowViews.size();
        }

        @Override
        public Object getItem(int position) {
            return rowViews.get(position).getTag();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return rowViews.get(position);
        }

        public void addOrUpdateConversation(String sender, Message message) {
            for (View rowView : rowViews) {
                ViewHolder viewHolder = (ViewHolder) rowView.getTag();

                if (viewHolder.name.equals(sender)) {
                    if (message.isSentByThis) {
                        viewHolder.tvMessage.setText("You: " + message.message);
                    } else {
                        viewHolder.tvMessage.setText(message.message);
                    }

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(DateFormat.parse(message.date));

                    int hour   = calendar.get(Calendar.HOUR_OF_DAY);
                    int minute = calendar.get(Calendar.MINUTE);

                    String hourText   = hour < 10 ? ("0" + String.valueOf(hour)) : String.valueOf(hour);
                    String minuteText = minute < 10 ? ("0" + String.valueOf(minute)) : String.valueOf(minute);

                    viewHolder.tvDate.setText(hourText + ":" + minuteText);

                    rowViews.remove(rowView);
                    rowViews.add(0, rowView);

                    return;
                }
            }

            View rowView = inflater.inflate(R.layout.conversation_row, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.ivProfile = (ImageView) rowView.findViewById(R.id.ivProfile);
            viewHolder.tvName    = (TextView) rowView.findViewById(R.id.tvName);
            viewHolder.tvMessage = (TextView) rowView.findViewById(R.id.tvMessage);
            viewHolder.tvDate    = (TextView) rowView.findViewById(R.id.tvDate);
            viewHolder.name      = sender;

            viewHolder.tvName.setText(sender);

            if (message.isSentByThis) {
                viewHolder.tvMessage.setText("You: " + message.message);
            } else {
                viewHolder.tvMessage.setText(message.message);
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(DateFormat.parse(message.date));

            int hour   = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String hourText   = hour < 10 ? ("0" + String.valueOf(hour)) : String.valueOf(hour);
            String minuteText = minute < 10 ? ("0" + String.valueOf(minute)) : String.valueOf(minute);

            viewHolder.tvDate.setText(hourText + ":" + minuteText);

            rowView.setTag(viewHolder);

            rowViews.add(0, rowView);
        }

        public boolean removeConversation(String username) {
            for (View rowView : rowViews) {
                ViewHolder viewHolder = (ViewHolder) rowView.getTag();

                if (viewHolder.name.equals(username)) {
                    rowViews.remove(rowView);
                    return true;
                }
            }
            return false;
        }

        public void updateImages(JSONObject imageIds) {
            Iterator<String> iterator = imageIds.keys();

            while (iterator.hasNext()) {
                try {
                    String username = iterator.next();
                    JSONObject user = imageIds.getJSONObject(username);

                    if (user.has("image_id")) {
                        String imageId  = imageIds.getJSONObject(username).getString("image_id");
                        for (View rowView : rowViews) {
                            ViewHolder viewHolder = (ViewHolder) rowView.getTag();

                            if (viewHolder.name.equals(username)) {
                                UrlImageViewHelper.setUrlDrawable(viewHolder.ivProfile,
                                        HttpRequester.WEB_CONTEXT + "/Images/" + imageId);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        class ViewHolder {
            ImageView ivProfile;
            TextView tvName;
            TextView tvMessage;
            TextView tvDate;
            String name;
        }
    }

    class UpdateImagesTask extends AsyncTask<JSONArray, Void, Void> {

        JSONObject response;
        JSONArray usernames;

        @Override
        protected Void doInBackground(JSONArray... params) {
            usernames = params[0];

            try {
                response = HttpRequester.profiles(usernames);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (response != null) {
                adapter.updateImages(response);
            }
        }
    }
}
