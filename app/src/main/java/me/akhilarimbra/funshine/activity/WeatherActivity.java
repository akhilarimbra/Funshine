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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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
import me.akhilarimbra.funshine.R;
import me.akhilarimbra.funshine.model.DailyWeatherReport;

public class WeatherActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        LocationListener {

    // final String latitude = "35.688879";
    // final String longitude = "139.619742";

    final String URL_BASE = "http://api.openweathermap.org/data/2.5/forecast";
    final String URL_COORDINATES = "?lat=" ;// + this.latitude + "&lon=" + this.longitude ?lat={lat}&lon={lon}
    final String URL_UNITS = "&units=metric";
    final String URL_API_KEY = "&APPID=9d0eb7b77dab04ca7f1b6f7fb038aba4";

    private GoogleApiClient mGoogleApiClient;

    private final int PERMISSION_LOCATION = 111;
    private ArrayList<DailyWeatherReport> weatherReportList = new ArrayList<>();

    private ImageView weatherIconMini, weatherIcon;
    private TextView weatherDate, currentTemp, minTemp, cityCountry, weatherDescription;

    WeatherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(me.akhilarimbra.funshine.R.layout.activity_weather);

        weatherIconMini = (ImageView) findViewById(R.id.weatherIconMini);
        weatherIcon = (ImageView) findViewById(R.id.weatherIcon);

        weatherDate = (TextView) findViewById(R.id.weatherDate);
        currentTemp = (TextView) findViewById(R.id.currentTemp);
        minTemp = (TextView) findViewById(R.id.minTemp);
        cityCountry = (TextView) findViewById(R.id.cityCountry);
        weatherDescription = (TextView) findViewById(R.id.weatherDescription);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.content_weather_reports);

        adapter = new WeatherAdapter(weatherReportList);
        recyclerView.setAdapter(adapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);

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

                updateUI();
                adapter.notifyDataSetChanged();
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

    public void updateUI() {
        if (weatherReportList.size() > 0) {
            DailyWeatherReport report = weatherReportList.get(0);

            switch (report.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLEAR:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.cloudy));
                    weatherIconMini.setImageDrawable(getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_HEART:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.rainy));
                    weatherIconMini.setImageDrawable(getDrawable(R.drawable.rainy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_SNOW:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.snow));
                    weatherIconMini.setImageDrawable(getDrawable(R.drawable.snow_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_WIND:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    break;
                default:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny));
                    weatherIconMini.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    Toast.makeText(this, "Weather Condition Unknown", Toast.LENGTH_SHORT).show();
                    break;
            }

            weatherDate.setText("Today, December 21");
            currentTemp.setText(Integer.toString(report.getCurrentTemp()) + "°");
            minTemp.setText(Integer.toString(report.getMinTemp()) + "°");
            cityCountry.setText(report.getCityName() + ", " + report.getCountryName());
            weatherDescription.setText(report.getWeather());
        }
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

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder> {

        private ArrayList<DailyWeatherReport> dailyWeatherReports;

        public WeatherAdapter(ArrayList<DailyWeatherReport> dailyWeatherReports) {
            this.dailyWeatherReports = dailyWeatherReports;
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report = dailyWeatherReports.get(position);
            holder.updateUI(report);
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View cardView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherReportViewHolder(cardView);
            //return null;
        }

        @Override
        public int getItemCount() {
            return dailyWeatherReports.size();
        }
    }

    public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

        private ImageView weatherIcon;
        private TextView weatherDate;
        private TextView weatherDescription;
        private TextView maximumTemperature;
        private TextView minimumTemperature;

        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            weatherIcon = (ImageView) itemView.findViewById(R.id.weather_icon);
            weatherDate = (TextView) itemView.findViewById(R.id.weather_day);
            weatherDescription = (TextView) itemView.findViewById(R.id.weather_description);
            maximumTemperature = (TextView) itemView.findViewById(R.id.max_temp);
            minimumTemperature = (TextView) itemView.findViewById(R.id.min_temp);
        }

        public void updateUI(DailyWeatherReport dailyWeatherReport) {

            weatherDate.setText(dailyWeatherReport.getFormattedDate());
            weatherDescription.setText(dailyWeatherReport.getWeather());
            maximumTemperature.setText(dailyWeatherReport.getMaxTemp() + "°");
            minimumTemperature.setText(dailyWeatherReport.getMinTemp() + "°");

            switch (dailyWeatherReport.getWeather()) {
                case DailyWeatherReport.WEATHER_TYPE_CLEAR:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_CLOUDS:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.cloudy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_HEART:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_RAIN:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.rainy_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_SNOW:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.snow_mini));
                    break;
                case DailyWeatherReport.WEATHER_TYPE_WIND:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    break;
                default:
                    weatherIcon.setImageDrawable(getDrawable(R.drawable.sunny_mini));
                    //Toast.makeText(this, "Weather Condition Unknown", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
