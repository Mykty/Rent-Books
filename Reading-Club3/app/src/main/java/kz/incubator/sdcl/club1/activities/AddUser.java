package kz.incubator.sdcl.club1.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.module.User;

public class AddUser extends AppCompatActivity {
    Toolbar toolbar;
    CardView cardView;
    CircleImageView putPhoto;
    File file;
    Uri fileUri;
    Button addUser;
    TextView changeIt;
    EditText nameOfUser;
    EditText numberOfUser;
    EditText cardOfUser;
    DatabaseReference databaseReference;
    String version;
    ProgressBar progressBar;
    TextView changeText;
    private static final int PERMISSION_REQUEST_CODE = 200;
    boolean photoSelected = false;
    StorageReference storageReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_user);
        initToolbar();
        takePhoto();
        initialzeStorage();
        addUser();
        initIncreaseVersion();
        loadSpinner();
    }

    public void initialzeStorage() {
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add User");
        changeIt = findViewById(R.id.changeIt);
    }

    public void takePhoto() {
        file = null;
        fileUri = null;
        cardView = findViewById(R.id.takePhoto);
        putPhoto = findViewById(R.id.putPhoto);
        putPhoto.setVisibility(View.GONE);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermission()) {
                    startTakeImage();
                } else {
                    requestPermission();
                }
            }
        });
    }

    public void startTakeImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
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

    String uName, uPhone, cardNumber, tickerPeriod;
    public void addUser() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
        addUser = findViewById(R.id.addUSer);
        nameOfUser = findViewById(R.id.nameOfUser);
        numberOfUser = findViewById(R.id.numberOfUser);
        cardOfUser = findViewById(R.id.cardOfUser);
        progressBar = findViewById(R.id.ProgressBar);
        progressBar.setVisibility(View.GONE);
        addUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean uOk = true;

                uName = nameOfUser.getText().toString();
                uPhone = numberOfUser.getText().toString();
                cardNumber = cardOfUser.getText().toString();
//                tickerPeriod = changeText.getText().toString();

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
                if(!photoSelected){
                    changeIt.setError("Please select book image");
                    uOk = false;
                }

                if (uOk) {
                    uploadImage();
                    /*
                    addUser.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                   // imageUrl = "https://firebasestorage.googleapis.com/v0/b/reading-club-e69cc.appspot.com/o/user_images%2F1543054311214.jpg?alt=media&token=952e2dab-8bd6-4a93-a1f9-6cf6dcdf7194";

                    HashMap<String, String> users = new HashMap<>();
                    users.put("card_number", cardOfUser.getText().toString());
                    users.put("id_number", getIdNumber());
                    users.put("info", nameOfUser.getText().toString());
                    users.put("phoneNumber", numberOfUser.getText().toString());
                    users.put("photo", imageUrl);
                    users.put("ticket_type", changeText.getText().toString());
*/
//                    public User(String firebaseKey, String info,  String id_number,   String card_number, String photo, String phoneNumber, String ticket_type){

                }
            }
        });
    }

    String downloadUri, imgStorageName;

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

                                addUser.setVisibility(View.GONE);
                                progressBar.setVisibility(View.VISIBLE);

                                String fKey = getIdNumber();

                                User user = new User(fKey, uName, fKey, cardNumber, downloadUri, uPhone, tickerPeriod, imgStorageName, 0);

                                databaseReference.child("user_list").child(user.getFirebaseKey()).setValue(user);
                                databaseReference.child("user_ver").setValue(getIncreasedVersion());

                                Toast.makeText(AddUser.this, "User added", Toast.LENGTH_SHORT).show();

                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddUser.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
        new AlertDialog.Builder(AddUser.this)
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

    public void loadSpinner() {
        Spinner spinner = findViewById(R.id.spinner);
        changeText = findViewById(R.id.changeText);


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String take = adapterView.getItemAtPosition(i).toString();

                DateFormat year = new SimpleDateFormat("yyyy");
                DateFormat day = new SimpleDateFormat("dd");
                DateFormat month = new SimpleDateFormat("MM");
                Calendar cal = Calendar.getInstance();


                int check = Integer.parseInt(month.format(cal.getTime())) + Integer.parseInt(take);

                String dayStr = day.format(cal.getTime());
                String yearStr = year.format(cal.getTime());

                if (check > 12) {
                    yearStr = ""+(Integer.parseInt(yearStr)+1);
                    check -= 12;
                }

                String monthStr = ""+check;
                if(monthStr.length()==1){
                    monthStr = "0"+monthStr;
                }
                tickerPeriod = dayStr+"/"+monthStr+"/"+yearStr;
                changeText.setText("Expiry subscription date: "+tickerPeriod);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

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
