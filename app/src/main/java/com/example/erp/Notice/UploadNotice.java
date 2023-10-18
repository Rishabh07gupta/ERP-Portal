package com.example.erp.Notice;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.erp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import java.io.IOException;

public class UploadNotice extends AppCompatActivity {
    private CardView addImage;
    private ImageView NoticeImageView;
    private EditText Noticetitle;
    private Button UploadeNoticeButton;
    private DatabaseReference reference, dbref;
    private StorageReference storageReference;
    private ProgressDialog pd;
    String downloadurl = "";

    private final int REQ = 1;
    private Bitmap bitmap;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_notice);

        addImage = findViewById(R.id.addImage);
        NoticeImageView = findViewById(R.id.NoticeImageView);
        Noticetitle = findViewById(R.id.noticeTitle);
        UploadeNoticeButton = findViewById(R.id.UploadNoticeBtn);
        reference = FirebaseDatabase.getInstance("https://imsec-489a0-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        //reference = FirebaseDatabase(databaseURL:"https://imsec-489a0-default-rtdb.asia-southeast1.firebasedatabase.app/").
        storageReference = FirebaseStorage.getInstance().getReference();
        pd = new ProgressDialog(this);

        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openGallery();

            }
        });

        UploadeNoticeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Noticetitle.getText().toString().isEmpty()) {

                    Noticetitle.setError("Empty Title");
                    Noticetitle.requestFocus();
                } else if (bitmap == null) {
                    UploadData();

                } else {
                    UploadImage();
                }
            }

            private void UploadImage() {
                pd.setMessage("Uploading...");
                pd.show();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] finalimage = baos.toByteArray();
                final StorageReference FilePath;
                FilePath = storageReference.child("Notice").child(finalimage + "jpg");
                final UploadTask uploadTask = FilePath.putBytes(finalimage);
                uploadTask.addOnCompleteListener(UploadNotice.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    FilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            downloadurl = String.valueOf(uri);
                                            UploadData();
                                        }
                                    });
                                }
                            });

                        } else {
                            pd.dismiss();
                            Toast.makeText(UploadNotice.this, "SomeThing Went wrong.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            private void UploadData() {
                dbref = reference.child("Notice");
                final String Uniquekey = dbref.push().getKey();
                String Title = Noticetitle.getText().toString();
                Calendar CalForDate = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("dd-MM-yy");
                String date = currentDate.format(CalForDate.getTime());
                Calendar CalForTime = Calendar.getInstance();
                SimpleDateFormat currentTime =  new SimpleDateFormat("hh:mm a");
                String Time = currentTime.format(CalForTime.getTime());

                NoticeData noticeData = new NoticeData(Title,downloadurl,date,Time,Uniquekey);
                reference.child(Uniquekey).setValue(noticeData).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        pd.dismiss();
                        Toast.makeText(UploadNotice.this, "Notice Uploaded", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(UploadNotice.this, "SomeThing Went Wrong", Toast.LENGTH_SHORT).show();

                    }
                });



            }
        });

    }

    private void openGallery() {

        Intent picImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(picImage, REQ);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ && resultCode == RESULT_OK){
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            NoticeImageView.setImageBitmap(bitmap);

        }
    }
}