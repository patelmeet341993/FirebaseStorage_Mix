package friendlyitsolution.com.filemanager2done;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class FileDownloaded extends AppCompatActivity {
    ListView lv;
    ArrayAdapter<String> adapter;
    String f[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_downloaded);

        lv=findViewById(R.id.lv);
        File Folder = new File(Environment.getExternalStorageDirectory(), "FriendlyItSolution/");
        if (Folder.exists()) {
            f=Folder.list();
            adapter=new ArrayAdapter<>(FileDownloaded.this,android.R.layout.simple_list_item_1,f);
            lv.setAdapter(adapter);
            adapter.notifyDataSetChanged();


        }
        else
        {
            Toast.makeText(getApplicationContext(),"No file found",Toast.LENGTH_LONG).show();
        }

    }
}
