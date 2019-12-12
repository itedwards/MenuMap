package com.example.menumap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
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

public class AudioActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView audioResult;
    private ImageView buttonSpeak;
    private Button backButton;
    private String mTranslateText;
    private String mResultText;
    private FirebaseFirestore mDB;
    private DocumentSnapshot user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDB = FirebaseFirestore.getInstance();
        getUser();
        setContentView(R.layout.activity_audio);
        audioResult = findViewById(R.id.audioResult);
        buttonSpeak = findViewById(R.id.buttonSpeak);
        buttonSpeak.setOnClickListener(this);
        backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    public void onClick(View view){
        getSpeechInput();
    }

    public void getSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);

        String iso = getISO(user.get("sourceLangPref").toString());


        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, iso);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, iso);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, iso);
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, iso);
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,iso);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, iso);
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, iso);

        if(intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Does Not Support Speech Input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 10:
                if(resultCode == RESULT_OK && data != null) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    mResultText = TextUtils.join(", ", result);
                    identifyLanguage(mResultText);
                    audioResult.setText(mTranslateText);
                }
                break;
        }
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
                                    audioResult.setText(mTranslateText);
                                }
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                mTranslateText = "Internal error. Make sure your wifi is connected.";
                                audioResult.setText(mTranslateText);
                            }
                        });
    }

    public void downloadTranslator(String text) {
        Log.d("source",  user.get("sourceLangPref").toString());
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
                                audioResult.setText(mTranslateText);
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
                                audioResult.setText(mTranslateText);
                                addToDB();

                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });

    }

    private String getISO(String lang) {
        switch (lang) {
            case "English":
                return "en";
            case "Spanish":
                return "es";
            case "Chinese":
                return "zh";
            case "French":
                return "fr";
            case "Dutch":
                return "nl";
            case "German":
                return "de";
            case "Korean":
                return "ko";
            case "Japanese":
                return "ja";
            case "Russian":
                return "ru";

        }

        return "und";
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

    private void getUser(){
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("uid", currentUser.getUid());
        if(mDB == null){
            Log.d("mDB", "found null");
        }
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

    public void addToDB() {

        String[] translateArray = mTranslateText.split("\\s+");
        String[] resultArray = mResultText.split("\\s+");

        for (int i = 0; i < resultArray.length; i++) {

            Map<String, Object> translation = new HashMap<>();
            translation.put("sourceText", resultArray[i]);
            // replace with actual values
            translation.put("sourceLang", user.get("sourceLangPref").toString());
            translation.put("resultText", translateArray[i]);
            translation.put("resultLang", user.get("targetLangPref").toString());
            translation.put("userID", user.getId());
            translation.put("createdAt", new Timestamp(new Date()));

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

}
