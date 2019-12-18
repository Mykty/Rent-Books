package kz.incubator.sdcl.club1.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import kz.incubator.sdcl.club1.fragments.BookListFragment;
import kz.incubator.sdcl.club1.module.Book;
import kz.incubator.sdcl.club1.module.User;

public class StoreDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "reading_club.db";
    private static final int DATABASE_VERSION = 13;

    public static final String TABLE_USER = "user_store";
    public static final String TABLE_BOOKS = "book_store";

    public static final String COLUMN_FKEY = "fkey";
    public static final String COLUMN_PHOTO = "photo";

    public static final String COLUMN_INFO = "info";
    public static final String COLUMN_ID_NUMBER = "id_number";
    public static final String COLUMN_CARD_NUMBER = "card_number";
    public static final String COLUMN_TYPE = "ticket_type";
    public static final String COLUMN_PHONE = "phone_number";

    public static final String TABLE_VER = "versions";
    public static final String COLUMN_USER_VER = "user_ver";
    public static final String COLUMN_BOOK_LIST_VER = "book_list_ver";

    public static final String COLUMN_BNAME = "name";
    public static final String COLUMN_BAUTHOR = "author";
    public static final String COLUMN_BDESC = "description";
    public static final String COLUMN_BPAGE_NUMBER = "page_number";
    public static final String COLUMN_BRATING = "rating";
    public static final String COLUMN_BCOUNT = "book_count";
    public static final String COLUMN_BRESERVED = "reserved";
    public static final String COLUMN_QR_CODE = "qr_code";
    public static final String COLUMN_IMG_STORAGE_NAME = "image_storage_name";


    Context context;

    public StoreDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + TABLE_USER + "(" +
                COLUMN_FKEY + " TEXT, " +
                COLUMN_INFO + " TEXT, " +
                COLUMN_ID_NUMBER + " TEXT, " +
                COLUMN_CARD_NUMBER + " TEXT, " +
                COLUMN_PHOTO + " TEXT, " +
                COLUMN_TYPE + " TEXT , " +
                COLUMN_PHONE + " TEXT , " +
                COLUMN_BCOUNT + " INTEGER , " +
                COLUMN_IMG_STORAGE_NAME + " TEXT )");

        db.execSQL("CREATE TABLE " + TABLE_BOOKS + "(" +
                COLUMN_FKEY + " TEXT, " +
                COLUMN_BNAME + " TEXT, " +
                COLUMN_BAUTHOR + " TEXT, " +
                COLUMN_BDESC + " TEXT, " +
                COLUMN_BPAGE_NUMBER + " INTEGER, " +
                COLUMN_BRATING + " TEXT , " +
                COLUMN_BCOUNT + " INTEGER, " +
                COLUMN_PHOTO + " TEXT," +
                COLUMN_BRESERVED + " TEXT," +
                COLUMN_QR_CODE + " TEXT ," +
                COLUMN_IMG_STORAGE_NAME + " TEXT )");

        db.execSQL("CREATE TABLE " + TABLE_VER + "(" +
                COLUMN_USER_VER + " TEXT, " +
                COLUMN_BOOK_LIST_VER + " TEXT)");

        addVersions(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VER);

        onCreate(db);
    }

    public void cleanUsers(SQLiteDatabase db) {
        db.execSQL("delete from " + TABLE_USER);

    }

    public void cleanBooks(SQLiteDatabase db) {
        db.execSQL("delete from " + TABLE_BOOKS);

    }

    public void cleanVersions(SQLiteDatabase db) {
        db.execSQL("delete from " + TABLE_VER);

    }

    public void addVersions(SQLiteDatabase db) {
        ContentValues versionValues = new ContentValues();
        versionValues.put(COLUMN_USER_VER, "0");
        versionValues.put(COLUMN_BOOK_LIST_VER, "0");

        db.insert(TABLE_VER, null, versionValues);
    }

    public Cursor getSinlgeEntry(String idNumber) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " +
                COLUMN_ID_NUMBER + "=?", new String[]{idNumber});
        return res;

    }

    public void deleteBook(SQLiteDatabase db, Book book) {
        db.delete(TABLE_BOOKS, COLUMN_FKEY + "='" + book.getFirebaseKey()+"'", null);
    }

    public void updateBook(SQLiteDatabase db, Book book) {
        ContentValues updateValues = new ContentValues();

        updateValues.put(COLUMN_BNAME, book.getName());
        updateValues.put(COLUMN_BAUTHOR, book.getAuthor());
        updateValues.put(COLUMN_BDESC, book.getDesc());
        updateValues.put(COLUMN_BPAGE_NUMBER, book.getPage_number());
        updateValues.put(COLUMN_BRATING, book.getRating());
        updateValues.put(COLUMN_BCOUNT, book.getBookCount());
        updateValues.put(COLUMN_PHOTO, book.getPhoto());
        updateValues.put(COLUMN_BRESERVED, book.getReserved());
        updateValues.put(COLUMN_QR_CODE, book.getQr_code());
        updateValues.put(COLUMN_IMG_STORAGE_NAME, book.getImgStorageName());

        db.update(TABLE_BOOKS, updateValues, COLUMN_FKEY + "='" + book.getFirebaseKey()+"'", null);
        Log.i("child", "db: "+book.getName());

    }

    public void updateUser(SQLiteDatabase db, User user) {
        ContentValues updateValues = new ContentValues();

        updateValues.put(COLUMN_INFO, user.getInfo());
        updateValues.put(COLUMN_ID_NUMBER, user.getId_number());
        updateValues.put(COLUMN_CARD_NUMBER, user.getCard_number());
        updateValues.put(COLUMN_PHOTO, user.getPhoto());
        updateValues.put(COLUMN_TYPE, user.getTicket_type());
        updateValues.put(COLUMN_PHONE, user.getPhoneNumber());
        updateValues.put(COLUMN_IMG_STORAGE_NAME, user.getImgStorageName());
        updateValues.put(COLUMN_BCOUNT, user.getBookCount());

        db.update(TABLE_USER, updateValues, COLUMN_FKEY + "='" + user.getFirebaseKey()+"'", null);
        Log.i("child", "db: "+user.getInfo());
    }

}