package com.kfa.kefa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.kfa.kefa.R;
import com.kfa.kefa.utils.User;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Extends Fragment Activity ?
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private ImageView imageView;

    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    int i;
    private User user;
    private ArrayList<User> friendList = new ArrayList<>();
    private boolean intercepMove = false;
    private Fragment fragment;
    private TextView textView_userTAG;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        user = getIntent().getParcelableExtra("user");
        if (user != null) {
            System.out.println("user not null");
        } else {
            System.out.println("user null");
        }
        friendList = getIntent().getParcelableArrayListExtra("friendList");

        Bundle bundle = new Bundle();
        bundle.putParcelable("user", user);
        bundle.putParcelableArrayList("friendList", friendList);

        /*
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(),bundle);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter); */
        imageView = findViewById(R.id.mainActivity_settings);
        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        TabLayout tabLayout = findViewById(R.id.tabs);
        textView_userTAG = findViewById(R.id.main_activity_textView_userTAG);
        // !!!! null
        textView_userTAG.setText("@" + user.getUserTAG());

        //Menu
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);

        //ToolBar
        setSupportActionBar(toolbar);
        navigationView.bringToFront();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


        // Instantiate a ViewPager2 and a PagerAdapter.
        viewPager = findViewById(R.id.view_pager);
        pagerAdapter = new ScreenSlidePagerAdapter(this, bundle);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(1);

        reduceDragSensitivity();
        viewPager.getChildAt(0).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                System.out.println(event.getX());
                System.out.println(event.getAction());
                if (100 * event.getX() > 5 * view.getWidth() && 100 * event.getX() < 95 * view.getWidth()) {
                }

                return false;
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(Gravity.LEFT);

                /*
                Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
                intent.putExtra("user",(Serializable) user);
                intent.putExtra("friendList",friendList);
                startActivity(intent);
                finish(); */
            }
        });


        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager, true, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position) {
                    case 0:
                        tab.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_calendar_today_24));
                        break;
                    case 1:
                        tab.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_map_24));
                        break;

                    case 2:
                        tab.setIcon(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_baseline_newspaper_24));
                        break;
                }
            }
        });
        tabLayoutMediator.attach();


    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        viewPager.setCurrentItem(1);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.change_profile:
                Intent intent = new Intent(getApplicationContext(), UpdateProfileActivity.class);
                intent.putExtra("user", (Serializable) user);
                intent.putExtra("friendList", friendList);
                startActivity(intent);
                finish();
                return true;

            case R.id.change_userTAG:
                intent = new Intent(getApplicationContext(), UpdateUserTagActivity.class);
                intent.putExtra("user", (Serializable) user);
                intent.putExtra("friendList", friendList);
                startActivity(intent);
                finish();
                return true;

            case R.id.add_friends:
                intent = new Intent(getApplicationContext(), AddFriendsActivity.class);
                intent.putExtra("user", (Serializable) user);
                intent.putExtra("friendList", friendList);
                startActivity(intent);
                finish();
                return true;

            case R.id.friend_requests:
                intent = new Intent(getApplicationContext(), FriendRequestsActivity.class);
                intent.putExtra("user", (Serializable) user);
                intent.putExtra("friendList", friendList);
                startActivity(intent);
                finish();
                return true;
            case R.id.friends_list:
                intent = new Intent(getApplicationContext(), FriendsListActivity.class);
                intent.putExtra("user", (Serializable) user);
                intent.putExtra("friendList", friendList);
                startActivity(intent);
                finish();
                return true;
            case R.id.logout:
                firebaseAuth.signOut();
                intent = new Intent(getApplicationContext(), SplashScreenActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.contacts:
                intent = new Intent(getApplicationContext(), ContactActivity.class);
                intent.putExtra("user", (Serializable) user);
                intent.putExtra("friendList", friendList);
                startActivity(intent);
                finish();
                return true;
            case R.id.newsletter:
                Toast.makeText(this, "Bientôt Diponible ", Toast.LENGTH_SHORT).show();
                return false;
            case R.id.settings:
                Toast.makeText(this, "Bientôt Diponible ", Toast.LENGTH_SHORT).show();
                return false;


        }
        return false;

    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        private Bundle bundle;

        public ScreenSlidePagerAdapter(MainActivity mainActivity, Bundle bundle) {
            super(mainActivity);
            this.bundle = bundle;
            createFragment(1);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            fragment = new NewsPaperFragment();
            switch (position) {
                case 0:
                    fragment = new CalendarFragment();
                    fragment.setArguments(bundle);
                    break;
                case 1:
                    fragment = new MapFragment();
                    fragment.setArguments(bundle);
                    break;
                case 2:
                    fragment = new NewsPaperFragment();
                    fragment.setArguments(bundle);
                    break;

            }
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private void reduceDragSensitivity() {
        try {
            Field ff = ViewPager2.class.getDeclaredField("mRecyclerView");
            ff.setAccessible(true);
            RecyclerView recyclerView = (RecyclerView) ff.get(viewPager);
            Field touchSlopField = RecyclerView.class.getDeclaredField("mTouchSlop");
            touchSlopField.setAccessible(true);
            int touchSlop = (int) touchSlopField.get(recyclerView);
            touchSlopField.set(recyclerView, touchSlop * 3);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}