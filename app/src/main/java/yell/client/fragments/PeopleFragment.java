package yell.client.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import yell.client.R;
import yell.client.activities.PersonActivity;
import yell.client.type.Person;
import yell.client.util.HttpRequester;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PeopleFragment extends Fragment implements OnItemClickListener {

    private Context context;
    private PeopleListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ListView view = (ListView) inflater.inflate(R.layout.people_listview, container, false);

        adapter = new PeopleListAdapter(inflater);
        view.setAdapter(adapter);

        view.setOnItemClickListener(this);

        String friendsJson = getContext().getSharedPreferences("private_preferences", Context.MODE_PRIVATE)
                .getString("friends", null);

        if (friendsJson != null) {
            try {
                JSONArray friends = new JSONArray(friendsJson);
                new PersonTask().execute(friends);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PeopleListAdapter.ViewHolder viewHolder = (PeopleListAdapter.ViewHolder) view.getTag();

        startActivity(new Intent(getContext(), PersonActivity.class)
                .putExtra("username", viewHolder.information.name));
    }

    class PersonTask extends AsyncTask<JSONArray, Void, Void> {

        public JSONArray usernames;
        public JSONObject response;

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
                for (int i = 0; i < usernames.length(); i++) {
                    try {
                        Person person = new Person();
                        JSONObject userInf = response.getJSONObject(usernames.getString(i));

                        person.name = usernames.getString(i);
                        person.description = userInf.getString("description");
                        person.signupDate = userInf.getString("sign_up_date");

                        if (userInf.has("image_id")) {
                            person.imageId = userInf.getString("image_id");
                        }

                        adapter.addPersonRow(person);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }
    }

    class PeopleListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private ArrayList<View> rowViews;

        public PeopleListAdapter(LayoutInflater inflater) {
            this.inflater = inflater;
            rowViews = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return rowViews.size();
        }

        @Override
        public Object getItem(int position) {
            return rowViews.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return rowViews.get(position);
        }

        public void addPersonRow(Person person) {
            View rowView = inflater.inflate(R.layout.people_row, null);

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.ivProfile = (ImageView) rowView.findViewById(R.id.ivProfile);
            viewHolder.tvName    = (TextView) rowView.findViewById(R.id.tvName);
            viewHolder.tvDesc    = (TextView) rowView.findViewById(R.id.tvDescription);
            viewHolder.information = person;

            if (person.imageId != null) {
                UrlImageViewHelper.setUrlDrawable(viewHolder.ivProfile, HttpRequester.WEB_CONTEXT + "/Images/" + person.imageId);
            }

            viewHolder.tvName.setText(person.name);
            viewHolder.tvDesc.setText(person.description);

            rowView.setTag(viewHolder);

            rowViews.add(rowView);
        }

        class ViewHolder {
            ImageView ivProfile;
            TextView tvName;
            TextView tvDesc;
            Person information;
        }
    }
}


