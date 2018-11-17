package cz.derdak.flashlight;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    //permission == 1 - permision granted
    //permission == 0 - permision denied

    private Camera camera;
    private Camera.Parameters params;
    private int interval;
    private String permission;
    SharedPreferences preferences;
    CheckBox check;
    ImageView imageFrontFlash;
    ToggleButton front_flash_toggle, flash;
    SeekBar seekBar;
    private boolean hasFlash, isFlashOn = false;
    private CountDownTimer countDownTimer;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
                recreate();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 1);
        }

        check = findViewById(R.id.checkBox);
        flash = findViewById(R.id.blink);
        imageFrontFlash = findViewById(R.id.imageFrontFlash);
        front_flash_toggle = findViewById(R.id.front_flash_Switch);
        seekBar = findViewById(R.id.seekBar);

        hasFlash = getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);

        if (!hasFlash) {

            Toast.makeText(MainActivity.this, R.string.sorry, Toast.LENGTH_LONG).show();

            return;
        }

        getCamera();


        front_flash_toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    imageFrontFlash.setVisibility(View.VISIBLE);
                    flash.setVisibility(View.INVISIBLE);
                    seekBar.setVisibility(View.INVISIBLE);
                    flash.setClickable(false);
                } else {
                    imageFrontFlash.setVisibility(View.INVISIBLE);
                    flash.setClickable(true);
                    flash.setVisibility(View.VISIBLE);
                    seekBar.setVisibility(View.VISIBLE);

                }
            }
        });

        seekBar.setMax(20);
        seekBar.setProgress(0);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                interval = progress;
                if (progress == 0) {
                    countDownTimer.cancel();
                    flash.setClickable(true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        flash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    if (interval != 0) {
                        blink(interval);
                        flash.setClickable(false);
                    } else {
                        if (!isFlashOn) {
                            flashOnOff(true);
                        } else {
                            flashOnOff(false);
                        }
                    }
            }
        });
    }

    private void flashOnOff(boolean on) {

        try {
            if (on) {

                params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                flash.setBackgroundResource(R.drawable.btn_on);
                isFlashOn = true;

            } else {

                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                flash.setBackgroundResource(R.drawable.btn_off);
                isFlashOn = false;

            }

            camera.setParameters(params);

        } catch (Exception e) {

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (check.isChecked()) {
            flashOnOff(true);
        } else {
            flashOnOff(false);
            try {
                countDownTimer.cancel();
            } catch (Exception e) {

            }
        }
    }

    private void getCamera() {
        if (camera == null) {
            try {
                camera = Camera.open();
                params = camera.getParameters();
            } catch (RuntimeException e) {
                Log.e(getString(R.string.errorX), e.getMessage());
            }
        }
    }

    private void blink(int intervalX) {

        countDownTimer = new CountDownTimer(100000, this.interval = intervalX) {

            public void onTick(long millisecondsUntilDone) {

                long sec = millisecondsUntilDone / 100;

                try {

                    if (sec % interval == 0) {
                        flashOnOff(true);
                    } else {
                        flashOnOff(false);
                    }
                } catch (ArithmeticException n) {

                }

            }

            public void onFinish() {

                flashOnOff(false);

            }

        }.start();

    }

}
