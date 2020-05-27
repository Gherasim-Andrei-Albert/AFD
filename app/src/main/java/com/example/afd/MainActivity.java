package com.example.afd;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.MimeTypeFilter;
import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.SequenceInputStream;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class MainActivity extends AppCompatActivity {


    FilesAdapter mAdapter;

    private File copiedFile;
    public String input;
    public String action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.files_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new FilesAdapter(this, new File("/storage/emulated/0"));
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(copiedFile!=null)
            outState.putString("copiedFilePath",copiedFile.getAbsolutePath());
        outState.putString("input",input);
        outState.putString("action",action);
        if(mAdapter.selectedFile!=null)
            outState.putString("selectedFilePath",mAdapter.selectedFile.getAbsolutePath());
        outState.putString("directoryPath",mAdapter.directory.getAbsolutePath());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String copiedFilePath = savedInstanceState.getString("copiedFilePath","");
        if(copiedFilePath!="")
            copiedFile = new File(copiedFilePath);
        String selectedFilePath = savedInstanceState.getString("selectedFilePath","");
        if(selectedFilePath!="")
            mAdapter.selectedFile = new File(selectedFilePath);

        input = savedInstanceState.getString("input","");
        if(input!=""){
            action = savedInstanceState.getString("action","");
            if(action.equals("rename")) {
                action = "rename";
                popUp(this, new ConfirmListener() {
                    @Override
                    public void onConfirm(String input) {
                        requestForPermission(142);
                    }
                });

            }
            if(action.equals("new file")) {
                action = "new file";
                popUp(this, new ConfirmListener() {

                    @Override
                    public void onConfirm(String input) {
                        requestForPermission(140);
                    }
                });

            }
            if(action.equals("new folder")) {
                action = "new folder";
                popUp(this, new ConfirmListener() {

                    @Override
                    public void onConfirm(String input) {
                        requestForPermission(139);
                    }
                });

            }
        }
        String dirPath = savedInstanceState.getString("directoryPath","");
        if(dirPath!=""){
            mAdapter.directory = new File(dirPath);
            mAdapter.files = mAdapter.directory.listFiles();
            mAdapter.notifyDataSetChanged();
        }
    }

    public void requestForPermission(int code) {

        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE},code);

    }

    interface ConfirmListener{void onConfirm(String input);}

    void popUp(final Context ctx, final ConfirmListener confirmListener){

        final EditText input = new EditText(ctx);
        input.setText(((MainActivity)ctx).input);
        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                ((MainActivity)ctx).input = s.toString();
            }
        });
        new AlertDialog.Builder(ctx)
            .setMessage("Name:")
            .setView(input)
            .setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    ((MainActivity)ctx).input = "";
                    ((MainActivity)ctx).action = "";
                }
            })
            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    confirmListener.onConfirm(input.getText().toString());
                    ((MainActivity)ctx).action = "";
                }
            })
            .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ((MainActivity)ctx).input = "";
                    ((MainActivity)ctx).action = "";
                }
            })
            .show();

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.new_folder:
                action = "new folder";
                popUp(this, new ConfirmListener() {

                    @Override
                    public void onConfirm(String input) {
                        requestForPermission(139);
                    }
                });
                return true;
            case R.id.new_file:
                action = "new file";
                popUp(this, new ConfirmListener() {

                    @Override
                    public void onConfirm(String input) {
                        requestForPermission(140);
                    }
                });
                return true;
            case R.id.paste:
                requestForPermission(143);
                return true;
            case R.id.internal:
                requestForPermission(144);
                return true;
            case R.id.sdcard:
                requestForPermission(145);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy:
                copiedFile = mAdapter.selectedFile;
                return true;
            case R.id.delete:
                requestForPermission(141);
                return true;
            case R.id.rename:
                action = "rename";
                popUp(this, new ConfirmListener() {
                    @Override
                    public void onConfirm(String input) {
                        requestForPermission(142);
                    }
                });
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }


    @Override
    public void onBackPressed() {
        File parent = mAdapter.directory.getParentFile();

        if(mAdapter.directory.getAbsolutePath().equals("/storage/emulated/0")
                || parent.getName().equals("storage"))
            super.onBackPressed();
        else {
            mAdapter.directory = parent;
            mAdapter.selectedFile = parent;
            requestForPermission(138);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        switch(requestCode) {
            case 138: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (mAdapter.selectedFile.isDirectory()) {
                        mAdapter.files = mAdapter.selectedFile.listFiles();
                        mAdapter.notifyDataSetChanged();
                    }

                    return;
                }
            }
            case 139: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    DocumentFile dir = DocumentFile.fromFile(mAdapter.directory);
                    dir.createDirectory(input);
                    input = "";
                    mAdapter.files = mAdapter.directory.listFiles();
                    mAdapter.notifyDataSetChanged();

                }
                return;
            }
            case 140:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        File newFile = new File(
                                mAdapter.directory.getAbsolutePath()
                                        + File.separator
                                        + input);
                        newFile.createNewFile();
                        input = "";
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mAdapter.files = mAdapter.directory.listFiles();
                    mAdapter.notifyDataSetChanged();

                }
                return;
            }
            case 141:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    DocumentFile file = DocumentFile.fromFile(mAdapter.selectedFile);
                    file.delete();
                    mAdapter.files = mAdapter.directory.listFiles();
                    mAdapter.notifyDataSetChanged();
                }
                return;
            }
            case 142:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    DocumentFile file = DocumentFile.fromFile(mAdapter.selectedFile);
                    file.renameTo(input);
                    input = "";
                    mAdapter.files = mAdapter.directory.listFiles();
                    mAdapter.notifyDataSetChanged();
                }
                return;
            }
            case 143:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    copy(copiedFile,mAdapter.directory);
                    mAdapter.files = mAdapter.directory.listFiles();
                    mAdapter.notifyDataSetChanged();
                }
                return;
            }
            case 144:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    mAdapter.directory = new File("storage/emulated/0");
                    mAdapter.files = mAdapter.directory.listFiles();
                    mAdapter.notifyDataSetChanged();
                }
                return;
            }
            case 145:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    File dir = null;
                    File[] children = new File("/storage/").listFiles();
                    for(File child: children)
                        if(!child.getName().equals("emulated")
                            && !child.getName().equals("self")){
                            dir = child;
                            break;
                        }

                    mAdapter.directory = dir;
                    mAdapter.files = mAdapter.directory.listFiles();
                    mAdapter.notifyDataSetChanged();
                }
                return;
            }
        }
    }

    public void copy(File source, File currDir){

        if(source.isFile()){
            try {
                File newFile = new File(
                        currDir.getAbsolutePath()
                                + File.separator
                                + source.getName());

                FileInputStream in = new FileInputStream(source);
                byte[] content = new byte[in.available()];
                in.read(content);
                in.close();
                FileOutputStream out = new FileOutputStream(newFile);
                out.write(content);
                out.flush();
                out.close();

                newFile.createNewFile();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if(source.isDirectory()){
            File[] children = source.listFiles();

            DocumentFile docFileDir = DocumentFile.fromFile(currDir);
            docFileDir.createDirectory(source.getName());

            if(children!=null)
                for(File child: children)
                    copy(child, new File(currDir.getAbsolutePath()
                            + File.separator + source.getName())
                    );
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.app_menu, menu);
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
    }

}