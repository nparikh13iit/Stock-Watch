package com.example.stockwatch;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class StockRowHolder extends RecyclerView.ViewHolder {

    TextView symbol,name,price,change,percentChange;
    ImageView iconChange;

    public StockRowHolder(@NonNull View itemView) {

        super(itemView);
        symbol = itemView.findViewById(R.id.symbol);
        name = itemView.findViewById(R.id.name);
        price = itemView.findViewById(R.id.price);
        change = itemView.findViewById(R.id.change);
        percentChange = itemView.findViewById(R.id.percentChange);
        iconChange = itemView.findViewById(R.id.iconChange);

    }
}
