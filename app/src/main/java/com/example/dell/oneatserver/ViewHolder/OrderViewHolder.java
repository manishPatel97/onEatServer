package com.example.dell.oneatserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.dell.oneatserver.Interface.ItemClickListener;
import com.example.dell.oneatserver.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView txtOrder_id;
    public TextView txtOrder_status;
    public TextView txtOrder_phone;
    public TextView txtOredr_address;
    private ItemClickListener itemClickListener;

    public OrderViewHolder(View itemView) {
        super(itemView);
       // txtOrder_id = itemView.findViewById(R.id.order_id);
        //txtOrder_status = itemView.findViewById(R.id.order_status);
        //txtOrder_phone = itemView.findViewById(R.id.order_phone);
        //txtOredr_address = itemView.findViewById(R.id.order_address);
        itemView.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
            itemClickListener.onClick(v,getAdapterPosition(),false);
    }
}
