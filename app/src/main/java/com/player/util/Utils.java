package com.player.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created on 3/8/16.
 */
public class Utils {

    public static boolean isInternetConnectionAlive(){
        try {

            URL url	= new URL("http://www.google.com");

            HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
            urlc.setRequestProperty("Connection", "close");
            urlc.setConnectTimeout(1000 * 10); // mTimeout is in seconds
            urlc.connect();
            if (urlc.getResponseCode() == 200 || urlc.getResponseCode() == 302) {
                //   Main.Log("getResponseCode == 200");
                return true;
            }
        } catch (MalformedURLException e1) {
            e1.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
