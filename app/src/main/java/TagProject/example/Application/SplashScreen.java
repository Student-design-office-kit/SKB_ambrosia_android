package TagProject.example.Application;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.material.snackbar.Snackbar;
import android.view.View;
import TagProject.example.Application.ui.MapFragment;

/** Класс отвечает за отображение приветсвенного экрана
 и получение разрешения на использование геолокации */
public class SplashScreen extends AppCompatActivity {

    /** Метод собирает и запускает экран */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return;
            }
        }

        startActivity(new Intent(getApplicationContext(), MapFragment.class));
        finish();
    }

    /** Метод получает разрешение на геолокацию */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(new Intent(getApplicationContext(), MapFragment.class));
                    finish();
                } else {
                    View view = findViewById(android.R.id.content);
                    Snackbar.make(view, "Разрешение на доступ к местоположению необходимо для работы приложения", Snackbar.LENGTH_INDEFINITE)
                            .setAction("Повторить", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ActivityCompat.requestPermissions(SplashScreen.this,
                                            new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                            1);
                                }
                            })
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}