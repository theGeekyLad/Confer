package com.blacklotus.confer;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blacklotus.confer.FirebaseTemplateClasses.Params;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class MainActivity extends AppCompatActivity {

    private static final int USER_PIN = 9820;

    public static final String EMAIL = "rahul^pillai03@gmail^com";
    //public static final String EMAIL = "mohitcagarwal@gmail^com";
    public static final boolean MODE_LENDER = false;

    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);

        // ---------------------------------------------------------------------------------------

        // TEMP (to Login/SignUpActivity)
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", EMAIL);
        editor.putBoolean("mode", MODE_LENDER);
        editor.apply();

        // ---------------------------------------------------------------------------------------

        // animate home
        YoYo.with(Techniques.Bounce).duration(500).playOn((findViewById(R.id.logo)));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, DeviceLoginActivity.class));
            }
        }, 500);

    }
}
