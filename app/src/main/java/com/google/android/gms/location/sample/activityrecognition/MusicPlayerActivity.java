package com.google.android.gms.location.sample.activityrecognition;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.DetectedActivity;


import java.util.ArrayList;

import static java.security.AccessController.getContext;

/**
 * This sample demonstrates use of the
 * {@link com.google.android.gms.location.ActivityRecognitionApi} to recognize a user's current
 * activity, such as walking, driving, or standing still. It uses an
 * {@link android.app.IntentService} to broadcast detected activities through a
 * {@link BroadcastReceiver}. See the {@link DetectedActivity} class for a list of DetectedActivity
 * types.
 * <p/>
 * Note that this activity implements
 * {@link ResultCallback<R extends com.google.android.gms.common.api.Result>}.
 * Requesting activity detection updates using
 * {@link com.google.android.gms.location.ActivityRecognitionApi#requestActivityUpdates}
 * and stopping updates using
 * {@link com.google.android.gms.location.ActivityRecognitionApi#removeActivityUpdates}
 * returns a {@link com.google.android.gms.common.api.PendingResult}, whose result
 * object is processed by the {@code onResult} callback.
 */
public class MusicPlayerActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<Status>, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener {

    public static final String TAG = "myLogs";

    final String DATA_SD = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
            + "/music.mp3";

    final Uri DATA_URI = ContentUris
            .withAppendedId(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    13359);

    protected ActivityDetectionBroadcastReceiver mBroadcastReceiver;

    public GoogleApiClient mApiClient;

    MediaPlayer mediaPlayer;
    AudioManager audioManager;
    DetectedActivity detectedActivity;
    Location location;


    private Button mRequestActivityUpdatesButton;
    private Button mRemoveActivityUpdatesButton;

    public  ListView mDetectedActivitiesListView;

    private DetectedActivitiesAdapter mAdapter;
    private ArrayList<DetectedActivity> mDetectedActivities;




  /*  @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "You are in onConnectionSuspended");
        Toast.makeText(getApplicationContext(), "You are in onConnectionSuspended", Toast.LENGTH_SHORT).show();
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        Log.d(TAG, "onCreate");
        Toast.makeText(getApplicationContext(), "You are in onCreate", Toast.LENGTH_SHORT).show();

        mRequestActivityUpdatesButton = (Button) findViewById(R.id.request_activity_updates_button);
        mRemoveActivityUpdatesButton = (Button) findViewById(R.id.remove_activity_updates_button);
        mRemoveActivityUpdatesButton = (Button) findViewById(R.id.remove_activity_updates_button);
        mDetectedActivitiesListView = (ListView) findViewById(R.id.detected_activities_listview);

        mBroadcastReceiver = new ActivityDetectionBroadcastReceiver();


        setButtonsEnabledState();

       /* mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        Log.d(TAG, "onCreate - mApiClient is built");
        Toast.makeText(getApplicationContext(), "You are in onCreate - mApiClient is built", Toast.LENGTH_SHORT).show();*/

       /* mApiClient.connect();
        Log.d(TAG, "onCreate - connected");
        Toast.makeText(getApplicationContext(), "You are in onCreate - connected", Toast.LENGTH_SHORT).show();*/

        if (savedInstanceState != null && savedInstanceState.containsKey(
                Constants.DETECTED_ACTIVITIES)) {
            mDetectedActivities = (ArrayList<DetectedActivity>) savedInstanceState.getSerializable(
                    Constants.DETECTED_ACTIVITIES);
        } else {
            mDetectedActivities = new ArrayList<DetectedActivity>();

            // Set the confidence level of each monitored activity to zero.
            for (int i = 0; i < Constants.MONITORED_ACTIVITIES.length; i++) {
                mDetectedActivities.add(new DetectedActivity(Constants.MONITORED_ACTIVITIES[i], 0));
            }
        }
        // Bind the adapter to the ListView responsible for display data for detected activities.
        mAdapter = new DetectedActivitiesAdapter(this, mDetectedActivities);
        mDetectedActivitiesListView.setAdapter(mAdapter);

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();

    }

    public void releaseMP() {
        Log.i(TAG, "releaseMP");
        if (mediaPlayer != null) {
            try {
                mediaPlayer.release();
                mediaPlayer = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkSpeed(Location location) {

        float speed = location.getSpeed();

        String speedStr = String.valueOf(speed);
        Toast.makeText(getApplicationContext(), speedStr, Toast.LENGTH_SHORT).show();

        return location.getSpeed() < 8;
    }

    public void recognizeAndPlay(ArrayList<DetectedActivity> mDetectedActivities) {



        for (DetectedActivity da : mDetectedActivities) {

            Log.i(TAG, "DetectedActivity - forEach");
            switch (da.getType()) {


                case DetectedActivity.STILL:
                    Log.i(TAG, "DetectedActivity - still");

                    if (da.getConfidence() > 75) {

                        Log.i(TAG, "DetectedActivity - still - confidence done");
                        releaseMP();
                    }
                    break;


                case DetectedActivity.ON_FOOT:
                    Log.i(TAG, "DetectedActivity - foot");
                    if (da.getConfidence() > 75) {
                        Log.i(TAG, "DetectedActivity - foot - confidence done");
                        mediaPlayer = MediaPlayer.create(this, R.raw.foot);
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing " + da.getType(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.ON_BICYCLE:
                    Log.i(TAG, "DetectedActivity - bike");
                    if (da.getConfidence() > 75) {
                        Log.i(TAG, "DetectedActivity - bike - confidence done");
                        mediaPlayer = MediaPlayer.create(this, R.raw.bike);
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing " + da.getType(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.IN_VEHICLE:
                    if (da.getConfidence() > 75) {
                        mediaPlayer = MediaPlayer.create(this, R.raw.vehicle);
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing " + da.getType(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.TILTING:
                    Log.i(TAG, "DetectedActivity - titling - confidence done");
                    if (da.getConfidence() > 75) {
                        Log.i(TAG, "DetectedActivity - titling- confidence done");
                        releaseMP();
                        mediaPlayer = MediaPlayer.create(this, R.raw.titling);
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing " + da.getType(), Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DetectedActivity.UNKNOWN:
                    if (da.getConfidence() > 75) {
                        mediaPlayer = MediaPlayer.create(this, R.raw.unknown);
                        mediaPlayer.start();
                        Toast.makeText(getApplicationContext(), "Playing " + da.getType(), Toast.LENGTH_SHORT).show();
                    }
                    break;


            }


        }
    }

    public void onClickStart(View v) {

        Log.i(TAG, "onClickStart");

        try {
            mediaPlayer = MediaPlayer.create(this, R.raw.foot);
        } catch (NullPointerException E) {
            Toast.makeText(getApplicationContext(), "NullPointerException", Toast.LENGTH_SHORT).show();
        }
    }


    /*  public void pickMusic() {
        Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 10);
    }*/

    /* @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
         if (resultCode == RESULT_OK) {
             if (requestCode == 10) {
                 Uri selectedMusicUri = data.getData();
                 if (selectedMusicUri != null) {
                     MediaPlayer mp = new MediaPlayer();
                     try {
                         mp.setDataSource(this, selectedMusicUri);
                         mp.prepare();
                         mp.start();

                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 }
             }
         }
     }
 */
    public void onClickPause(View V) {
        Log.i(TAG, "onClickPause");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            Log.i(TAG, "onClickPause - Paused");
        }

        else
            mediaPlayer.start();
        Log.i(TAG, "onClickPause - Started");

    }

    protected synchronized void buildGoogleApiClient() {

        Log.i(TAG, " buildGoogleApiClient");
        mApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(ActivityRecognition.API)
                .build();
    }

    @Override
    protected void onStart() {
        Log.i(TAG, " onStart");
        super.onStart();
        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        Log.i(TAG, " onStop");
        super.onStop();
        mApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        Log.i(TAG, " onResume");
        super.onResume();
        // Register the broadcast receiver that informs this activity of the DetectedActivity
        // object broadcast sent by the intent service.
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(Constants.BROADCAST_ACTION));
    }

    @Override
    protected void onPause() {
        Log.i(TAG, " onPause");
        // Unregister the broadcast receiver that was registered during onResume().
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(TAG, "You are in onConnected");
        Toast.makeText(getApplicationContext(), "You are in onConnected", Toast.LENGTH_SHORT).show();
        // Intent intent = new Intent(this, ActivityRecognition.class);
        Log.i(TAG, "You are in onConnected - Intent created");
        Toast.makeText(getApplicationContext(), "You are in onConnected - Intent created", Toast.LENGTH_SHORT).show();
        // PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(mApiClient, 1000, pendingIntent);
        Log.i(TAG, "You are in onConnected - end");
        Toast.makeText(getApplicationContext(), "You are in onConnected - end", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "You are in onConnectionFailed");
        Toast.makeText(getApplicationContext(), "You are in onConnectionFailed", Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "onConnectionSuspended");

        mApiClient.connect();
    }


    public void requestActivityUpdatesButtonHandler(View view) {
        Log.i(TAG, "requestActivityUpdatesButtonHandler");
        if (!mApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                mApiClient,
                Constants.DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);

        recognizeAndPlay(mDetectedActivities);

      /*  if (detectedActivity.getConfidence() > 75){
            mediaPlayer = MediaPlayer.create(this, R.raw.still);
            mediaPlayer.start();

        }*/
    }

    public void removeActivityUpdatesButtonHandler(View view) {
        Log.i(TAG, "requestActivityUpdatesButtonHandler");
        if (!mApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }
        // Remove all activity updates for the PendingIntent that was used to request activity
        // updates.
        ActivityRecognition.ActivityRecognitionApi.removeActivityUpdates(
                mApiClient,
                getActivityDetectionPendingIntent()
        ).setResultCallback(this);
    }

    public void onResult(Status status) {
        Log.i(TAG, "onResult");

        if (status.isSuccess()) {
            // Toggle the status of activity updates requested, and save in shared preferences.
            boolean requestingUpdates = !getUpdatesRequestedState();
            setUpdatesRequestedState(requestingUpdates);

            // Update the UI. Requesting activity updates enables the Remove Activity Updates
            // button, and removing activity updates enables the Add Activity Updates button.
            setButtonsEnabledState();

            Toast.makeText(
                    this,
                    getString(requestingUpdates ? R.string.activity_updates_added :
                            R.string.activity_updates_removed),
                    Toast.LENGTH_SHORT
            ).show();
        } else {
            Log.i(TAG, "Error adding or removing activity detection: " + status.getStatusMessage());
        }
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);

        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // requestActivityUpdates() and removeActivityUpdates().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void setButtonsEnabledState() {

        Log.i(TAG, "setButtonsEnabledState");

        if (getUpdatesRequestedState()) {
            mRequestActivityUpdatesButton.setEnabled(false);
            mRemoveActivityUpdatesButton.setEnabled(true);
        } else {
            mRequestActivityUpdatesButton.setEnabled(true);
            mRemoveActivityUpdatesButton.setEnabled(false);
        }
    }

    private SharedPreferences getSharedPreferencesInstance() {
        return getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
    }

    private boolean getUpdatesRequestedState() {
        return getSharedPreferencesInstance()
                .getBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, false);
    }

    private void setUpdatesRequestedState(boolean requestingUpdates) {
        getSharedPreferencesInstance()
                .edit()
                .putBoolean(Constants.ACTIVITY_UPDATES_REQUESTED_KEY, requestingUpdates)
                .apply();
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(Constants.DETECTED_ACTIVITIES, mDetectedActivities);
        super.onSaveInstanceState(savedInstanceState);
    }

    protected void updateDetectedActivitiesList(ArrayList<DetectedActivity> detectedActivities) {
        mAdapter.updateActivities(detectedActivities);
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    public class ActivityDetectionBroadcastReceiver extends BroadcastReceiver {
        //  protected static final String TAG = "activity-detection-response-receiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "onReceive");
            ArrayList<DetectedActivity> updatedActivities =
                    intent.getParcelableArrayListExtra(Constants.ACTIVITY_EXTRA);
            updateDetectedActivitiesList(updatedActivities);

        }
    }
}


