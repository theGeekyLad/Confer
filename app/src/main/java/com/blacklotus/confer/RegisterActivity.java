package com.blacklotus.confer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener , AdapterView.OnItemSelectedListener{

    private Button buttonregister;
    EditText editTextemail , aadharNumber , name ,Address ,age ;
    EditText accountbalance , familycount , income ,maritialstatus;
    private EditText editTextpass;
    private TextView newuserlink , statuschange;
    private ProgressDialog progressdialog;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference mdata;
    Spinner employmentType , Gender , Purpose , Residence_type ;
    String purPose ,Employment_TYpe , genDer ,residence_Type ;
    Switch simpleSwitch ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() == null) {

            //startActivity(new Intent(this, Main2Activity.class));
            //loginactivity
        }

        simpleSwitch = (Switch)findViewById(R.id.switch1);
        employmentType = (Spinner)findViewById(R.id.employmentTypeSpinner);
        Gender=(Spinner)findViewById(R.id.genderSpinner) ;
        Purpose = (Spinner)findViewById(R.id.purposeSpinner);
        Residence_type = (Spinner)findViewById(R.id.residence_typeSpinner);

        employmentType.setOnItemSelectedListener(this);
        Gender.setOnItemSelectedListener(this);
        Purpose.setOnItemSelectedListener(this);
        Residence_type.setOnItemSelectedListener(this);

        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        employmentType.setAdapter(aa);
        Gender.setAdapter(aa);
        Purpose.setAdapter(aa);
        Residence_type.setAdapter(aa);
        age = (EditText)findViewById(R.id.age);
        accountbalance = (EditText)findViewById(R.id.accountbalance);
        familycount = (EditText)findViewById(R.id.familyCount) ;
        income = (EditText)findViewById(R.id.income);
        maritialstatus = (EditText)findViewById(R.id.marritial_status) ;

        name = (EditText)findViewById(R.id.name);
        Address = (EditText) findViewById(R.id.address);
        statuschange = (TextView)findViewById(R.id.status);
        aadharNumber = (EditText)findViewById(R.id.aadharValidate);
        progressdialog = new ProgressDialog(this);
        buttonregister = (Button) findViewById(R.id.button1);
        editTextemail = (EditText) findViewById(R.id.edittextemail);
        editTextpass = (EditText) findViewById(R.id.edittextpass);
        newuserlink = (TextView) findViewById(R.id.textview1);
        buttonregister.setOnClickListener(this);
        newuserlink.setOnClickListener(this);
        mdata = FirebaseDatabase.getInstance().getReference().child("Users");




        aadharNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(aadharverification()==1)
                {
                    statuschange.setText("VALID");
                    statuschange.setTextColor(Color.GREEN);
                    buttonregister.setEnabled(true);
                }
                else if(aadharverification()==0)
                {
                    statuschange.setText("NOT VALID");
                    statuschange.setTextColor(Color.RED);
                    buttonregister.setEnabled(false);

                }
                else if(aadharverification()==3)
                {
                    statuschange.setText("STATUS");
                    statuschange.setTextColor(Color.GREEN);
                    buttonregister.setEnabled(false);
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }
    public int aadharverification()
    {


        String a = aadharNumber.getText().toString();
        boolean result = Verhoeff.validateVerhoeff(a);

        if(a.length()==12)
        {
            if(result == true)
            {
                return 1 ;
            }
            else
                return 0;

        }
        else if(a.length()==0)
        {
            return 3;
        }
        else
            return 0;

    }

    private void registeruser() {

        final String Age = age.getText().toString().trim();
        final String AccountBalance = accountbalance.getText().toString().trim();
        final String Martial_Status = maritialstatus.getText().toString().trim();
        final String Income = income.getText().toString().trim();
        final String Family_Count = familycount.getText().toString().trim();
        final String Name = name.getText().toString().trim();
        final String address = Address.getText().toString().trim();
        //final String MobileNumber = mobileNumber.getText().toString().trim();
        final String a = editTextemail.getText().toString().trim();
        final String email = Tools.formatEmail(a);

        String pass = editTextpass.getText().toString().trim();
        if (TextUtils.isEmpty(a) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(Name) || TextUtils.isEmpty(address) )  {
            Toast.makeText(getApplicationContext(), "Please Enter all of your Credentials ", Toast.LENGTH_LONG).show();
            return;
        }


        progressdialog.setMessage("Registering User ....");
        progressdialog.show();
        firebaseAuth.createUserWithEmailAndPassword(a, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressdialog.dismiss();
                        if (task.isSuccessful()) {


                            DatabaseReference databaseReference = mdata.child("borrower").child(email).child("profile");
                            //String userid = mdata.push().getKey();
                            //String userid = firebaseAuth.getCurrentUser().getEmail();

                            databaseReference.child("name").setValue(Name);
                            //databaseReference.child("phone").setValue(MobileNumber);
                            databaseReference.child("address").setValue(address);
                            databaseReference.child("age").setValue(Age);
                            databaseReference.child("account_balance").setValue(AccountBalance);
                            databaseReference.child("employment_type").setValue(Employment_TYpe);
                            databaseReference.child("family_member_count").setValue(Family_Count);
                            databaseReference.child("gender").setValue(genDer);
                            databaseReference.child("income").setValue(Income);
                            databaseReference.child("marital_status").setValue(Martial_Status);
                            databaseReference.child("purpose").setValue(purPose);
                            databaseReference.child("residence_type").setValue(residence_Type);


                            String userid = firebaseAuth.getCurrentUser().getUid();
                            DatabaseReference current_database_reference = mdata.child(userid);
                            current_database_reference.child("Name").setValue(email);

                            SharedPreferences sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
                            sharedPreferences.edit().putString("email", Tools.formatEmail(editTextemail.getText().toString().trim())).apply();

                            if (((Switch) findViewById(R.id.switch1)).isChecked())
                                sharedPreferences.edit().putBoolean("mode", false).apply();
                            else
                                sharedPreferences.edit().putBoolean("mode", true).apply();

                            Toast.makeText(getApplicationContext(), "You are Registered ", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                });

    }

    @Override
    public void onClick(View view) {
        if (view == buttonregister) {
            //for registering
            registeruser();
        }
        if (view == newuserlink) {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            //for new user
        }

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(view == employmentType)
        {
            String Employment_TYpe = employmentType.getSelectedItem().toString();

        }
        else if(view == Gender )
        {
            String genDer = Gender.getSelectedItem().toString();

        }
        else if(view == Residence_type)
        {

            String residence_Type = Residence_type.getSelectedItem().toString();
        }
        else
        {
            String purPose = Purpose.getSelectedItem().toString();
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}