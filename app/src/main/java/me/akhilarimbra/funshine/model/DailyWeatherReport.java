package me.akhilarimbra.funshine.model;

/**
 * Created by akhilraj on 17/12/16.
 */

public class DailyWeatherReport {
    private String cityName;
    private String countryName;
    private String weather;
    private String formattedDate;
    private int currentTemp;
    private int minTemp;
    private int maxTemp;

    public DailyWeatherReport(
            String cityName,
            String countryName,
            String weather,
            String rawDate,
            int currentTemp, int minTemp, int maxTemp) {
        this.cityName = cityName;
        this.countryName = countryName;
        this.weather = weather;
        this.formattedDate = rawDateToPretty(rawDate);
        this.currentTemp = currentTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
    }

    public String rawDateToPretty(String date) {
        // convert raw date into formatted date
        return "May 1st";
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getWeather() {
        return weather;
    }

    public String getFormattedDate() {
        return formattedDate;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }
}
