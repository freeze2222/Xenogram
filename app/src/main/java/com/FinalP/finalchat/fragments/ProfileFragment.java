package com.FinalP.finalchat.fragments;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.FinalP.finalchat.R;
import com.FinalP.finalchat.listeners.SimpleListener;
import com.FinalP.finalchat.models.application.User;
import com.FinalP.finalchat.services.DatabaseService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ProfileFragment extends Fragment {
    EditText name;
    EditText surname;
    Button galleryButton;
    ImageView avatar;
    View animView;
    TextView idView;
    Bitmap bitmap;
    //Animation fadeout = new AlphaAnimation(0.0F, 1.0F);
    String currentUserEmail=DatabaseService.reformString(Objects.requireNonNull(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail()));
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
        idView=rootView.findViewById(R.id.idField);
        animView=rootView.findViewById(R.id.animLayoutProfile);

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

        idView.setText(currentUserEmail);
        confirmChanges.setOnClickListener(view -> {
            String nameUser=name.getText().toString();
            String surnameUser=surname.getText().toString();
            if (!nameUser.equals("")&&!surnameUser.equals("")){
                DatabaseService.updateUserName(currentUserEmail,nameUser+" "+surnameUser);
                Toast.makeText(getContext(),"Успешно!",Toast.LENGTH_LONG).show();
            }
        });
        galleryButton.setOnClickListener(view -> {
            Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(galleryIntent, 2);
        });
        DatabaseService.getPicture(currentUserEmail, arg -> avatar.setImageBitmap(arg));
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
                Cursor cursor = requireActivity().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                // Move to first row
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String imgDecodableString = cursor.getString(columnIndex);
                cursor.close();
                bitmap = BitmapFactory.decodeFile(imgDecodableString);
                if (bitmap != null) {
                    avatar.setImageBitmap(bitmap);

                    DatabaseService.uploadPicture(currentUserEmail, DatabaseService.BitmapToByte(bitmap), arg -> Log.e("Uploading","Pic"));
                }
            }
        }
    }
}