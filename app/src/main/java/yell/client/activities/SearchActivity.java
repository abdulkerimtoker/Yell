package yell.client.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import yell.client.R;
import yell.client.type.Person;
import yell.client.util.HttpRequester;

public class SearchActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Bind(R.id.lvSearchResult)
    ListView lvSearchResult;

    @Bind(R.id.etKeyword)
    EditText etKeyword;

    @Bind(R.id.btSearch)
    Button btSearch;

    private Toolbar toolbar;

    private SearchListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Search");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        adapter = new SearchListAdapter(getLayoutInflater());
        lvSearchResult.setAdapter(adapter);
        lvSearchResult.setOnItemClickListener(this);
    }

    @OnClick(R.id.btSearch)
    public void search() {
        adapter.clear();
        adapter.notifyDataSetChanged();
        String keyword = etKeyword.getText().toString();

        if (!keyword.equals("")) {
            new SearchTask().execute(keyword);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SearchListAdapter.ViewHolder viewHolder = (SearchListAdapter.ViewHolder) view.getTag();

        Person person = viewHolder.information;

        startActivity(new Intent(this, PersonActivity.class).putExtra("username", person.name));
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

    class SearchTask extends AsyncTask<String, Void, Void> {

        private JSONArray response;

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SearchActivity.this, "Search", "Loading...");
        }

        @Override
        protected Void doInBackground(String... params) {
            String keyword = params[0];

            try {
                response = HttpRequester.search(keyword);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (response != null) {
                for (int i = 0; i < response.length(); i++) {
                    try {
                        JSONObject userInf = response.getJSONObject(i);
                        Person person = new Person();

                        person.name = userInf.getString("username");
                        person.signupDate = userInf.getString("sign_up_date");
                        person.description = userInf.has("description") ? userInf.getString("description") : "";

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

            if (progressDialog != null) {
                progressDialog.dismiss();
            }
        }
    }

    class SearchListAdapter extends BaseAdapter {

        private LayoutInflater inflater;

        private ArrayList<View> rowViews;

        public SearchListAdapter(LayoutInflater inflater) {
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

        public void clear() {
            rowViews.clear();
        }

        class ViewHolder {
            ImageView ivProfile;
            TextView tvName;
            TextView tvDesc;
            Person information;
        }
    }
}