package com.blacklotus.confer;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blacklotus.confer.FirebaseTemplateClasses.Params;
import com.blacklotus.confer.FirebaseTemplateClasses.Success;
import com.blacklotus.confer.TemplateClasses.User;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ibm.watson.developer_cloud.conversation.v1.Conversation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import es.dmoral.toasty.Toasty;

public class ListActivity extends AppCompatActivity {

    FloatingActionsMenu floatingActionsMenu;
    FloatingActionButton statusFAB;

    DatabaseReference databaseReference;
    SharedPreferences sharedPreferences;

    String participant;
    String[] participants;

    ArrayList<User> lenderArrayList, borrowerArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        //notifyMe("Due Date Reached!", "If not paid by the end of the day a legal notice will be sent across");

        // init
        floatingActionsMenu = findViewById(R.id.fab);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        sharedPreferences = getSharedPreferences("prefs", MODE_PRIVATE);
        participants = new String[2];

        initFabs();

        // populate recycler view
        if (sharedPreferences.getBoolean("mode", true))
            initLendersList();
        else
            initBorrowersList();

        updateStatusFAB();

        // alert if payment date is due
        //ifTodayIsRepaymentDate();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            // lender -> borrower
            case 121:
                Bundle bundle = data.getExtras();
                String response = bundle.get(bundle.keySet().toArray()[0].toString()).toString();
                String[] responseParameters = response.split("&");
                if (responseParameters[3].substring(responseParameters[3].indexOf("=")+1).equalsIgnoreCase("success")) {
                    Calendar today = Calendar.getInstance();
                    Success success = new Success(
                            today.get(Calendar.DAY_OF_MONTH) + "-" + (today.get(Calendar.MONTH)+1) + "-" + today.get(Calendar.YEAR),
                            responseParameters[0].substring(responseParameters[0].indexOf("=")+1)
                    );
                    databaseReference
                            .child("lender")
                            .child(participants[0])
                            .child("lendable_borrowable")
                            .child("transaction")
                            .child("success")
                            .setValue(success);
                    databaseReference
                            .child("borrower")
                            .child(participants[1])
                            .child("lendable_borrowable")
                            .child("transaction")
                            .child("success")
                            .setValue(success);
                    statusFAB = addActionButton("Status", R.drawable.ic_attach_money_black_24dp, "#FFC107", "#FFA000");
                    sharedPreferences.edit().putInt("pending", 1).apply();
                }
                else
                    Toasty.error(ListActivity.this, "Transaction failed!", Toast.LENGTH_SHORT, true).show();
                break;

            // borrower -> lender
            case 169:
                statusFAB = addActionButton("Status", R.drawable.ic_attach_money_black_24dp, "#FFC107", "#FFA000");
                sharedPreferences.edit().putInt("pending", 1).apply();
        }
    }

    private void initFabs() {
        addActionButton("Profile", R.drawable.ic_account_circle_black_24dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ListActivity.this, ProfileActivity.class));
            }
        });
        addActionButton("Transactions", R.drawable.ic_history_black_24dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // perform TRANSACTION
            }
        });
        addActionButton("Customer Care", R.drawable.ic_perm_phone_msg_black_24dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ListActivity.this, ConversationActivity.class);
                intent.putExtra("email", sharedPreferences.getString("email", ""));
                startActivity(intent);
            }
        });
        addActionButton("+ Loan", R.drawable.ic_open_in_new_black_24dp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View paramsView = getLayoutInflater().inflate(R.layout.layout_params, null, false);
                new AlertDialog.Builder(ListActivity.this)
                        .setView(paramsView)
                        .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                pushParams(paramsView);
                            }
                        })
                        .create()
                        .show();
            }
        });
        addActionButton("Logout", R.drawable.ic_launcher).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(ListActivity.this, LoginActivity.class));
                finish();
            }
        });
    }

    private void pushParams(View paramsView) {
        float amount = Float.parseFloat(((EditText) paramsView.findViewById(R.id.amount)).getText().toString());
        float rate = Float.parseFloat(((EditText) paramsView.findViewById(R.id.rate)).getText().toString());
        int tenure = Integer.parseInt(((EditText) paramsView.findViewById(R.id.tenure)).getText().toString());
        if (!(amount >= 25000 && amount <= 500000 && rate >= 12)) {
            Toasty.error(ListActivity.this, "Kindly refer to RBI guidelines for data limits!", Toast.LENGTH_SHORT, true).show();
            return;
        }
        databaseReference
                .child((sharedPreferences.getBoolean("mode", false))?"lender":"borrower")
                .child(sharedPreferences.getString("email", ""))
                .child("lendable_borrowable")
                .child("params")
                .setValue(new Params(
                        amount,
                        rate,
                        tenure
                ));
    }

    private FloatingActionButton addActionButton(String title, int resId) {
        FloatingActionButton actionButton = new FloatingActionButton(getBaseContext());
        actionButton.setColorNormal(Color.parseColor("#FAFAFA"));
        actionButton.setColorPressed(Color.parseColor("#FF14AE87"));
        actionButton.setTitle(title);
        actionButton.setIconDrawable(getResources().getDrawable(resId, null));
        floatingActionsMenu.addButton(actionButton);
        return actionButton;
    }

    private FloatingActionButton addActionButton(String title, int resId, String colorNormal, String colorPressed) {
        FloatingActionButton actionButton = new FloatingActionButton(getBaseContext());
        actionButton.setColorNormal(Color.parseColor(colorNormal));
        actionButton.setColorPressed(Color.parseColor(colorPressed));
        actionButton.setTitle(title);
        actionButton.setIconDrawable(getResources().getDrawable(resId, null));
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = getLayoutInflater().inflate(R.layout.layout_timeline, null, false);
                new AlertDialog.Builder(ListActivity.this)
                        .setView(view1)
                        .create()
                        .show();
            }
        });
        return actionButton;
    }

    // update { status_FAB }
    private void updateStatusFAB() {
        if (sharedPreferences.getInt("pending", 0) == 1)
            statusFAB = addActionButton("Status", R.drawable.ic_attach_money_black_24dp, "#FFC107", "#FFA000");
    }

    // notify
    private void notifyMe(String title, String text) {
        Notification notification = new Notification.Builder(ListActivity.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .build();
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(25, notification);
    }

    // date of repayment check
    private void ifTodayIsRepaymentDate() {
        databaseReference
                .child((sharedPreferences.getBoolean("mode", true))?"lender":"borrower")
                .child(sharedPreferences.getString("email", ""))
                .child("lendable_borrowable")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        int tenure = dataSnapshot.child("params").child("tenure").getValue(Integer.class);
                        Calendar dateOfRepayment = new SimpleDateFormat("dd-mm-yy").getCalendar();
                        dateOfRepayment.set(Calendar.YEAR, dateOfRepayment.get(Calendar.YEAR)+tenure);
                        Calendar today = Calendar.getInstance();

                        if (today.compareTo(dateOfRepayment) == 0) {
                            floatingActionsMenu.removeButton(statusFAB);
                            notifyMe("Due Date Reached!", "If not paid by the end of the day a legal notice will be sent across");
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    // payment { lend }
    private void paymentLend(String pa, String pn, String tn, float am) {
        String upiUrl = "upi://pay?pa="+pa+"&pn="+pn+"&tn="+tn+"&am="+am+"&cu=INR";
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(upiUrl));
        Intent chooser = Intent.createChooser(intent, "Pay with UPI");
        startActivityForResult(chooser, 121);
    }

    // recycler view { lenders }
    private void initLendersList() {
        getSupportActionBar().setTitle("Available Lenders");
        lenderArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("lender").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> lenderIterator = dataSnapshot.getChildren().iterator(); // lender_email : { }
                while (lenderIterator.hasNext()) {
                    DataSnapshot lenderSnapshot = lenderIterator.next();
                    User user = new User();
                    user.email = lenderSnapshot.getKey();
                    user.name = lenderSnapshot.child("profile").child("name").getValue(String.class);
                    user.amount = lenderSnapshot.child("lendable_borrowable").child("params").child("amount").getValue(Float.class);
                    user.rate = lenderSnapshot.child("lendable_borrowable").child("params").child("rate").getValue(Float.class);
                    user.tenure = lenderSnapshot.child("lendable_borrowable").child("params").child("tenure").getValue(Float.class);
                    lenderArrayList.add(user);
                }
                RecyclerView lendersRecyclerView = findViewById(R.id.lendersList);
                lendersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                lendersRecyclerView.setAdapter(new LendersAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // recycler view { borrowers }
    private void initBorrowersList() {
        getSupportActionBar().setTitle("Available Borrowers");
        borrowerArrayList = new ArrayList<>();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        databaseReference.child("borrower").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> lenderIterator = dataSnapshot.getChildren().iterator(); // lender_email : { }
                while (lenderIterator.hasNext()) {
                    DataSnapshot borrowerSnapshot = lenderIterator.next();
                    Params params = borrowerSnapshot.child("lendable_borrowable").child("params").getValue(Params.class);
                    User user = new User();
                    user.email = borrowerSnapshot.getKey();
                    user.name = borrowerSnapshot.child("profile").child("name").getValue(String.class);
                    user.age = borrowerSnapshot.child("profile").child("age").getValue(Integer.class);
                    user.gender = borrowerSnapshot.child("profile").child("gender").getValue(Integer.class);
                    user.marital_status = borrowerSnapshot.child("profile").child("marital_status").getValue(Integer.class);
                    user.residence_type = borrowerSnapshot.child("profile").child("residence_type").getValue(Integer.class);
                    user.address = borrowerSnapshot.child("profile").child("address").getValue(String.class);
                    user.employment_type = borrowerSnapshot.child("profile").child("employment_type").getValue(Integer.class);
                    user.income = borrowerSnapshot.child("profile").child("income").getValue(Float.class);
                    user.account_balance = borrowerSnapshot.child("profile").child("account_balance").getValue(Float.class);
                    user.amount = borrowerSnapshot.child("lendable_borrowable").child("params").child("amount").getValue(Float.class);
                    user.purpose = borrowerSnapshot.child("profile").child("purpose").getValue(Integer.class);
                    borrowerArrayList.add(user);
                }
                RecyclerView lendersRecyclerView = findViewById(R.id.lendersList);
                lendersRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                lendersRecyclerView.setAdapter(new BorrowersAdapter());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // adapter { lender } ----------------------------------------------------------------------
    public class LendersAdapter extends RecyclerView.Adapter<LendersViewHolder> {

        public LendersAdapter() {
            super();
        }

        @Override
        public LendersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View lendersView = getLayoutInflater().inflate(R.layout.layout_lender, parent, false);
            return new LendersViewHolder(lendersView);
        }

        @Override
        public void onBindViewHolder(LendersViewHolder holder, final int position) {
            holder.nameView.setText(lenderArrayList.get(position).name);
            holder.amountView.setText("Rs. "+lenderArrayList.get(position).amount);
            holder.rateView.setText(lenderArrayList.get(position).rate+"%");
            holder.tenureView.setText(lenderArrayList.get(position).tenure+" years");
            holder.itemView.setId(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) { // recycler view item
                    new AlertDialog.Builder(ListActivity.this)
                            .setTitle("Application")
                            .setMessage("You can apply for the loan or chat with the lender for negotiation.")
                            .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) { // apply / accept button
                                    databaseReference
                                            .child((sharedPreferences.getBoolean("mode", true))?"lender":"borrower")
                                            .child(lenderArrayList.get(view.getId()).email)
                                            .child("lenable_borrowable")
                                            .child("transaction")
                                            .child("b_or_l")
                                            .setValue(sharedPreferences.getString("email", ""));
                                    Toasty.success(ListActivity.this, "Applied successfully!", Toast.LENGTH_SHORT, true).show();
                                    statusFAB = addActionButton("Status", R.drawable.ic_attach_money_black_24dp, "#FFC107", "#FFA000");
                                    sharedPreferences.edit().putInt("pending", 1).apply();
                                }
                            })
                            .setNegativeButton("Negotiate", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(ListActivity.this, MessageActivity.class);
                                    intent.putExtra("email", lenderArrayList.get(view.getId()).email);
                                    startActivity(intent);
                                }
                            })
                            .create()
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return lenderArrayList.size();
        }
    }

    // adapter { borrower } ----------------------------------------------------------------------
    public class BorrowersAdapter extends RecyclerView.Adapter<BorrowersViewHolder> {

        public BorrowersAdapter() {
            super();
        }

        @Override
        public BorrowersViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View borrowersView = getLayoutInflater().inflate(R.layout.layout_lender, parent, false);
            return new BorrowersViewHolder(borrowersView);
        }

        @Override
        public void onBindViewHolder(BorrowersViewHolder holder, final int position) {
            holder.nameView.setText(borrowerArrayList.get(position).name);
            holder.amountView.setText("Rs. "+borrowerArrayList.get(position).amount);
            holder.itemView.setId(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) { // recycler view item
                    // user detail in alert dialog
                    final View popUpLenderView = getLayoutInflater().inflate(R.layout.layout_borrower_profile, null, false);
                    final User borrowerUser = borrowerArrayList.get(view.getId());
                    ((TextView) popUpLenderView.findViewById(R.id.name)).setText(borrowerUser.name);
                    ((TextView) popUpLenderView.findViewById(R.id.address)).setText(borrowerUser.address);
                    //((TextView) popUpLenderView.findViewById(R.id.phone)).setText(borrowerUser));
                    ((TextView) popUpLenderView.findViewById(R.id.amount)).setText(borrowerUser.amount+"");
                    new AlertDialog.Builder(ListActivity.this)
                            .setView(popUpLenderView)
                            .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) { // apply / accept button
                                    // perform PAYMENT
                                    participants[0] = sharedPreferences.getString("email", "");
                                    participants[1] = borrowerUser.email;
                                    paymentLend(
                                            "lend_escrow@dcbbank",
                                            borrowerUser.name,
                                            ((EditText)popUpLenderView.findViewById(R.id.note)).getText().toString(),
                                            borrowerUser.amount);
                                }
                            })
                            .setNegativeButton("Discuss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(ListActivity.this, MessageActivity.class);
                                    intent.putExtra("email", borrowerArrayList.get(view.getId()).email);
                                    startActivity(intent);
                                }
                            })
                            .create()
                            .show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return borrowerArrayList.size();
        }
    }

    // view holder { lender } -----------------------------------------------------------------
    public class LendersViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView nameView, amountView, rateView, tenureView;
        public LendersViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameView = itemView.findViewById(R.id.name);
            amountView = itemView.findViewById(R.id.amount);
            rateView = itemView.findViewById(R.id.rate);
            tenureView = itemView.findViewById(R.id.tenure);
        }
    }

    // view holder { borrower } -----------------------------------------------------------------
    public class BorrowersViewHolder extends RecyclerView.ViewHolder {
        View itemView;
        TextView nameView, amountView, rateView, tenureView;
        public BorrowersViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            nameView = itemView.findViewById(R.id.name);
            amountView = itemView.findViewById(R.id.amount);
            //rateView = itemView.findViewById(R.id.rate);
            //tenureView = itemView.findViewById(R.id.tenure);
        }
    }

}
