package friendlyitsolution.com.filemanager2done;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.OpenableColumns;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    Button btn;
    TextView msg;
    ProgressBar pb;


    Uri fileuri;


   static FirebaseDatabase db;
   static DatabaseReference ref;

   static FirebaseStorage sdb;
   static StorageReference sref;

    ImageView history;

    static Context con;


    RecyclerView recy;
    static MyAdpter m;
    static List<ContactModel> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btn=(Button)findViewById(R.id.button2);
        msg=(TextView)findViewById(R.id.msg);
        pb=(ProgressBar)findViewById(R.id.progressBar2);

        con=this;
        history=findViewById(R.id.history);
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i=new Intent(MainActivity.this,FileDownloaded.class);
                startActivity(i);


            }
        });


        db=FirebaseDatabase.getInstance();
        ref=db.getReference("myfiles");

        sdb=FirebaseStorage.getInstance();
        sref=sdb.getReference();
        list=new ArrayList<>();
        m=new MyAdpter(list);

        recy=(RecyclerView)findViewById(R.id.recy);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this);
        recy.setLayoutManager(mLayoutManager);
        recy.setItemAnimator(new DefaultItemAnimator());
        recy.setAdapter(m);



        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                fileChooser();
            }
        });
        getData();
        checkWriteExternalPermission();
    }

    void fileChooser()
    {
        Intent intent = new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), 11);


    }

    void getData()
    {
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Map<String,String> data=(Map<String, String>)dataSnapshot.getValue();
                ContactModel cm=new ContactModel(dataSnapshot.getKey(),data.get("name"),data.get("url"),data.get("time"),data.get("type"));

                list.add(cm);
                m.notifyDataSetChanged();


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {



            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==11 && resultCode==RESULT_OK && data.getData()!=null)
        {

            fileuri=data.getData();
            uploadFile(fileuri);

        }
        else
        {
            msg.setText("");
            pb.setProgress(0);
        }
    }
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    void uploadFile(Uri fileuri)
    {


        btn.setEnabled(false);
        pb.setProgress(0);
        final String filename=getFileName(fileuri);

        final StorageReference tempref=sref.child("Files").child(filename);

        tempref.putFile(fileuri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(final UploadTask.TaskSnapshot taskSnapshot) {

                pb.setProgress(100);


                tempref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {


                        Date d=new Date();
                        SimpleDateFormat fm=new SimpleDateFormat("dd MMM,yyyy hh:mm a");
                        String time=fm.format(d);

                        Map<String,String> data=new HashMap<>();
                        data.put("name",filename);
                        data.put("url",""+uri);
                        data.put("type",taskSnapshot.getMetadata().getContentType());
                        data.put("time",time);


                        ref.push().setValue(data);

                        btn.setEnabled(true);
                        msg.setText("Successfully Uploaded");





                    }
                });


            }
        }).addOnCanceledListener(new OnCanceledListener() {
            @Override
            public void onCanceled() {

                btn.setEnabled(true);
                pb.setProgress(0);
                msg.setText("Try again later");

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                int per=(int)(100*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                pb.setProgress(per);
                msg.setText("Uploading... "+per+"%");


            }
        });



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==101)
        {
            String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
            int res = getApplicationContext().checkCallingOrSelfPermission(permission);
            if(res != PackageManager.PERMISSION_GRANTED)
            {

                btn.setEnabled(false);

            }
            else
            {
                btn.setEnabled(true);

            }



        }

    }

    void checkWriteExternalPermission()
    {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = getApplicationContext().checkCallingOrSelfPermission(permission);


        if(res != PackageManager.PERMISSION_GRANTED)
        {

            btn.setEnabled(false);
            String[] permissions=new String[]{permission};
            requestPermissions(permissions,101);


        }
        else
        {

            btn.setEnabled(true);
        }



    }



    static void shareFile(String url)
    {
        try {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_SUBJECT, "File Share From Frienly IT Solution");
            String data ="Download Link : " ;
            data = data + url+"\n";
            i.putExtra(Intent.EXTRA_TEXT, data);
            con.startActivity(Intent.createChooser(i, "Share with Friends"));
        } catch (Exception es) {
            //e.toString();
        }
    }

}
