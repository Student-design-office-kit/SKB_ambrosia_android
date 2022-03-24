package com.example.myapplication;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Models.Markers;
import com.example.myapplication.Models.PhotoBase64;
import com.example.myapplication.Models.ResponseModel;
import com.example.myapplication.Models.SendModel;
import com.example.myapplication.Services.JSONPlaceHolderApi;
import com.example.myapplication.Services.NetworkServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Marker> markers;
    private SupportMapFragment mapFragment;
    private ArrayList<com.example.myapplication.Models.Response> allMarkers = getAllMarkers();
    private static final int REQUEST_TAKE_PHOTO_AMBROSE = 1;
    private static final int REQUEST_TAKE_PHOTO_PIT = 2;
    private PhotoBase64 result = new PhotoBase64("-");
    private ImageAdapter adapter = new ImageAdapter();
    private LatLng myLocation;
    private ImageView ambrosia;
    private ImageView unevenness;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        findView();
        if (isGeoDisabled()) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        mapFragment.getMapAsync(this);

        /*if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mapFragment.getMapAsync(this);
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }*/
    }

    public boolean isGeoDisabled() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean mIsGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean mIsNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean mIsGeoDisabled = !mIsGPSEnabled && !mIsNetworkEnabled;
        return mIsGeoDisabled;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        markers = new ArrayList<>();
        setAllMarkers();
        mMap.setMinZoomPreference(13);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                String titleInfo[] = marker.getTitle().split("some_id");
                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);
                ImageView snippet = new ImageView(getApplicationContext());
                TextView title = new TextView(getApplicationContext());
                snippet.setMinimumHeight(300);
                snippet.setMinimumWidth(300);
                title.setMaxWidth(300);

                title.setGravity(Gravity.CENTER);
                try {
                    title.setText(titleInfo[1]);
                } catch (Exception e) {
                    title.setText("No title");
                }

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String url = "https://tagproject.sfedu.ru/map/api/marker/" + titleInfo[0] + "/get_photo";
                String in = null;
                try {
                    in = Jsoup.connect(url).ignoreContentType(true).execute().body();
                    JSONObject reader = new JSONObject(in);
                    in = reader.getString("photo_base64");
                    Log.d("myLog", in);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    snippet.setImageBitmap(adapter.decodeImage(in));
                } catch (Exception e) {
                    snippet.setImageResource(R.drawable.ic_launcher_background);
                }

                info.addView(snippet);
                info.addView(title);
                return info;
            }
        });

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int permissionStatus = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                setOnMyLocationChangeListener();
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myLocation));
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
            }
            return;
        }
        mMap.setMyLocationEnabled(true);

        ambrosia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAmbrosiaClickListener();
            }
        });
        unevenness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setPitClickListener();
            }
        });
    }

    private void setOnMyLocationChangeListener() {
        if (mMap != null) {
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location arg0) {
                    myLocation = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    if (myLocation == null)
                        myLocation = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                }
            });
        }
    }

    private void setAmbrosiaClickListener() {
        permissionCheck();
        getPhoto(REQUEST_TAKE_PHOTO_AMBROSE);
        setAllMarkers();
    }

    private void setPitClickListener() {
        permissionCheck();
        getPhoto(REQUEST_TAKE_PHOTO_PIT);
        setAllMarkers();
    }

    private void permissionCheck() {
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                setOnMyLocationChangeListener();
            } else {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        } catch (IOError e) {
            Toast.makeText(getApplicationContext(), "Убедитесь, что у вас включен GPS", Toast.LENGTH_LONG).show();
        }
    }

    private void getPhoto(int requestCode) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePhotoIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void findView() {
        ambrosia = findViewById(R.id.ambrosia);
        unevenness = findViewById(R.id.unevenness);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO_AMBROSE && resultCode == RESULT_OK) {
            getImageFromResult(data);
            showDialog(mMap.getMyLocation().getLatitude() + " " + mMap.getMyLocation().getLongitude(), "ambros", result.getPhoto());
        } else if (requestCode == REQUEST_TAKE_PHOTO_PIT && resultCode == RESULT_OK) {
            getImageFromResult(data);
            showDialog(mMap.getMyLocation().getLatitude() + " " + mMap.getMyLocation().getLongitude(), "road", result.getPhoto());
        }
    }

    private void getImageFromResult(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap thumbnailBitmap = (Bitmap) extras.get("data");
        ImageAdapter adapter = new ImageAdapter();
        result.setPhoto(adapter.encodeImage(thumbnailBitmap));
        Log.d("getImage", result.getPhoto());
    }

    public ArrayList<com.example.myapplication.Models.Response> getAllMarkers() {
        ArrayList<com.example.myapplication.Models.Response> markersArrayList = new ArrayList<>();
        NetworkServices.getInstance()
                .getJSONApi()
                .getAllMarks()
                .enqueue(new Callback<Markers>() {
                    @Override
                    public void onResponse(Call<Markers> call, Response<Markers> response) {
                        if (response.code() == 200) {
                            markersArrayList.addAll(response.body().getMarkers());
                            Log.d("myLog", "accept \n" + response.message());
                        } else Log.d("myLog", response.message());
                    }

                    @Override
                    public void onFailure(Call<Markers> call, Throwable t) {
                        Log.d("myLog", "Failure");
                    }
                });
        return markersArrayList;
    }

    public void setAllMarkers() {
        for (int i = 0; i < allMarkers.size(); i++) {
            if (allMarkers.get(i).getMarkerType().equals("road")) {
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(allMarkers.get(i).getLat(),
                        allMarkers.get(i).getLon())).title(allMarkers.get(i).getSlug()
                        + "some_id" + allMarkers.get(i).getDescription())
                        .icon(adapter.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_pit))));
            } else {
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(allMarkers.get(i).getLat(),
                        allMarkers.get(i).getLon())).title(allMarkers.get(i).getSlug()
                        + "some_id" + allMarkers.get(i).getDescription())
                        .icon(adapter.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_flower))));
            }
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(allMarkers.get(3).getLat(), allMarkers.get(3).getLon())));
    }

    public void showDialog(String gps, String request_type, String image) {
        LayoutInflater li = LayoutInflater.from(getApplicationContext());
        View promptsView = li.inflate(R.layout.alert_view, null);

        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(MapsActivity.this);
        mDialogBuilder.setView(promptsView);

        final EditText userName = promptsView.findViewById(R.id.input_name);
        final EditText userInput = promptsView.findViewById(R.id.input_text);

        mDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                String name = (userName.getText().toString().length() > 0) ? userName.getText().toString() : "unnamed";
                                String info = (userInput.getText().toString().length() > 0) ? userInput.getText().toString() : "no info";
                                if (image.length() == 0) dialog.cancel();
                                sendImage(new SendModel(name, info, gps, request_type, image));

                                dialog.cancel();
                            }
                        })
                .setNegativeButton("Отмена",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        AlertDialog alertDialog = mDialogBuilder.create();
        alertDialog.show();
    }

    public void sendImage(SendModel sendModel) {
        final String BASE_URL = "https://tagproject.sfedu.ru/api/";

        //Initializing a Retrofit library object
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        JSONPlaceHolderApi rest_api = retrofit.create(JSONPlaceHolderApi.class);

        rest_api.uploadMarker(sendModel).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.code() == 200) {
                    Log.d("alert", "accept");
                    Toast.makeText(getApplicationContext(), "Метка отправлена на проверку",
                            Toast.LENGTH_LONG).show();
                    //allMarkers = getAllMarkers();
                } else Log.d("alert", response.message());
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d("alert", "fail");
            }
        });
    }
}