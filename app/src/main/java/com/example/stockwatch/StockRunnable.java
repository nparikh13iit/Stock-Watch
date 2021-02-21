package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class StockRunnable implements Runnable {

    private static final String TAG = "StockRunnable";
    private static final String STOCK_URL = "https://api.iextrading.com/1.0/ref-data/symbols";
    private MainActivity mainActivity;

    public StockRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {

        Uri dataUri = Uri.parse(STOCK_URL);
        String urlToUse = dataUri.toString();
        Log.d(TAG, "run: " + urlToUse);

        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlToUse);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "run: HTTP ResponseCode NOT OK: " + conn.getResponseCode());
                handleResults(null);
                return;
            }

            InputStream is = conn.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }

            //Log.d(TAG, "run: " + sb.toString());

        } catch (Exception e) {
            Log.e(TAG, "run: ", e);
            handleResults(null);
            return;
        }

        handleResults(sb.toString());

    }


    private void handleResults(String s) {

        if (s == null) {
            Log.d(TAG, "handleResults: Failure in data download");
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.downloadStockFailed();
                }
            });
            return;
        }


        final HashMap<String, String> stock_hmap = parseJSON(s);
        Log.d(TAG, "handleResults: "+stock_hmap);

        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (stock_hmap != null) {
                    mainActivity.updateStock(stock_hmap);
                }
            }
        });
    }

    private HashMap<String, String> parseJSON(String s) {

        HashMap<String, String> stock_hmap = new HashMap<String, String>();
        try {

            JSONArray jObjMain = new JSONArray(s);

            for (int i = 0; i < jObjMain.length(); i++) {

                JSONObject stock = (JSONObject) jObjMain.get(i);

                String name = stock.getString("name");
                String symbol = stock.getString("symbol");

                stock_hmap.put(symbol, name);

            }

            //Log.d(TAG, "parseJSON: "+stock_hmap);
            return stock_hmap;

        } catch (Exception e) {
            Log.d(TAG, "parseJSON: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }



}