package kz.incubator.sdcl.club1.fragments.profile_fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.Profile;
import kz.incubator.sdcl.club1.adapters.RecommendationBookListAdapter;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.module.Book;

import static kz.incubator.sdcl.club1.MenuActivity.isAdmin;


public class RecommendationBookListFragment extends Fragment {
    View view;
    RecyclerView recyclerView;
    RecommendationBookListAdapter adapter;
    ArrayList<Book> bookList = new ArrayList<>();
    Button fab;
    ProgressBar progressBar;
    DatabaseReference mDatabase;
    ArrayList<String> keys = new ArrayList<>();
    StoreDatabase database;
    public RecommendationBookListFragment(){

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_recommendation_book_list, container, false);
        uploadKeys();
        initializeFloatingActionButton();
        return view;
    }

    public void initializeRecyclerView(){
        recyclerView = view.findViewById(R.id.recyclerView);
        Collections.reverse(bookList);
        adapter = new RecommendationBookListAdapter(getActivity(),bookList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);
    }

    public void initializeFloatingActionButton(){
        fab = view.findViewById(R.id.fab);
        if(!isAdmin()){
            fab.setVisibility(View.GONE);
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View alerDialogView = getLayoutInflater().inflate(R.layout.create_recommendation_book,null);
                final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                final TextView author = alerDialogView.findViewById(R.id.AuthorOfBook);
                final TextView name = alerDialogView.findViewById(R.id.BookName);
                Button btn = alerDialogView.findViewById(R.id.addBook);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!author.getText().toString().equals("") && !name.getText().toString().equals("")){
                            Book book = new Book(getIdNumber(),author.getText().toString(),name.getText().toString());
                            mDatabase.child("user_list").child(Profile.getId()).child("recommendations").child(getIdNumber()).setValue(book);
                            alertDialog.dismiss();
                        }
                    }
                });
                alertDialog.setView(alerDialogView);
                alertDialog.show();
            }
        });
    }

    public void uploadKeys() {
        progressBar = view.findViewById(R.id.ProgressBar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_list").child(Profile.getId()).child("recommendations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                keys.clear();
                bookList.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    Book book = data.getValue(Book.class);
                    bookList.add(book);
                }
                initializeRecyclerView();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public String getIdNumber() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }

}
