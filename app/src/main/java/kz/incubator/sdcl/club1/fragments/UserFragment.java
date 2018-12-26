package kz.incubator.sdcl.club1.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.AddUser;
import kz.incubator.sdcl.club1.activities.Profile;
import kz.incubator.sdcl.club1.activities.fillUsersAsyncTask;
import kz.incubator.sdcl.club1.adapters.UserListAdapter;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.module.Book;
import kz.incubator.sdcl.club1.module.User;

import static kz.incubator.sdcl.club1.MenuActivity.admin;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BCOUNT;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_CARD_NUMBER;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_FKEY;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_ID_NUMBER;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_INFO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHONE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_TYPE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.TABLE_USER;

public class UserFragment extends Fragment {
    DatabaseReference mDatabaseRef, userRef;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager linearLayoutManager;
    ArrayList<User> userListCopy;
    ArrayList<User> userList;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    SearchView searchView;
    View view;
    FloatingActionButton fabBtn;
    ProgressBar progressBar;
    UserListAdapter listAdapter;
    String TABLE_USER = "user_store";
    User user;
    static String now_user;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_list, container, false);

        now_user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        userRef = mDatabaseRef.child("user_list");
        userList = new ArrayList<>();

        initProgressBar();
        setUpRecyclerView();
        initializeSearchView();
        initFab();
        fillUsers();

        listAdapter = new UserListAdapter(getActivity(),userList);
        addListener();

//        manageDate();
//        setRecScrollPos();


        return view;
    }

    public void addListener() {

        /*
        Book b1 = new Book("1", "Vasya", "Ulya", "Desc", 124, 5, 1, "photo", "yes", "q1234");
        Book b2 = new Book("2", "Nikola", "Mawa", "Desc", 324, 4, 2, "photo", "no", "q1235");
        Book b3 = new Book("3", "Yahoo", "Tihonya", "Desc", 424, 3, 1, "photo", "yes", "q1236");

        booksRef.child("1").setValue(b1);
        booksRef.child("2").setValue(b2);
        booksRef.child("3").setValue(b3);
        */

        userRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                storeDb.updateUser(sqdb, user);

                //new BackgroundTaskForUserFill(getActivity(),recyclerView,progressBar).execute();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                /*
                book = dataSnapshot.getValue(Book.class);
                mDatabaseRef.child("user_list").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot bookDataSnapshot: dataSnapshot.getChildren()){
                            String userId = bookDataSnapshot.getKey();
                            mDatabaseRef.child("user_list").child(userId).child("reading").child(book.getFirebaseKey()).removeValue();
                            mDatabaseRef.child("user_list").child(userId).child("readed").child(book.getFirebaseKey()).removeValue();

                            storeDb.deleteBook(sqdb, book);
                            initIncreaseVersion();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                */

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void fillUsers() {
        new BackgroundTaskForUserFill(getActivity(),recyclerView,progressBar).execute();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public void initProgressBar(){
        progressBar = view.findViewById(R.id.ProgressBar);
    }

    public void initFab(){
        fabBtn = view.findViewById(R.id.fabBtn);
        if(!admin()){
            fabBtn.setVisibility(View.GONE);
        }
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddUser.class));
            }
        });
    }

    public void initializeSearchView(){
        searchView = view.findViewById(R.id.searchView);
        userListCopy = new ArrayList<>();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String s) {
                filter(s);
                return false;
            }
        });
    }

    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListCopy);
        } else {
            text = text.toLowerCase();
            for (User item : userListCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toLowerCase().contains(text) ||
                        item.getPhoneNumber().toUpperCase().contains(text)) {
                    userList.add(item);
                }
            }
        }
        recyclerView.setAdapter(listAdapter);
    }

    private void setUpRecyclerView() {
        recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        int resId = R.anim.layout_anim;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutAnimation(animation);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        setupSwipeRefresh();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.profile_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    Dialog dialog;
    EditText card_id;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.search_by_card_menu:
                dialog = new Dialog(getActivity());
                dialog.setContentView(R.layout.dialog_search_by_card);
                card_id = dialog.findViewById(R.id.card_id);
                card_id.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {

                        if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                            String card_number = card_id.getText().toString().toLowerCase();

                            if (card_number.length() > 0) {
                                searchUser(card_number);

                            }
                        }
                        return false;
                    }
                });
                dialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchUser(String card_number) {
        String[] params = new String[]{card_number};
        Cursor userCursor = sqdb.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE card_number=?", params);

        if (userCursor != null && (userCursor.getCount() > 0)) {
            userCursor.moveToNext();
            User user = new User(
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_FKEY)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_ID_NUMBER)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_CARD_NUMBER)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_TYPE)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME)),
                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BCOUNT)));

            String abone = checkAbone(user.getTicket_type());
            dialog.dismiss();

            Intent intent = new Intent(getContext(), Profile.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("user",user);
            bundle.putString("abonement", abone);
            intent.putExtras(bundle);
            getContext().startActivity(intent);

        }else{
            Toast.makeText(getActivity(), "User not found", Toast.LENGTH_SHORT).show();
            card_id.setText("");
        }
    }

    public String checkAbone(String ticketDay) {
        String abone = "norm";

        DateFormat day = new SimpleDateFormat("dd");
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat year = new SimpleDateFormat("yyyy");
        int first_slash = ticketDay.indexOf('/');
        int last_slash = ticketDay.lastIndexOf('/');

        String period_day = ticketDay.substring(0, first_slash);
        String period_month = ticketDay.substring(first_slash + 1, last_slash);
        String period_year = ticketDay.substring(last_slash + 1, ticketDay.length());

        Calendar cal = Calendar.getInstance();

        if (period_year.equals(year.format(cal.getTime()))) {
            if (period_month.equals(month.format(cal.getTime()))) {
                if (period_day.equals(day.format(cal.getTime()))) {
                    abone = "last day";
                } else if (Integer.parseInt(period_day) > Integer.parseInt(day.format(cal.getTime()))) {
                    int counter = Integer.parseInt(period_day) - Integer.parseInt(day.format(cal.getTime()));
                    if (counter <= 7) {
                        abone = counter + " days left";
                    }
                } else {
                    abone = "Your subscription is up";

                }
            } else if (Integer.parseInt(period_month) < Integer.parseInt(month.format(cal.getTime()))) {
                abone = "Your subscription is up";

            }
        } else if (Integer.parseInt(period_year) < Integer.parseInt(year.format(cal.getTime()))) {
            abone = "Your subscription is up";

        }

        return abone;
    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("user_ver");

        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getDayCurrentVersion().equals(newVersion)) {
                        refreshUsers(newVersion);
                    }else{
                        onItemsLoadComplete();
                    }
                }else{
                    Toast.makeText(getActivity(), "Can not find user_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshUsers(String version) {
        new fillUsersAsyncTask(getActivity(),recyclerView, mSwipeRefreshLayout,version).execute();
    }
    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }

    public void incrementUserVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("user_ver");
        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long version = (long) dataSnapshot.getValue();
                version++;
                mDatabaseRef.child("user_ver").setValue(version);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void onItemsLoadComplete() {
        fillUsers();
        mSwipeRefreshLayout.setRefreshing(false);

    }

    public void refreshItems() {

        if (!isOnline()) {
            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_LONG).show();
        } else {
            checkVersion();
        }

    }

    public String getIdNumber() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }

    public boolean isOnline() {
        return true;
    }


    public class BackgroundTaskForUserFill extends AsyncTask<Void,Void,Void> {
        RecyclerView recyclerView;
        ProgressBar progressBar;
        StoreDatabase storeDb;
        SQLiteDatabase sqdb;
        DatabaseReference mDatabaseRef, userRef;
        Context context;

        public BackgroundTaskForUserFill(Context context, RecyclerView recyclerView, ProgressBar progressBar){
            this.recyclerView = recyclerView;
            this.progressBar = progressBar;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            progressBar.setVisibility(View.VISIBLE);
            storeDb = new StoreDatabase(context);
            sqdb = storeDb.getWritableDatabase();
            userRef = mDatabaseRef.child("user_list");
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Cursor userCursor = sqdb.rawQuery("SELECT * FROM " + TABLE_USER, null);
            if (((userCursor != null) && (userCursor.getCount() > 0))) {
                userList.clear();
                while (userCursor.moveToNext()) {
                    userList.add(new User(
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_FKEY)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_ID_NUMBER)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_CARD_NUMBER)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_TYPE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_BCOUNT))
                    ));
                }

                userListCopy = (ArrayList<User>) userList.clone();
                Collections.reverse(userListCopy);
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Collections.reverse(userList);
            recyclerView.setAdapter(listAdapter);
            progressBar.setVisibility(View.GONE);
        }

    }

    /*
    public void setRecScrollPos(){
        int scrollPos = 0;
        for(int i = 0; i < teachers2.size(); i++){
            Teacher teacher = teachers2.get(i);
            String dateSplit[] = teacher.getDate().split(" ");
            if(date.equals(dateSplit[1])){
                scrollPos = i;
                break;
            }
        }
        recyclerView.scrollToPosition(scrollPos);
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }



    public void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }


    public void createDialog() {
        callSms = new Dialog(getActivity());
        callSms.setContentView(R.layout.dialog_call_sms);
        Button call = callSms.findViewById(R.id.btnCall);
        Button sms = callSms.findViewById(R.id.btnSms);
        tImage = callSms.findViewById(R.id.teacherPhoto);
        tName = callSms.findViewById(R.id.teacherName);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));
                startActivity(callIntent);
            }
        });
        sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("smsto:" + phoneNumber);
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO, uri);
                smsIntent.putExtra("sms_body", "Сулейман Демирель мектеп-интернат-колледж");
                startActivity(smsIntent);
            }
        });

    }
    */
}


/*

    public void referenseAddListener(){
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                // A new comment has been added, add it to the displayed list
               Teacher teacher = dataSnapshot.getValue(Teacher.class);

                String fkey = dataSnapshot.getKey();

                ContentValues teacherValue = new ContentValues();
                teacherValue.put(COLUMN_FKEY, fkey);
                teacherValue.put(COLUMN_DATE, teacher.getDate());
                teacherValue.put(COLUMN_INFO, teacher.getInfo());
                teacherValue.put(COLUMN_PHONE, teacher.getPhoneNumber());
                teacherValue.put(COLUMN_PHOTO, teacher.getPhoto());

                sqdb.insert(TABLE_NAME3, null, teacherValue);

               System.out.println("onChildAdded: " + teacher.getInfo());
                // ...
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Teacher changedTeacher = dataSnapshot.getValue(Teacher.class);
                String tkey = dataSnapshot.getKey();

                System.out.println("onChildChanged: " + changedTeacher.getInfo());
                updateTeacherData(tkey, changedTeacher);

//                storeDb.cleanDayTable(sqdb);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                String commentKey = dataSnapshot.getKey();
                System.out.println("onChildRemoved: ");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Teacher movedComment = dataSnapshot.getValue(Teacher.class);
                String commentKey = dataSnapshot.getKey();
                System.out.println("onChildMoved: ");


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                Toast.makeText(getActivity(), "Failed to load comments.",
                        Toast.LENGTH_SHORT).show();
            }
        };

        teacherDutyRef.addChildEventListener(childEventListener);

    }



        mFirebaseAdapter = new FirebaseRecyclerAdapter<Teacher, FirebaseTeacherViewHolder>
                (Teacher.class, R.layout.day_duty_item, FirebaseTeacherViewHolder.class, teacherDutyRef) {

            @Override
            protected void populateViewHolder(FirebaseTeacherViewHolder viewHolder, Teacher teacher, int position) {
                viewHolder.bindTeacher(teacher);

//                ContentValues teacherValue = new ContentValues();
//                teacherValue.put(COLUMN_DATE, teacher.getDate());
//                teacherValue.put(COLUMN_INFO, teacher.getInfo());
//                teacherValue.put(COLUMN_PHONE, teacher.getPhoneNumber());
//                teacherValue.put(COLUMN_PHOTO, teacher.getPhoto());
//
//                sqdb.insert(TABLE_NAME3, null, teacherValue);

                teachers2.add(new Teacher(""+teacher.getDate(), ""+teacher.getInfo(), ""+teacher.getPhoneNumber(), ""+teacher.getPhoto()));

            }

        };
//        fillDayTeachers();
        recyclerView.setAdapter(mFirebaseAdapter);

 */