package kz.incubator.sdcl.club1.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.fragments.profile_fragments.ReadedBookListFragment;
import kz.incubator.sdcl.club1.fragments.profile_fragments.ReadingBookListFragment;
import kz.incubator.sdcl.club1.fragments.profile_fragments.RecommendationBookListFragment;
import kz.incubator.sdcl.club1.fragments.profile_fragments.ReviewsForBookFragment;
import kz.incubator.sdcl.club1.module.User;

import static kz.incubator.sdcl.club1.MenuActivity.admin;

public class Profile extends AppCompatActivity {
    Toolbar toolbar;
    AppBarLayout appBarLayout;
    static String abonement;
    StoreDatabase db;
    TextView username, ticketType, phoneNumber, subTime;
    TextView readBookCount;
    CircleImageView userImage;
    ViewPager viewPager;
    TabLayout tabLayout;

    StoreDatabase storeDb;
    SQLiteDatabase sqdb;

    public static User user;
    public static String id;
    Bundle bundle;
    ReadingBookListFragment readingBookListFragment;
    StorageReference storageReference;
    DatabaseReference mDatabase;
    LinearLayout subscriptionL, phoneL;
    Dialog subDialog;
    String tickerPeriod;
    Button saveBtn;
    final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 77;

    private int[] tabIcons = {
            R.drawable.ic_class_black_24dp,
            R.drawable.ic_cloud_done_black_24dp,
            R.drawable.ic_receipt_black_24dp,
            R.drawable.ic_assistant_black_24dp,
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_users);
        initWidgets();
        initializeToolbar();

        bundle = getIntent().getExtras();
        user = (User) bundle.getSerializable("user");
        abonement = getIntent().getStringExtra("abonement");
        storageReference = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        db = new StoreDatabase(this);
        initializeBundle(bundle, user);
        loadSubSpinner();
    }

    public void initializeBundle(Bundle bundle, User u) {

        if (bundle != null && u != null) {

            Glide.with(this)
                    .load(u.getPhoto())
                    .into(userImage);

            Log.i("info", "initializeBundle: " + u.getInfo());

            username.setText(u.getInfo());
            phoneNumber.setText(u.getPhoneNumber());
            ticketType.setText(u.getTicket_type());
            readBookCount.setText("0");
            id = u.getId_number();
        }
    }

    public void initializeToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Profile");
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    public void initWidgets() {
        toolbar = findViewById(R.id.toolbars);
        appBarLayout = findViewById(R.id.app_bar);

        username = findViewById(R.id.userName);
        phoneNumber = findViewById(R.id.phoneNumber);
        ticketType = findViewById(R.id.ticketType);
        readBookCount = findViewById(R.id.readBookCount);
        userImage = findViewById(R.id.userImage);
        viewPager = findViewById(R.id.viewPager);
        subscriptionL = findViewById(R.id.subscriptionL);
        phoneL = findViewById(R.id.phoneL);

        readingBookListFragment = new ReadingBookListFragment();

        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        setupTabIcons();
        phoneL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPhoneCallDialog();
            }
        });
    }


    public void loadPhoneCallDialog() {

        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                .setTitle("User: " + user.getInfo())
                .setMessage("Phone Number: " + user.getPhoneNumber())
                .setPositiveButton("CALL", new DialogInterface.OnClickListener() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:" + user.getPhoneNumber()));
                        if (ContextCompat.checkSelfPermission(Profile.this,
                                Manifest.permission.CALL_PHONE)
                                != PackageManager.PERMISSION_GRANTED) {

                            ActivityCompat.requestPermissions(Profile.this,
                                    new String[]{Manifest.permission.CALL_PHONE},
                                    MY_PERMISSIONS_REQUEST_CALL_PHONE);

                        } else {
                            try {
                                startActivity(callIntent);
                            } catch (SecurityException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                })
                .setNeutralButton("SMS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Uri uri = Uri.parse("smsto:" + user.getPhoneNumber());
                        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                        smsIntent.putExtra("sms_body", "Dear, " + user.getInfo() + "!\nInforming you that your subscription date ends " + user.getTicket_type() + ".\nReading Club!");
                        startActivity(smsIntent);
                    }
                })
                .show();

    }

    public void loadSubSpinner() {
        subDialog = new Dialog(Profile.this);
        subDialog.setContentView(R.layout.dialog_subscription);
        subTime = subDialog.findViewById(R.id.subTime);
        saveBtn = subDialog.findViewById(R.id.saveBtn);
        saveBtn.setVisibility(View.INVISIBLE);

        subTime.setText("Expire subscription date: " + user.getTicket_type());

        subscriptionL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subDialog.show();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDatabase.child("user_list").child(user.getFirebaseKey()).child("ticket_type").setValue(tickerPeriod);
                ticketType.setText(tickerPeriod);
                subDialog.dismiss();
                readingBookListFragment.checkAbone("cool");
                Toast.makeText(Profile.this, "User subscription date changed", Toast.LENGTH_SHORT).show();
            }
        });

        Spinner spinner = subDialog.findViewById(R.id.spinner);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String take = adapterView.getItemAtPosition(i).toString();

                if (!take.equals("not chosen")) {
                    DateFormat year = new SimpleDateFormat("yyyy");
                    DateFormat day = new SimpleDateFormat("dd");
                    DateFormat month = new SimpleDateFormat("MM");
                    Calendar cal = Calendar.getInstance();


                    int check = Integer.parseInt(month.format(cal.getTime())) + Integer.parseInt(take);

                    String dayStr = day.format(cal.getTime());
                    String yearStr = year.format(cal.getTime());

                    if (check > 12) {
                        yearStr = "" + (Integer.parseInt(yearStr) + 1);
                        check -= 12;
                    }

                    String monthStr = "" + check;
                    if (monthStr.length() == 1) {
                        monthStr = "0" + monthStr;
                    }
                    tickerPeriod = dayStr + "/" + monthStr + "/" + yearStr;
                    subTime.setText("Expiry subscription date: " + tickerPeriod);
                    saveBtn.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
        tabLayout.getTabAt(3).setIcon(tabIcons[3]);
    }

    public void setBookReadedCount(long bookReadedCount) {
        readBookCount.setText("" + bookReadedCount);
    }

    public static String getId() {
        return user.getId_number();
    }

    public static String getAbonement() {
        return abonement;
    }

    private void setupViewPager(ViewPager viewPager) {
        SimplePageFragmentAdapter adapter = new SimplePageFragmentAdapter(getSupportFragmentManager());

        adapter.addFragment(readingBookListFragment, "Reading");
        adapter.addFragment(new ReadedBookListFragment(), "Readed");
        adapter.addFragment(new ReviewsForBookFragment(), "Reviews");
        adapter.addFragment(new RecommendationBookListFragment(), "Recommendations");

        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (admin()) {
            inflater.inflate(R.menu.one_book_menu, menu);
        }
        return true;
    }

    int USER_EDIT = 97;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (!admin()) {
            Toast.makeText(this, "Sorry you can not change this user", Toast.LENGTH_SHORT).show();
        } else {

            switch (id) {
                case R.id.edit_book:

                    Intent intent = new Intent(this, EditUser.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", user);
                    intent.putExtras(bundle);
                    startActivityForResult(intent, USER_EDIT);

                    break;

                case R.id.delete_book:

                    new AlertDialog.Builder(this, R.style.AlertDialogTheme)
                            .setTitle("Delete User!")
                            .setMessage("Are you sure to delete user: " + user.getInfo())
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @SuppressLint("MissingPermission")
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    mDatabase.child("user_list").child(user.getFirebaseKey()).removeValue();

                                    mDatabase.child("book_list").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            for (DataSnapshot books : dataSnapshot.getChildren()) {
                                                mDatabase.child("book_list").child(books.getKey()).child("reading").child(user.getFirebaseKey()).removeValue();
                                                mDatabase.child("book_list").child(books.getKey()).child("readed").child(user.getFirebaseKey()).removeValue();
                                            }
                                            increaseUserVersion();
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });


                                    StorageReference desertRef = storageReference.child("users").child(user.getImgStorageName());
                                    desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(Profile.this, "User deleted", Toast.LENGTH_SHORT).show();
                                            onBackPressed();
                                            finish();
                                        }

                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d("info", "onFailure: did not delete file");
                                        }
                                    });

                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();

                    break;
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public void increaseUserVersion() {
        mDatabase.child("user_ver").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String version;

                if (dataSnapshot.exists()) {
                    version = dataSnapshot.getValue().toString();
                    long ver = Long.parseLong(version);
                    ver += 1;
                    mDatabase.child("user_ver").setValue(ver);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == USER_EDIT) {
            if (resultCode == Activity.RESULT_OK) {

                Bundle bundle = data.getExtras();
                User us = (User) bundle.getSerializable("edited_user");
                abonement = data.getStringExtra("abonement");
                initializeBundle(bundle, us);

                readingBookListFragment.checkAbone(abonement);

            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the phone call

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public class SimplePageFragmentAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private List<String> titles = new ArrayList<>();

        public SimplePageFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return mFragmentList.get(i);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String one) {
            mFragmentList.add(fragment);
            titles.add(one);
        }
    }
}