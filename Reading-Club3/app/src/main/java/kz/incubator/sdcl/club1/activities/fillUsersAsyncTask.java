package kz.incubator.sdcl.club1.activities;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import kz.incubator.sdcl.club1.adapters.UserListAdapter;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.module.User;

import static kz.incubator.sdcl.club1.MenuActivity.setTitle;
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

public class fillUsersAsyncTask extends AsyncTask<Void, User, Void> {

    ArrayList<User> userList = new ArrayList<>();
    RecyclerView recyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    DatabaseReference mDatabaseRef, userRef;
    UserListAdapter listAdapter;
    Context context;
    String version = "";

    public fillUsersAsyncTask(Context context, RecyclerView recyclerView, SwipeRefreshLayout refreshLayout, String version) {
        this.recyclerView = recyclerView;
        this.swipeRefreshLayout = refreshLayout;
        this.context = context;
        this.version = version;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        swipeRefreshLayout.setRefreshing(true);
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(context);
        sqdb = storeDb.getWritableDatabase();
        userRef = mDatabaseRef.child("user_list");
    }

    @Override
    protected Void doInBackground(Void... voids) {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        String getDay = res.getString(0);

        ContentValues versionValues = new ContentValues();
        versionValues.put("user_ver", version);
        sqdb.update("versions", versionValues, "user_ver=" + getDay, null);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                storeDb.cleanUsers(sqdb);

                for (DataSnapshot usersSnapshot : dataSnapshot.getChildren()) {
                    if (!usersSnapshot.getKey().equals("reading")) {
                        User user = usersSnapshot.getValue(User.class);
                        Log.i("user_info", user.getInfo());

                        String fKey = usersSnapshot.getKey();
                        String info = user.getInfo();
                        String id_number = user.getId_number();
                        String card_number = user.getCard_number();
                        String photo = user.getPhoto();
                        String phoneNumber = user.getPhoneNumber();
                        String ticket_type = user.getTicket_type();
                        String imgStorageName = user.getImgStorageName();
                        int bookCount = user.getBookCount();

                        ContentValues teacherValue = new ContentValues();
                        teacherValue.put(COLUMN_FKEY, fKey);
                        teacherValue.put(COLUMN_INFO, info);
                        teacherValue.put(COLUMN_ID_NUMBER, id_number);
                        teacherValue.put(COLUMN_CARD_NUMBER, card_number);
                        teacherValue.put(COLUMN_PHOTO, photo);
                        teacherValue.put(COLUMN_PHONE, phoneNumber);
                        teacherValue.put(COLUMN_TYPE, ticket_type);
                        teacherValue.put(COLUMN_IMG_STORAGE_NAME, imgStorageName);
                        teacherValue.put(COLUMN_BCOUNT, bookCount);

                        sqdb.insert(TABLE_USER, null, teacherValue);
                        userList.add(new User(fKey, info, id_number, card_number, photo, phoneNumber, ticket_type, imgStorageName, bookCount));
                    }
                }
                Collections.reverse(userList);
                listAdapter = new UserListAdapter(context, userList);
                recyclerView.setAdapter(listAdapter);

                setTitle("Users "+userList.size());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return null;
    }

    @Override
    protected void onProgressUpdate(User... values) {
        super.onProgressUpdate(values);
        userList.add(values[0]);
        listAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        swipeRefreshLayout.setRefreshing(false);
    }
}
