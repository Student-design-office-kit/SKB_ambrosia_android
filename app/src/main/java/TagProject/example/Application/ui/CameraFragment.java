package TagProject.example.Application.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Date;

import TagProject.example.Application.ImageAdapter;
import TagProject.example.Application.Models.PhotoBase64;
import TagProject.example.Application.Models.ResponseModel;
import TagProject.example.Application.Models.SendModel;
import TagProject.example.Application.R;
import TagProject.example.Application.Services.NetworkServices;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CameraFragment extends AppCompatActivity {

    private static final int REQUEST_TAKE_PHOTO_AMBROSE = 1; //Код для ответа от камеры
    private PhotoBase64 result = new PhotoBase64("-"); //Результат конвертации в base64
    private ImageAdapter adapter; //Класс для конвертации изображений
    private Bitmap bitmap;
    private LocationManager locationManager;
    private LatLng latLng;
    private String APP_PREFERENCES = "settings";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_camera);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.item_camera);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.item_map:
                        startActivity(new Intent(getApplicationContext(), MapFragment.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.item_camera:
                        return true;
                    case R.id.item_info:
                        startActivity(new Intent(getApplicationContext(), InfoFragment.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
        adapter = new ImageAdapter(getApplicationContext(), CameraFragment.this);
        findView();
    }

    /**
     * Найти все элементы разметик
     * и установить обработчики нажатий
     */
    private void findView() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        findViewById(R.id.takePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStoragePermissionGranted()) adapter.sendTakePictureIntent();
                else Toast.makeText(getApplicationContext(), "Предоставьте доступ к файлам", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG","Permission is granted");
                return true;
            } else {

                Log.v("TAG","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("TAG","Permission is granted");
            return true;
        }
    }

    /**
     * Метод обрабатывает изображение с камеры и
     * открывает диалог с пользователем
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO_AMBROSE && resultCode == RESULT_OK) {
            getImageFromResult();
            try {
                showDialog(1, result.getPhoto());
            } catch (NullPointerException e) {
                Toast.makeText(getApplicationContext(), "Проверьте, включена ли геолокация и повторите попытку", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Конвертирует изображение из
     * bitmap в base64 формат
     */
    private void getImageFromResult() {
        bitmap = adapter.getBitmap();
        result.setPhoto(adapter.encodeImage(bitmap));
        Log.d("getImage", result.getPhoto());
    }

    /**
     * Метод вызывает диалог с пользователем об
     * отправке изображения и геолокации на сервер
     *
     * @param request_type - тип отправляемой метки (амброзия) (Integer)
     * @param image        - base64 изображение (String)
     */
    public void showDialog(int request_type, String image) {
        View promptsView = View.inflate(this, R.layout.alert_view, null);
        final TextInputEditText userInput = promptsView.findViewById(R.id.input_text);
        CheckBox toSave = promptsView.findViewById(R.id.checkboxToSave);
        androidx.appcompat.app.AlertDialog alertDialog = new MaterialAlertDialogBuilder(CameraFragment.this, R.style.RoundShapeTheme)
                .setView(promptsView)
                .create();

        toSave.setChecked(loadPref());
        toSave.setTextColor(toSave.isChecked() ?
                getResources().getColor(R.color.checked_color) :
                getResources().getColor(R.color.unchecked_color));/*
        toSave.setButtonDrawable(toSave.isChecked() ?
                getDrawable(R.drawable.ic_round_checked) :
                getDrawable(R.drawable.ic_round_unchecked));*/

        toSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toSave.setTextColor(toSave.isChecked() ?
                        getResources().getColor(R.color.checked_color) :
                        getResources().getColor(R.color.unchecked_color));/*
                toSave.setButtonDrawable(toSave.isChecked() ?
                        getDrawable(R.drawable.ic_round_checked) :
                        getDrawable(R.drawable.ic_round_unchecked));*/
            }
        });

        promptsView.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = "Без имени";
                String info = userInput.getText().toString();
                if (info == null) info = "Описание отсутствует";
                if (image.length() == 0) alertDialog.cancel();
                if (toSave.isChecked()) {
                    String timeStamp = new SimpleDateFormat("d MMM yyyy HH:mm:ss").format(new Date());
                    MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, timeStamp, "Изображение сделано приложением \"Амброзия\" ");
                }
                try {
                    savePref(toSave.isChecked());
                    sendImage(new SendModel(name, info, latLng.latitude + ", " + latLng.longitude, request_type, image));
                } catch (NullPointerException e) {
                    if (!isNetworkConnected())
                        Toast.makeText(getApplicationContext(), "Проверьте ваше интернет соединение", Toast.LENGTH_SHORT).show();
                    else
                    Toast.makeText(getApplicationContext(), "Что-то пошло не так. Повторите попытку позже", Toast.LENGTH_SHORT).show();
                }

                alertDialog.cancel();
                alertDialog.dismiss();
            }
        });

        alertDialog.show();
    }

    /**
     * Метод проверяет наличие интернет соединения
     */
    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Метод выполняет отправку данных на сервер
     *
     * @param sendModel - отправляемая модель с описанием (SendModel)
     */
    public void sendImage(SendModel sendModel) {
        Log.d("sendModel", sendModel.toString());

        if (!isNetworkConnected())
            Toast.makeText(getApplicationContext(), "Проверьте интернет соединение", Toast.LENGTH_SHORT).show();
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
                            //getAllMarkers();
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

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Првоверьте геолокацию", Toast.LENGTH_SHORT).show();
            return;
        }
        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0,
                locationListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Слушатель измениения геолокации
     */
    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), "Првоверьте геолокацию", Toast.LENGTH_SHORT).show();
                return;
            }
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    /**
     * Получение геолокации от
     * слушателя изменения локации
     */
    private void showLocation(Location location) {
        if (location == null) return;
        latLng = new LatLng(location.getLatitude(), location.getLongitude());
    }

    @Override
    public void onBackPressed() {

    }

    /**
     * Сохранение в преференсе состояния
     * checkBox-а в диалоге
     *
     * @param checked - сохраняемый параметр
     */
    private void savePref(boolean checked) {
        SharedPreferences mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean("checkBox", checked);
        editor.apply();
    }

    /**
     * Получение из преференса состояния
     * checkbox-а при последнем отправлении.
     */
    private boolean loadPref() {
        SharedPreferences sharedPreferences = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        return sharedPreferences.getBoolean("checkBox", false);
    }
}
