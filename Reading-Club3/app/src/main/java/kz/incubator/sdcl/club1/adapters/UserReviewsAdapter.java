package kz.incubator.sdcl.club1.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.Profile;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.module.ReviewInBook;
import kz.incubator.sdcl.club1.module.User;

import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BCOUNT;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_BNAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_CARD_NUMBER;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_FKEY;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_ID_NUMBER;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_IMG_STORAGE_NAME;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_INFO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHONE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_PHOTO;
import static kz.incubator.sdcl.club1.database.StoreDatabase.COLUMN_TYPE;
import static kz.incubator.sdcl.club1.database.StoreDatabase.TABLE_USER;

public class UserReviewsAdapter extends RecyclerView.Adapter<UserReviewsAdapter.MyTViewHolder> {
    private Context context;
    private List<ReviewInBook> userList;
    DateFormat dateF;
    String date;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;

    public class MyTViewHolder extends RecyclerView.ViewHolder{
        CircleImageView person_photo;
        TextView userName;
        TextView review_text;
        RatingBar bookRating;
        LinearLayout userLinear;

        public MyTViewHolder(View view) {
            super(view);
            person_photo = view.findViewById(R.id.person_photo);
            userName = view.findViewById(R.id.userName);
            review_text = view.findViewById(R.id.review_text);
            bookRating = view.findViewById(R.id.bookRating);
            userLinear = view.findViewById(R.id.userLinear);
        }
    }

    public UserReviewsAdapter(Context context, List<ReviewInBook> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review2, parent, false);
        manageDate();

        storeDb = new StoreDatabase(context);
        sqdb = storeDb.getWritableDatabase();

        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {
        ReviewInBook item = userList.get(position);

        String userKey = item.getUser_id();
        String userName;
        String userPhoto;
        int bookRating = item.getUser_rate();

        final Cursor userCursor = getUserByFKey(userKey);
        if (userCursor != null && userCursor.getCount() > 0) {
            userCursor.moveToNext();

            userName = userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO));
            userPhoto = userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO));


            holder.userName.setText(userName);
            Glide.with(context)
                    .load(userPhoto)
                    .placeholder(R.drawable.user_def)
                    .into(holder.person_photo);

            holder.review_text.setText("\""+item.getReview_text()+"\"");
            holder.bookRating.setRating(bookRating);

            holder.userLinear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    User user = new User(
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_FKEY)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_ID_NUMBER)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_CARD_NUMBER)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_TYPE)),
                            userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME)),
                            userCursor.getInt(userCursor.getColumnIndex(COLUMN_BCOUNT)));

                    String abone = checkAbone(user.getTicket_type());

                    Intent intent = new Intent(context, Profile.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user",user);
                    bundle.putString("abonement", abone);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }
            });
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

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public Cursor getUserByFKey(String fkey) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " +
                COLUMN_FKEY + "=?", new String[]{fkey});
        return res;

    }
}