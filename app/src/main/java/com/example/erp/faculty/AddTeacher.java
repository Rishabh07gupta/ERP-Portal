package com.example.erp.faculty;

import static android.provider.MediaStore.Images.Media.insertImage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
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
import java.io.IOException;

public class AddTeacher extends AppCompatActivity {

    private ImageView addTeacherImage;
    private TextView addTeacherName, addTeacherEmail, addTeacherPost;
    private Spinner addTeacherCategory;
    private Button addTeacherBtn;
    private final int REQ = 1;
    private Bitmap bitmap = null;
    private String category;
    private String name, email, post, downloadurl="";
    private ProgressDialog pd;
    private StorageReference storageReference;
    private DatabaseReference reference, dbRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_teacher);

        addTeacherImage = findViewById(R.id.addTeacherImage);
        addTeacherName = findViewById(R.id.addTeacherName);
        addTeacherEmail = findViewById(R.id.addTeacherEmail);
        addTeacherPost = findViewById(R.id.addTeacherPost);
        addTeacherCategory = findViewById(R.id.addTeacherCategory);
        addTeacherBtn = findViewById(R.id.addTeacherBtn);
        pd = new ProgressDialog(this);

        reference = FirebaseDatabase.getInstance("https://imsec-489a0-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("teacher");
        storageReference = FirebaseStorage.getInstance().getReference();

        String[] items = new String[]{"Select Category", "Computer Science", "Mechanical","Information Technology", "ECE", "Other Branches"};
        addTeacherCategory.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,items));

        addTeacherCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                category = addTeacherCategory.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        addTeacherImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                openGallery();

            }
        });

        addTeacherBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });
    }

    private void checkValidation() {
        name = addTeacherName.getText().toString();
        email = addTeacherEmail.getText().toString();
        post = addTeacherPost.getText().toString();
        
        if(name.isEmpty()){
            addTeacherName.setError("Error");
            addTeacherName.requestFocus();
        }else if(email.isEmpty()){
            addTeacherEmail.setError("Error");
            addTeacherEmail.requestFocus();
        }else if(post.isEmpty()){
            addTeacherPost.setError("Error");
            addTeacherPost.requestFocus();
        }else if(category.equals("Select Category")){
            Toast.makeText(this, "Please provide teacher category", Toast.LENGTH_SHORT).show();
        }else if(bitmap==null){
            insertdata();
        }else {
            pd.setMessage("Uploading...");
            pd.show();
            uploadImage();
        }

    }

    private void insertdata() {
        dbRef = reference.child(category);
        final String Uniquekey = dbRef.push().getKey();

        TeacherData teacherData = new TeacherData(name, email, post, downloadurl, Uniquekey);
        dbRef.child(Uniquekey).setValue(teacherData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                pd.dismiss();
                Toast.makeText(AddTeacher.this, "Teacher Added", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                pd.dismiss();
                Toast.makeText(AddTeacher.this, "SomeThing Went Wrong", Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void uploadImage() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] finalimage = baos.toByteArray();
        final StorageReference FilePath;
        FilePath = storageReference.child("Teachers").child(finalimage + "jpg");
        final UploadTask uploadTask = FilePath.putBytes(finalimage);
        uploadTask.addOnCompleteListener(AddTeacher.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                                    insertdata();
                                }
                            });
                        }
                    });

                } else {
                    pd.dismiss();
                    Toast.makeText(AddTeacher.this, "SomeThing Went wrong.", Toast.LENGTH_SHORT).show();
                }
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
            addTeacherImage.setImageBitmap(bitmap);

        }
    }
}