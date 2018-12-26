package kz.incubator.sdcl.club1;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;

import com.dk.view.folder.ResideMenu;
import com.dk.view.folder.ResideMenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import kz.incubator.sdcl.club1.Authentications.AuthenPage;
import kz.incubator.sdcl.club1.about_us.AboutUsFragment;
import kz.incubator.sdcl.club1.about_us.RuleFragment;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.fragments.BookListFragment;
import kz.incubator.sdcl.club1.fragments.UserFragment;
import kz.incubator.sdcl.club1.module.Book;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    public ResideMenu resideMenu;
    private ResideMenuItem users, book_list, reserve, rules, wishes, about_us, log_out;
    private Toolbar actionToolbar;
    DatabaseReference mDatabaseRef, booksRef;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    BookListFragment bookListFragment;
    RuleFragment ruleFragment;

    Book book;
    FirebaseUser user;
    static String now_user;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout2);
        setUpMenu();
        now_user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        actionToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(actionToolbar);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null){
            finish();
        }

        actionToolbar.setNavigationIcon(R.drawable.ic_home_black_24dp);
        actionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });

        bookListFragment = new BookListFragment();
        ruleFragment = new RuleFragment();

        changeFragment(new UserFragment());

        if (savedInstanceState == null)
            changeFragment(bookListFragment);


        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        booksRef = mDatabaseRef.child("book_list");
        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();

        //addListener();
    }

    public void addListener() {

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
                book = dataSnapshot.getValue(Book.class);
                mDatabaseRef.child("user_list").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot bookDataSnapshot : dataSnapshot.getChildren()) {
                            String userId = bookDataSnapshot.getKey();
                            mDatabaseRef.child("user_list").child(userId).child("reading").child(book.getFirebaseKey()).removeValue();
                            mDatabaseRef.child("user_list").child(userId).child("readed").child(book.getFirebaseKey()).removeValue();
                            storeDb.deleteBook(sqdb, book);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.color.back);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);

        resideMenu.setScaleValue(0.6f);

        users = new ResideMenuItem(this, R.drawable.ic_supervisor_account_black_24dp, "Users");
        book_list = new ResideMenuItem(this, R.drawable.ic_list_black_24dp, "Book list");
        reserve = new ResideMenuItem(this, R.drawable.ic_bookmark_border_black_24dp, "Reserved");

        rules = new ResideMenuItem(this, R.drawable.ic_assignment_black_24dp, "Rules");
        wishes = new ResideMenuItem(this, R.drawable.ic_local_library_black_24dp, "Wishes");
        about_us = new ResideMenuItem(this, R.drawable.ic_info_outline_black_24dp, "About us");
        log_out = new ResideMenuItem(this,R.drawable.ic_exit_to_app_black_24dp,"Sign Out");


        users.setOnClickListener(this);
        book_list.setOnClickListener(this);
        reserve.setOnClickListener(this);

        rules.setOnClickListener(this);
        wishes.setOnClickListener(this);
        about_us.setOnClickListener(this);
        log_out.setOnClickListener(this);

        resideMenu.addMenuItem(users, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(book_list, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(reserve, ResideMenu.DIRECTION_LEFT);

        resideMenu.addMenuItem(rules, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(wishes, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(about_us, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(log_out,ResideMenu.DIRECTION_LEFT);

        // You can disable a direction by setting ->
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
//        resideMenu.setSoundEffectsEnabled(true);


        /*
        findViewById(R.id.title_bar_left_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        */

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    public static boolean admin(){
        if(now_user.equals("admin")){
            return true;
        }
        else{
            return false;
        }
    }

    @Override
    public void onClick(View view) {

        if (view == users) {
            changeFragment(new UserFragment());
            getSupportActionBar().setTitle("Users");
        } else if (view == book_list) {
            changeFragment(bookListFragment);
            getSupportActionBar().setTitle("Books");
        } else if (view == about_us) {
            changeFragment(new AboutUsFragment());
            getSupportActionBar().setTitle("About us");
        } else if (view == rules) {
            changeFragment(ruleFragment);
            getSupportActionBar().setTitle("Rules");
        }
        else if(view == log_out){
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this, AuthenPage.class));
        }
        resideMenu.closeMenu();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
//            Toast.makeText(mContext, "Menu is opened!", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void closeMenu() {
            //Toast.makeText(mContext, "Menu is closed!", Toast.LENGTH_SHORT).show();
        }
    };

    private void changeFragment(Fragment targetFragment) {
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public ResideMenu getResideMenu() {
        return resideMenu;
    }
}
