package kz.incubator.sdcl.club1.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.Profile;
import kz.incubator.sdcl.club1.interfaces.ItemClickListener;
import kz.incubator.sdcl.club1.module.User;

public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.MyTViewHolder>{
    private Context context;
    public ArrayList<User> userList;

    DateFormat dateF;
    String date;
    String number;

    public static class MyTViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView ticket_type, info, bookCount;
        ImageView person_photo;
        ItemClickListener clickListener;

        public MyTViewHolder(View view) {
            super(view);
            person_photo = view.findViewById(R.id.person_photo);
            ticket_type = view.findViewById(R.id.ticket_type);
            info = view.findViewById(R.id.info);
            bookCount = view.findViewById(R.id.bookCount);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            this.clickListener.onItemClick(view,getLayoutPosition());
        }

        public void setOnClick(ItemClickListener clickListener){
            this.clickListener = clickListener;
        }
    }
    public UserListAdapter(Context context, ArrayList<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        manageDate();
        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyTViewHolder holder, int position){
        User item = userList.get(position);
        Glide.with(context)
                .load(item.getPhoto())
                .into(holder.person_photo);

        holder.ticket_type.setText(item.getTicket_type().toString());
        holder.info.setText(item.getInfo());
        holder.bookCount.setText("Books: "+item.getBookCount());

        number = holder.bookCount.getText().toString();

        holder.setOnClick(new ItemClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onItemClick(View v, int pos) {

                if(isOnline()) {
                    Intent intent = new Intent(v.getContext(), Profile.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("user", userList.get(pos));
                    bundle.putString("abonement", holder.ticket_type.getText().toString());
                    intent.putExtras(bundle);
                    v.getContext().startActivity(intent);
                }

            }

        });

        String ticketDay = item.getTicket_type().toString();
        DateFormat day = new SimpleDateFormat("dd");
        DateFormat month = new SimpleDateFormat("MM");
        DateFormat year = new SimpleDateFormat("yyyy");
        int first_slash = ticketDay.indexOf('/');
        int last_slash = ticketDay.lastIndexOf('/');

        String period_day = ticketDay.substring(0,first_slash);
        String period_month = ticketDay.substring(first_slash + 1,last_slash);
        String period_year = ticketDay.substring(last_slash+1,ticketDay.length());

        Calendar cal = Calendar.getInstance();
        holder.ticket_type.setTextColor(context.getResources().getColor(R.color.bronze2));

        if(period_year.equals(year.format(cal.getTime()))){
            if (period_month.equals(month.format(cal.getTime()))){
                if(period_day.equals(day.format(cal.getTime()))){
                    holder.ticket_type.setTextColor(context.getResources().getColor(R.color.orange));
                    holder.ticket_type.setText("last day");
                }
                else if(Integer.parseInt(period_day) > Integer.parseInt(day.format(cal.getTime()))){
                    int counter = Integer.parseInt(period_day) - Integer.parseInt(day.format(cal.getTime()));
                    if(counter <= 7){
                        holder.ticket_type.setTextColor(context.getResources().getColor(R.color.orange));
                        holder.ticket_type.setText(counter + " days left");
//                        holder.itemView.setBackgroundResource(R.color.opened_back);
                    }
                }
                else{
                    holder.ticket_type.setTextColor(Color.RED);
                    holder.ticket_type.setText("Your subscription is up");

                }
            }
            else if(Integer.parseInt(period_month) < Integer.parseInt(month.format(cal.getTime()))){
                holder.ticket_type.setTextColor(Color.RED);
                holder.ticket_type.setText("Your subscription is up");

            }
        }
        else if (Integer.parseInt(period_year) < Integer.parseInt(year.format(cal.getTime()))){
            holder.ticket_type.setTextColor(Color.RED);
            holder.ticket_type.setText("Your subscription is up");

        }
    }

    private boolean isOnline() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(context, context.getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

}