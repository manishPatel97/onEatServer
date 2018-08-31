package com.example.dell.oneatserver.ViewHolder;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.dell.oneatserver.Model.Order;
import com.example.dell.oneatserver.R;

import java.util.List;

class myViewHolder extends RecyclerView.ViewHolder{

    public TextView product_name,product_price,product_quantity,product_discount;


    public myViewHolder(View itemView) {
        super(itemView);
        product_name = itemView.findViewById(R.id.product_name);
        product_price = itemView.findViewById(R.id.product_price);
        product_quantity = itemView.findViewById(R.id.product_quantity);
        product_discount = itemView.findViewById(R.id.product_discount);

    }
}



public class orderDetailAdapter extends RecyclerView.Adapter<myViewHolder> {


    List<Order> myOrders;

    public orderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }



    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_detail_layout,parent,false);
        return new myViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {
        Order order = myOrders.get(position);
        holder.product_name.setText(String.format("Name: %s",order.getProductName()));
        holder.product_quantity.setText(String.format("Quanity : %s",order.getQuantity()));
        holder.product_price.setText(String.format("Price : %s",order.getPrice()));
        holder.product_discount.setText(String.format("Quantity : %s",order.getDiscount()));

    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
