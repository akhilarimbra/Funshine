package me.akhilarimbra.funshine.activity;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import me.akhilarimbra.funshine.*;
import me.akhilarimbra.funshine.model.DailyWeatherReport;

public class WeatherActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    // final String latitude = "35.688879";
    // final String longitude = "139.619742";

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORDINATES = "?lat=" ;// + this.latitude + "&lon=" + this.longitude ?lat={lat}&lon={lon}
    final String URL_UNITS = "&units=imperial";
    final String URL_API_KEY = "&APPID=9d0eb7b77dab04ca7f1b6f7fb038aba4";

    private GoogleApiClient mGoogleApiClient;

    private final int PERMISSION_LOCATION = 111;
    private ArrayList<DailyWeatherReport> weatherReportList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(me.akhilarimbra.funshine.R.layout.activity_weather);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // downloadWeatherData();
    }

    public void downloadWeatherData(Location location) {
        final String fullCoordiantes = URL_COORDINATES + location.getLatitude() + "&lon=" + location.getLongitude();
        final String url = this.URL_BASE + fullCoordiantes + this.URL_UNITS + this.URL_API_KEY;

        final JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                // Log.v("Funshine", "Response : " + response.toString());
                // Toast.makeText(WeatherActivity.this, "Response : " + response.toString(), Toast.LENGTH_SHORT).show();

                try {
                    JSONObject city = response.getJSONObject("city"); // city is a json object, check sample json parse for reference
                    String cityName = city.getString("name"); // Since 'city' is a singular non-json object
                    String countryName = city.getString("country");

                    JSONArray list = response.getJSONArray("list");

                    for (int i = 0; i <= 4 ; i++) { // Getting the first 4 items in the list array
                        JSONObject object = list.getJSONObject(i);
                        JSONObject main = object.getJSONObject("main");

                        Double currentTemp = main.getDouble("temp"); // current temperature
                        Double minTemp = main.getDouble("temp_min"); // minimum temperature
                        Double maxTemp = main.getDouble("temp_max"); // maximum temperature

                        JSONArray weatherArray = object.getJSONArray("weather"); // parsing array containing weather details
                        JSONObject weather = weatherArray.getJSONObject(0); // getting the first and only array element

                        String weatherType = weather.getString("main"); // getting type of the weather
                        String rawDate = object.getString("dt_txt"); // parsing the raw date time

                        DailyWeatherReport report = new DailyWeatherReport(
                                cityName,
                                countryName,
                                weatherType,
                                rawDate,
                                currentTemp.intValue(),
                                minTemp.intValue(),
                                maxTemp.intValue()
                        );

                        Log.v("JSON", "Printing from class : " + report.getWeather()); // Checking Bugs
                        weatherReportList.add(report);
                    }

                    Log.v("JSON", "City : " + cityName + ", Country : " + countryName);
                } catch (JSONException exception) {
                    Log.v("JSON", "Exception : " + exception.getLocalizedMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Log.v("Funshine", "Error : " + error.getLocalizedMessage());
                // Toast.makeText(WeatherActivity.this, "Error : " + error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            }, PERMISSION_LOCATION);
        } else {
            startLocationServices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void startLocationServices() {
        try {
            LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this);
        } catch (SecurityException exception) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationServices();
                } else {
                    Toast.makeText(this, "Starting Location Services Failed : PERMISSION DENIED", Toast.LENGTH_SHORT).show();
                }
        }
    }
}
