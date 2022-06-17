package TagProject.example.Application;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import TagProject.example.Application.Models.Markers;
import TagProject.example.Application.Models.PhotoBase64;
import TagProject.example.Application.Models.ResponseModel;
import TagProject.example.Application.Models.SendModel;

import com.example.myapplication.R;

import TagProject.example.Application.Services.NetworkServices;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
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

/**
 * Класс отвечает за взаимодействие с
 * картой и отправку меток на сервер
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap; //Google карта
    private ArrayList<Marker> markers; //Массив с метками на карте
    private SupportMapFragment mapFragment; //Экран с картой
    private ArrayList<Markers> allMarkers = getAllMarkers(); //Массив со всеми меткми на сервере
    private static final int REQUEST_TAKE_PHOTO_AMBROSE = 1; //Код для ответа от камеры
    private PhotoBase64 result = new PhotoBase64("-"); //Результат конвертации в base64
    private ImageAdapter adapter = new ImageAdapter(); //Класс для конвертации изображений
    private LatLng myLocation; //Текущая геолокация

    //Элементы разметки (View)
    private ImageView camera;
    private ImageView restore;
    private ConstraintLayout cameraScreen;
    private ImageView map;
    private ImageView question;
    private FrameLayout myMap;
    private AppCompatButton takePhoto;


    /**
     * Метод собирает и запускает экран
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
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
                "\"elementType\": \"labels\",\n" +
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
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                String titleInfo[] = marker.getTitle().split("some_id");
                String url = "http://tagproject-api.sfedu.ru/api/v1/map/markers/" + titleInfo[0] + "/get_image_base64";
                LinearLayout info = new LinearLayout(getApplicationContext());
                info.setOrientation(LinearLayout.VERTICAL);
                ImageView snippet = new ImageView(getApplicationContext());

                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);

                String in = null;
                try {
                    in = Jsoup.connect(url).ignoreContentType(true).execute().body();
                    JSONObject reader = new JSONObject(in);
                    in = reader.getString("image_base64");
                    Log.d("myIn", in);
                    try {
                        snippet.setImageBitmap(Bitmap.createScaledBitmap(adapter.decodeImage(in), 200, 280, false));
                    } catch (Exception e) {
                        snippet.setImageResource(R.drawable.map_tag);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                info.addView(snippet);
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
                    if (myLocation == null)
                        myLocation = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                }
            });
        }
    }

    /**
     * Метод проверяет проверяет наличие доступа к
     * камере устройства и открывает её
     */
    private void setTakePhoto() {
        permissionCheck();
        getPhoto(REQUEST_TAKE_PHOTO_AMBROSE);
    }

    /**
     * Метод проверяет проверяет наличие
     * доступа ккамере устройства
     */
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

    /**
     * Метод вызывает стандартное приложение камеры
     * устройства с ожиданием результата
     */
    private void getPhoto(int requestCode) {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePhotoIntent, requestCode);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Метод находит все элементы разметки и
     * устанавливает обработчики нажатий на них
     */
    private void findView() {
        camera = findViewById(R.id.item_camera);
        map = findViewById(R.id.item_map);
        restore = findViewById(R.id.restore);
        takePhoto = findViewById(R.id.takePhoto);
        myMap = findViewById(R.id.myMap);
        cameraScreen = findViewById(R.id.cameraScreen);
        question = findViewById(R.id.question);

        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showQuestionDialog();
            }
        });
        takePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTakePhoto();
            }
        });
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getAllMarkers();
                setAllMarkers();
            }
        });
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraScreen.setVisibility(View.VISIBLE);
                camera.setImageResource(R.drawable.camera_green);
                map.setImageResource(R.drawable.tag_grey);
                myMap.setVisibility(View.INVISIBLE);
                takePhoto.setVisibility(View.VISIBLE);
            }
        });
        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraScreen.setVisibility(View.INVISIBLE);
                camera.setImageResource(R.drawable.camera_grey);
                map.setImageResource(R.drawable.tag_green);
                myMap.setVisibility(View.VISIBLE);
                takePhoto.setVisibility(View.INVISIBLE);
            }
        });

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    /**
     * Метод обрабатывает изображение с камеры и
     * открывает диалог с пользователем
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO_AMBROSE && resultCode == RESULT_OK) {
            getImageFromResult(data);
            showDialog(mMap.getMyLocation().getLatitude() + ", " + mMap.getMyLocation().getLongitude(), 1, result.getPhoto());
        }
    }

    /**
     * Метод конвертирует узображение в base64
     * формат для отправки на сервер
     */
    private void getImageFromResult(Intent data) {
        Bundle extras = data.getExtras();
        Bitmap thumbnailBitmap = (Bitmap) extras.get("data");
        ImageAdapter adapter = new ImageAdapter();
        result.setPhoto(adapter.encodeImage(thumbnailBitmap));
        Log.d("getImage", result.getPhoto());
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
        for (int i = 0; i < allMarkers.size(); i++) {
            markers.add(mMap.addMarker(new MarkerOptions().position(new LatLng(allMarkers.get(i).getLat(),
                    allMarkers.get(i).getLon())).title(allMarkers.get(i).getId()
                    + "some_id" + allMarkers.get(i).getDescription())
                    .icon(adapter.getBitmapFromVectorDrawable(getApplicationContext(), R.drawable.map_tag))));
        }

    }

    /**
     * Метод вызывает диалог с пользователем об
     * отправке изображения и геолокации на сервер
     */
    public void showDialog(String gps, int request_type, String image) {
        View promptsView = View.inflate(this, R.layout.alert_view, null);
        final TextInputEditText userInput = promptsView.findViewById(R.id.input_text);
        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(MapsActivity.this, R.style.RoundShapeTheme)
                .setView(promptsView)
                .create();

        promptsView.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = "Без имени";
                String info = userInput.getText().toString();
                if (info == null) info = "Описание отсутствует";
                if (image.length() == 0) alertDialog.cancel();
                sendImage(new SendModel(name, info, gps, request_type, image));
                alertDialog.cancel();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * Метод вызывает диалоговое окно
     * с описанием идеи приложения
     */
    public void showQuestionDialog(){
        View promptsView = View.inflate(this, R.layout.question_dialog, null);
        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(MapsActivity.this, R.style.RoundShapeTheme)
                .setView(promptsView)
                .create();

        alertDialog.show();
    }

    /**
     * Метод выполняет отправку данных на сервер
     */
    public void sendImage(SendModel sendModel) {
        Log.d("sendModel", sendModel.toString());
        NetworkServices.getInstance().getJSONApi().uploadMarker(sendModel).enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                if (response.code() == 200) {
                    Log.d("alert", response.body().getMsg());
                } else if (response.code() == 202) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getAllMarkers();
                        }
                    }, 1000);
                } else Log.d("alert", response.message() + " " + response.code());
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