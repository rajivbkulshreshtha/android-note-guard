package com.rajiv.noteguard;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.List;

public class ImageActivity extends AppCompatActivity implements PointCollectorListener {

    public final static int REQUEST_KEY_IMAGECAPTURE = 10;
    private ImageView imageView;
    private final static int POINT_CLOSENESS = 50;
    private final static String PASSPOINT_SET = "passpointset";
    private PointCollector pointCollector = new PointCollector();
    private Database db = new Database(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        //Action bar settings
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_launcher);

        //setting up point collector listener
        pointCollector.setListener(this);

        setMenuItemCamera();

        //When Reset Passpoint option from MainActivity is touched
        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            boolean resetCheck = extra.getBoolean(MainActivity.RESET_KEY);
            if (resetCheck) {
                resetPoint();
            }
        }

        //Checking if Passpoint is set or not in Database
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        boolean setCheck = pref.getBoolean(PASSPOINT_SET, false);

        if (!setCheck) {
            showPasspointPrompt();
        }

        setTouchListener();

    }

    //When Reset Passpoint option from MainActivity is touched, Then this method do rest of work
    public void resetPoint() {
        pointCollector.clear();
        passpointSet(false);
        setTouchListener();


    }

    //When option menu touched
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_image, menu);
        return true;
    }

    //When option menuItem touched
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_image_group_camera:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, REQUEST_KEY_IMAGECAPTURE);
                break;
            case R.id.menu_image_group_reset_background:
                resetBackground();
                break;
        }

        return true;

    }

    //menuItem Reset Background touched
    private void resetBackground() {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.background_image));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
    }

    //menuItem camera touched
    private void setMenuItemCamera() {
        MenuItem camera = (MenuItem) findViewById(R.id.menu_image_group_camera);
        imageView = (ImageView) findViewById(R.id.imageView);

        if (!hasCamera()) {
            camera.setEnabled(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_KEY_IMAGECAPTURE && resultCode == RESULT_OK) {

            Bundle extra = data.getExtras();
            Bitmap photo = (Bitmap) extra.get("data");
            imageView.setImageBitmap(photo);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

        }
    }

    // Check wheater mobile has camera or not
    public boolean hasCamera() {
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }


    //When you Touch on display for setting up or varify your passpoint
    private void setTouchListener() {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setOnTouchListener(pointCollector);
    }

    // When user open the application for the first time OR when user try to Reset the passpoint this prompt heppen
    private void showPasspointPrompt() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Create your Passpoint");




        builder.setMessage(Html.fromHtml("Touch to any four points on the screen to set your passpoint." +"<br><b>NOTE: </b>"+
                "Remember the sequences and position of the points you touched."));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //When user touches the passpoint this method determine where to save them as Lock or wheather to use them as password of passpoint
    @Override
    public void pointCollected(final List<Point> pointList) {

        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        boolean setCheck = pref.getBoolean(PASSPOINT_SET, false);

        if (!setCheck) {

            savePasspoint(pointList);
        } else {

            varifyPasspoint(pointList);
        }

    }

    //This method save the passpoint into database
    private void savePasspoint(final List<Point> pointList) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Saving...");
        final AlertDialog dialog = builder.create();
        dialog.show();
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                db.storePoints(pointList);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                passpointSet(true);


                dialog.dismiss();
                pointCollector.clear();

            }
        };
        task.execute();

    }

    // This method set edit preferences for setting passpoint
    private void passpointSet(boolean set) {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(PASSPOINT_SET, set);
        editor.commit();
    }

    //This method use passpoint as a key to varify them with database's passpoint
    private void varifyPasspoint(final List<Point> touchPoints) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Checking...");
        final AlertDialog dialog = builder.create();
        dialog.show();
        AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


                List<Point> savedPoints = db.getPoint();

                if ((savedPoints.size() != PointCollector.NUM_POINT) || touchPoints.size() != PointCollector.NUM_POINT) {
                    return false;
                }

                for (int i = 0; i < PointCollector.NUM_POINT; i++) {
                    Point savedPoint = savedPoints.get(i);
                    Point touchPoint = touchPoints.get(i);

                    int xDiff = savedPoint.x - touchPoint.x;
                    int yDiff = savedPoint.y - touchPoint.y;

                    int distSquared = (xDiff * xDiff) + (yDiff + yDiff);

                    if (distSquared > (POINT_CLOSENESS * POINT_CLOSENESS)) {

                        return false;
                    }
                }
                return true;
            }

            @Override
            protected void onPostExecute(Boolean pass) {

                dialog.dismiss();
                pointCollector.clear();

                if (pass) {
                    Intent intent = new Intent(ImageActivity.this, MainActivity.class);
                    startActivity(intent);

                } else {
                    Toast.makeText(ImageActivity.this, R.string.access_denied, Toast.LENGTH_LONG).show();
                }


            }
        };

        task.execute();
    }


}