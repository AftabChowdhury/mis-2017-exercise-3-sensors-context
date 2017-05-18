package com.google.android.gms.location.sample.activityrecognition;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Random;

public class AccelerometerFft extends AppCompatActivity implements SensorEventListener {

    private static final Random RANDOM = new Random();
    private LineGraphSeries<DataPoint> xseries,yseries,zseries, magseries, fftseries;
    private int lastX = 0;
    private int lastY = 0;
    private int lastZ = 0;
    private int lastMagnitude = 0;
    private int lastFFtx = 0;
    private int lastFFtCount = 0;
    private Sensor mySensor;
    private SensorManager SM;
    double xAxies=0;
    double yAxies=0;
    double zAxies=0;
    double omegaMagnitude = 0;
    double[] fftx = new double[8192];
    double[] ffty = new double[8192];
    double[] fftResult = new double[8192];
    GraphView graph,graphFft;

    //http://www.ssaurel.com/blog/create-a-real-time-line-graph-in-android-with-graphview/
    //https://androidstream.wordpress.com/2013/01/16/android-collecting-and-plotting-accelerometer-data/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer_fft);
        // we get graph view instance
        graph = (GraphView) findViewById(R.id.graph);
        graphFft = (GraphView) findViewById(R.id.graphFft);
        graphFft.setVisibility(View.INVISIBLE);
        // data
        xseries = new LineGraphSeries<DataPoint>();
        yseries = new LineGraphSeries<DataPoint>();
        zseries = new LineGraphSeries<DataPoint>();
        magseries = new LineGraphSeries<DataPoint>();
        fftseries = new LineGraphSeries<DataPoint>();
        graph.addSeries(xseries);
        graph.addSeries(yseries);
        graph.addSeries(zseries);
        graph.addSeries(magseries);
        graphFft.addSeries(fftseries);
        graph.setTitle("Accelerometer");
        graphFft.setTitle("FFT");

        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(10);
        viewport.setScrollable(true);

        Viewport viewportFft = graphFft.getViewport();
        viewportFft.setYAxisBoundsManual(true);
        viewportFft.setMinY(0);
        viewportFft.setMaxY(10);
        viewportFft.setScrollable(true);
        // Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // Register sensor Listener
        SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not in use
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        xAxies = event.values[0];
        yAxies = event.values[1];
        zAxies = event.values[2];
        // https://developer.android.com/reference/android/hardware/SensorEvent.html
        omegaMagnitude = Math.sqrt(xAxies*xAxies + yAxies*yAxies + zAxies*zAxies);
        fftx[lastFFtx] = omegaMagnitude;
       /* for (int i = 0; i < lastFFtx; i++) {
            ffty[i] = 0;
        }*/
        lastFFtx++;
        /*if(lastFFtx>1024 &&  lastFFtx<8192){
            fftResult = fftCalculator(fftx, ffty);
        }*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        // we're going to simulate real time with thread that append data to the graph
        new Thread(new Runnable() {

            @Override
            public void run() {
                // we add 10000 new entries
                for (int i = 0; i < 10000; i++) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            addEntry();
                        }
                    });

                    // sleep to slow down the add of entries
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        // manage error ...
                    }
                }
            }
        }).start();
    }

    // add random data to graph
    private void addEntry() {
        // here, we choose to display max 10 points on the viewport and we scroll to end
        xseries.appendData(new DataPoint(lastX++, xAxies), true, 100);
        xseries.setColor(Color.RED);
        yseries.appendData(new DataPoint(lastY++, yAxies), true, 100);
        yseries.setColor(Color.GREEN);
        zseries.appendData(new DataPoint(lastZ++, zAxies), true, 100);
        zseries.setColor(Color.BLUE)  ;
        magseries.appendData(new DataPoint(lastMagnitude++, omegaMagnitude), true, 100);
        magseries.setColor(Color.BLACK)  ;
        if(lastFFtCount<8192){
            fftseries.appendData(new DataPoint(lastFFtCount++, fftResult[lastFFtCount++]), true, 100);
            fftseries.setColor(Color.MAGENTA);
        }
    }
    public void fftTransform(View v) {
        graphFft.setVisibility(View.VISIBLE);
        //graph.setVisibility(View.INVISIBLE);
        for (int i = 0; i < 8192; i++) {
            ffty[i] = 0;
        }
        fftResult = fftCalculator(fftx, ffty);
    }
    // http://stackoverflow.com/questions/9272232/fft-library-in-android-sdk
    public double[] fftCalculator(double[] re, double[] im) {
        if (re.length != im.length) return null;
        FFT fft = new FFT(re.length);
        fft.fft(re, im);
        double[] fftMag = new double[re.length];
        for (int i = 0; i < 8192; i++) {
            //fftMag[i] = Math.pow(re[i], 2) + Math.pow(im[i], 2);
            fftMag[i] = Math.sqrt(re[i]*re[i] + im[i]*im[i]);
        }
        return fftMag;
    }
}
