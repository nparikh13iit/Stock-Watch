package com.example.stockwatch;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class StockDataRunnable implements Runnable{

    private static final String TAG = "StockDataRunnable";
    private MainActivity mainActivity;
    private static final String urlKey = "pk_188c22a238cc41058b4ed4eaddfc0a33";

    public StockDataRunnable(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void run() {

        ArrayList<Stock> stockArrayList = mainActivity.getStockArrayList();
        final ArrayList<Stock> tempList = new ArrayList<>();

        for (Stock stock : stockArrayList){

            String symbol = stock.getSymbol();
            String STOCKDATA_URL = "https://cloud.iexapis.com/stable/stock/"+symbol+"/quote?token="+urlKey;

            Uri dataUri = Uri.parse(STOCKDATA_URL);
            String urlToUse = dataUri.toString();

            StringBuilder sb = new StringBuilder();

            //Read from api
            try {
                URL url = new URL(urlToUse);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.connect();

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append('\n');
                }

                Log.d(TAG, "run: " + sb.toString());

            } catch (Exception e) {
                Log.e(TAG, "run: ", e);
            }

            Stock stock_full = null;
            try {
                stock_full = parseJSON(sb.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tempList.add(stock_full);

        }
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (tempList != null) {
                    mainActivity.updateStockData(tempList);
                }
            }
        });

    }

    private Stock parseJSON(String s) throws JSONException {

        JSONObject jsonStock = new JSONObject(s);

        String symbol = "";
        try
        {
            symbol = jsonStock.getString("symbol");
        }
        catch (Exception e)
        {
            Log.d(TAG, "ERROR| getSymbolfromAPI| bp:" + e.getMessage());
            e.printStackTrace();
        }

        String name = "";
        try
        {
            name = jsonStock.getString("companyName");
        }
        catch (Exception e)
        {
            Log.d(TAG, "ERROR| getCompanyNamefromAPI| bp:" + e.getMessage());
            e.printStackTrace();
        }

        double price = 0.00;
        try
        {
            String a = jsonStock.getString("latestPrice");
            price = Math.round(Double.parseDouble(a)*100.00)/100.00;
        }
        catch (Exception e)
        {
            Log.d(TAG, "ERROR| getLatestPricefromAPI| bp:" + e.getMessage());
            e.printStackTrace();
        }

        double change = 0.00;
        try
        {
            String a = jsonStock.getString("change");
            change = Math.round(Double.parseDouble(a)*100.00)/100.00;
        }
        catch (Exception e)
        {
            Log.d(TAG, "ERROR| getChangefromAPI| bp:" + e.getMessage());
            e.printStackTrace();
        }

        double changePercent = 0.00;
        try
        {
            JSONObject jStock = new JSONObject(s);
            String a = jsonStock.getString("changePercent");
            changePercent = Math.round(Double.parseDouble(a)*10000.00)/100.00;
        }
        catch (Exception e)
        {
            Log.d(TAG, "ERROR| getChangePercentfromAPI| bp:" + e.getMessage());
            e.printStackTrace();
        }

        return new Stock(symbol, name, change, changePercent, price);

    }
}
