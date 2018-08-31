package com.example.dell.oneatserver;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.dell.oneatserver.Common.currentUser;
import com.example.dell.oneatserver.Interface.ItemClickListener;
import com.example.dell.oneatserver.Model.Notification;
import com.example.dell.oneatserver.Model.Order;
import com.example.dell.oneatserver.Model.Request;
import com.example.dell.oneatserver.Model.Sender;
import com.example.dell.oneatserver.Model.Token;
import com.example.dell.oneatserver.Model.myResponses;
import com.example.dell.oneatserver.Remote.APIService;
import com.example.dell.oneatserver.ViewHolder.OrderViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jaredrummler.materialspinner.MaterialSpinner;

import static com.example.dell.oneatserver.Common.currentUser.convertCodeToStatus;
import static com.example.dell.oneatserver.Common.currentUser.currentuser;
import android.content.Intent;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderStatus extends AppCompatActivity {

        RecyclerView recyclerView;
        RecyclerView.LayoutManager layoutManager;
        FirebaseRecyclerAdapter<Request,OrderViewHolder> adapter;
        FirebaseDatabase database;
        DatabaseReference requests;
        MaterialSpinner spinner;
        APIService mService;


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
        mService = currentUser.getFCMService();
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
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, final int position, @NonNull final Request model) {
                holder.txtOrder_id.setText(adapter.getRef(position).getKey());
                //System.out.println("order id: "+ adapter.getRef(position).getKey());
                holder.txtOrder_status.setText(convertCodeToStatus(model.getStatus()));
                //System.out.println("order status: "+ convertCodeToStatus(model.getStatus()) );
                holder.txtOrder_phone.setText(model.getPhone());
                //System.out.println("order phone: "+ model.getPhone() );
                holder.txtOredr_address.setText(model.getAddress());
                //System.out.println("order address: "+ model.getAddress() );
                holder.btn_edit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showUpdateDialog(adapter.getRef(position).getKey(),adapter.getItem(position));
                    }
                });
                holder.btn_remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteOrder(adapter.getRef(position).getKey());
                    }
                });
                holder.btn_direction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent  trackingIntent = new Intent(OrderStatus.this,TrackingOrder.class);
                        currentUser.currentrequest = model;
                        startActivity(trackingIntent);
                    }
                });
                holder.btn_detail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent  orderDetailIntent = new Intent(OrderStatus.this,orderDetail.class);
                        currentUser.currentrequest = model;
                        orderDetailIntent.putExtra("OrderId",adapter.getRef(position).getKey());
                        startActivity(orderDetailIntent);
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
            adapter.notifyDataSetChanged();
            sendOrderStautsToUser(localkey,item);
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

    private void sendOrderStautsToUser(final String key,final Request item) {
        DatabaseReference tokens= database.getReference("Tokens");
        tokens.orderByKey().equalTo(item.getPhone())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot postSnapShot : dataSnapshot.getChildren()){
                            Token token = postSnapShot.getValue(Token.class);
                            //raw payload
                            Notification notification = new Notification("OnEat","Your Order "+key +"was updated");
                            Sender content = new Sender(token.getToken(),notification);
                            mService.sendnotifiaction(content)
                                    .enqueue(new Callback<myResponses>() {
                                        @Override
                                        public void onResponse(Call<myResponses> call, Response<myResponses> response) {
                                            if (response.code() == 200) {
                                                if (response.body().success == 1) {
                                                    Toast.makeText(OrderStatus.this, "Order was Updated", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(OrderStatus.this, "Order was updated but failed to send notification", Toast.LENGTH_SHORT).show();

                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<myResponses> call, Throwable t) {

                                            Log.e("Error",t.getMessage());

                                        }
                                    });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void deleteOrder(String key){

        requests.child(key).removeValue();
        adapter.notifyDataSetChanged();
    }


}
