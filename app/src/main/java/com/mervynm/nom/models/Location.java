package com.mervynm.nom.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("Location")
public class Location extends ParseObject {
    public static final String KEY_NAME = "name";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_LAT_LONG = "latLong";
    public static final String KEY_RATING = "rating";
    public static final String KEY_PRICE_LEVEL = "priceLevel";
    public static final String KEY_PICTURE = "picture";

    public Location() {}

    public String getName() {
        return getString(KEY_NAME);
    }

    public void setName(String name) {
        put(KEY_NAME, name);
    }

    public String getAddress() {
        return getString(KEY_ADDRESS);
    }

    public void setAddress(String address) {
        put(KEY_ADDRESS, address);
    }

    public ParseGeoPoint getLatLong() {
        return getParseGeoPoint(KEY_LAT_LONG);
    }

    public void setLatLong(ParseGeoPoint latLong) {
        put(KEY_LAT_LONG, latLong);
    }

    public double getRating() {
        return getDouble(KEY_RATING);
    }

    public void setRating(double rating) {
        put(KEY_RATING, rating);
    }

    public int getPriceLevel() {
        return getInt(KEY_PRICE_LEVEL);
    }

    public void setPriceLevel(int priceLevel) {
        put(KEY_PRICE_LEVEL, priceLevel);
    }

    public ParseFile getPicture() {
        return getParseFile(KEY_PICTURE);
    }

    public void setPicture(ParseFile picture) {
        put(KEY_PICTURE, picture);
    }
}
