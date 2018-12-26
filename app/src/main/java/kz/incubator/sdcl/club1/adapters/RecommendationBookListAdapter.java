package kz.incubator.sdcl.club1.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.interfaces.ItemClickListener;
import kz.incubator.sdcl.club1.module.Book;

public class RecommendationBookListAdapter extends RecyclerView.Adapter<RecommendationBookListAdapter.MyTViewHolder> {
    private Context context;
    private List<Book> bookList;
    DateFormat dateF;
    String date;

    public class MyTViewHolder extends RecyclerView.ViewHolder{
        public TextView info;
        TextView author;
        ImageView notebook;
        public MyTViewHolder(View view) {
            super(view);
            info = view.findViewById(R.id.titleOfBook);
            author = view.findViewById(R.id.author);
            notebook = view.findViewById(R.id.notebook);
            notebook.setImageResource(R.drawable.love);
        }
    }

    public RecommendationBookListAdapter(Context context, List<Book> bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @Override
    public MyTViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.design_of_book, parent, false);
        manageDate();
        return new MyTViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyTViewHolder holder, int position) {
        Book item = bookList.get(position);
        holder.info.setText(item.getName());
        holder.author.setText(item.getAuthor());
    }

    public void manageDate() {
        dateF = new SimpleDateFormat("dd.MM");//2001.07.04
        date = dateF.format(Calendar.getInstance().getTime());
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }

}