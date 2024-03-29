package edu.sjsu.ireportgrp8;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;

public class UserRegistration extends AppCompatActivity {

    private DatabaseReference mDatabase;

    EditText tv_ScreenName,tv_FirstName,tv_LastName,tv_StreetName,tv_AptNo,tv_CityName,tv_ZipCode;
    Button registrationSubmit;

    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_registration);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference();

        registrationSubmit = (Button) findViewById(R.id.registration_submit);
        registrationSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitData();
            }
        });
        tv_AptNo = (EditText) findViewById(R.id.apt_number);
        tv_FirstName = (EditText) findViewById(R.id.first_name);
        tv_LastName = (EditText) findViewById(R.id.last_name);
        tv_StreetName = (EditText) findViewById(R.id.street_name);
        tv_ScreenName = (EditText) findViewById(R.id.screen_name);
        tv_CityName = (EditText) findViewById(R.id.city_name);
        tv_ZipCode = (EditText) findViewById(R.id.zipcode);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(mAuth.getCurrentUser() == null){
                    Intent intent =new Intent(getApplicationContext(),MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                else{
                    //tv_ScreenName.setText(mAuth.getCurrentUser().getEmail());
                    pullUserProfile();
                }
            }

        };



    }

    public void pullUserProfile(){
        mDatabase.child(getString(R.string.key_public_users)).child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                PublicUser userProfileData = dataSnapshot.getValue(PublicUser.class);
                tv_ScreenName.setText(userProfileData.getScreenName());
                tv_FirstName.setText(userProfileData.getFirstName());
                tv_LastName.setText(userProfileData.getLastName());
                tv_StreetName.setText(userProfileData.getStreetAddress());
                tv_AptNo.setText(userProfileData.getAptNumber());
                tv_CityName.setText(userProfileData.getCityName());
                tv_ZipCode.setText(userProfileData.getZipCode());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void submitData(){
        String screenName = tv_ScreenName.getText().toString();
        String firstName = tv_FirstName.getText().toString();
        String lastName = tv_LastName.getText().toString();
        String streetName = tv_StreetName.getText().toString();
        String aptNo = tv_AptNo.getText().toString();
        String cityName = tv_CityName.getText().toString();
        String zipcode = tv_ZipCode.getText().toString();

        //Replace hard coded email address with user's email address
        PublicUser newPublicUser = new PublicUser(mAuth.getCurrentUser().getEmail(),screenName,firstName,lastName,streetName,aptNo,cityName,zipcode);

        //replace screen name with User UUID
        mDatabase.child(getString(R.string.key_public_users)).child(mAuth.getCurrentUser().getUid()).setValue(newPublicUser);

        new SendEmailAsyncTask().execute();
        Toast.makeText(getApplicationContext(),getString(R.string.update_profile),Toast.LENGTH_LONG).show();


    }

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(mAuthListener);
    }

    public class SendEmailAsyncTask extends AsyncTask<Void,Void,Void>
    {
        @Override
        protected Void doInBackground(Void... voids) {
            Client client = Client.create();
            client.addFilter(new HTTPBasicAuthFilter("api",
                    "key-48171d3fd8035e9e3a21881aaade014a"));
            WebResource webResource =
                    client.resource("https://api.mailgun.net/v3/mail.pruthvi-nadunooru.name" +
                            "/messages");
            MultivaluedMapImpl formData = new MultivaluedMapImpl();
            formData.add("from", "CMPE 277 Grp 8 <akshay@mail.pruthvi-nadunooru.name>");
            formData.add("to", "akshay.mathur@sjsu.edu");
            formData.add("subject", "Test from Android");
            formData.add("text", "Your Profile has been updated");
            webResource.type(MediaType.APPLICATION_FORM_URLENCODED).
                    post(ClientResponse.class, formData);

            return null;
        }
    }
}
