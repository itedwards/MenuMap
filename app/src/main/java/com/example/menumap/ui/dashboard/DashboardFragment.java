package com.example.menumap.ui.dashboard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.Image;
import android.os.Build;
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

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.menumap.MainActivity;
import com.example.menumap.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.document.FirebaseVisionCloudDocumentRecognizerOptions;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentText;
import com.google.firebase.ml.vision.document.FirebaseVisionDocumentTextRecognizer;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Arrays;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private ImageView mImageView;
    private Button mCameraBtn;
    private TextView mResult;
    private FirebaseVisionTextRecognizer mDetector;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mImageView = root.findViewById(R.id.imageView);
        mImageView.setVisibility(View.GONE);
        mResult = root.findViewById(R.id.resultView);
        mResult.setVisibility(TextView.GONE);
        mCameraBtn = root.findViewById(R.id.cameraBtn);
        mCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 1);
            }
        });

        FirebaseVisionCloudTextRecognizerOptions options =
                new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("en", "hi"))
                .build();

        mDetector =  FirebaseVision.getInstance()
                .getCloudTextRecognizer();

        return root;
    }

    private FirebaseVisionImage imageFromBitmap(Bitmap bitmap) {
        // [START image_from_bitmap]
        return FirebaseVisionImage.fromBitmap(bitmap);
        // [END image_from_bitmap]
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 1 && resultCode == -1 && data != null){
            Log.d("check", "in onActivityResult");
            Bundle extras = data.getExtras();
            if(extras != null) {
                Bitmap bm = (Bitmap) extras.get("data");
                this.mImageView.setImageBitmap(bm);
                mImageView.setVisibility(View.VISIBLE);
                mCameraBtn.setVisibility(Button.GONE);
                FirebaseVisionImage image = imageFromBitmap(bm);


                Task<FirebaseVisionText> result = mDetector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText result) {
                                mResult.setVisibility(View.VISIBLE);
                                String resultText = result.getText();
                                mResult.setText(result.getText());
                                Log.d("result", resultText);
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                e.printStackTrace();
                            }
                        });

            }
        }
    }



}