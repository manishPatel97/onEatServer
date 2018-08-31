package com.example.dell.oneatserver;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dell.oneatserver.Common.currentUser;
import com.example.dell.oneatserver.Interface.ItemClickListener;
import com.example.dell.oneatserver.Model.Category;
import com.example.dell.oneatserver.Model.Token;

import com.example.dell.oneatserver.ViewHolder.MenuViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.UUID;

import info.hoang8f.widget.FButton;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    TextView TxtName,TxtEmail;
    FirebaseDatabase database;
    DatabaseReference category;
    TextView username;
    TextView useremail;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    FirebaseStorage storage;
    StorageReference storageReference;
    MaterialEditText addMenu;
    Button btnSelect, btnupload;
    Category newCatrgory;
    Uri saveuri;
    private final int PICK_IMAGE_REQUEST = 71;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Menu ");
        Paper.init(this);
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");
        storage =FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog();
            }
        });

         drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        TxtName = headerView.findViewById(R.id.header_username);
        TxtEmail = headerView.findViewById(R.id.header_useremail);
        TxtName.setText(currentUser.currentuser.getName());
        TxtEmail.setText(currentUser.currentuser.getEmail());
        recycler_menu= findViewById(R.id.recyclerView);
        recycler_menu.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(layoutManager);
        loadMenu();
        //Register Service

        //send token
        updateToken(FirebaseInstanceId.getInstance().getToken());

    }

    private void updateToken(String token) {
        FirebaseDatabase db= FirebaseDatabase.getInstance();
        DatabaseReference Tokens =db.getReference("Tokens");
        Token data = new Token(token,true);
        Tokens.child(currentUser.currentuser.getPhone()).setValue(data);//make userphone number as a key

    }

    private void showDialog() {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(Home.this);
        alertdialog.setTitle("Add new Category");
        alertdialog.setMessage("Please fill Details");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_main_layout,null);
        System.out.println("addMenu1 "+addMenu);
        addMenu = add_menu_layout.findViewById(R.id.FoodName);
        System.out.println("addMenu1 "+addMenu);
        System.out.println("button1 "+btnSelect);
        System.out.println("button2 "+btnSelect);
        btnSelect = add_menu_layout.findViewById(R.id.btn_select);
        System.out.println("button3 "+btnSelect);
        btnupload = add_menu_layout.findViewById(R.id.btn_upload);
        alertdialog.setView(add_menu_layout);
        alertdialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadImage();
            }
        });
        alertdialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if(newCatrgory!=null){
                    category.push().setValue(newCatrgory);
                    Snackbar.make(drawer,"New Category "+newCatrgory.getName()+" was added",Snackbar.LENGTH_SHORT).show();
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
                Toast.makeText(Home.this,"Uploaded !!!",Toast.LENGTH_SHORT).show();
                imagefolder.getDownloadUrl()
                        .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        newCatrgory =new Category(addMenu.getText().toString(),uri.toString());
                    }
                });

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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
            btnSelect.setText("Image selected!");
        }
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),PICK_IMAGE_REQUEST);
    }

    private void loadMenu() {
        Query query = FirebaseDatabase.getInstance().getReference().child("Category");
        System.out.println("Category query "+ query);
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>().setQuery(query,Category.class).build();
        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder holder, int position, @NonNull Category model) {
                holder.menu_name.setText(model.getName());
                System.out.println(""+model.getName());
                System.out.println(model.getImage());
                Picasso.with(getBaseContext()).load(model.getImage()).into(holder.menu_img);
                final Category clickitem = model;
                holder.setItemClickListener(new ItemClickListener() {


                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        System.out.println("inside menu");
                        Toast.makeText(Home.this,""+clickitem.getName(),Toast.LENGTH_SHORT).show();
                        Intent foodintent = new Intent(Home.this,FoodList.class);
                        foodintent.putExtra("CategoryID",adapter.getRef(position).getKey());
                        startActivity(foodintent);

                    }
                });

            }

            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item,parent,false);

                return new MenuViewHolder(view);

            }
        };
        adapter.notifyDataSetChanged();//refresh layout if data changed
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
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(currentUser.update)){
            showUpdateDialog(adapter.getRef(item.getOrder()).getKey(),adapter.getItem(item.getOrder()));
        }
        else if(item.getTitle().equals(currentUser.delete)){
            DeleteCategory(adapter.getRef(item.getOrder()).getKey());
        }
        return super.onContextItemSelected(item);
    }

    private void DeleteCategory(String key) {

        DatabaseReference food = database.getReference("Food");
        System.out.println("Key : "+key);
        Query foodCategory = food.orderByChild("menuid").equalTo(key);
        foodCategory.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapShot:dataSnapshot.getChildren()){
                    System.out.println("Hello world!!");
                    System.out.println("postSnap Shot "+postSnapShot.getRef());

                    postSnapShot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
       category.child(key).removeValue();
        Toast.makeText(this,"Item Deleted !!! ",Toast.LENGTH_SHORT).show();
    }

    private void showUpdateDialog(final String key, final Category item) {
        AlertDialog.Builder alertdialog = new AlertDialog.Builder(Home.this);
        alertdialog.setTitle("Update Category");
        alertdialog.setMessage("Please fill Details");

        LayoutInflater inflater = this.getLayoutInflater();
        View add_menu_layout = inflater.inflate(R.layout.add_new_main_layout,null);
        //System.out.println("addMenu1 "+addMenu);
        addMenu = add_menu_layout.findViewById(R.id.FoodName);
        //System.out.println("addMenu1 "+addMenu);
        //System.out.println("button1 "+btnSelect);
        //System.out.println("button2 "+btnSelect);
        btnSelect = add_menu_layout.findViewById(R.id.btn_select);
        //System.out.println("button3 "+btnSelect);
        btnupload = add_menu_layout.findViewById(R.id.btn_upload);
        alertdialog.setView(add_menu_layout);
        alertdialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);
        addMenu.setText(item.getName());
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        btnupload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeImage(item);
            }
        });
        alertdialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                item.setName(addMenu.getText().toString());
                category.child(key).setValue(item);

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

    private void changeImage(final Category item) {
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
                        Toast.makeText(Home.this,"Uploaded !!!",Toast.LENGTH_SHORT).show();
                        imagefolder.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        item.setImage(uri.toString());
                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(Home.this,""+e.getMessage(),Toast.LENGTH_SHORT).show();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_orders) {
            // Handle the camera action
            Intent orderIntent = new Intent(Home.this,OrderStatus.class);
            startActivity(orderIntent);
        } else if (id == R.id.nav_menu) {

        } else if (id == R.id.nav_cart) {
           // Intent cartIntent = new Intent(Home.this,Cart.class);
            //startActivity(cartIntent);

        } else if (id == R.id.nav_logout) {
            Paper.book().destroy();
            Intent logoutIntent = new Intent(Home.this,SignIn.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
