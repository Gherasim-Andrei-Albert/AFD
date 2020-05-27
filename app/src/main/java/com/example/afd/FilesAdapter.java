package com.example.afd;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class FilesAdapter
        extends RecyclerView.Adapter<FilesAdapter.FilesViewHolder> {


    private final Context ctx;
    LayoutInflater inflater;
    public File[] files;
    public File directory;
    public File selectedFile;

    public FilesAdapter(Context ctx, File directory){
        this.ctx = ctx;
        this.directory = directory;
        inflater = LayoutInflater.from(ctx);

        this.files = directory.listFiles();
    }



    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_file,parent,false);
        FilesViewHolder holder = new FilesViewHolder(view);
        return holder;
    }


    public void requestForPermission(int code) {
        requestPermissions(
            (Activity) ctx,
            new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE},
            code
        );
    }


    @Override
    public void onBindViewHolder(@NonNull FilesViewHolder holder, final int position) {
        holder.fileNameTxt.setText(files[position].getName());
        holder.bgImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                selectedFile = files[position];
                if(selectedFile.isDirectory()){
                    directory = selectedFile;
                    requestForPermission(138);
                }else{
                    Intent intent = new Intent(ctx,EditActivity.class);
                    intent.putExtra("filePath",selectedFile.getAbsolutePath());
                    ctx.startActivity(intent);
                }
            }
        });
        holder.bgImg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectedFile = files[position];
                return false;
            }
        });
        if(files[position].isDirectory())
            holder.fileIconImg.setImageResource(R.drawable.folder_ic);
        else
            holder.fileIconImg.setImageResource(R.drawable.file_ic2);
        ((Activity)ctx).registerForContextMenu(holder.bgImg);
    }

    @Override
    public int getItemCount() {
        return files.length;
    }

    class FilesViewHolder extends RecyclerView.ViewHolder{

        TextView fileNameTxt;
        ImageView fileIconImg;
        ImageView bgImg;

        public FilesViewHolder(@NonNull View itemView) {
            super(itemView);
            fileNameTxt = itemView.findViewById(R.id.file_name_txt);
            fileIconImg = itemView.findViewById(R.id.file_icon_img);
            bgImg = itemView.findViewById(R.id.bg_img);
        }
    }
}
