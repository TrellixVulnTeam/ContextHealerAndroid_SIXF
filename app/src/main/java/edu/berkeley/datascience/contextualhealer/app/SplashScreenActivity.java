package edu.berkeley.datascience.contextualhealer.app;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.berkeley.datascience.contextualhealer.R;

public class SplashScreenActivity extends AppCompatActivity {

    private Button btnGetStarted;
    private TextView txtGoalTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        txtGoalTitle = (TextView) findViewById(R.id.txtSplashTitle);
        btnGetStarted = (Button) findViewById(R.id.btnGetStarted);

        //Set Color of the logo
        Spannable word = new SpannableString("Goal");

        word.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.logoSecondaryColor)), 0, word.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        txtGoalTitle.setText(word);
        Spannable wordTwo = new SpannableString("Tick");

        wordTwo.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.logoMainColor)), 0, wordTwo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        txtGoalTitle.append(wordTwo);


        btnGetStarted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
}
