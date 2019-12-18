package kz.incubator.sdcl.club1.module;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class User2 implements Serializable {
    String firebaseKey;
    String id_number;
    String card_number;
    String info;
    String photo;
    String ticket_type;
    String phoneNumber;
    String imgStorageName;
    int bookCount;

    public User2() {

    }

    public User2(String firebaseKey, String info, String id_number, String card_number, String photo, String phoneNumber, String ticket_type, String imgStorageName, int bookCount){
        this.firebaseKey = firebaseKey;
        this.id_number = id_number;
        this.card_number = card_number;
        this.info = info;
        this.photo = photo;
        this.phoneNumber = phoneNumber;
        this.ticket_type = ticket_type;
        this.imgStorageName = imgStorageName;
        this.bookCount = bookCount;
    }

    public int getBookCount() {
        return bookCount;
    }

    public void setBookCount(int bookCount) {
        this.bookCount = bookCount;
    }

    public String getImgStorageName() {
        return imgStorageName;
    }

    public void setImgStorageName(String imgStorageName) {
        this.imgStorageName = imgStorageName;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }

    public String getTicket_type() {
        return ticket_type;
    }

    public void setTicket_type(String ticket_type) {
        this.ticket_type = ticket_type;
    }

    public String getId_number() {
        return id_number;
    }

    public void setId_number(String id_number) {
        this.id_number = id_number;
    }


    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

}
