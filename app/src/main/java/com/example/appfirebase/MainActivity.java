package com.example.appfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appfirebase.model.Post;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;
    private FirebaseFirestore firestore;
    private RecyclerView rvNotes;
    private FloatingActionButton btnAdd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference().child("posts");

        firestore = FirebaseFirestore.getInstance();


        rvNotes = findViewById(R.id.rv_notes);
        rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        btnAdd = findViewById(R.id.btn_add);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addNote();
            }
        });


    }

    public void addNote() {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View mView = inflater.inflate(R.layout.add_note, null);
        mDialog.setView(mView);

        AlertDialog dialog = mDialog.create();
        dialog.setCancelable(true);
        dialog.show();

        Button save = mView.findViewById(R.id.btn_save);
        EditText edtTitle = mView.findViewById(R.id.edt_title);
        EditText edtContent = mView.findViewById(R.id.edt_content);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = myRef.push().getKey();
                String title = edtTitle.getText().toString();
                String content = edtContent.getText().toString();
                myRef.child(id).setValue(new Post(id, title, content, getRandomColor()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Add note successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Add note fail", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Post> options =
                new FirebaseRecyclerOptions.Builder<Post>()
                        .setQuery(myRef, Post.class)
                        .build();

        FirebaseRecyclerAdapter adapter = new FirebaseRecyclerAdapter<Post, PostHolder>(options) {
            @Override
            public PostHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.note_items, parent, false);

                return new PostHolder(view);
            }

            @Override
            protected void onBindViewHolder(PostHolder holder, int position, Post model) {
                holder.tvTitle.setText(model.getTitle().toString());
                holder.tvContent.setText(model.getContent().toString());
                holder.layoutNote.setBackgroundColor(Color.parseColor(model.getColor()));

                ImageView ivAction = holder.itemView.findViewById(R.id.iv_action);
                ivAction.setOnClickListener(new View.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(View view) {
                        PopupMenu popupMenu = new PopupMenu(view.getContext(), view);
                        popupMenu.setGravity(Gravity.END);
                        popupMenu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                updateNote(model);
                                return true;
                            }
                        });
                        popupMenu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem menuItem) {
                                deleteNode(model);
                                return false;
                            }
                        });
                        popupMenu.show();
                    }
                });
            }
        };

        rvNotes.setAdapter(adapter);
        adapter.startListening();
    }

    private String getRandomColor() {
        ArrayList<String> colors = new ArrayList<>();
        colors.add("#35ad68");
        colors.add("#c27ba0");
        colors.add("#baa9aa");
        colors.add("#bfbd97");
        colors.add("#09bfb8");
        colors.add("#decade");
        colors.add("#d0ed0e");
        colors.add("#b3ffee");
        colors.add("#d3dfe1");
        colors.add("#894160");
        Random random = new Random();
        return colors.get(random.nextInt(colors.size()));
    }

    public static class PostHolder extends RecyclerView.ViewHolder {
        public TextView tvTitle;
        public TextView tvContent;
        public LinearLayout layoutNote;


        public PostHolder(View view) {
            super(view);
            tvTitle = view.findViewById(R.id.tv_title);
            tvContent = view.findViewById(R.id.tv_content);
            layoutNote = view.findViewById(R.id.layout_note);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_logout:
                mAuth.signOut();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateNote(Post model) {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        View mView = inflater.inflate(R.layout.add_note, null);

        Button save = mView.findViewById(R.id.btn_save);
        EditText edtTitle = mView.findViewById(R.id.edt_title);
        EditText edtContent = mView.findViewById(R.id.edt_content);

        edtTitle.setText(model.getTitle());
        edtContent.setText(model.getContent());
        mDialog.setView(mView);

        AlertDialog dialog = mDialog.create();
        dialog.setCancelable(true);
        dialog.show();


        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = edtTitle.getText().toString();
                String content = edtContent.getText().toString();
                myRef.child(model.getId().toString()).setValue(new Post(model.getId().toString(), title, content, getRandomColor()))
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "update note successful", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "update note fail", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                dialog.dismiss();
            }
        });
    }

    private void deleteNode(Post model) {
        myRef.child(model.getId().toString()).removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "delete note successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "delete note fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    //    private void login(String email, String password) {
//        mAuth.signInWithEmailAndPassword(email, password)
//                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            Log.d("DEBUG", "Login sucessful");
//                        } else {
//                            Log.d("DEBUG", "Login fail");
//                        }
//                    }
//                });
//    }

//    private void resetPass(String email) {
//        mAuth.sendPasswordResetEmail(email)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful())
//                            Log.d("DEBUG", "create new user sucessful");
//                        else
//                            Log.d("DEBUG", "create new user fail");
//                    }
//                });
//
//    }

//    private void signOut() {
//        mAuth.signOut();
//    }

//    private void postDataToRealTimeDB(String data) {
//
//        myRef.setValue(data)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d("DEBUG", "post data sucessful");
//                        } else {
//                            Log.d("DEBUG", "post data fail");
//                        }
//                    }
//                });
//    }

//    private void readDataFromREalTimeDB() {
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String value = snapshot.getValue(String.class);
//                Log.d("DEBUG", "Value is: " + value);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                // Failed to read value
//                Log.w("DEBUG", "Failed to read value.", error.toException());
//            }
//        });
//    }

//    private void postDataToFireStore() {
//        // Create a new user with a first and last name
//        Map<String, Object> user = new HashMap<>();
//        user.put("first", "Ada");
//        user.put("last", "Lovelace");
//        user.put("born", 1815);
//
//        // Add a new document with a generated ID
//        firestore.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d("DEBUG", "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w("DEBUG", "Error adding document", e);
//                    }
//                });
//    }

//    private void addPostData(Post data) {
//        DatabaseReference myRefRoot = database.getReference();
//        myRefRoot.child("posts").setValue(data)
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        if (task.isSuccessful()) {
//                            Log.d("DEBUG", "post data " + data + " successful");
//                        } else {
//                            Log.d("DEBUG", "post data " + data + " fail");
//                        }
//                    }
//                });
//    }

}