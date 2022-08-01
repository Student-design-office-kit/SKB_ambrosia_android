package TagProject.example.Application.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;

import TagProject.example.Application.ImageAdapter;
import TagProject.example.Application.Models.Markers;
import TagProject.example.Application.Services.NetworkServices;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapFragment extends AppCompatActivity implements OnMapReadyCallback {

    private BottomSheetDialog sheetDialog;

    private GoogleMap mMap; //Google карта
    private ArrayList<Marker> markers; //Массив с метками на карте
    private SupportMapFragment mapFragment; //Экран с картой
    private ArrayList<Markers> allMarkers = getAllMarkers(); //Массив со всеми меткми на сервере
    private ImageAdapter adapter; //Класс для конвертации изображений
    private LatLng myLocation; //Текущая геолокация
    private LatLng lastLocation = new LatLng(0, 0); //Текущая геолокация на случай ошибки
    private LocationManager locationManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_map);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.item_map);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.item_camera:
                        startActivity(new Intent(getApplicationContext(),CameraFragment.class));
                        overridePendingTransition(0,0);
                        return true;
                    case R.id.item_map:
                        return true;
                    case R.id.item_info:
                        startActivity(new Intent(getApplicationContext(),InfoFragment.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
        adapter = new ImageAdapter(getApplicationContext(), MapFragment.this);
        if (isGeoDisabled()) {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
        findView();
    }

    /**
     * Метод проверяет состояние геолокации (вкл / выкл) на устройстве
     */
    public boolean isGeoDisabled() {
        LocationManager mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean mIsGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean mIsNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean mIsGeoDisabled = !mIsGPSEnabled && !mIsNetworkEnabled;
        return mIsGeoDisabled;
    }

    /**
     * Метод переопределяет состояние карты.
     * Устанавливает положение камеры и формат
     * представления содержимого окон над маркерами.
     * Устанавливает метки на карту
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        String style = "[\n" +
                "{\n" +
                "\"featureType\" : \"all\",\n" +
                "\"elementType\": \"labels.icon\",\n" +
                "\"stylers\": [\n" +
                "{\n" +
                "\"visibility\": \"off\"\n" +
                "}\n" +
                "]\n" +
                "}\n" +
                "]";
        mMap.setMapStyle(new MapStyleOptions(style));

        markers = new ArrayList<>();
        setAllMarkers();
        if (allMarkers.size() != 0)
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(allMarkers.get(allMarkers.size() - 1).getLat(), allMarkers.get(allMarkers.size() - 1).getLon())));
        mMap.setMinZoomPreference(12);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                getMarkerById(Integer.parseInt(marker.getTitle()));
                return false;
            }
        });
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                LinearLayout info = new LinearLayout(getApplicationContext());
                return info;
            }

            @Override
            public View getInfoContents(Marker marker) {
                return null;
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
    }

    /**
     * Метод обрабатывает изменение геолокации пользователя
     */
    private void setOnMyLocationChangeListener() {
        if (mMap != null) {
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location arg0) {
                    myLocation = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    lastLocation = myLocation;
                    if (myLocation == null)
                        myLocation = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                }
            });
        }
    }

    private void findView() {
        sheetDialog = new BottomSheetDialog(MapFragment.this);
        findViewById(R.id.question).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuestionDialog();
            }
        });
        findViewById(R.id.restore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllMarkers();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setAllMarkers();
                        Toast.makeText(getApplicationContext(), "Карта обновлена", Toast.LENGTH_SHORT).show();
                    }
                }, 500);
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Метод получает все маркеры с сервера
     */
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

    /**
     * Метод устанавливает маркеры на карту и
     * сохраняет необходимые данные в
     * title каждого маркера для дальнейшего парсинга
     */
    public void setAllMarkers() {
        mMap.clear();
        for (int i = 0; i < allMarkers.size(); i++) {
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(allMarkers.get(i).getLat(),
                    allMarkers.get(i).getLon())).title(allMarkers.get(i).getId() + "")
                    .icon(adapter.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.map_tag))));
        }

    }

    /**
     * Метод вызывает диалоговое окно
     * с описанием идеи приложения
     */
    public void showQuestionDialog() {
        View promptsView = View.inflate(this, R.layout.question_dialog, null);
        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(MapFragment.this, R.style.RoundShapeTheme)
                .setView(promptsView)
                .create();

        alertDialog.show();
    }

    /**
     * Метод вызывает диалоговое окно
     * с описанием выбранной метки на карте
     */
    private void createSheetDialog(Markers marker) {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog, null, false);

        ImageView image = view.findViewById(R.id.image_dialog);
        TextView address = view.findViewById(R.id.address_dialog);
        Glide.with(this).load(marker.getGetImage()).into(image);
        address.setText(marker.getStreet().toString());

        sheetDialog.setContentView(view);
    }

    /**
     * Метод получает с сервера маркер
     * по его id и размещает всю
     * информацию в выплывающий диалог
     */
    private void getMarkerById(int id) {
        NetworkServices.getInstance()
                .getJSONApi()
                .getMarkerById(id)
                .enqueue(new Callback<Markers>() {
                    @Override
                    public void onResponse(Call<Markers> call, Response<Markers> response) {
                        if (response.code() == 200) {
                            Markers marker = response.body();
                            Log.d("myLog", "accept \n" + marker.toString());
                            createSheetDialog(marker);
                            sheetDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                            sheetDialog.show();
                        } else Log.d("myLog", response.message());
                    }

                    @Override
                    public void onFailure(Call<Markers> call, Throwable t) {
                        Log.d("myLog", "Failure" + t.getMessage());
                    }
                });
    }

    @Override
    public void onBackPressed() {

    }
}
