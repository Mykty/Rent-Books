package kz.incubator.sdcl.club1.fragments.one_book_fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.OneBookAcvitiy;
import kz.incubator.sdcl.club1.activities.Profile;
import kz.incubator.sdcl.club1.adapters.UserBookListAdapter;
import kz.incubator.sdcl.club1.adapters.UserListAdapter;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.fragments.UserFragment;
import kz.incubator.sdcl.club1.module.User;

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

public class AlreadyReadFragment extends Fragment {

    ReadedListAdapter readedListAdapter;
    RecyclerView recyclerView;
    private List<User> userList;
    View view;
    OneBookAcvitiy oneBookAcvitiy;
    DatabaseReference mDatabaseRef;
    String bookId;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    ProgressBar progressBar;
    TextView checkIsEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_already_read, container, false);
        initialize();
        return view;
    }

    public void initialize() {
        recyclerView = view.findViewById(R.id.recyclerForBook);
        userList = new ArrayList<>();
        oneBookAcvitiy = new OneBookAcvitiy();
        bookId = oneBookAcvitiy.getBookId();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        checkIsEmpty = view.findViewById(R.id.checkIsEmpty);
        progressBar = view.findViewById(R.id.ProgressBar);

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("book_list").child(bookId).child("readed");
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    userList.clear();
                    checkIsEmpty.setVisibility(View.GONE);

                    for (DataSnapshot users : dataSnapshot.getChildren()) {
                        String userId = users.getKey();
                        Cursor userCursor = storeDb.getSinlgeEntry(userId);

                        if (((userCursor != null) && (userCursor.getCount() > 0))) {
                            userCursor.moveToNext();
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
                    }

                    readedListAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                } else {
                    checkIsEmpty.setVisibility(View.VISIBLE);
                }

                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        readedListAdapter = new ReadedListAdapter(getActivity(), userList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(readedListAdapter);
    }

    public class ReadedListAdapter extends RecyclerView.Adapter<ReadedListAdapter.MyTViewHolder> {
        private Context context;
        private List<User> userList;

        public class MyTViewHolder extends RecyclerView.ViewHolder {
            public TextView info, phone_number;
            public ImageView person_photo;
            LinearLayout linearUser;

            public MyTViewHolder(View view) {
                super(view);
                person_photo = view.findViewById(R.id.person_photo);
                info = view.findViewById(R.id.info);
                phone_number = view.findViewById(R.id.number);
                linearUser = view.findViewById(R.id.linearUser);
            }
        }

        public ReadedListAdapter(Context context, List<User> userList) {
            this.context = context;
            this.userList = userList;
        }

        @Override
        public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_readed, parent, false);
            return new MyTViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyTViewHolder holder, int position) {
            final User item = userList.get(position);

            Glide.with(context)
                    .load(item.getPhoto())
                    .into(holder.person_photo);

            holder.info.setText(item.getInfo());
            holder.phone_number.setText(item.getPhoneNumber().toString());

            holder.linearUser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String abone = checkAbone(item.getTicket_type());

                    Intent intent = new Intent(getContext(), Profile.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user",item);
                    bundle.putString("abonement", abone);
                    intent.putExtras(bundle);
                    getContext().startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return userList.size();
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
}


