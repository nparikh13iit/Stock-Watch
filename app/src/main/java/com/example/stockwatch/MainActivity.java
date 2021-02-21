package com.example.stockwatch;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnClickListener {

    private static final String TAG = "MainActivity";

    private SwipeRefreshLayout srl;
    private RecyclerView rv;
    private StockAdapter sa;
    private DatabaseHandler dbh;

    private HashMap<String,String> stock_hmap;
    private ArrayList<Stock> stockArrayList;
    private String searchStock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Linking Components
        srl = findViewById(R.id.swiper);
        rv = findViewById(R.id.recycler);
        stock_hmap = new HashMap<>();
        stockArrayList = new ArrayList<>();

        //Loading the Stock hashmap with names and symbols
        if(networkChecker())
        {
            StockRunnable stockRunnable = new StockRunnable(this);
            new Thread(stockRunnable).start();
        }
        else {
            noNetworkDialog("Stocks Cannot be Updated Without a Network Connection");
        }

        //Swiper Refresh
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if(networkChecker())
                {
                    getStockData();

                }

                else
                {
                    noNetworkDialog("Stocks Cannot be Updated Without a Network Connection");
                    srl.setRefreshing(false);
                }
                Toast.makeText(MainActivity.this, "Refreshed", Toast.LENGTH_SHORT).show();
            }
        });

        //Initializing database and recycler view
        dbh = new DatabaseHandler(this);
        sa = new StockAdapter(stockArrayList,this);
        rv.setAdapter(sa);
        rv.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        ArrayList<Stock> tempList = dbh.loadStocks();
        stockArrayList.clear();
        stockArrayList.addAll(sortList(tempList));
        sa.notifyDataSetChanged();
        if(networkChecker())
            getStockData();
        else
            noNetworkDialog("Stocks Cannot be Updated Without a Network Connection");
    }

    @Override
    protected void onDestroy()
    {
        dbh.shutDown();
        super.onDestroy();
    }

    /*----------Recycler View Functions----------*/
    @Override
    public boolean onLongClick(View view) {
        deleteAlert(view);
        return true;
    }

    @Override
    public void onClick(View view) {
        int stock_position = rv.getChildAdapterPosition(view);
        Stock stock = stockArrayList.get(stock_position);

        String URL = "http://www.marketwatch.com/investing/stock/" + stock.getSymbol();
        Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(URL));
        startActivity(i);
    }

    private void deleteAlert(final View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int stock_position = rv.getChildAdapterPosition(view);
                //Delete the stock from database
                dbh.deleteStock(stockArrayList.get(stock_position));
                //Remove stock from the list
                stockArrayList.remove(stock_position);
                //Update changes in view
                sa.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Do nothing on cancel
            }
        });

        builder.setIcon(R.drawable.baseline_delete_black_18dp);
        builder.setTitle("Delete Stock");
        int stock_position = rv.getChildAdapterPosition(view);
        builder.setMessage("Delete Stock Symbol " + stockArrayList.get(stock_position).getSymbol()+"?");
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /*-------------------------------------------*/


    /*----------Network Check Functions----------*/
    public boolean networkChecker(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cm == null)
            return false;
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    public void noNetworkDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("No Network Connection");
        builder.setMessage(message);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    /*-------------------------------------------*/

    /*----------Thread Return functions----------*/
    public void updateStock(HashMap<String, String> hmap) {
        stock_hmap.putAll(hmap);
        Log.d(TAG, "updateStock: "+stock_hmap);
    }

    public void downloadStockFailed() {
        Log.d(TAG, "downloadStockFailed: Failed to download stock data");
    }

    public void updateStockData(ArrayList<Stock> tempList) {
        stockArrayList.clear();
        stockArrayList.addAll(sortList(tempList));
        srl.setRefreshing(false);
        sa.notifyDataSetChanged();
    }
    /*-------------------------------------------*/

    /*----------Adding stock Functions----------*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_stock_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.add_stock) {
            if(networkChecker())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                final EditText et = new EditText(this);
                et.setInputType(InputType.TYPE_CLASS_TEXT);
                et.setGravity(Gravity.CENTER_HORIZONTAL);
                builder.setView(et);

                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        searchStock = et.getText().toString().toUpperCase().trim();
                        if (!searchStock.equals("")) {
                            Log.d(TAG, "onClick: " + searchStock);
                            searchStock(searchStock);
                        } else {
                            Toast.makeText(MainActivity.this, "Search field cannot be left blank", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "Search cancelled", Toast.LENGTH_SHORT).show();
                    }
                });

                //builder.setIcon(R.drawable.ic_search);
                builder.setMessage("Please enter a Stock Symbol:");
                builder.setTitle("Stock Selection");

                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else {
                noNetworkDialog("Stocks Cannot be Added Without a Network Connection");
            }


        } else {
            Toast.makeText(this, "No option selected", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void searchStock(String s){

        final ArrayList<Stock> searchList = new ArrayList<>();

        for (Map.Entry<String, String> entry : stock_hmap.entrySet()) {
            if (entry.getKey().startsWith(s) || entry.getValue().contains(s)){
                //Log.d(TAG, "searchStock: "+"Key = " + entry.getKey() + ", Value = " + entry.getValue());
                Stock searchResult = new Stock(entry.getKey(),entry.getValue());
                searchList.add(searchResult);
            }
        }

        if (searchList.size() == 1) {
            if (!duplicateStockCheck(searchList.get(0))){
                if(networkChecker())
                {
                    stockArrayList.add(searchList.get(0));
                    stockArrayList = sortList(stockArrayList);
                    dbh.addStock(searchList.get(0));
                    getStockData();
                    sa.notifyDataSetChanged();
                }
                else {
                    noNetworkDialog("Stocks Cannot be Added Without a Network Connection");
                }

            }
        }

        else if (searchList.size() > 1) {
            final CharSequence[] listChoice = new CharSequence[searchList.size()];
            for (int i = 0; i < searchList.size(); i++)
                listChoice[i] = searchList.get(i).getSymbol() + " | " + searchList.get(i).getName().toLowerCase();

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Make a selection");

            builder.setItems(listChoice, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which)
                {
                    Stock stock  = searchList.get(which);
                    if(!duplicateStockCheck(stock)){
                        if(networkChecker())
                        {
                            stockArrayList.add(stock);
                            dbh.addStock(stock);
                            getStockData();
                            sa.notifyDataSetChanged();
                        }
                        else {
                            noNetworkDialog("Stocks Cannot be Added Without a Network Connection");
                        }

                    }
                }
            });

            builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Toast.makeText(MainActivity.this, "No selections done", Toast.LENGTH_SHORT).show();
                }
            });
            AlertDialog dialog = builder.create();

            dialog.show();
        }

        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Symbol Not Found: " + s);
            builder.setMessage("Data for Stock symbol");
            AlertDialog dialog = builder.create();
            dialog.show();
        }

    }

    public boolean duplicateStockCheck(Stock k) {
        for (Stock i : stockArrayList) {
            if (i.getSymbol().equals(k.getSymbol())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setTitle("Duplicate Stock");
                builder.setMessage("Stock symbol " + " is already displayed");

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
        }
        return false;
    }
    /*-------------------------------------------*/

    ArrayList<Stock> sortList(ArrayList<Stock> temp){
        Collections.sort(temp, new Comparator<Stock>() {
            @Override
            public int compare(Stock s1, Stock s2) {
                return s1.getSymbol().compareTo(s2.getSymbol());
            }
        });

        return temp;
    }

    public void getStockData(){
        StockDataRunnable stockDataRunnable = new StockDataRunnable(this);
        new Thread(stockDataRunnable).start();
    }

    public ArrayList<Stock> getStockArrayList() {
        return stockArrayList;
    }

}

