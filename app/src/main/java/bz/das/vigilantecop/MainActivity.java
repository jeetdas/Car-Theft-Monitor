package bz.das.vigilantecop;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.openalpr.OpenALPR;
import org.openalpr.model.Results;
import org.openalpr.model.ResultsError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ActionBarDrawerToggle mDrawerToggle;
    ListView mDrawerList;
    RelativeLayout mDrawerPane;
    DrawerLayout mDrawerLayout;
    ArrayList<NavItem> mNavItems = new ArrayList<NavItem>();
    Button recordButton;
    List flaggedNumbersList;
    String ANDROID_DATA_DIR;
    Camera mcamera;
    CameraPreview cameraPreview;
    TextView resultTextView;
    boolean isRunning;
    FrameLayout preview;
    LocationManager locationManager;
    LocationListener locationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        TextView userNameView = (TextView) findViewById(R.id.userName);
        mDrawerPane = (RelativeLayout) findViewById(R.id.drawerPane);
        mDrawerList = (ListView) findViewById(R.id.navList);
        recordButton = (Button) findViewById(R.id.recording_button);
        resultTextView = (TextView) findViewById(R.id.resultTextView);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        flaggedNumbersList = new ArrayList();
        mcamera = getCameraInstance();
        cameraPreview = new CameraPreview(this, mcamera);
        preview.addView(cameraPreview);
        mNavItems.add(new NavItem("Home", "Start recording"));
        mNavItems.add(new NavItem("Location Tracking", "Report location data to insurance companies for discount"));
        mNavItems.add(new NavItem("Rewards", "Checkout rewards for your great service to the community"));
        mNavItems.add(new NavItem("Settings", "Toggle recording and location settings"));

        userNameView.setText("Jeeto Torando"); // Update user's name here
        flaggedNumbersList.add("FP5S3A"); // Sample flagged number
        ANDROID_DATA_DIR = "data/data/bz.das.vigilantecop";
        isRunning = false;

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mcamera.takePicture(null, null, mPicture);
                handler.postDelayed(this, 3000);
                boolean flag = displayGpsStatus();
                if (flag) {

                    Log.v("LALA", "LOCATION 1");

                    resultTextView.setText(resultTextView.getText() + "\nPlease!! move your device to see the changes in coordinates." + "\nWait..");

                    locationListener = new MyLocationListener(MainActivity.this);
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(MainActivity.this, "GPS permission not granted", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
                    }

                } else {
                    Toast.makeText(MainActivity.this, "Your GPS is: OFF", Toast.LENGTH_SHORT).show();
                }
            }
        };

        DrawerListAdapter adapter = new DrawerListAdapter(this, mNavItems);
        mDrawerList.setAdapter(adapter);

        recordButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (isRunning) {
                    recordButton.setText("START CAPTURING");
                    handler.removeCallbacks(runnable);
                    isRunning = !isRunning;
                }
                else {
                    recordButton.setText("STOP CAPTURING");
                    handler.removeCallbacks(runnable);
                    handler.postDelayed(runnable, 1000);
                    isRunning = !isRunning;
                }
            }
        });

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItemFromDrawer(position);
            }
        });

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, 0, 0) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private Boolean displayGpsStatus() {
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        boolean gpsStatus = Settings.Secure.isLocationProviderEnabled(contentResolver, LocationManager.GPS_PROVIDER);
        return gpsStatus;
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
        }
        return camera;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, final Camera camera) {
            final File pictureFile = getOutputMediaFile();
            Log.d("LALA", "2");

            if (pictureFile == null) {
                Log.d("LALA", "3");
                return;
            }
            try {
                Log.d("LALA", "4");
                //resultTextView.setText(resultTextView.getText() + "Working right now, hold your horses" + "\n");
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

                if (true) {
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            final String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inSampleSize = 10;

                            Log.d("LALA", pictureFile.getAbsolutePath().toString());

                            final String result = OpenALPR.Factory.create(MainActivity.this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig("us", "mo", pictureFile.getAbsolutePath(), openAlprConfFile, 10);

                            Log.d("LALA", result);

                            try {
                                final Results results = new Gson().fromJson(result, Results.class);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (results == null || results.getResults() == null || results.getResults().size() == 0) {
                                            //Toast.makeText(MainActivity.this, "It was not possible to detect the licence plate.", Toast.LENGTH_LONG).show();
                                            resultTextView.setText(resultTextView.getText() + "It was not possible to detect the licence plate." + "\n");
                                            deleteImageFile(pictureFile.getAbsolutePath().toString());
                                        } else {
                                            Log.d("LALA", "" + results.getResults().size());
                                            for (int i = 0; i < results.getResults().size(); i += 1)
                                            {
                                                Log.d("LALA", "" + results.getResults().get(i));
                                                if (results.getResults().get(i).getConfidence() >= 90.0)
                                                {
                                                    resultTextView.setText(resultTextView.getText() + "Plate: " + results.getResults().get(i).getPlate()
                                                            // Trim confidence to two decimal places
                                                            + " Confidence: " + String.format("%.2f", results.getResults().get(i).getConfidence()) + "%"
                                                            // Convert processing time to seconds and trim to two decimal places
                                                            + " Processing time: " + String.format("%.2f", ((results.getProcessingTimeMs() / 1000.0) % 60)) + " seconds" + "\n");

                                                    if (flaggedNumbersList.contains(results.getResults().get(i).getPlate().toUpperCase()))
                                                    {
                                                        resultTextView.setText(resultTextView.getText() + "Found flagged number" + "\n");
                                                    }
                                                }
                                                else {
                                                    resultTextView.setText(resultTextView.getText() + "Confidence not high enough\n");
                                                    deleteImageFile(pictureFile.getAbsolutePath().toString());
                                                }
                                            }
                                        }
                                    }
                                });

                            } catch (JsonSyntaxException exception) {
                                final ResultsError resultsError = new Gson().fromJson(result, ResultsError.class);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        resultTextView.setText(resultsError.getMsg());
                                    }
                                });
                            }
                        }
                    });
                }

            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }
            mcamera.stopPreview();
            mcamera.startPreview();
        }
    };

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("LALA", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void deleteImageFile(final String filePath){
        File file = new File(filePath);
        if(file.exists()){
            file.delete();
            Log.i("LALA", String.format("Deleted file %s", filePath));
        }
    }

    private void selectItemFromDrawer(int position) {

        switch (position) {
            case 0:
                startActivity(new Intent(MainActivity.this, MainActivity.class));
                finish();
                Toast.makeText(MainActivity.this, "MainActivity", Toast.LENGTH_SHORT).show();
                break;
            case 1:
                //startActivity(new Intent(MainActivity.this, LocationActivity.class));
                Toast.makeText(MainActivity.this, "Location", Toast.LENGTH_SHORT).show();
                break;
            case 2:
                //startActivity(new Intent(MainActivity.this, RewardsActivity.class));
                Toast.makeText(MainActivity.this, "RewardsActivity", Toast.LENGTH_SHORT).show();
                break;
            case 3:
                //startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                Toast.makeText(MainActivity.this, "SettingsActivity", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        // If it returns true, then it has handled
        // the nav drawer indicator touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    class NavItem {
        String mTitle;
        String mSubtitle;

        public NavItem(String title, String subtitle) {
            mTitle = title;
            mSubtitle = subtitle;
        }
    }

    class DrawerListAdapter extends BaseAdapter {

        Context mContext;
        ArrayList<NavItem> mNavItems;

        public DrawerListAdapter(Context context, ArrayList<NavItem> navItems) {
            mContext = context;
            mNavItems = navItems;
        }

        @Override
        public int getCount() {
            return mNavItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNavItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.drawer_item, null);
            }
            else {
                view = convertView;
            }

            TextView titleView = (TextView) view.findViewById(R.id.title);
            TextView subtitleView = (TextView) view.findViewById(R.id.subTitle);

            titleView.setText( mNavItems.get(position).mTitle );
            subtitleView.setText( mNavItems.get(position).mSubtitle );

            return view;
        }
    }

}