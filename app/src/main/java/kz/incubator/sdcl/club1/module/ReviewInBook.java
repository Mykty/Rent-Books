package kz.incubator.sdcl.club1.module;


import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class ReviewInBook implements Serializable {
    String fKey;
    String user_id;
    String review_text;
    int user_rate;

    public ReviewInBook() {

    }

    public ReviewInBook(String fKey, String user_id, int user_rate, String review_text){
        this.fKey = fKey;
        this.user_id = user_id;
        this.review_text = review_text;
        this.user_rate = user_rate;
    }

    public int getUser_rate() {
        return user_rate;
    }

    public void setUser_rate(int user_rate) {
        this.user_rate = user_rate;
    }

    public String getfKey() {
        return fKey;
    }

    public void setfKey(String fKey) {
        this.fKey = fKey;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getReview_text() {
        return review_text;
    }

    public void setReview_text(String review_text) {
        this.review_text = review_text;
    }
}
