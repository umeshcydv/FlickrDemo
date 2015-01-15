package com.android.flickrdemo;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Date;

import network.model.FlickrFeedDTO;

/**
 * Created by umeshchandrayadav on 15/01/15.
 */
public class FlickrIntentService extends IntentService {

    private SharedPreferences preferences;
    private static final String MY_PREFS_NAME = "com.myntra.android";
    private static final String SERVICE_URL = "https://api.flickr.com/services/feeds/photos_public.gne?format=json&lang=en-us";
    private static final String CACHE_TIME_KEY = "cache_time";
    private static final Long CACHE_DURATION = 60000L;
    public static final String NOTIFICATION = "com.android.flickrdemo.FlickrIntentService";

    public FlickrIntentService() {
        super("FlickrIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        preferences = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        FlickrFeedDTO dto = getDTOFromJsonString();
        Intent sendigIntent = new Intent(NOTIFICATION);
        Bundle bundle = new Bundle();
        bundle.putSerializable("Data", dto);
        sendigIntent.putExtras(bundle);
        sendBroadcast(sendigIntent);
    }

    private FlickrFeedDTO getDTOFromJsonString() {
        Gson gson = new Gson();
        FlickrFeedDTO dto = gson.fromJson(getJsonFromServer(), FlickrFeedDTO.class);
        return dto;
    }

    private String getJsonFromServer() {
        String jsonData = "";
        Long lastUpdated = preferences.getLong(CACHE_TIME_KEY, 0);
        if ((new Date().getTime() - lastUpdated) > CACHE_DURATION) {
            OkHttpClient client = new OkHttpClient();
            try {
                HttpURLConnection connection = client.open(new URL(SERVICE_URL));
                connection.setRequestMethod("GET");
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException();
                }
                byte[] res = IOUtils.toByteArray(connection.getInputStream());
                jsonData = new String(res, "UTF-8");
                jsonData = jsonData.substring("jsonFlickrFeed(".length(), jsonData.length() - 1);
                saveData(jsonData);
            } catch (MalformedURLException e) {
            } catch (ProtocolException e) {
            } catch (IOException e) {
            }
        } else {
            jsonData = preferences.getString(SERVICE_URL, "");
        }
        return jsonData;
    }

    private void saveData(String str) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SERVICE_URL, str);
        editor.putLong(CACHE_TIME_KEY, new Date().getTime());
        editor.apply();
    }

}
