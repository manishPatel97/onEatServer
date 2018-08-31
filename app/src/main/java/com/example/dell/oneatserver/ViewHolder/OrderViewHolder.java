package com.example.dell.oneatserver.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.dell.oneatserver.Interface.ItemClickListener;
import com.example.dell.oneatserver.R;

import info.hoang8f.widget.FButton;

public class OrderViewHolder extends RecyclerView.ViewHolder  {
    public TextView txtOrder_id;
    public TextView txtOrder_status;
    public TextView txtOrder_phone;
    public TextView txtOredr_address;
    private ItemClickListener itemClickListener;
   public Button btn_edit,btn_remove,btn_direction,btn_detail;


    public OrderViewHolder(View itemView) {
        super(itemView);
        txtOrder_id = itemView.findViewById(R.id.order_id);
        txtOrder_status = itemView.findViewById(R.id.order_status);
        txtOrder_phone = itemView.findViewById(R.id.order_phone);
        txtOredr_address = itemView.findViewById(R.id.order_address);
        btn_edit = itemView.findViewById(R.id.btnEdit);
        btn_remove = itemView.findViewById(R.id.btnRemove);
        btn_direction = itemView.findViewById(R.id.btn_Direction);
        btn_detail = itemView.findViewById(R.id.btndetail);


    }


}
