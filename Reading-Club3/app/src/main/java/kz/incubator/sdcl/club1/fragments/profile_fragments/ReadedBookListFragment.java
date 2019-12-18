package kz.incubator.sdcl.club1.fragments.profile_fragments;


import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.Profile;
import kz.incubator.sdcl.club1.adapters.UserBookListAdapterForReaded;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.interfaces.ItemClickListener;
import kz.incubator.sdcl.club1.module.Book;

import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BAUTHOR;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BCOUNT;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BDESC;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BPAGE_NUMBER;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BRATING;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BRESERVED;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_FKEY;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_QR_CODE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.TABLE_BOOKS;

public class ReadedBookListFragment extends Fragment {
    ArrayList<Book> bookList = new ArrayList<>();
    UserBookListAdapterForReaded bookAdapter;
    RecyclerView recyclerView;
    View view;
    DatabaseReference mDatabase;
    ProgressBar progressBar;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    long bookReadedCount = 0;
    TextView checkIsEmpty;
    Profile profile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_readed_book_list, container, false);
        initialize();
        getReadedBooks();
        return view;
    }

    public void initialize() {
        recyclerView = view.findViewById(R.id.recyclerForBook);
        Collections.reverse(bookList);
        checkIsEmpty = view.findViewById(R.id.checkIsEmpty);
        bookAdapter = new UserBookListAdapterForReaded(getActivity(), bookList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bookAdapter);

        profile = (Profile) getActivity();
        progressBar = view.findViewById(R.id.ProgressBar);
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        bookAdapter.setOnItemClickListener(new ItemClickListener() {
            @Override
            public void onItemClick(View v, final int pos) {

            }
        });
    }

    public void getReadedBooks() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_list").child(Profile.getId()).child("readed").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bookList.clear();
                if(dataSnapshot.exists()) {

                    checkIsEmpty.setVisibility(View.GONE);
                    for (DataSnapshot last : dataSnapshot.getChildren()) {
                        String bKey = last.getKey().toString();
                        bookReadedCount = dataSnapshot.getChildrenCount();

                        Cursor userCursor = getBookByFKey(bKey);
                        if (userCursor != null && userCursor.getCount() > 0) {
                            userCursor.moveToNext();
                            bookList.add(new Book(
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_FKEY)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BNAME)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BAUTHOR)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BDESC)),
                                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_BPAGE_NUMBER)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BRATING)),
                                    userCursor.getInt(userCursor.getColumnIndex(COLUMN_BCOUNT)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_BRESERVED)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_QR_CODE)),
                                    userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME))
                            ));

                        }
                    }

                    mDatabase.child("user_list").child(Profile.getId()).child("bookCount").setValue(bookReadedCount);
                    profile.setBookReadedCount(bookReadedCount);

                }else {

                    checkIsEmpty.setVisibility(View.VISIBLE);
                }

                progressBar.setVisibility(View.GONE);
                bookAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public Cursor getBookByFKey(String fkey) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_BOOKS + " WHERE " +
                COLUMN_FKEY + "=?", new String[]{fkey});
        return res;

    }

}
