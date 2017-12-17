package com.blacklotus.confer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button buttonlogin;
    private EditText editTextemail ;
    private EditText editTextpass ;
    private TextView signuplink;
    private ProgressDialog progressDialog ;
    private FirebaseAuth firebaseAuth ;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null)
        {
            startActivity(new Intent(this, ListActivity.class));
            finish();
        }
        progressDialog = new ProgressDialog(this );
        buttonlogin = (Button) findViewById(R.id.button2);
        editTextemail= (EditText) findViewById(R.id.edittextemail);
        editTextpass= (EditText) findViewById(R.id.edittextpass);
        signuplink = (TextView) findViewById(R.id.textview2);
        buttonlogin.setOnClickListener(this);
        signuplink.setOnClickListener(this);
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    private void userlogin()
    {
        String email = editTextemail.getText().toString().trim();
        String  pass = editTextpass.getText().toString().trim();
        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(),"Please Enter email ",Toast.LENGTH_LONG).show();
            return;
        }
        if(TextUtils.isEmpty(pass))
        {
            Toast.makeText(this,"Enter Your password",Toast.LENGTH_LONG).show();
            return;
        }
        progressDialog.setMessage("Logging User ...");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email,pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if(task.isSuccessful())
                        {
                            final SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
                            sharedPreferences.edit().putString("email", Tools.formatEmail(editTextemail.getText().toString().trim())).apply();
                            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child("lender").child(Tools.formatEmail(editTextemail.getText().toString().trim())).exists()) {
                                        sharedPreferences.edit().putBoolean("mode", false).apply();
                                    }
                                    else
                                        sharedPreferences.edit().putBoolean("mode", true).apply();
                                    Toast.makeText(getApplicationContext(),"You are Signed In",Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(getApplicationContext(), ListActivity.class));
                                    finish();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                        else {

                            Toast.makeText(getApplicationContext(),"Failed To Sign",Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
    public void checkuserexist()
    {
        final String user_id = firebaseAuth.getCurrentUser().getUid();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(user_id))
                {
                    //startActivity(new Intent(getApplicationContext(), HistoryActivity.class));
                    //Here start new activity
                    Toast.makeText(getApplicationContext(),"You are Signed In",Toast.LENGTH_LONG).show();

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    @Override
    public void onClick(View view) {
        if(view == buttonlogin )
        {
            userlogin();
            return;
        }
        if (view == signuplink)
        {
            finish();
            startActivity(new Intent(this, RegisterActivity.class));
            return;
        }
    }
}
