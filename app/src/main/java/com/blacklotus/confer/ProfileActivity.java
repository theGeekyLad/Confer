package com.blacklotus.confer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.blacklotus.confer.TemplateClasses.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.zip.Inflater;

public class ProfileActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    TextView nameProfile, emailProfile, numberProfile, addressProfile;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    ArrayList lenderArrayList, BorrowerArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        nameProfile = (TextView) findViewById(R.id.nameprofile);
        addressProfile = (TextView) findViewById(R.id.addressprofile);
        numberProfile = (TextView) findViewById(R.id.numberprofile);
        emailProfile = (TextView) findViewById(R.id.emailidprofile);
        recyclerView = (RecyclerView) findViewById(R.id.lastTransactionRecycle);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        LenderTransaction myAdapter = new LenderTransaction();
        recyclerView.setAdapter(myAdapter);
        profile();

    }

    public void profile() {

        String a = firebaseUser.getEmail().toString().trim();
        final String email = Tools.formatEmail(a).toString().trim();
        emailProfile.setText(a);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nameProfile.setText(dataSnapshot.child(email).child("name").getValue().toString());
                numberProfile.setText(dataSnapshot.child(email).child("phone").getValue().toString());
                addressProfile.setText(dataSnapshot.child(email).child("address").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> lenderIterator = dataSnapshot.getChildren().iterator();
                // lender_email : { }
                while (lenderIterator.hasNext()) {
                    DataSnapshot lenderSnapshot = lenderIterator.next();
                    Iterator<DataSnapshot> planIterator = lenderSnapshot.child("lender").getChildren().iterator(); // lender_plan : { }
                    while (planIterator.hasNext()) {
                        DataSnapshot lenderPlanSnapshot = planIterator.next();
                        User user = new User();
                        user.name = lenderSnapshot.child("name").getValue(String.class);
                        user.address = lenderSnapshot.child("address").getValue(String.class);
                        user.amount = lenderPlanSnapshot.child("amount").getValue(Float.class);
                        user.rate = lenderPlanSnapshot.child("rate").getValue(Float.class);
                        user.tenure = lenderPlanSnapshot.child("tenure").getValue(Float.class);

                        /*String[] lenderData = new String[4];
                        lenderData[0] = lenderSnapshot.child("name").getValue(String.class);
                        lenderData[1] = "Rs. " + lenderPlanSnapshot.child("amount").getValue(Float.class);
                        lenderData[2] = lenderPlanSnapshot.child("rate").getValue(Float.class) + "%";
                        lenderData[3] = lenderPlanSnapshot.child("tenure").getValue(Float.class) + " years";
                          */
                        //lenderArrayList.add(user);bo
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public class LenderTransaction extends RecyclerView.Adapter<LendersViewHolder> {

        public LenderTransaction() {
            super();
        }

        @Override
        public LendersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View thisItemview = getLayoutInflater().inflate(R.layout.transactionrecycle, parent, false);
            return new LendersViewHolder(thisItemview);
        }

        @Override
        public void onBindViewHolder(LendersViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private class LendersViewHolder extends RecyclerView.ViewHolder {

        public LendersViewHolder(View itemView) {
            super(itemView);
        }


    }
}