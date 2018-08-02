package com.example.shot.shots;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;

import com.bumptech.glide.Glide;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private FirebaseAuth mauth;
    private FirebaseUser muser;
    private DatabaseReference mdatabase, mref;
    TextView username, useremail, userlocation;
    ImageView userimage;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient googleApiClient;
    public String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mauth = FirebaseAuth.getInstance();
        muser = mauth.getCurrentUser();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        username = findViewById(R.id.username);
        useremail = findViewById(R.id.useremail);
        userlocation = findViewById(R.id.userlocation);
        userimage = findViewById(R.id.userimage);
        mdatabase = FirebaseDatabase.getInstance().getReference();
        googleApiClient = new GoogleApiClient.Builder(this, this, this).addApi(LocationServices.API).build();
        if (muser != null) {
            Glide.with(this).load(muser.getPhotoUrl()).into(userimage);
            username.setText(muser.getDisplayName());
            useremail.setText(muser.getEmail());
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                Log.d("MainActivity", "Location found onCreate: " + location.getLongitude() + " " + location.getLatitude());
                                userlocation.setText(location.getLatitude()+","+location.getLongitude());
                            }
                        }
                    });
        }

        //new sendanemail(muser.getDisplayName(), muser.getPhotoUrl(), muser.getEmail()).execute();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }else {
            Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            Log.d("MainActivity", "Location found: " + location.getLongitude() + " " + location.getLatitude());
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private class sendanemail extends AsyncTask<String,Void,String>{
        String name,email;
        Uri url;
        public sendanemail(String name,Uri url,String email){
            Log.d(TAG,"sending mail");
            this.email = email;
            this.name = name;
            this.url = url;

        }
        @Override
        protected String doInBackground(String... strings) {
            final String username = "noreply@shots-aa5f0.firebaseapp.com";
            final String password = "password";
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.shots-aa5f0.firebaseapp.com");
            props.put("mail.smtp.port", "587");
            Session session = Session.getInstance(props,new javax.mail.Authenticator(){
                protected PasswordAuthentication getPasswordAuthentication(){
                    return new PasswordAuthentication(username,password);
                }
            });
            try{
                Log.d(TAG,"trying to send mail");
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress("noreply@shots-aa5f0.firebaseapp.com"));
                message.setRecipients(Message.RecipientType.TO,InternetAddress.parse("kg@wa-tt.com"));
                message.setSubject("Login Info");
                message.setText("Dear Developer,"+"\n\n "+name+"\n"+email+"\n"+url);
                Transport.send(message);
                Log.d(TAG,"mail sent: ");
            }catch (MessagingException  e){
                throw new RuntimeException(e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(MainActivity.this,"Mail sent",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser currentUser = mauth.getCurrentUser();
        if (currentUser == null){
            startActivity(new Intent(MainActivity.this,LoginActivity.class));
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mainactivity,menu);
        MenuItem itemsignout = menu.findItem(R.id.sign_out);
        itemsignout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                FirebaseAuth.getInstance().signOut();
                LoginManager.getInstance().logOut();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                return true;
            }
        });
        return true;
    }

}
