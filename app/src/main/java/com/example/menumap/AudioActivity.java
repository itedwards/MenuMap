package com.example.menumap;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

public class AudioActivity extends AppCompatActivity {

    private TextView audioResult;
    private ImageView buttonSpeak;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio);
        audioResult = findViewById(R.id.audioResult);
        buttonSpeak = findViewById(R.id.buttonSpeak);
        buttonSpeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getSpeechInput();
            }
        });
    }

    public void getSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "zh");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "zh");
        intent.putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, "zh");
        intent.putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE,"zh");
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "zh");
        intent.putExtra(RecognizerIntent.EXTRA_RESULTS, "zh");

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
                    audioResult.setText(result.get(0));
                }
                break;
        }
    }
}
