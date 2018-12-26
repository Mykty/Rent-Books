package kz.incubator.sdcl.club1.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.module.Book;
import kz.incubator.sdcl.club1.module.User;

public class EditUser extends AppCompatActivity {
    Toolbar toolbar;
    CircleImageView putPhoto;
    File file;
    Uri fileUri;
    Button saveUser;
    TextView changeIt;
    DatabaseReference databaseReference;
    String version;
    ProgressBar progressBar;
    TextView expDate;
    private static final int PERMISSION_REQUEST_CODE = 200;
    boolean photoSelected = false;
    StorageReference storageReference;
    String uName, uPhone, cardNumber, tickerPeriod;
    User user;
    String uId;
    Spinner spinner;
    CardView changePhoto;
    String downloadUri;
    String imgStorageName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_user);
        initView();

        initIncreaseVersion();

    }

    EditText nameOfUser, numberOfUser, cardOfUser, bookPNumber;
    String bookCount;
    CircleImageView userPhoto;
    String photoUrl;

    public void initView() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Edit User");
        changeIt = findViewById(R.id.changeIt);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        storageReference = FirebaseStorage.getInstance().getReference();

        file = null;
        fileUri = null;

        progressBar = findViewById(R.id.ProgressBar);
        progressBar.setVisibility(View.GONE);


        changePhoto = findViewById(R.id.takePhoto);

        saveUser = findViewById(R.id.saveUser);
        userPhoto = findViewById(R.id.userPhoto);
        changeIt = findViewById(R.id.changeIt);

        nameOfUser = findViewById(R.id.nameOfUser);
        numberOfUser = findViewById(R.id.numberOfUser);
        cardOfUser = findViewById(R.id.cardOfUser);

        bookPNumber = findViewById(R.id.bookPNumber);

        spinner = findViewById(R.id.spinner);
        expDate = findViewById(R.id.changeText);

        Bundle bundle = getIntent().getExtras();
        user = (User) bundle.getSerializable("user");

        if (bundle != null && user != null) {

            uId = user.getFirebaseKey();
            String uName = user.getInfo();
            String uPhone = user.getPhoneNumber();
            String uCardNumber = user.getCard_number();
            tickerPeriod = user.getTicket_type();
            photoUrl = user.getPhoto();
            imgStorageName = user.getImgStorageName();
            bookCount = user.getBookCount();

            Glide.with(this)
                    .load(photoUrl)
                    .placeholder(R.drawable.user_def)
                    .into(userPhoto);

            nameOfUser.setText(uName);
            numberOfUser.setText(uPhone);
            cardOfUser.setText(uCardNumber);
            expDate.setText("Expiry subscription date: " + tickerPeriod);
        }

        changePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    startTakeImage();
                } else {
                    requestPermission();
                }
            }
        });

        saveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkUserChanges();
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                String take = adapterView.getItemAtPosition(i).toString();

                if (!take.equals("not chosen")) {
                    DateFormat year = new SimpleDateFormat("yyyy");
                    DateFormat day = new SimpleDateFormat("dd");
                    DateFormat month = new SimpleDateFormat("MM");
                    Calendar cal = Calendar.getInstance();


                    int check = Integer.parseInt(month.format(cal.getTime())) + Integer.parseInt(take);

                    String dayStr = day.format(cal.getTime());
                    String yearStr = year.format(cal.getTime());

                    if (check > 12) {
                        yearStr = "" + (Integer.parseInt(yearStr) + 1);
                        check -= 12;
                    }

                    String monthStr = "" + check;
                    if (monthStr.length() == 1) {
                        monthStr = "0" + monthStr;
                    }
                    tickerPeriod = dayStr + "/" + monthStr + "/" + yearStr;

                    expDate.setText("Expiry subscription date: " + dayStr + "/" + monthStr + "/" + yearStr);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void checkUserChanges() {

        boolean uOk = true;

        uName = nameOfUser.getText().toString();
        uPhone = numberOfUser.getText().toString();
        cardNumber = cardOfUser.getText().toString();

        if (uName.trim().equals("")) {
            nameOfUser.setError("Please fill Name");
            uOk = false;
        }

        if (uPhone.trim().equals("")) {
            numberOfUser.setError("Please fill Number ");
            uOk = false;
        }

        if (cardNumber.trim().equals("")) {
            cardOfUser.setError("Please fill Card");
            uOk = false;
        }

        if (uOk) {
            if (photoSelected) {
                uploadImage();
            } else {
                saveUserChanges();
            }
        }
    }

    public void saveUserChanges() {
        User user = new User(uId, uName, uId, cardNumber, photoUrl, uPhone, tickerPeriod, imgStorageName, bookCount);

        databaseReference.child("user_ver").setValue(getIncreasedVersion());
        databaseReference.child("user_list").child(uId).setValue(user);

        saveUser.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        Toast.makeText(EditUser.this, "User saved", Toast.LENGTH_SHORT).show();

        String abone = checkAbone(user.getTicket_type());

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putSerializable("edited_user", user);
        bundle.putString("abonement", abone);
        intent.putExtras(bundle);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    public void startTakeImage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
    }

    private String uploadImage() {
        if (fileUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle(getResources().getString(R.string.photoLoading));
            progressDialog.show();
            imgStorageName = UUID.randomUUID().toString();
            final String photoPath = "users/" + imgStorageName;
            final StorageReference ref = storageReference.child(photoPath);
            ref.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            downloadUri = taskSnapshot.getDownloadUrl().toString();

                            if (downloadUri != null) {

                                saveUser.setVisibility(View.GONE);
                                progressBar.setVisibility(View.VISIBLE);

                                String fKey = getIdNumber();

                                User user = new User(fKey, uName, fKey, cardNumber, downloadUri, uPhone, tickerPeriod, imgStorageName, bookCount);

                                databaseReference.child("user_list").child(user.getFirebaseKey()).setValue(user);
                                databaseReference.child("user_ver").setValue(getIncreasedVersion());

                                Toast.makeText(EditUser.this, "User added", Toast.LENGTH_SHORT).show();

                                String abone = checkAbone(user.getTicket_type());

                                Intent intent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("edited_user", user);
                                bundle.putString("abonement", abone);
                                intent.putExtras(bundle);
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(EditUser.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
        return downloadUri;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                fileUri = result.getUri();
                putPhoto.setImageURI(fileUri);
                putPhoto.setVisibility(View.VISIBLE);
                changeIt.setText("Change Image");

                photoSelected = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
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

    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.CAMERA},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "You have been given permission.Now you can use CAMERA.", Toast.LENGTH_SHORT).show();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel("You need to allow access permissions to take user image",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(EditUser.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void initIncreaseVersion() {
        databaseReference.child("user_ver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                version = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public int getIncreasedVersion() {
        int ver = Integer.parseInt(version);
        ver += 1;
        return ver;
    }


    public String getIdNumber() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }
}
