package com.example.dell.oneatserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.dell.oneatserver.Interface.ItemClickListener;
import com.example.dell.oneatserver.Model.Category;
import com.example.dell.oneatserver.Model.Food;
import com.example.dell.oneatserver.ViewHolder.FoodViewHolder;
import com.example.dell.oneatserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class FoodList extends AppCompatActivity {
    FirebaseDatabase db;
    DatabaseReference foodlist;
    RelativeLayout rootlayout;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Food,FoodViewHolder> adapter;
    String CategoryId = "";
    FirebaseStorage storage;
    MaterialEditText food_name,food_price,food_description,food_discount;
    Button btn_select_food,btn_upload_food;
    StorageReference storageReference;
    FloatingActionButton fab;
    Food newFood;
    Uri saveuri;
    private final int PICK_IMAGE_REQUEST = 71;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_food_list);
        db = FirebaseDatabase.getInstance();
        foodlist = db.getReference("Food");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        recycler_menu =findViewById(R.id.food_recycler);
        recycler_menu.setHasFixedSize(true);
        layoutManager=new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);
        fab = findViewById(R.id.fab_button);
        rootlayout = findViewById(R.id.foodlayout);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddfoodDialog();

            }
        });
        if(getIntent()!=null){
            CategoryId = getIntent().getStringExtra("CategoryID");
        }
        if(!CategoryId.isEmpty()){
            loadListFood(CategoryId);
        }
    }

    private void showAddfoodDialog() {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(FoodList.this);
        alertdialog.setTitle("Add new Food");
        alertdialog.setMessage("Please fill Details");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_food_layout,null);
        //System.out.println("addMenu1 "+addMenu);
        food_name =add_menu_layout.findViewById(R.id.foodname);
        food_description =add_menu_layout.findViewById(R.id.Desciption);
        food_price =add_menu_layout.findViewById(R.id.foodprice);
        food_discount =add_menu_layout.findViewById(R.id.FoodDiscount);

        btn_select_food = add_menu_layout.findViewById(R.id.btn_select_food);
       // System.out.println("button3 "+btnSelect);
        btn_upload_food = add_menu_layout.findViewById(R.id.btn_upload_food);
        alertdialog.setView(add_menu_layout);
        alertdialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        btn_select_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btn_upload_food.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });
        alertdialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(newFood!=null){
                    foodlist.push().setValue(newFood);
                    Snackbar.make(rootlayout,"New Category "+newFood.getName()+" was added",Snackbar.LENGTH_SHORT).show();
                }

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
    private void UploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading..");
        progressDialog.show();
        String imageString = UUID.randomUUID().toString();
        final StorageReference imagefolder = storageReference.child("image/"+imageString);
        imagefolder.putFile(saveuri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(FoodList.this,"Uploaded !!!",Toast.LENGTH_SHORT).show();
                        imagefolder.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        newFood =new Food();
                                        newFood.setName(food_name.getText().toString());
                                        newFood.setDescription(food_description.getText().toString());
                                        newFood.setPrice(food_price.getText().toString());
                                        newFood.setDiscount(food_discount.getText().toString());
                                        newFood.setMenuid(CategoryId);
                                        newFood.setImage(uri.toString());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(FoodList.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress= (100*taskSnapshot.getBytesTransferred() /taskSnapshot.getTotalByteCount());
                        progressDialog.setMessage("uploaded : "+progress+" %");
                    }
                });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK
                &&data!=null && data.getData()!=null){
            saveuri = data.getData();
            btn_select_food.setText("Image selected!");
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    private void loadListFood(String categoryId) {
        Query query = FirebaseDatabase.getInstance().getReference().child("Food").orderByChild("menuid").equalTo(categoryId);
        //System.out.println("Category4 "+categoryID);
        //System.out.println("Query "+query);
        FirebaseRecyclerOptions<Food> options = new FirebaseRecyclerOptions.Builder<Food>().setQuery(query,Food.class).build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_item,parent,false);
                return new FoodViewHolder(view);


            }

            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, int position, @NonNull Food model) {
                holder.food_name.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.food_img);
                final Food clickitem = model;
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                    }
                });
            }
        };
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);

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
}
