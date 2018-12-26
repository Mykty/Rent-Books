package kz.incubator.sdcl.club1.fragments.profile_fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.ArrayList;
import java.util.Arrays;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.activities.AddBook;
import kz.incubator.sdcl.club1.activities.Profile;
import kz.incubator.sdcl.club1.activities.ScannerActivity;
import kz.incubator.sdcl.club1.adapters.UserBookListAdapter;
import kz.incubator.sdcl.club1.database.StoreDatabase;
import kz.incubator.sdcl.club1.interfaces.ItemClickListener;
import kz.incubator.sdcl.club1.module.Book;
import kz.incubator.sdcl.club1.module.ReviewInBook;
import kz.incubator.sdcl.club1.module.ReviewInUser;

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

public class ReadingBookListFragment extends Fragment implements View.OnClickListener, RatingDialogListener {
    ArrayList<Book> bookList;
    UserBookListAdapter bookAdapter;
    RecyclerView recyclerView;
    View view;
    DatabaseReference mDatabase;
    ProgressBar progressBar;
    TextView checkIsEmpty;
    static Button takeBookBtn;
    static String abonement;
    long bCount;
    AlertDialog scanDialog;
    Button submitBtn;
    CardView cardView1, cardView2;
    View alerDialogView;
    TextView changeIt, bName, bAuthor;
    private final int BOOK_QR_SCANNER = 101;
    boolean book_scannered = false;
    Book book;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    String TABLE_BOOKS = "book_store";
    static Activity activity;
    String userId;


    public ReadingBookListFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_reading_book_list, container, false);
        initialize();
        userId = Profile.getId();

        downloadKeys();
        return view;
    }

    public void initialize() {
        recyclerView = view.findViewById(R.id.recyclerForBook);
        takeBookBtn = view.findViewById(R.id.takeBookBtn);

        checkIsEmpty = view.findViewById(R.id.checkIsEmpty);
        activity = getActivity();
        takeBookBtn.setOnClickListener(this);

        bookList = new ArrayList<>();
        bookAdapter = new UserBookListAdapter(getActivity(), bookList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bookAdapter);

        addListener();
        initializeDialog();
        progressBar = view.findViewById(R.id.ProgressBar);

        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();
        abonement = Profile.getAbonement();
        checkAbone(abonement);

        //openRateDialog("vasya");

    }

    public static void checkAbone(String abone) {
        if (abone.charAt(0) == 'Y') {
            takeBookBtn.setVisibility(View.GONE);
            Toast.makeText(activity, "Your subscription is up\nYou can not take a book", Toast.LENGTH_SHORT).show();
        } else {
            takeBookBtn.setVisibility(View.VISIBLE);

//            super.setBook
        }
        if (!admin()) {
            takeBookBtn.setVisibility(View.GONE);
        }
    }

    public void initializeDialog() {

        alerDialogView = getLayoutInflater().inflate(R.layout.take_a_book, null);
        scanDialog = new AlertDialog.Builder(getActivity()).create();

        submitBtn = alerDialogView.findViewById(R.id.submitBtn);
        cardView1 = alerDialogView.findViewById(R.id.cardView1);
        cardView2 = alerDialogView.findViewById(R.id.cardView2);
        changeIt = alerDialogView.findViewById(R.id.changeIt);
        bName = alerDialogView.findViewById(R.id.bName);
        bAuthor = alerDialogView.findViewById(R.id.bAuthor);

        scanDialog.setView(alerDialogView);
        cardView1.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
        scanDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                cardView2.setVisibility(View.GONE);
                changeIt.setText("Scan Book Qr code");
            }
        });
    }

    private void addListener() {
        if (admin()) {
            bookAdapter.setOnItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, final int pos) {
                    LayoutInflater factory = LayoutInflater.from(getActivity());

                    final View deleteDialogView = factory.inflate(R.layout.dialog_for_reading_book, null);
                    final AlertDialog deleteDialog = new AlertDialog.Builder(getActivity()).create();

                    LinearLayout delete = deleteDialogView.findViewById(R.id.Delete);
                    LinearLayout finished = deleteDialogView.findViewById(R.id.Finished);
                    final String bookId = bookList.get(pos).getFirebaseKey();
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                                    .setTitle("Not finished book: " + bookList.get(pos).getName())
                                    .setMessage("Are you sure to delete this book?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @SuppressLint("MissingPermission")
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            mDatabase.child("user_list").child(Profile.getId()).child("reading").child(bookId).removeValue();
                                            increaseBookAmount(bookId);
                                            downloadKeys();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();

                            deleteDialog.dismiss();
                        }
                    });

                    finished.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme)
                                    .setTitle("Finished book: " + bookList.get(pos).getName())
                                    .setMessage("Are you sure that you finished this book?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @SuppressLint("MissingPermission")
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            openRateDialog(bookList.get(pos), bookId);

                                            mDatabase.child("user_list").child(Profile.getId()).child("reading").child(bookId).removeValue();

                                            mDatabase.child("user_list").child(Profile.getId()).child("readed").child(bookId).setValue(1);

                                            mDatabase.child("book_list").child(bookId).child("reading").child(Profile.getId()).removeValue();
                                            mDatabase.child("book_list").child(bookId).child("readed").child(Profile.getId()).setValue(1);

                                            increaseBookAmount(bookId);
                                            downloadKeys();
                                        }
                                    })
                                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    })
                                    .show();

                            deleteDialog.dismiss();
                        }
                    });
                    deleteDialog.setView(deleteDialogView);
                    deleteDialog.show();
                }
            });
        }
        else{
            bookAdapter.setOnItemClickListener(new ItemClickListener() {
                @Override
                public void onItemClick(View v, int pos) {
                    Toast.makeText(getActivity(),bookList.get(pos).getName(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    String bId;
    Book book2;

    public void openRateDialog(Book book, String bookId) {
        book2 = book;
        bId = bookId;

        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(3)
                .setTitle("Rate book: " + book.getName())
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
//                .setDefaultComment("This app is pretty cool !")
                .setStarColor(R.color.starColor)
                .setNoteDescriptionTextColor(R.color.noteDescriptionTextColor)
                .setTitleTextColor(R.color.titleTextColor)
                .setDescriptionTextColor(R.color.hintTextColor)
                .setHint("Please write your comment here ... (250 chars)")
                .setHintTextColor(R.color.hintTextColor)
                .setCommentTextColor(R.color.black)
                .setCommentBackgroundColor(R.color.back_color)
//                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(getActivity())
                .setTargetFragment(this, 15) // only if listener is implemented by fragment
                .show();
    }

    @Override
    public void onPositiveButtonClicked(int rate, String comment) {

        String ratingSplit[] = book2.getRating().split(",");

        int ratingFirstPart = Integer.parseInt(ratingSplit[0]);
        int ratingSecondPart = Integer.parseInt(ratingSplit[1]);
        int increasedSecdPart = ratingSecondPart + 1;

        int lastRate = (ratingFirstPart + rate) / (increasedSecdPart);

        mDatabase.child("book_list").child(bId).child("rating").setValue("" + lastRate + "," + increasedSecdPart);

        if (comment.length() > 0) {
            String fKey = mDatabase.child("book_list").child(bId).child("reviews").push().getKey();

            ReviewInBook review = new ReviewInBook("" + fKey, "" + userId, rate, "" + comment);
            ReviewInUser review2 = new ReviewInUser("" + fKey, "" + bId, rate, "" + comment);

            mDatabase.child("book_list").child(bId).child("reviews").child(fKey).setValue(review);
            mDatabase.child("user_list").child(userId).child("reviews").child(fKey).setValue(review2);
        }

        Toast.makeText(getActivity(), book2.getName() + " rated successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    public void downloadKeys() {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("user_list").child(userId).child("reading").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bookList.clear();
                if (dataSnapshot.exists()) {

                    checkIsEmpty.setVisibility(View.GONE);
                    for (DataSnapshot last : dataSnapshot.getChildren()) {
                        String bookKey = last.getKey();

                        Cursor userCursor = getBookByFKey(bookKey);
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

                } else {
                    checkIsEmpty.setVisibility(View.VISIBLE);
                }

                bookAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.takeBookBtn:

                Intent t = new Intent(getActivity(), ScannerActivity.class);
                startActivityForResult(t, BOOK_QR_SCANNER);

                //scanDialog.show();
                break;

            case R.id.submitBtn:
                if (book_scannered) {
                    int bCount = book.getBookCount();
                    if (bCount == 0) {
                        Toast.makeText(getActivity(), "Please, get back a Book into the club\nTry again, later", Toast.LENGTH_SHORT).show();
                    } else {
                        String bID = book.getFirebaseKey();

                        mDatabase.child("user_list").child(Profile.getId()).child("reading").child(bID).setValue(1);
                        mDatabase.child("book_list").child(bID).child("reading").child(Profile.getId()).setValue(1);

                        decreaseBookAmount(bID);
                        scanDialog.dismiss();
                    }
                } else {
                    Toast.makeText(getActivity(), "Scan Book QR code", Toast.LENGTH_SHORT).show();
                }
//                changeIt.setText("Change Book");
//                cardView2.setVisibility(View.VISIBLE);

                break;

            case R.id.cardView1:

                Intent t1 = new Intent(getActivity(), ScannerActivity.class);
                startActivityForResult(t1, BOOK_QR_SCANNER);

                break;
        }
    }

    String book_qr_code;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BOOK_QR_SCANNER) {
            if (resultCode == Activity.RESULT_OK) {

                Bundle bundle = data.getExtras();
                book_qr_code = (String) bundle.getSerializable("book_qr_code");

                Cursor userCursor = getBookByQrCode(book_qr_code);

                if (userCursor != null && userCursor.getCount() > 0) {
                    userCursor.moveToNext();
                    book = new Book(
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
                    );

                    cardView2.setVisibility(View.VISIBLE);
                    bName.setText(book.getName());
                    bAuthor.setText(book.getAuthor());
                    changeIt.setText("Change Book");
                    scanDialog.show();

                } else {
                    LayoutInflater factory = LayoutInflater.from(getActivity());

                    final View deleteDialogView = factory.inflate(R.layout.dialog_qr_not_found, null);
                    final AlertDialog addDialog = new AlertDialog.Builder(getActivity()).create();

                    Button okBtn = deleteDialogView.findViewById(R.id.okBtn);
                    Button cancelBtn = deleteDialogView.findViewById(R.id.cancelBtn);

                    okBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(getActivity(), AddBook.class);
                            intent.putExtra("bQrCode", book_qr_code);
                            addDialog.dismiss();
                            startActivity(intent);
                        }
                    });

                    cancelBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addDialog.dismiss();
                        }
                    });
                    addDialog.setView(deleteDialogView);
                    addDialog.show();

                    Toast.makeText(getActivity(), "Can not find Book Qr Code", Toast.LENGTH_SHORT).show();
                }

                book_scannered = true;

            }

            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Qr scanner error", Toast.LENGTH_SHORT).show();
                book_scannered = false;
            }
        }
    }

    public Cursor getBookByQrCode(String qr_code) {
        Cursor res = sqdb.rawQuery("SELECT * FROM " + TABLE_BOOKS + " WHERE " +
                COLUMN_QR_CODE + "=?", new String[]{qr_code});
        return res;

    }

    public void increaseBookAmount(final String bookId) {
        mDatabase.child("book_list").child(bookId).child("bookCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    bCount = Long.parseLong(dataSnapshot.getValue().toString());
                    bCount++;
                    mDatabase.child("book_list").child(bookId).child("bookCount").setValue(bCount);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void decreaseBookAmount(final String bookId) {
        mDatabase.child("book_list").child(bookId).child("bookCount").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    bCount = Long.parseLong(dataSnapshot.getValue().toString());
                    bCount--;
                    mDatabase.child("book_list").child(bookId).child("bookCount").setValue(bCount);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}