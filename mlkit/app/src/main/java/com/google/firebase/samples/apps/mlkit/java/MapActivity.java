package com.google.firebase.samples.apps.mlkit.java;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.samples.apps.mlkit.R;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener {
    private final String TAG = "MapActivity";

    private GoogleMap mMap;
    private RequestQueue queue;
    private FusedLocationProviderClient fusedLocationClient;

    private Location mCurrentLocation;

    TextToSpeech t1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem reminders= new PrimaryDrawerItem().withIdentifier(0).withName("Reminders Before");
        PrimaryDrawerItem frequency = new PrimaryDrawerItem().withIdentifier(1).withName("Frequency");
        PrimaryDrawerItem feedback = new PrimaryDrawerItem().withIdentifier(2).withName("Feedback after");
        PrimaryDrawerItem camera = new PrimaryDrawerItem().withIdentifier(3).withName("Camera");



        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(this)
                //.withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        reminders,
                        frequency,
                        feedback,
                        camera
                )

                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 3) {
                            // load tournament screen
                            Intent intent = new Intent(getBaseContext(), LivePreviewActivity.class);
                            view.getContext().startActivity(intent);
                        }
                        return true;
                    }

                })
                .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        new Timer().scheduleAtFixedRate(new TimerTask(){
            @Override
            public void run(){
                Log.i("tag", "A timed event.");
            }
        },0,100);
        t1=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // TODO: Before enabling the My Location layer, you must request
        // location permission from the user. This sample does not include
        // a request for location permission.
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);


        String url = "https://maps.london.ca/arcgisa/rest/services/OpenData/OpenData_Transportation/MapServer/16/query?where=1%3D1&outFields=*&outSR=4326&f=json";
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject json = new JSONObject(response);
                            JSONArray features = json.getJSONArray("features");
                            for (int i = 0; i < features.length(); i++) {
                                JSONObject f = features.getJSONObject(i);
                                String type = f.getJSONObject("attributes").getString("IntersectionType");
                                double x = f.getJSONObject("geometry").getDouble("x");
                                double y = f.getJSONObject("geometry").getDouble("y");

                                LatLng pos = new LatLng(y, x);
                                mMap.addMarker(new MarkerOptions().position(pos).title(type));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError e) {
                e.printStackTrace();
            }
        });
        queue.add(stringRequest);

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mCurrentLocation = location;
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(location.getLatitude(), location.getLongitude()), 15));
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to get location: ", e);
                    }
                });
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        t1.speak("HELLO FROM THE OTHER SIDE 1", TextToSpeech.QUEUE_FLUSH, null);
        t1.speak("HELLO FROM THE OTHER SIDE 2", TextToSpeech.QUEUE_FLUSH, null);
        t1.speak("HELLO FROM THE OTHER SIDE 3", TextToSpeech.QUEUE_FLUSH, null);

        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }
}
