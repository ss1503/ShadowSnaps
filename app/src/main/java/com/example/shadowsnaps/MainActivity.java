package com.example.shadowsnaps;

import static com.example.shadowsnaps.FBref.FBST;
import static com.example.shadowsnaps.FBref.refUsers;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    //components vars
    Spinner spinner;
    TextView selectedUserTv;
    ImageView iv;

    //vars
    ArrayList<String> spinnerList;
    ArrayAdapter<String> adapter;



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
                    spinnerList.add(data.getKey());
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
        final long MAX_BYTES = 4096 * 4096;
        ArrayList<TextTranslate> resList = new ArrayList<>();


        //get the id of the selected user
        String id = spinnerList.get(spinner.getSelectedItemPosition());

        pd = ProgressDialog.show(this, "Downloading image", "Downloading...", true);

        refUsers.child(id).child("translate").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}