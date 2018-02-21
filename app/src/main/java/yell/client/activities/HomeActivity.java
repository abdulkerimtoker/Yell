

package yell.client.activities;


import android.content.Intent;
import android.graphics.Color;
import android.os.PersistableBundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.mixpanel.android.mpmetrics.MixpanelAPI;

import yell.client.R;
import yell.client.fragments.ConversationFragment;
import yell.client.fragments.PeopleFragment;

public class HomeActivity extends AppCompatActivity implements
        TabLayout.OnTabSelectedListener, ViewPager.OnPageChangeListener {

    private ViewPager pager;
    private ScreenSlidePagerAdapter pagerAdapter;
    private TabLayout tabLayout;
    private Toolbar toolbar;

    private static String tabTitles[] = {"Messages", "People"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize the toolbar and set it as the action bar of the app
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setLogo(R.mipmap.yell_logo);
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setTitle("Yell");

        setSupportActionBar(toolbar);

        // Initialize the tabs
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.addTab(tabLayout.newTab().setText("CONVERSATIONS"));
        tabLayout.addTab(tabLayout.newTab().setText("FRIENDS"));
        tabLayout.setOnTabSelectedListener(this);

        // Initialize the view pager
        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        pager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));
    }

    @Override
    public void onPageSelected(int position) {
        tabLayout.getTabAt(position).select();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

    @Override
    public void onPageScrollStateChanged(int state) {}

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        pager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {}

    @Override
    public void onTabReselected(TabLayout.Tab tab) {}

    class ScreenSlidePagerAdapter extends FragmentPagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
            conversationFragment = new ConversationFragment();
            peopleFragment = new PeopleFragment();
        }

        private ConversationFragment conversationFragment;
        private PeopleFragment peopleFragment;

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return conversationFragment == null ? new ConversationFragment() : conversationFragment;
                case 1:
                    return peopleFragment == null ? new PeopleFragment() : peopleFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                startActivity(new Intent(this, SearchActivity.class));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}