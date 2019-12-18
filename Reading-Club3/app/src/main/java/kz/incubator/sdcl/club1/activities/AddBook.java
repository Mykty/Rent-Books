package kz.incubator.sdcl.club1.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import kz.incubator.sdcl.club1.R;
import kz.incubator.sdcl.club1.module.Book;
import kz.incubator.sdcl.club1.module.User;

public class AddBook extends AppCompatActivity {
    Toolbar toolbar;
    CardView cardView;
    private final int CAMERA_REQUEST = 1;
    private final int BOOK_QR_SCANNER = 101;
    StorageReference storageReference;
    ImageView bookImage;
    File file;
    Uri fileUri;
    final int RC_TAKE_PHOTO = 1;
    Button addBook, bookQrCode;
    TextView changeIt;
    EditText bookName, bookAuthor, bookDesc, bookPNumber, bookCount;
    private static final int PERMISSION_REQUEST_CODE = 200;
    DatabaseReference databaseReference;
    String imageUrl;
    String version;
    ProgressBar progressBar;
    File last_file;
    Uri last_file_uri;
    boolean book_scannered = false;
    String book_qr_code = "empty";
    boolean photoSelected = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_book);
        initToolbar();
        takePhoto();
        initialzeStorage();
        addBook();
        initIncreaseVersion();
        Intent t = getIntent();
        if (t.hasExtra("bQrCode")) {
            String bQrCode = t.getStringExtra("bQrCode");
            book_qr_code = bQrCode;
            bookQrCode.setText("Scan code: " + bQrCode);
            book_scannered = true;
        }
    }

    public void initialzeStorage() {
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    public void initToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Add Book");
        changeIt = findViewById(R.id.changeIt);
    }

    public void takePhoto() {
        file = null;
        fileUri = null;
        cardView = findViewById(R.id.takePhoto);
        bookImage = findViewById(R.id.bookImage);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if (checkPermission()) {
                    startTakeImage();
                } else {
                    requestPermission();
                }

//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, RC_TAKE_PHOTO);


            }
        });
    }

    public void startTakeImage() {

        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(this);
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

    public void addBook() {
        databaseReference = FirebaseDatabase.getInstance().getReference();

        addBook = findViewById(R.id.addBook);
        bookQrCode = findViewById(R.id.bookQrCode);

        bookName = findViewById(R.id.bookName);
        bookAuthor = findViewById(R.id.bookAuthor);
        bookDesc = findViewById(R.id.bookDesc);
        bookPNumber = findViewById(R.id.bookPNumber);
        bookCount = findViewById(R.id.bookCount);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        bookQrCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent t = new Intent(AddBook.this, ScannerActivity.class);
                startActivityForResult(t, BOOK_QR_SCANNER);
            }
        });


        addBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean bookFilled = true;
                //book_scannered = true;

                if (bookName.getText().toString().trim().equals("")) {
                    bookName.setError("Please fill name");
                    bookFilled = false;
                }
                if (bookAuthor.getText().toString().trim().equals("")) {
                    bookAuthor.setError("Please fill author ");
                    bookFilled = false;
                }
                if (bookDesc.getText().toString().trim().equals("")) {
                    bookDesc.setError("Please fill description");
                    bookFilled = false;
                }
                if (bookPNumber.getText().toString().trim().equals("")) {
                    bookPNumber.setError("Please fill page number");
                    bookFilled = false;
                }
                if (bookCount.getText().toString().trim().equals("")) {
                    bookCount.setError("Please fill amount of book");
                    bookFilled = false;
                }
                if (!book_scannered) {
                    bookFilled = false;
                    bookQrCode.setError("Please scan book qr code");
                }
                if(!photoSelected){
                    bookFilled = false;
                    changeIt.setError("Please select book image");
                }

                if (bookFilled) {
                    uploadImage();
                }else{
                    Toast.makeText(AddBook.this, "Check errors and try again!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    String downloadUri;

    //check Image added

    private String uploadImage() {
        if (fileUri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);

            progressDialog.setTitle(getResources().getString(R.string.photoLoading));
            progressDialog.show();

            final String imageStorageName = UUID.randomUUID().toString();
            final String photoPath = "book_images/" + imageStorageName;
            final StorageReference ref = storageReference.child(photoPath);
            ref.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            downloadUri = taskSnapshot.getDownloadUrl().toString();

                            if (downloadUri != null) {

                                databaseReference.child("book_list_ver").setValue(getIncreasedVersion());

                                String fkey = getFId();
                                String bName = bookName.getText().toString();
                                String bAuthor = bookAuthor.getText().toString();
                                String bDesc = bookDesc.getText().toString();
                                int bPNumber = Integer.parseInt(bookPNumber.getText().toString());
                                int bCount = Integer.parseInt(bookCount.getText().toString());

                                //imageUrl = "url";

                                Book b = new Book(fkey, bName, bAuthor, bDesc, bPNumber, "0,0", bCount, downloadUri, "no", book_qr_code, imageStorageName);

                                databaseReference.child("book_list").child(fkey).setValue(b);

                                addBook.setVisibility(View.GONE);
                                progressBar.setVisibility(View.VISIBLE);
                                Toast.makeText(AddBook.this, "Book added", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(AddBook.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }else{
            Toast.makeText(this, "Error with book image, try again!", Toast.LENGTH_SHORT).show();
        }
        return downloadUri;
    }

    public void initIncreaseVersion() {
        databaseReference.child("book_list_ver").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                version = dataSnapshot.getValue().toString();

                if (dataSnapshot.exists()) {
                    version = dataSnapshot.getValue().toString();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public long getIncreasedVersion() {
        long ver = Long.parseLong(version);
        ver += 1;
        return ver;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == BOOK_QR_SCANNER) {
            if (resultCode == Activity.RESULT_OK) {
                Bundle bundle = data.getExtras();
                book_qr_code = (String) bundle.getSerializable("book_qr_code");
                bookQrCode.setText("Scan code: " + book_qr_code);
                bookQrCode.setError(null);

                book_scannered = true;

            }

            if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Qr scanner error", Toast.LENGTH_SHORT).show();
                book_scannered = false;
            }

        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                fileUri = result.getUri();
                bookImage.setImageURI(fileUri);
                changeIt.setText("Change Image");
                changeIt.setError(null);

                photoSelected = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

        if (requestCode == 10) {
            bookImage.setImageURI(last_file_uri);
        }

        /*
        if (requestCode == 10) {
            bookImage.setImageURI(last_file_uri);

//          intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            bookImage.setVisibility(View.VISIBLE);
            bookImage.setImageURI(fileUri);
            changeIt.setText("Change Image");
        }*/

    }

    public String getFId() {
        Date date = new Date();
        String idN = "i" + date.getTime();
        return idN;
    }
}
