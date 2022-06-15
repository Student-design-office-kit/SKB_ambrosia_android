package TagProject.example.Application;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.myapplication.R;

/** Класс отвечает за отображение приветсвенного экрана
    и получение разрешения на использование геолокации */
public class SplashScreen extends AppCompatActivity {

    /** Метод собирает и запускает экран */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        if ( Build.VERSION.SDK_INT >= 23){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED  ){
                requestPermissions(new String[]{
                                android.Manifest.permission.ACCESS_FINE_LOCATION},
                        1);
                return ;
            }
        }

        splashScreen();
    }

    /** Метод отображает анимацию экрана */
    void splashScreen(){
        ImageView backgroundImage = findViewById(R.id.SplashScreenImage);
        backgroundImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.side_slide));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                finish();
            }
        }, 2000);
    }

    /** Метод получает разрешение на геолокацию */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    splashScreen();
                } else {
                    Toast.makeText(this, "your message", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}