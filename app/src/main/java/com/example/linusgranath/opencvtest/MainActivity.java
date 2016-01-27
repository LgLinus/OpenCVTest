package com.example.linusgranath.opencvtest;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Application used to test OpenCV
 */
public class MainActivity extends OptionsSuperClass implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final String TAG = "test opencv";

    private CameraBridgeViewBase mOpenCvCameraView;
    private int camera = CameraBridgeViewBase.CAMERA_ID_FRONT;
    private boolean gray = false;
    private boolean flipVert = false, flipHorizontal = false;
    private ImageView imageView;
    private Mat currentFrame;

    private int counter = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        mOpenCvCameraView = (CameraBridgeViewBase)findViewById(R.id.HelloOpenCvView);

        // Make the cameraview visible
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_FRONT);
        // Set the listener to the implemented CvCameraViewListener
        mOpenCvCameraView.setCvCameraViewListener(this);

        imageView = (ImageView)findViewById(R.id.ivImage);

        ImageButton btn = (ImageButton)findViewById(R.id.btnChangeCamera);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (camera == CameraBridgeViewBase.CAMERA_ID_BACK) {
                    camera = CameraBridgeViewBase.CAMERA_ID_FRONT;
                } else {
                    Log.d(TAG, "switched to back");
                    camera = CameraBridgeViewBase.CAMERA_ID_BACK;
                }

                mOpenCvCameraView.disableView();
                mOpenCvCameraView.setCameraIndex(camera);
                mOpenCvCameraView.enableView();
            }
        });


        ImageButton btnChangeColor = (ImageButton)findViewById(R.id.btnChangeColor);
        btnChangeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gray = !gray;
            }
        });

        Button btnTakePicture = (Button)findViewById(R.id.btnTakePicture);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveImage(currentFrame);
            }
        });


        Button btnFlipVert = (Button)findViewById(R.id.btnFlipVert);
        btnFlipVert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipVert = !flipVert;
            }
        });


        Button btnFlipHorizontal = (Button)findViewById(R.id.btnFlipHorizontal);
        btnFlipHorizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flipHorizontal = !flipHorizontal;
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        initOpenCV();
    }

    /**
     * Init functions below
     */
    private void initOpenCV(){
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0,this,mLoaderCallback);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status){
            switch(status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                }
            }
        }
    };
    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }


    public void saveImage(Mat matrix){

        matrix = currentFrame;
        // Create bitmap from image
        Bitmap resultBitmap = Bitmap.createBitmap(matrix.cols(),matrix.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(matrix, resultBitmap);
/*
        BitmapDrawable drawableBitmap = new BitmapDrawable(getResources(),resultBitmap);
        imageView.setBackgroundResource(0);
        imageView.setBackground(drawableBitmap);
        */
       // imageView.setBackgroundResource(R.drawable.homer);
        Log.d(TAG,"set image");

        Date date = new Date();

        SimpleDateFormat ft =
                new SimpleDateFormat ("yyyy/mm/dd_hh/mm/ss");

      //  String filename = ft.format(date);
        String filename = "apple"+counter;
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir",Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,filename+".png");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), resultBitmap, filename+".png", "xaxa");
            counter++;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }

    }

    /**
     * Called when the frame for the camera changes, here appropiate transformations should occur
     * @param inputFrame
     * @return
     */
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat res,res2;
        res2 = new Mat();

        res = checkColor(inputFrame);

        flipImage(res);

        currentFrame = res;
        return res;
    }

    /**
     * Function that returns either a colored or gray scaled image
     * @param inputFrame
     * @return
     */
    private Mat checkColor(CameraBridgeViewBase.CvCameraViewFrame inputFrame){
        if(gray)
            return inputFrame.gray();
        else
            return inputFrame.rgba();

    }

    /**
     * Function to flip a matrix, either horizontally, vertically or both
     * @param res
     */
    private void flipImage(Mat res){

        if(flipHorizontal&&flipVert)
            Core.flip(res,res,-1);
        else if(flipVert)
            Core.flip(res,res,0);
        else if(flipHorizontal)
            Core.flip(res,res,1);

    }
}

