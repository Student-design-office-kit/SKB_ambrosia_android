package TagProject.example.Application;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ImageView backgroundImage = findViewById(R.id.SplashScreenImage);
        backgroundImage.startAnimation(AnimationUtils.loadAnimation(this, R.anim.side_slide));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                finish();
            }
        }, 3000);
    }
}