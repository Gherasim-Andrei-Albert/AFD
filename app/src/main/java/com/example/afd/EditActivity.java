package com.example.afd;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class EditActivity extends AppCompatActivity {


    public File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String filePath = getIntent().getExtras().getString("filePath");
        try {
            file = new File(filePath);
            BufferedReader reader = new BufferedReader(
                    new FileReader(file));
            String line;
            String result = "";
            while((line = reader.readLine())!=null)
                result+=line+"\n";
            if(!result.equals(""))
                result = result.substring(0,result.length()-1);
            ((TextView)findViewById(R.id.text_area)).setText(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.save:

                String content = ((EditText)findViewById(R.id.text_area))
                        .getText().toString();

                try {
                    FileWriter writer = new FileWriter(file);
                    writer.write(content);
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
