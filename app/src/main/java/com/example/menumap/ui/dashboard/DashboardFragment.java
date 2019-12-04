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
    private FirebaseVisionTextRecognizer mDetector;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        mImageView = root.findViewById(R.id.imageView);
        mImageView.setOnClickListener(new View.OnClickListener() {
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

//    private FirebaseVisionImage imageFromBitmap(Bitmap bitmap) {
//        // [START image_from_bitmap]
//        Image image = (Image) bitmap;
//        int rotation = getRotationCompensation(
//                CameraCharacteristics.LENS_FACING_FRONT, MainActivity.this, this);
//        return FirebaseVisionImage.fromMediaImage(image, rotation);
//        // [END image_from_bitmap]
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if(requestCode == 1 && resultCode == -1 && data != null){
//            Log.d("check", "in onActivityResult");
//            Bundle extras = data.getExtras();
//            if(extras != null) {
//                Bitmap bm = (Bitmap) extras.get("data");
//                this.mImageView.setImageBitmap(bm);
//
//                FirebaseVisionImage image = imageFromBitmap(bm);
//
//
//                Task<FirebaseVisionText> result = mDetector.processImage(image)
//                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
//                            @Override
//                            public void onSuccess(FirebaseVisionText result) {
//                                String resultText = result.getText();
//                                Log.d("result", resultText);
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                e.printStackTrace();
//                            }
//                        });
//
//            }
//        }
//    }
//    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
//    static {
//        ORIENTATIONS.append(Surface.ROTATION_0, 90);
//        ORIENTATIONS.append(Surface.ROTATION_90, 0);
//        ORIENTATIONS.append(Surface.ROTATION_180, 270);
//        ORIENTATIONS.append(Surface.ROTATION_270, 180);
//    }
//
//        /**
//         * Get the angle by which an image must be rotated given the device's current
//         * orientation.
//         */
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        private int getRotationCompensation(String cameraId, Activity activity, Context context)
//                throws CameraAccessException {
//            // Get the device's current rotation relative to its "native" orientation.
//            // Then, from the ORIENTATIONS table, look up the angle the image must be
//            // rotated to compensate for the device's rotation.
//            int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
//            int rotationCompensation = ORIENTATIONS.get(deviceRotation);
//
//            // On most devices, the sensor orientation is 90 degrees, but for some
//            // devices it is 270 degrees. For devices with a sensor orientation of
//            // 270, rotate the image an additional 180 ((270 + 270) % 360) degrees.
//            CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
//            int sensorOrientation = cameraManager
//                    .getCameraCharacteristics(cameraId)
//                    .get(CameraCharacteristics.SENSOR_ORIENTATION);
//            rotationCompensation = (rotationCompensation + sensorOrientation + 270) % 360;
//
//            // Return the corresponding FirebaseVisionImageMetadata rotation value.
//            int result;
//            switch (rotationCompensation) {
//                case 0:
//                    result = FirebaseVisionImageMetadata.ROTATION_0;
//                    break;
//                case 90:
//                    result = FirebaseVisionImageMetadata.ROTATION_90;
//                    break;
//                case 180:
//                    result = FirebaseVisionImageMetadata.ROTATION_180;
//                    break;
//                case 270:
//                    result = FirebaseVisionImageMetadata.ROTATION_270;
//                    break;
//                default:
//                    result = FirebaseVisionImageMetadata.ROTATION_0;
//                    Log.e("Rotation", "Bad rotation value: " + rotationCompensation);
//            }
//            return result;
//        }


}