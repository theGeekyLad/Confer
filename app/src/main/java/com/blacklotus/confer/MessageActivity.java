package com.blacklotus.confer;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class MessageActivity extends AppCompatActivity implements View.OnClickListener{

    EditText editmessage ;
    Button sendbutton ;
    DatabaseReference databaseReference , databaseUsers;
    private RecyclerView recyclerView;
    private FirebaseAuth firebaseAuth ;
    private FirebaseUser mCurrentUser ;
    String current_user ;
    String message ;
    FirebaseAuth.AuthStateListener authStateListener;
    String lenderEmailID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        lenderEmailID = getIntent().getStringExtra("email");
        //lenderEmailID = "rahul^pillai03@gmail^com";
        firebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = firebaseAuth.getCurrentUser();
        editmessage = (EditText)findViewById(R.id.editmessage);
        sendbutton = (Button)findViewById(R.id.sendbutton);

        current_user = Tools.formatEmail(mCurrentUser.getEmail()).toString().trim();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("borrower").child(current_user).child("messages");

        /*FirebaseDatabase.getInstance().getReference().child("borrower").child(current_user).child("messages").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator<DataSnapshot> iterator = dataSnapshot.getChildren().iterator();
                while (iterator.hasNext())
                {
                    DataSnapshot dataSnapshot1 = iterator.next();
                    databaseReference = FirebaseDatabase.getInstance().getReference().child("borrower").child(current_user).child("messages").child(dataSnapshot1.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/


        recyclerView = (RecyclerView)findViewById(R.id.messageRec);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() == null){
                    //startActivity(new Intent(MessageActivity.this,MainActivity.class));
                }
            }
        };
        sendbutton.setOnClickListener(this);
    }

    public void sendButtonclicked (View view)
    {


        message = editmessage.getText().toString().trim();

        databaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        //final DatabaseReference lenderDatabaseusers= FirebaseDatabase.getInstance().getReference().child("lender");

        if(!TextUtils.isEmpty(message))
        {

            final DatabaseReference newPost = databaseReference.push();
            databaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    newPost.child("content").setValue(message);

                    //Toast.makeText(getApplicationContext(),"yoyoyoy",Toast.LENGTH_LONG).show();
                    newPost.child("username").setValue(dataSnapshot.child("Name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });

                }


                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            //newPost.child("message").setValue(message);
            //newPost.child("username").setValue(lenderDatabaseusers);

            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());

        }
    }
    @Override
    public void onClick(View view) {
        if(view == sendbutton)
        {
            sendButtonclicked(sendbutton);
            recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter <Message,MessageViewholder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Message, MessageViewholder>(
                Message.class,
                R.layout.messagelistlayout,
                MessageViewholder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(MessageViewholder viewHolder, Message model, int position) {
                viewHolder.setContent(model.getContent());
                viewHolder.setUsername(model.getUsername());
            }

        };

        recyclerView.setAdapter(firebaseRecyclerAdapter);
        recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
    }

    public static class MessageViewholder extends RecyclerView.ViewHolder{

        View mView ;
        public MessageViewholder(View itemView) {

            super(itemView);
            mView = itemView;

        }
        public void setContent (String content)
        {
            TextView messageContent = (TextView)mView.findViewById(R.id.textmessage);
            messageContent.setText(content);
        }
        public  void setUsername (String username)
        {
            TextView username_text = (TextView)mView.findViewById(R.id.textviewname);
            username_text.setText(username);
        }
    }
}