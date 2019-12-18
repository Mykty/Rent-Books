package kz.incubator.sdcl.club1.Authentications;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import kz.incubator.sdcl.club1.MainActivity;
import kz.incubator.sdcl.club1.MenuActivity;
import kz.incubator.sdcl.club1.R;

public class AuthenPage extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    FirebaseUser auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authentication);
        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        auth = FirebaseAuth.getInstance().getCurrentUser();

        if(auth != null){
            startActivity(new Intent(AuthenPage.this, MenuActivity.class));
            finish();
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        SimplePagerAdapter adapter = new SimplePagerAdapter(getSupportFragmentManager());

        adapter.addFragment(new LoginPage(), "Login");

//        adapter.addFragment(new RegisterPage(), "Registration");

        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(0);
    }
    class SimplePagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public SimplePagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @SuppressLint("RestrictedApi")
        @Override
        public Fragment getItem(int position) {
            Fragment men = mFragmentList.get(position);
            return men;
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {

            return  mFragmentTitleList.get(position);
        }
    }
}
