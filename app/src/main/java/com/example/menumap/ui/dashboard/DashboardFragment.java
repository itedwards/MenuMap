package com.example.menumap.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.menumap.AudioActivity;

import com.example.menumap.CameraActivity;
import com.example.menumap.MainActivity;
import com.example.menumap.R;


public class DashboardFragment extends Fragment {


    private Button mCameraBtn;
    private Button mAudioBtn;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mCameraBtn = root.findViewById(R.id.cameraBtn);
        mCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            }
        });

        mAudioBtn = root.findViewById(R.id.audioBtn);
        mAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), AudioActivity.class);
                startActivity(intent);
            }
        });

        return root;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == -1 && data != null){
            Log.d("check", "in onActivityResult");
            Bundle extras = data.getExtras();
            if(extras != null) {
                Intent explicit = new Intent(getActivity(), CameraActivity.class);
                Bitmap bm = (Bitmap) extras.get("data");
                explicit.putExtra("photo", bm);
                startActivity(explicit);



            }
        }
    }

}