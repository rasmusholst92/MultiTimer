package com.example.multitimer;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private NumberPicker numberPickerHour, numberPickerMinute, numberPickerSecond;
    private LinearLayout timersContainer;
    boolean isPaused = false;
    boolean isRestarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGui();
    }

    private void initGui() {
        numberPickerHour    = findViewById(R.id.numberPickerHour);
        numberPickerMinute  = findViewById(R.id.numberPickerMinute);
        numberPickerSecond  = findViewById(R.id.numberPickerSecond);

        numberPickerHour.setMinValue(0);
        numberPickerHour.setMaxValue(23);
        numberPickerMinute.setMinValue(0);
        numberPickerMinute.setMaxValue(59);
        numberPickerSecond.setMinValue(0);
        numberPickerSecond.setMaxValue(59);

        timersContainer = findViewById(R.id.timersContainer);

        Button buttonAddTimer = findViewById(R.id.buttonAddTimer);
        buttonAddTimer.setOnClickListener(view -> addNewTimer());

        Button buttonReset = findViewById(R.id.buttonReset);
        buttonReset.setOnClickListener(view -> resetTimer());
    }

    @SuppressLint("SetTextI18n")
    private void addNewTimer() {
        int hours   = numberPickerHour.getValue();
        int minutes = numberPickerMinute.getValue();
        int seconds = numberPickerSecond.getValue();

        // Hvis alle værdier = 0, return.
        if (hours == 0 && minutes == 0 && seconds == 0) {
            Toast.makeText(this, "SÆT EN TIMER FOR AT STARTE", Toast.LENGTH_SHORT).show();
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);
        // Opretter et nyt objekt af timer_view og tilføjer det til containeren.
        View timerView = inflater.inflate(R.layout.timer_item, timersContainer, false);

        TextView timerTextView  = timerView.findViewById(R.id.timerTextView);
        Button pauseButton      = timerView.findViewById(R.id.pauseButton);
        Button deleteButton     = timerView.findViewById(R.id.deleteButton);

        int totalSeconds = (int) (hours * 3600L + minutes * 60L + seconds);

        Handler handler     = new Handler();
        Runnable runnable   = new Runnable() {
            int remainingSeconds = totalSeconds;
            boolean isPaused = false;
            boolean timerCompleted = false;

            @SuppressLint("DefaultLocale")
            @Override
            public void run() {
                // Opdaterer timer display hvis antal sekunder tilbage er højere end 1 & timeren ikke er pauset.
                if (remainingSeconds > 0 && !isPaused) {
                    int hours  = remainingSeconds / 3600;
                    int mins   = (remainingSeconds % 3600) / 60;
                    int secs   = remainingSeconds % 60;
                    timerTextView.setText(String.format("%02d:%02d:%02d", hours, mins, secs));
                    remainingSeconds--;
                    handler.postDelayed(this, 1000);
                } else if (!isPaused) {
                    timerTextView.setText("00:00:00");
                    timerCompleted = true;
                    pauseButton.setText(R.string.restart);
                    pauseButton.setOnClickListener(v -> {
                        if (!isPaused && !timerCompleted) {
                            handler.removeCallbacks(this);
                            isPaused = true;
                            pauseButton.setText(R.string.fortsaet);
                        } else if (timerCompleted) {
                            remainingSeconds = totalSeconds;
                            timerCompleted = false;
                            isPaused = false;
                            handler.postDelayed(this, 0);
                            pauseButton.setText(R.string.pause);
                        } else {
                            isPaused = false;
                            handler.postDelayed(this, 0);
                            pauseButton.setText(R.string.pause);
                        }
                    });

                }
            }
        };

        // Starter ny thread uden delay.
        handler.postDelayed(runnable, 0);

        pauseButton.setOnClickListener(v -> {
            if (!isPaused) {
                handler.removeCallbacks(runnable);
                isPaused = true;
                pauseButton.setText(R.string.fortsaet);
            } else {
                isPaused = false;
                handler.postDelayed(runnable, 0);
                pauseButton.setText(R.string.pause);
            }
        });

        deleteButton.setOnClickListener(v -> {
            handler.removeCallbacks(runnable);
            timersContainer.removeView(timerView);
        });

        timersContainer.addView(timerView);
    }

    private void resetTimer() {
        numberPickerSecond.setValue(0);
        numberPickerMinute.setValue(0);
        numberPickerHour.setValue(0);
    }
}
