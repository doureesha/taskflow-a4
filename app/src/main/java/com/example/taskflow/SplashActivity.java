package com.example.taskflow;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private Button btnGetStarted;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        AppData.init();

        View layoutLogo = findViewById(R.id.layoutLogo);
        TextView txtTagline = findViewById(R.id.txtTagline);
        btnGetStarted = findViewById(R.id.btnGetStarted);

        // the logo pops in, the text slides up and the button fades in
        Animation popIn = AnimationUtils.loadAnimation(this, R.anim.pop_in);
        layoutLogo.startAnimation(popIn);

        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        txtTagline.startAnimation(slideUp);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        btnGetStarted.startAnimation(fadeIn);

        // the small icons keep floating up and down, each with its own speed
        int[] iconIds = {R.id.iconNavy, R.id.iconTeal, R.id.iconBlue, R.id.iconGreen, R.id.iconPink};
        for (int i = 0; i < iconIds.length; i++) {
            Animation bob = AnimationUtils.loadAnimation(this, R.anim.float_bob);
            bob.setDuration(1200 + i * 300);
            findViewById(iconIds[i]).startAnimation(bob);
        }

        btnGetStarted.setOnClickListener(v -> {
            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
