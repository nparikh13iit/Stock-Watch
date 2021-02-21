package com.example.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockRowHolder> {

    private static final String TAG = "StockAdapter";
    private List<Stock> stockList;
    private MainActivity mainActivity;

    public StockAdapter(List<Stock> stockList, MainActivity mainActivity) {
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StockRowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list_row,parent,false);
        itemView.setOnClickListener(mainActivity);
        itemView.setOnLongClickListener(mainActivity);
        return new StockRowHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockRowHolder holder, int position) {
        Stock stock = stockList.get(position);
        holder.symbol.setText(stock.getSymbol());
        holder.name.setText(stock.getName());
        holder.price.setText(String.format("$ %s",stock.getLatestPrice()));
        holder.change.setText(String.format("%s",stock.getChange()));
        holder.percentChange.setText(String.format("(%s %%)", stock.getChangePercent()));

        if (stock.getChange() > 0){
            holder.symbol.setTextColor(Color.parseColor("#0f9d58"));
            holder.name.setTextColor(Color.parseColor("#0f9d58"));
            holder.price.setTextColor(Color.parseColor("#0f9d58"));
            holder.change.setTextColor(Color.parseColor("#0f9d58"));
            holder.percentChange.setTextColor(Color.parseColor("#0f9d58"));
            holder.iconChange.setBackgroundResource(R.drawable.ic_up_arrow);
        }

        else if (stock.getChange() < 0){
            holder.symbol.setTextColor(Color.parseColor("#db4437"));
            holder.name.setTextColor(Color.parseColor("#db4437"));
            holder.price.setTextColor(Color.parseColor("#db4437"));
            holder.change.setTextColor(Color.parseColor("#db4437"));
            holder.percentChange.setTextColor(Color.parseColor("#db4437"));
            holder.iconChange.setBackgroundResource(R.drawable.ic_down_arrow);
        }

        else {
            holder.symbol.setTextColor(Color.parseColor("#FFFFFF"));
            holder.name.setTextColor(Color.parseColor("#FFFFFF"));
            holder.price.setTextColor(Color.parseColor("#FFFFFF"));
            holder.change.setTextColor(Color.parseColor("#FFFFFF"));
            holder.percentChange.setTextColor(Color.parseColor("#FFFFFF"));
            holder.iconChange.setBackgroundResource(R.drawable.ic_neutral);
        }

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }
}
