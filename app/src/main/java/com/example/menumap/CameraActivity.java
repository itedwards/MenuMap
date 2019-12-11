package com.example.menumap;


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
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentification;
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentificationOptions;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.util.Locale;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener{

    private ImageView mImageView;

    private String mResultText;
    private String mTranslateText;
    private TextView mResultView;
    private TextView mTranslateView;
    private Button mTranslateBtn;
    private Button mBackBtn;
    private Button mAddBtn;
    private FirebaseVisionTextRecognizer mDetector;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);

        Bundle extras = getIntent().getExtras();
        Bitmap photo = (Bitmap) extras.get("photo");
        mImageView = findViewById(R.id.pictureView);
        mImageView.setImageBitmap(photo);
        mResultView = findViewById(R.id.resultView);
        mResultText = mResultView.getText().toString();
        mTranslateView = findViewById(R.id.translateView);
        mTranslateView.setVisibility(View.INVISIBLE);
        mTranslateText = mTranslateView.getText().toString();
        mTranslateBtn = findViewById(R.id.translateBtn);
        mBackBtn = findViewById(R.id.backBtn);
        mAddBtn = findViewById(R.id.addBtn);
        mDetector =  FirebaseVision.getInstance()
                .getCloudTextRecognizer();

        processImage(photo);

    }
    private FirebaseVisionImage imageFromBitmap(Bitmap bitmap) {
        // [START image_from_bitmap]
        return FirebaseVisionImage.fromBitmap(bitmap);
        // [END image_from_bitmap]
    }
    private void processImage(Bitmap bm) {
        FirebaseVisionImage image = imageFromBitmap(bm);

        Task<FirebaseVisionText> result = mDetector.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText result) {

                        String resultText = result.getText();
                        mResultText = resultText;
                        identifyLanguage(mResultText);
                        mResultView.setText(mResultText);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

    }

    public void identifyLanguage(String text) {
        FirebaseLanguageIdentification languageIdentifier =
                FirebaseNaturalLanguage.getInstance().getLanguageIdentification();
        languageIdentifier.identifyLanguage(text)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@Nullable String languageCode) {
                                if (languageCode != "und") {
                                    downloadTranslator(languageCode);
                                } else {
                                    mTranslateText = "Can't identify language.";
                                    mTranslateView.setText(mTranslateText);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mTranslateText = "Internal error. Make sure your wifi is connected.";
                                mTranslateView.setText(mTranslateText);
                            }
                        });
    }

    public void downloadTranslator(String text) {
        int sourceLanguage = 0;
        try {
            sourceLanguage = FirebaseTranslateLanguage
                    .languageForLanguageCode(text);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguage)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build();
        final FirebaseTranslator langTranslator =
                FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();
        langTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                Log.d("translator", "downloaded lang  model");

                                translateText(langTranslator);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mTranslateText = "Download failed.";
                                mTranslateView.setText(mTranslateText);
                            }
                        });
    }


    public void translateText(FirebaseTranslator langTranslator) {

        langTranslator.translate(mResultText)
                .addOnSuccessListener(
                        new OnSuccessListener<String>() {
                            @Override
                            public void onSuccess(@NonNull String translatedText) {
                                mTranslateText = translatedText;
                                mTranslateView.setText(mTranslateText);

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

    }


    @Override
    public void onClick(View v) {
        if(v == mTranslateBtn) {
            mTranslateView.setVisibility(View.VISIBLE);
            mTranslateView.setText(mTranslateText);
        }
        else if(v == mBackBtn) {
            finish();
        }
        else {
            
        }
    }
}
