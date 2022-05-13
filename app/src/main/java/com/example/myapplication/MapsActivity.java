package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
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
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Models.Markers;
import com.example.myapplication.Models.PhotoBase64;
import com.example.myapplication.Models.ResponseModel;
import com.example.myapplication.Models.SendModel;
import com.example.myapplication.Services.NetworkServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Marker> markers;
    private SupportMapFragment mapFragment;
    private ArrayList<Markers> allMarkers = getAllMarkers();
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
        mMap.setMinZoomPreference(12);
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                String titleInfo[] = marker.getTitle().split("some_id");
                String url = "http://tagproject-api.sfedu.ru/api/v1/map/markers/" + titleInfo[0] + "/get_image_base64";
                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);
                ImageView snippet = new ImageView(getApplicationContext());
                TextView title = new TextView(getApplicationContext());
                title.setMaxWidth(300);

                title.setGravity(Gravity.CENTER);
                try {
                    title.setText(titleInfo[1]);
                } catch (Exception e) {
                    title.setText("No title");
                }

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String in = null;
                try {
                    in = Jsoup.connect(url).ignoreContentType(true).execute().body();
                    JSONObject reader = new JSONObject(in);
                    in = reader.getString("image_base64");
                    Log.d("myIn", in);
                    try {
                        snippet.setImageBitmap(Bitmap.createScaledBitmap(adapter.decodeImage(in), 200, 200, false));
                    } catch (Exception e) {
                        snippet.setImageResource(R.drawable.ic_flower);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
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
            showDialog(mMap.getMyLocation().getLatitude() + ", " + mMap.getMyLocation().getLongitude(), 1, result.getPhoto());
        } else if (requestCode == REQUEST_TAKE_PHOTO_PIT && resultCode == RESULT_OK) {
            getImageFromResult(data);
            showDialog(mMap.getMyLocation().getLatitude() + ", " + mMap.getMyLocation().getLongitude(), 0, result.getPhoto());
        }
    }

    private void getImageFromResult(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap thumbnailBitmap = (Bitmap) extras.get("data");
        ImageAdapter adapter = new ImageAdapter();
        result.setPhoto(adapter.encodeImage(thumbnailBitmap));
        Log.d("getImage", result.getPhoto());
    }

    public ArrayList<Markers> getAllMarkers() {
        ArrayList<Markers> markersArrayList = new ArrayList<>();
        NetworkServices.getInstance()
                .getJSONApi()
                .getAllMarks()
                .enqueue(new Callback<ArrayList<Markers>>() {
                    @Override
                    public void onResponse(Call<ArrayList<Markers>> call, Response<ArrayList<Markers>> response) {
                        if (response.code() == 200) {
                            allMarkers = response.body();
                            Log.d("myLog", "accept \n" + response.body().get(0).toString());
                        } else Log.d("myLog", response.message());
                    }

                    @Override
                    public void onFailure(Call<ArrayList<Markers>> call, Throwable t) {
                        Log.d("myLog", "Failure" + t.getMessage());
                    }
                });
        return markersArrayList;
    }

    public void setAllMarkers() {
        for (int i = 0; i < allMarkers.size(); i++) {
            if (allMarkers.get(i).getMarkerType() == 1) {
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(allMarkers.get(i).getLat(),
                        allMarkers.get(i).getLon())).title(allMarkers.get(i).getId()
                        + "some_id" + allMarkers.get(i).getDescription())
                        .icon(adapter.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_pit))));
            } else {
                markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(allMarkers.get(i).getLat(),
                        allMarkers.get(i).getLon())).title(allMarkers.get(i).getId()
                        + "some_id" + allMarkers.get(i).getDescription())
                        .icon(adapter.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.ic_flower))));
            }
        }

        if (allMarkers.size() != 0)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(allMarkers.get(allMarkers.size() - 1).getLat(), allMarkers.get(allMarkers.size() - 1).getLon())));
    }

    public void showDialog(String gps, int request_type, String image) {
        View promptsView = View.inflate(this, R.layout.alert_view, null);
        final TextInputEditText userName = promptsView.findViewById(R.id.input_name);
        final TextInputEditText userInput = promptsView.findViewById(R.id.input_text);
        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(MapsActivity.this, R.style.RoundShapeTheme)
                .setTitle("Отправка метки")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                })
                .setView(promptsView)
                .create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                ((androidx.appcompat.app.AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String name = userName.getText().toString();
                        String info = userInput.getText().toString();
                        if (image.length() == 0) alertDialog.cancel();
                        if (name.length() < 2 || info.length() < 3) {
                            Toast.makeText(getApplicationContext(), "Заполните все поля", Toast.LENGTH_SHORT).show();
                        } else {
                            sendImage(new SendModel(name, info, gps, request_type, image));
                            alertDialog.cancel();
                            alertDialog.dismiss();
                        }
                    }
                });
            }
        });

        alertDialog.show();
    }

    public void sendImage(SendModel sendModel) {
        Log.d("sendModel", sendModel.toString());
        NetworkServices.getInstance().getJSONApi().uploadMarker(sendModel).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.code() == 200) {
                    Log.d("alert", response.body().getMsg());
                    allMarkers = getAllMarkers();
                } else Log.d("alert", response.message());
                Toast.makeText(getApplicationContext(), "Метка отправлена на проверку",
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d("alert", "fail");
            }
        });
    }
}