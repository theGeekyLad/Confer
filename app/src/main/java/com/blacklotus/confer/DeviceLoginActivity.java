package com.blacklotus.confer;

import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;

import es.dmoral.toasty.Toasty;

public class DeviceLoginActivity extends AppCompatActivity {

    private static final int USER_PIN = 9820;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_login);

        getSupportActionBar().setTitle("Verify Identity");

        // init
        YoYo.with(Techniques.Pulse).repeat(YoYo.INFINITE).duration(500).playOn(findViewById(R.id.finerprint));

        // login by fingerprints
        FingerprintManager fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
        fingerprintManager.authenticate(null, null, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                verificationFailed(false);
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                super.onAuthenticationHelp(helpCode, helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                verified();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                verificationFailed(false);
            }
        }, null);

        // login by pin
        ((EditText) findViewById(R.id.pin)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Integer.parseInt((charSequence.toString().equals(""))?"0":charSequence.toString()) == USER_PIN) {
                    verified();
                    return;
                }
                if (charSequence.length() == 4)
                    verificationFailed(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void verified() {
        Toasty.success(getApplicationContext(), "Success", Toast.LENGTH_SHORT, true).show();
        startActivity(new Intent(DeviceLoginActivity.this, LoginActivity.class));
        finish();
    }

    private void verificationFailed(boolean pin) {
        Toasty.error(getApplicationContext(), "Invalid "+((pin)?"PIN":"Fingerprint"), Toast.LENGTH_SHORT, true).show();
    }
}
