package com.example.googlelens;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
//수정


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.googlelens.DataModal;
import com.example.googlelens.SearchResultsRVAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabel;
import com.google.firebase.ml.vision.label.FirebaseVisionImageLabeler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.example.googlelens.SearchResultsRVAdapter.*;

public class MainActivity extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;

    // variables for our image view, image bitmap,
    // buttons, recycler view, adapter and array list.
    private ImageView img;
    private Button snap, searchResultsBtn;
    private Bitmap imageBitmap;
    private RecyclerView resultRV;
    private SearchResultsRVAdapter searchResultsRVAdapter;
    private ArrayList<DataModal> dataModalArrayList;
    private String title, link, displayed_link, snippet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    //수정

        // initializing all our variables for views
        img = (ImageView) findViewById(R.id.image);
        snap = (Button) findViewById(R.id.snapbtn);
        searchResultsBtn = findViewById(R.id.idBtnSearchResuts);
        resultRV = findViewById(R.id.idRVSearchResults);

        // initializing our array list
        dataModalArrayList = new ArrayList<>();

        // initializing our adapter class.
        searchResultsRVAdapter = new SearchResultsRVAdapter(dataModalArrayList, MainActivity.this);

        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);

        resultRV.setLayoutManager(manager);
        resultRV.setAdapter(searchResultsRVAdapter);

        snap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dataModalArrayList.clear();
                searchResultsRVAdapter.notifyDataSetChanged();
                dispatchTakePictureIntent();
            }
        });

        searchResultsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataModalArrayList.clear();
                searchResultsRVAdapter.notifyDataSetChanged();
                getResults();
            }
        });
    }

    private void getResults() {

        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionImageLabeler labeler = FirebaseVision.getInstance().getOnDeviceImageLabeler();

        labeler.processImage(image).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionImageLabel>>() {
            @Override
            public void onSuccess(List<FirebaseVisionImageLabel> firebaseVisionImageLabels) {
                String searchQuery = firebaseVisionImageLabels.get(0).getText();
                searchData(searchQuery);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(MainActivity.this, "Fail to detect image..", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchData(String searchQuery) {
        String apiKey = "59b3cfd9e0cf360ba6e491afd277d6105d12371d24bd9d81464bd78ceadd7e61";
        String url = "https://serpapi.com/search.json?q=" + searchQuery.trim() + "&location=Seoul,Korea&hl=ko&gl=kr&google_domain=google.co.kr&api_key=" + apiKey;

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {

                    JSONArray organicResultsArray = response.getJSONArray("organic_results");
                    for (int i = 0; i < organicResultsArray.length(); i++) {
                        JSONObject organicObj = organicResultsArray.getJSONObject(i);
                        if (organicObj.has("title")) {
                            title = organicObj.getString("title");
                        }
                        if (organicObj.has("link")) {
                            link = organicObj.getString("link");
                        }
                        if (organicObj.has("displayed_link")) {
                            displayed_link = organicObj.getString("displayed_link");
                        }
                        if (organicObj.has("snippet")) {
                            snippet = organicObj.getString("snippet");
                        }

                        dataModalArrayList.add(new DataModal(title, link, displayed_link, snippet));
                    }

                    searchResultsRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "No Result found for the search query..", Toast.LENGTH_SHORT).show();
            }
        });

        queue.add(jsonObjectRequest);
    }

    // method to capture image.
    private void dispatchTakePictureIntent() {
        // inside this method we are calling an implicit intent to capture an image.
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // calling a start activity for result when image is captured.
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // inside on activity result method we are
        // setting our image to our image view from bitmap.
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            // on below line we are setting our
            // bitmap to our image view.
            img.setImageBitmap(imageBitmap);
        }
    }
}
