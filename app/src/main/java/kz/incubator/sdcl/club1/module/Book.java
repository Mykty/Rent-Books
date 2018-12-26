package kz.incubator.sdcl.club1.module;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class Book   implements Serializable {
    String firebaseKey;
    String name;
    String author;
    String desc;
    int page_number;
    String rating;
    int bookCount;
    String photo;
    String reserved;
    String qr_code;
    String imgStorageName;

    public Book(){

    }

    public Book(String idNumber, String author, String name) {
        this.firebaseKey = idNumber;
        this.author = author;
        this.name = name;
    }

    public Book(String firebaseKey, String name, String author, String desc, int page_number, String rating, int bookCount, String photo , String reserved, String qr_code, String imgStorageName){
        this.firebaseKey = firebaseKey;
        this.name = name;
        this.author = author;
        this.desc = desc;
        this.page_number = page_number;
        this.rating = rating;
        this.bookCount = bookCount;
        this.photo = photo;
        this.reserved = reserved;
        this.qr_code = qr_code;
        this.imgStorageName = imgStorageName;
    }

    public String getImgStorageName() {
        return imgStorageName;
    }

    public void setImgStorageName(String imgStorageName) {
        this.imgStorageName = imgStorageName;
    }

    public String getQr_code() {
        return qr_code;
    }

    public void setQr_code(String qr_code) {
        this.qr_code = qr_code;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }

    public String getReserved() {
        return reserved;
    }

    public void setReserved(String reserved) {
        this.reserved = reserved;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getPage_number() {
        return page_number;
    }

    public void setPage_number(int page_number) {
        this.page_number = page_number;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }
}
