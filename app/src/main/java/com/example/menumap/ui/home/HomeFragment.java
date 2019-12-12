package com.example.menumap.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import java.util.ArrayList;

import com.example.menumap.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class HomeFragment extends Fragment {
    private static final String SOURCE_TEXT = "sourceText";
    private static final String SOURCE_LANGUAGE = "sourceLang";
    private static final String RESULT_TEXT = "resultText";
    private static final String RESULT_LANGUAGE = "resultLang";
    private static final String COLLECTION_PATH = "translations";

    private FirebaseFirestore mDB;
    private DocumentSnapshot mTranslationsDB;
    ListView listView;



    ArrayList<String> foodList;
    ArrayAdapter<String> arrayAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        mDB = FirebaseFirestore.getInstance();

        listView = root.findViewById(R.id.ListView);

        foodList = new ArrayList<>();

        arrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, foodList);

        listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String footName = foodList.get(i);
            }
        });
        makeListFromDB();
        return root;
    }

    private void makeListFromDB() {

        mDB.collection(COLLECTION_PATH).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        StringBuilder fields = new StringBuilder("");
                        fields.append("Original Text: ").append(document.getString(SOURCE_TEXT));
                        fields.append("\nTranslation: ").append(document.getString(RESULT_TEXT));
                        fields.append("\nOriginal Language: ").append(document.getString(SOURCE_LANGUAGE));
                        fields.append("\nTranslated to: ").append(document.getString(RESULT_LANGUAGE));
                        arrayAdapter.add(fields.toString());


                    }
                } else {
                    Log.d("collection", "Error getting documents: ", task.getException());
                }
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("collection", "Error getting documents: ");
                    }
                });


    }


}