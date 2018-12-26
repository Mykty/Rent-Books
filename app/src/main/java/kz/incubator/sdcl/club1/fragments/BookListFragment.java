package kz.incubator.sdcl.club1.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.AddBook;
import kz.incubator.sdcl.club1.adapters.BookListAdapter;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.module.Book;

import static kz.incubator.sdcl.club1.MenuActivity.admin;
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

public class BookListFragment extends Fragment{
    RecyclerView bookListRv;//, bookListRv2;
    BookListAdapter bookListAdapter;
    ArrayList<Book> bookList;
    ArrayList<Book> bookListCopy;
    private RecyclerView.LayoutManager linearLayoutManager, gridLayoutManager;
    DatabaseReference mDatabaseRef, booksRef;
    View rootView;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    FloatingActionButton fabBtn;
    SwipeRefreshLayout mSwipeRefreshLayout;
    ProgressBar progressBar;
    Book book;
    SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_book_list, container, false);
        setupViews();
        setupSwipeRefresh();
        checkVersion();

        fillBooks();

        initializeSearchView();
        //doChange();

        return rootView;
    }

    public void doChange(){
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    for (DataSnapshot booksSnapshot : dataSnapshot.getChildren()) {
                        String fKey = booksSnapshot.getKey();
                        booksRef.child(fKey).child("rating").setValue("0,0");
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void initializeSearchView(){
        searchView = rootView.findViewById(R.id.searchView);
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

    public void setupViews() {
        bookListRv = rootView.findViewById(R.id.bookListRv);
        bookList = new ArrayList<>();
        bookListCopy = new ArrayList<>();

        linearLayoutManager = new LinearLayoutManager(getActivity());
        gridLayoutManager = new GridLayoutManager(getActivity(), 2);

        bookListAdapter = new BookListAdapter(getActivity(), bookList);

        int resId = R.anim.layout_anim_book;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        bookListRv.setLayoutManager(linearLayoutManager);
        bookListRv.setItemAnimator(new DefaultItemAnimator());
        bookListRv.setLayoutAnimation(animation);
        bookListRv.setAdapter(bookListAdapter);

/*
        bookListAdapter.onClickListener(new BookListAdapter.onMeClick() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClickThisUser(View v, int position) {
                Intent intent = new Intent(getActivity(), OneBookAcvitiy.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("book", bookList.get(position));
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });*/

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        booksRef = mDatabaseRef.child("book_list");
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        fabBtn = rootView.findViewById(R.id.fabBtn);
        if(!admin()){
            fabBtn.setVisibility(View.GONE);
        }
        fabBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), AddBook.class));
            }
        });

        progressBar = rootView.findViewById(R.id.ProgressBar);
        addListener();
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

        booksRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                book = dataSnapshot.getValue(Book.class);
                storeDb.updateBook(sqdb, book);
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

    public void initIncreaseVersion() {
        mDatabaseRef.child("book_list_ver").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String version;

                if (dataSnapshot.exists()) {
                    version = dataSnapshot.getValue().toString();
                    long ver = Long.parseLong(version);
                    ver += 1;
                    mDatabaseRef.child("book_list_ver").setValue(ver);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("book_list_ver");
        myTopPostsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getCurrentBookVersion().equals(newVersion)) {
                        updateVersion(newVersion);
                        refreshBooks();
                    }else{
                        onItemsLoadComplete();
                    }
                } else {
                    Toast.makeText(getActivity(), "Can not find book_list_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshBooks() {
        booksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    bookList.clear();
                    storeDb.cleanBooks(sqdb);

                    for (DataSnapshot booksSnapshot : dataSnapshot.getChildren()) {

                        Book book = booksSnapshot.getValue(Book.class);

                        String fKey = book.getFirebaseKey();
                        String name = book.getName();
                        String author = book.getAuthor();
                        String desc = book.getDesc();
                        int page_number = book.getPage_number();
                        String rating = book.getRating();
                        int book_count = book.getBookCount();
                        String photo = book.getPhoto();
                        String reserved = book.getReserved();
                        String qr_code = book.getQr_code();
                        String imgStorageName = book.getImgStorageName();

                        ContentValues teacherValue = new ContentValues();
                        teacherValue.put(COLUMN_FKEY, fKey);
                        teacherValue.put(COLUMN_BNAME, name);
                        teacherValue.put(COLUMN_BAUTHOR, author);
                        teacherValue.put(COLUMN_BDESC, desc);
                        teacherValue.put(COLUMN_BPAGE_NUMBER, page_number);
                        teacherValue.put(COLUMN_BRATING, rating);
                        teacherValue.put(COLUMN_BCOUNT, book_count);
                        teacherValue.put(COLUMN_PHOTO, photo);
                        teacherValue.put(COLUMN_BRESERVED, reserved);
                        teacherValue.put(COLUMN_QR_CODE, qr_code);
                        teacherValue.put(COLUMN_IMG_STORAGE_NAME, imgStorageName);

                        sqdb.insert(TABLE_BOOKS, null, teacherValue);
                        bookList.add(book);
                    }

                    bookListAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void updateVersion(String version){
        Cursor res = sqdb.rawQuery("SELECT book_list_ver FROM versions ", null);
        res.moveToNext();
        String verStr = res.getString(0);

        ContentValues versionValues = new ContentValues();
        versionValues.put("book_list_ver", version);
        sqdb.update("versions", versionValues, "book_list_ver=" + verStr, null);

    }

    public void fillBooks() {
        Cursor userCursor = getBooks();
        bookList.clear();

        if (((userCursor != null) && (userCursor.getCount() > 0))) {
            while (userCursor.moveToNext()) {
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
            bookListCopy = (ArrayList<Book>) bookList.clone();
        }
        mSwipeRefreshLayout.setRefreshing(false);
        bookListAdapter.notifyDataSetChanged();
    }

    public Cursor getBooks() {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_BOOKS + " ORDER BY " + COLUMN_BNAME, null);
        return res;
    }

    public void updateCurrentVersion(String newVersion) {
        ContentValues versionValues = new ContentValues();
        versionValues.put("book_list_ver", newVersion);
        sqdb.update("versions", versionValues, "book_list_ver=" + currentBookVersion, null);
    }

    String currentBookVersion;

    public String getCurrentBookVersion() {
        Cursor res = sqdb.rawQuery("SELECT book_list_ver FROM versions ", null);
        res.moveToNext();
        currentBookVersion = res.getString(0);
        return currentBookVersion;
    }

    public void filter(String text) {
        bookList.clear();
        if (text.isEmpty()) {
            bookList.addAll(bookListCopy);

        } else {
            text = text.toLowerCase();
            for (Book item : bookListCopy) {

                if (item.getName().toLowerCase().contains(text) || item.getAuthor().toLowerCase().contains(text) ||
                        item.getName().toUpperCase().contains(text) || item.getAuthor().toUpperCase().contains(text)) {

                    bookList.add(item);
                }
            }
        }

        bookListAdapter.notifyDataSetChanged();

    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = rootView.findViewById(R.id.swipeRefreshLayout);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void refreshItems() {

        if (!isOnline()) {
            Toast.makeText(getActivity(), "Check internet connection", Toast.LENGTH_LONG).show();
        } else {
            checkVersion();
        }

    }

    public void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.book_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    int c = 0;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        c++;

        switch (id) {
            case R.id.change_view:
                if(c%2==1) item.setIcon(getResources().getDrawable(R.drawable.ic_view_list));
                else item.setIcon(getResources().getDrawable(R.drawable.ic_grid_view));

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean isOnline() {
        return true;
    }

}


