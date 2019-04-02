package com.lib.adloader.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.util.Log;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ServiceHandler {

    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
    // static String base_url = null;

    public static String host = "13.232.25.94";
    public ServiceHandler() {
    }

    /*
     * Making service call
     * @url - url to make request
     * @method - http request method
     * @params - http request params
     * */
    public static JSONObject makeRequest(String url, String param) {

        URL urlConnection;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        StringBuilder result;
        JSONObject jResponse;

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {

            // http://ecreateinfotech.info/test/advideos.php
            // "http://35.154.179.171/index.php"

            MLog.d("REQUEST URL  : ",url);

            urlConnection = new URL(url);

            connection = (HttpURLConnection) urlConnection.openConnection();
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
            connection.setUseCaches(false);
//            connection.setAllowUserInteraction(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-type", "application/json;charset=UTF-8");
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setReadTimeout(600000);
            connection.setConnectTimeout(600000);
            //connection.connect();

            dataOutputStream = new DataOutputStream(connection.getOutputStream());


            MLog.d("OUTPUT STREAM  ", param);
            dataOutputStream.writeBytes(param);
            dataOutputStream.flush();
            dataOutputStream.close();

            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            MLog.d("INPUTSTREAM: ", result.toString());
            jResponse = new JSONObject(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
            MLog.e("SERVICE HANDLER  ", e.toString());
            return jResponse = null;
        } catch (JSONException e) {
            e.printStackTrace();
            MLog.e("SERVICE HANDLER  ", e.toString());
            return jResponse = null;
        }
        connection.disconnect();
        return jResponse;
    }

    public static JSONObject makeGetRequest(String url) {

        URL urlConnection;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        StringBuilder result;
        JSONObject jResponse;

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {

            // http://ecreateinfotech.info/test/advideos.php
            // "http://35.154.179.171/index.php"

            MLog.d("GET REQUEST URL  : ",url);

            urlConnection = new URL(url);

            connection = (HttpURLConnection) urlConnection.openConnection();
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
            connection.setUseCaches(false);
//            connection.setAllowUserInteraction(false);
            connection.setRequestProperty("Content-type", "application/json;charset=UTF-8");
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setReadTimeout(600000);
            connection.setConnectTimeout(600000);
            //connection.connect();

//            InputStream in = connection.getInputStream();
//            InputStreamReader isw = new InputStreamReader(in);
//            int data = isw.read();
//            while (data != -1) {
//                char current = (char) data;
//                data = isw.read();
//                System.out.print(current);
//            }

            //dataOutputStream = new DataOutputStream(connection.getOutputStream());

            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            MLog.d("INPUTSTREAM: ", result.toString());
            jResponse = new JSONObject(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
            MLog.e("SERVICE HANDLER  ", e.toString());
            return jResponse = null;
        } catch (JSONException e) {
            e.printStackTrace();
            MLog.e("SERVICE HANDLER  ", e.toString());
            return jResponse = null;
        }
        connection.disconnect();
        return jResponse;
    }

    public static JSONArray makeHttpRequestForArray(String url) {

        URL urlConnection;
        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        StringBuilder result;
        JSONArray jResponse;

        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        try {

            // http://ecreateinfotech.info/test/advideos.php
            // "http://35.154.179.171/index.php"

            MLog.d("GET REQUEST URL  : ",url);

            urlConnection = new URL(url);

            connection = (HttpURLConnection) urlConnection.openConnection();
//            connection.setDoInput(true);
//            connection.setDoOutput(true);
            connection.setUseCaches(false);
//            connection.setAllowUserInteraction(false);
            connection.setRequestProperty("Content-type", "application/json;charset=UTF-8");
//            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            connection.setReadTimeout(600000);
            connection.setConnectTimeout(600000);
            //connection.connect();

//            InputStream in = connection.getInputStream();
//            InputStreamReader isw = new InputStreamReader(in);
//            int data = isw.read();
//            while (data != -1) {
//                char current = (char) data;
//                data = isw.read();
//                System.out.print(current);
//            }

            //dataOutputStream = new DataOutputStream(connection.getOutputStream());

            InputStream in = new BufferedInputStream(connection.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

            MLog.d("INPUTSTREAM: ", result.toString());
            jResponse = new JSONArray(result.toString());

        } catch (IOException e) {
            e.printStackTrace();
            MLog.e("SERVICE HANDLER  ", e.toString());
            return jResponse = null;
        } catch (JSONException e) {
            e.printStackTrace();
            MLog.e("SERVICE HANDLER  ", e.toString());
            return jResponse = null;
        }
        connection.disconnect();
        return jResponse;
    }

    public static void testRequest() {
        JSONObject param=new JSONObject();
        try {
            param.put("username","someusername");
            param.put("message","this is a sweet message");
            param.put("image","http://localhost/someimage.jpg");
            param.put("time",  "present time");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject result = makeRequest("http://"+host+"/index.php", param.toString());

        MLog.e("test", "Aresult: " + result.toString());

    }

    public static String GetUrlWithCDATA(String url) {
        if(url != null) {
            if(url.contains("<![CDATA[")) {
                return  url;
            } else {
                return "<![CDATA["+url+"]]";
            }
        }
        return url;
    }
}

