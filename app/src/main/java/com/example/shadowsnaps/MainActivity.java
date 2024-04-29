package com.example.shadowsnaps;

import static com.example.shadowsnaps.FBref.FBST;
import static com.example.shadowsnaps.FBref.refUsers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //components vars
    Spinner spinner;
    TextView selectedUserTv;
    ImageView iv;

    //vars
    ArrayList<String> spinnerList;
    ArrayList<String> idList;
    ArrayAdapter<String> adapter;

    private static final long MAX_BYTES = 4096 * 4096;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //init components
        spinner = (Spinner) findViewById(R.id.usersSp);
        selectedUserTv = (TextView) findViewById(R.id.uesrSelectedTv);
        iv = (ImageView) findViewById(R.id.resultIv);

        //init vars
        spinnerList = new ArrayList<>();
        idList = new ArrayList<>();
        spinnerList.add("Select User");
        adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, spinnerList);


        //make a listener to the spinner
        spinner.setOnItemSelectedListener(this);

        //get all users
        getUsers();
        spinner.setAdapter(adapter);

    }

    private void getUsers()
    {
        refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot data : snapshot.getChildren())
                {
                    Users user = data.getValue(Users.class);
                    spinnerList.add(user.getName());
                    idList.add(user.getUserId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedUserTv.setText("Selected: " +  spinnerList.get(position));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }


    public void getLastImageScanned(View view)
    {
        final ProgressDialog pd;
        ArrayList<TextTranslate> resList = new ArrayList<>();


        //get the id of the selected user
        String id = idList.get(spinner.getSelectedItemPosition() - 1); //minus one because of the "Select user"

        pd = ProgressDialog.show(this, "Downloading image", "Downloading...", true);


        refUsers.child(id).child("translate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists())
                {

                    //getting date of last image scan
                    String lastImageDate = "";

                    for(DataSnapshot data : snapshot.getChildren())
                    {
                        resList.add(data.getValue(TextTranslate.class));
                    }

                    lastImageDate = resList.get(resList.size() - 1).getDate(); //get the date of the  last image int the array

                    //download image from user
                    String path = "scan_images/" + id + "/image_" + lastImageDate + ".jpg";
                    StorageReference storageReference = FBST.getReference().child(path);

                    storageReference.getBytes(MAX_BYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes)
                        {
                            //succeeded downloading image
                            pd.dismiss();

                            //convert byte to bitmap
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            iv.setImageBitmap(bitmap);

                            Toast.makeText(MainActivity.this, "downloaded image successfully", Toast.LENGTH_SHORT).show();
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            //failed downloading image
                            pd.dismiss();
                            Toast.makeText(MainActivity.this, "Failed due to: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else
                {
                    pd.dismiss();
                    Toast.makeText(MainActivity.this, "No image scanned yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


    }

    public void capture(View view)
    {
        //find the right user for capturing
        final ProgressDialog pd;
        refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Users user = null;
                for(DataSnapshot data : snapshot.getChildren())
                {
                    user = data.getValue(Users.class);
                    if(user.getName().equals(spinner.getSelectedItem().toString())) //getting the right users info
                        break;
                }

                if (user != null)
                {
                    user.setToCapture(1);
                    refUsers.child(user.getUserId()).setValue(user);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        //check if the flas has been rested
        pd = ProgressDialog.show(this, "Downloading image", "Downloading...", true);
        refUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                Users user = null;
                for(DataSnapshot data : snapshot.getChildren())
                {
                    user = data.getValue(Users.class);
                    if(user.getName().equals(spinner.getSelectedItem().toString()))
                        break;
                }

                if(user != null)
                {
                    //TODO::download the image from storage
                    ArrayList<StorageReference> images = new ArrayList<>();

                    String id = idList.get(spinner.getSelectedItemPosition() - 1);
                    String pathToDirectory = "secret_images/" + id + "/";
                    StorageReference storageReference = FBST.getReference().child(pathToDirectory);


                    try {
                        Thread.sleep(7500);//wait till the image is dull uploaded
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                        @Override
                        public void onSuccess(ListResult listResult)
                        {
                            //images.addAll(listResult.getItems()); //getting all the images into the ArrayList
                            for(StorageReference item: listResult.getItems())
                            {
                                images.add(item);
                            }

                            if(!images.isEmpty())
                            {
                                StorageReference lastImage = images.get(images.size() - 1);

                                lastImage.getBytes(MAX_BYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes)
                                    {
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        iv.setImageBitmap(bitmap);

                                        pd.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        pd.dismiss();
                                        Log.e("Error downloading", "Couldnt download image");
                                    }
                                });
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Log.e("Error getting all images", Objects.requireNonNull(e.getMessage()));
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
}

















