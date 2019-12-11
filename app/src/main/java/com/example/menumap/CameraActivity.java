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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
    private FirebaseFirestore mDB;
    private DocumentSnapshot user;

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
        mDB = FirebaseFirestore.getInstance();
        DocumentSnapshot user;
        processImage(photo);

        getUser();
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
        int sourceLanguage = getLang(user.get("sourceLangPref").toString());
        int targetLanguage = getLang(user.get("targetLangPref").toString());

        FirebaseTranslatorOptions options =
                new FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(sourceLanguage)
                        .setTargetLanguage(targetLanguage)
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
            addToDB();
        }
    }

    public void addToDB() {


        String[] translateArray = mTranslateText.split("\\s+");
        String[] resultArray = mResultText.split("\\s+");

        for (int i = 0; i < resultArray.length; i++) {

            Map<String, String> translation = new HashMap<>();
            translation.put("sourceText", resultArray[i]);
            // replace with actual values
            translation.put("sourceLang", user.get("sourceLangPref").toString());
            translation.put("resultText", translateArray[i]);
            translation.put("resultLang", user.get("targetLangPref").toString());

            mDB.collection("translations")
                    .add(translation)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("added doc", documentReference.getId());
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



    private void getUser(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = mDB.collection("users").document(currentUser.getUid());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()){
                        user = document;
                    }
                    else {
                        Log.d("not found", "no doc");
                    }
                }
                else {
                    Log.d("get failed", task.getException().toString());
                }
            }
        });
    }

    private int getLang(String lang) {
        switch (lang) {
            case "English":
                return FirebaseTranslateLanguage.EN;
            case "Spanish":
                return FirebaseTranslateLanguage.ES;
            case "Chinese":
                return FirebaseTranslateLanguage.ZH;
            case "French":
                return FirebaseTranslateLanguage.FR;
            case "Dutch":
                return FirebaseTranslateLanguage.NL;
            case "German":
                return FirebaseTranslateLanguage.DE;
            case "Korean":
                return FirebaseTranslateLanguage.KO;
            case "Japanese":
                return FirebaseTranslateLanguage.JA;
            case "Russian":
                return FirebaseTranslateLanguage.RU;

        }

        return -1;
    }
}
