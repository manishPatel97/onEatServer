package com.example.dell.oneatserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.example.dell.oneatserver.Common.currentUser;
import com.example.dell.oneatserver.ViewHolder.orderDetailAdapter;

public class orderDetail extends AppCompatActivity {

    TextView order_Id,order_Phone,order_total,order_Address;
    String order_Id_Value = "";
    RecyclerView lstFoods;
    RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        order_Id = findViewById(R.id.order_id);
        order_Phone = findViewById(R.id.order_phone);
        order_total = findViewById(R.id.order_total);
        order_Address =findViewById(R.id.order_address);
        lstFoods =findViewById(R.id.order_list);
        lstFoods.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        lstFoods.setLayoutManager(layoutManager);

        if(getIntent()!=null)
            order_Id_Value =getIntent().getStringExtra("OrderId");

        order_Id.setText(order_Id_Value);
        order_Phone.setText(currentUser.currentrequest.getPhone());
        order_total.setText(currentUser.currentrequest.getTotal());
        order_Address.setText(currentUser.currentrequest.getAddress());

        orderDetailAdapter adapter= new orderDetailAdapter(currentUser.currentrequest.getFoods());
        adapter.notifyDataSetChanged();
        lstFoods.setAdapter(adapter);


    }
}
