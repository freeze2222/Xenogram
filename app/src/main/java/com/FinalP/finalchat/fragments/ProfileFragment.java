package com.FinalP.finalchat.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.Callback;
import com.FinalP.finalchat.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;

public class ProfileFragment extends Fragment {
    EditText name;
    EditText surname;
    Button galleryButton;
    ImageView avatar;
    Bitmap bitmap;
    String currentUserEmail=DatabaseService.reformString(FirebaseAuth.getInstance().getCurrentUser().getEmail());
    String bitmapAvatarDB;
    Button confirmChanges;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_profile, container, false);
        name=rootView.findViewById(R.id.newName);
        surname=rootView.findViewById(R.id.newSurName);
        avatar=rootView.findViewById(R.id.avatar);
        galleryButton=rootView.findViewById(R.id.galleryButton);
        confirmChanges=rootView.findViewById(R.id.confirmChanges);
        final String[] userName = new String[1];
        final String[] userSurname = new String[1];
        DatabaseService.getUser(currentUserEmail, new SimpleListener<User>() {
            @Override
            public void onValue(User value) {
                bitmapAvatarDB=value.avatar;
                if (bitmapAvatarDB.equals("Default")){
                    avatar.setImageResource(R.drawable.alien_without_text);
                }
                else {
                    avatar.setImageBitmap(DatabaseService.StringToBitMap(bitmapAvatarDB));
                }
            }
        });
        DatabaseService.getUser(currentUserEmail, new SimpleListener<User>() {
            @Override
            public void onValue(User value) {
                String[] array=value.name.split(" ");
                if (array.length>1){
                    name.setText(array[0]);
                    surname.setText(array[1]);
                }
                else {
                    name.setText(array[0]);
                }
            }
        });
        confirmChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String nameUser=name.getText().toString();
                String surnameUser=surname.getText().toString();
                if (!nameUser.equals("")&&!surnameUser.equals("")){
                    DatabaseService.updateUserName(currentUserEmail,nameUser+" "+surnameUser);
                    Toast.makeText(getContext(),"Успешно!",Toast.LENGTH_LONG).show();
                }
            }
        });
        galleryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, 2);
            }
        });
        return rootView;
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {

                //pick image from gallery
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                // Get the cursor
                Cursor cursor = getActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                bitmap = BitmapFactory.decodeFile(imgDecodableString);
                if (bitmap != null) {
                    DatabaseService.addAvatar(currentUserEmail, DatabaseService.BitMapToString(bitmap));
                    avatar.setImageBitmap(bitmap);
                }


            }
        }
    }


}