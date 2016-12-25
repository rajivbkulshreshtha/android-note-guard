package com.rajiv.noteguard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {
    public final static String RESET_KEY = "resetkey";
    private final static String FILENAME = "testFile.txt";
    private final static String FILESAVED = "filesaved";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayShowHomeEnabled(false);


        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        boolean check = pref.getBoolean(FILESAVED, false);
        if (check) {
            loadSavedFile();
        }

        setSaveButtonListener();
        setLockButtonListener();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_main_group_resetPasspoint:
                resetPoints();
        }

        return true;
    }

    //When lock button is pressed
    private void setLockButtonListener() {
        Button button = (Button) findViewById(R.id.buttonLock);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockText();
            }
        });


    }

    //To lock Text
    private void lockText() {
        saveText();
        Intent intent = new Intent(MainActivity.this, ImageActivity.class);
        startActivity(intent);
    }


    //To resetPoint
    private void resetPoints() {
        lockText();

        Intent intent = new Intent(MainActivity.this, ImageActivity.class);
        intent.putExtra(RESET_KEY, true);
        startActivity(intent);
    }

    //Load saved Text File
    private void loadSavedFile() {
        EditText editText = (EditText) findViewById(R.id.editText);

        try {
            FileInputStream fis = openFileInput(FILENAME);
            BufferedReader br = new BufferedReader(new InputStreamReader(new DataInputStream(fis)));
            String line = null;

            while ((line = br.readLine()) != null) {
                editText.append(line + "\n");
            }

            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(FILESAVED, true);
            editor.commit();

            Toast.makeText(this, R.string.file_loaded, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, R.string.failed_to_load, Toast.LENGTH_LONG).show();
        }
    }

    //When save button is pressed
    private void setSaveButtonListener() {
        Button button = (Button) findViewById(R.id.buttonSave);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveText();

            }
        });
    }

    //To save Text
    private void saveText() {
        EditText editText = (EditText) findViewById(R.id.editText);
        String text = editText.getText().toString();


        try {
            FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
            fos.write(text.getBytes());
            fos.close();

            SharedPreferences pref = getPreferences(MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean(FILESAVED, true);
            editor.commit();

            Toast.makeText(MainActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, R.string.failed_to_save, Toast.LENGTH_LONG).show();
        }
    }

}
