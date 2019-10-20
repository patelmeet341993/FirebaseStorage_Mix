package friendlyitsolution.com.filemanager2done;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;

import java.io.File;
import java.util.List;

public class MyAdpter extends RecyclerView.Adapter<MyAdpter.MyViewHolder> {

    private List<ContactModel> moviesList;

    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView name,msg,time,type;
        ImageView del,down,img,share;

        public MyViewHolder(View view) {
            super(view);

            share=view.findViewById(R.id.share);
            type=view.findViewById(R.id.type);
            time=view.findViewById(R.id.time);
            del =view.findViewById(R.id.delete);
            down=view.findViewById(R.id.dl);
            name=view.findViewById(R.id.name);
            msg =view.findViewById(R.id.msg);
            img=view.findViewById(R.id.img);
        }
    }


    public MyAdpter(List<ContactModel> moviesList) {
        this.moviesList = moviesList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.itemlayout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final ContactModel model = moviesList.get(position);


        holder.name.setText(model.name);
        holder.type.setText(model.type);
        holder.time.setText(model.time);

        if(model.type.contains("image"))
        {

                    Glide.with(holder.img.getContext()).load(model.path)
                    .override(80, 80)
                    .fitCenter()
                    .into(holder.img);


            holder.img.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.img.setVisibility(View.GONE);
        }



        holder.share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainActivity.shareFile(model.path);
            }
        });

        holder.down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try{
                    File publicFolder = new File(Environment.getExternalStorageDirectory()+"/FriendlyItSolution");

                    if(!publicFolder.exists()) {
                        publicFolder.mkdir();

                    }

                    File f=new File(Environment.getExternalStorageDirectory()+"/FriendlyItSolution/"+model.name);
                    MainActivity.sref.child("Files").child(model.name).getFile(f).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {

                            holder.msg.setText("Successfully downloaded");
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            holder.msg.setText("try again later");
                        }
                    }).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();

                            holder.msg.setText("Downloading..." + ((int) progress) + "%  ");
                        }
                    });


                }
                catch(Exception e)
                {
                    holder.msg.setText("Error : "+e.getMessage());
                }




            }
        });
        holder.del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.sref.child("Files").child(model.name).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        MainActivity.ref.child(model.id).removeValue();
                        MainActivity.list.remove(model);
                        MainActivity.m.notifyDataSetChanged();


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.msg.setText("try again");



                    }
                });
            }
        });
    }
    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return moviesList.size();
    }
}
