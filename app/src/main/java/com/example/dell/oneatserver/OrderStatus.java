package com.example.dell.oneatserver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.dell.oneatserver.Common.currentUser;
import com.example.dell.oneatserver.Interface.ItemClickListener;
import com.example.dell.oneatserver.Model.Order;
import com.example.dell.oneatserver.Model.Request;
import com.example.dell.oneatserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jaredrummler.materialspinner.MaterialSpinner;

import static com.example.dell.oneatserver.Common.currentUser.convertCodeToStatus;
import static com.example.dell.oneatserver.Common.currentUser.currentuser;
import android.content.Intent;
public class OrderStatus extends AppCompatActivity {

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;
        FirebaseDatabase database;
        DatabaseReference requests;
        MaterialSpinner spinner;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");
        recyclerView = findViewById(R.id.listOrder);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        loadOrder();


    }


    private void loadOrder() {
        //System.out.println("Phone "+phone);
        Query query =  FirebaseDatabase.getInstance().getReference().child("Requests");//request.orderByChild("phone").equalTo(phone);
        //System.out.println("order qyeey "+ query);
        FirebaseRecyclerOptions<Request> options = new FirebaseRecyclerOptions.Builder<Request>().setQuery(query,Request.class).build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout,parent,false);
                return new OrderViewHolder(view);

            }

            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull final Request model) {
                holder.txtOrder_id.setText(adapter.getRef(position).getKey());
                //System.out.println("order id: "+ adapter.getRef(position).getKey());
                holder.txtOrder_status.setText(convertCodeToStatus(model.getStatus()));
                //System.out.println("order status: "+ convertCodeToStatus(model.getStatus()) );
                holder.txtOrder_phone.setText(model.getPhone());
                //System.out.println("order phone: "+ model.getPhone() );
                holder.txtOredr_address.setText(model.getAddress());
                //System.out.println("order address: "+ model.getAddress() );
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent  trackingIntent = new Intent(OrderStatus.this,TrackingOrder.class);
                        currentUser.currentrequest = model;
                        startActivity(trackingIntent);
                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }
    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        if(item.getTitle().equals(currentUser.update)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(currentUser.delete)){
            deleteOrder(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void showUpdateDialog(String key, final Request item) {
    final AlertDialog.Builder alertdialog = new AlertDialog.Builder(OrderStatus.this);
    alertdialog.setTitle("Update Order");
    alertdialog.setMessage("Please Choose Status");
    LayoutInflater inflater = this.getLayoutInflater();
    final View view = inflater.inflate(R.layout.update_order_layout,null);
    spinner = view.findViewById(R.id.statusSpinner);
    spinner.setItems("Placed","On My Way","Shipped");
    alertdialog.setView(view);
    final String localkey = key;
    alertdialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            item.setStatus(String.valueOf(spinner.getSelectedIndex()));
            requests.child(localkey).setValue(item);
        }
    });
    alertdialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();

        }
    });
    alertdialog.show();

    }
    private void deleteOrder(String key){
        requests.child(key).removeValue();
    }


}
