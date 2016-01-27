package com.example.linusgranath.opencvtest;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public static final String TAG = "test opencv";

    private CameraBridgeViewBase mOpenCvCameraView;
    private int camera = CameraBridgeViewBase.CAMERA_ID_FRONT;
    private boolean gray = false;
    private boolean flipVert = false, flipHorizontal = false;
    private ImageView imageView;
    private Mat currentFrame;
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

        Button btn = (Button)findViewById(R.id.btnChangeCamera);
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

        Button btnChangeColor = (Button)findViewById(R.id.btnChangeColor);
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

        BitmapDrawable drawableBitmap = new BitmapDrawable(getResources(),resultBitmap);
        imageView.setBackgroundResource(0);
        imageView.setBackground(drawableBitmap);
       // imageView.setBackgroundResource(R.drawable.homer);
        Log.d(TAG,"set image");

        Date date = new Date();

        SimpleDateFormat ft =
                new SimpleDateFormat ("yyyy/mm/dd/hh/mm/ss");

        String filename = ft.format(date);

        String path = Environment.getExternalStorageDirectory().toString();
        OutputStream fOut = null;
        File file = new File(path, filename+".jpg"); // the File to save to
        file.mkdirs();
        try {
            fOut = new FileOutputStream(file);
            resultBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush();
            fOut.close(); // do not forget to close the stream

            MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
        }
        catch(FileNotFoundException e){
            Log.d(TAG,"couldn\'t find file");
        }
        catch(Exception e){
            Log.d(TAG,"error save file");
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

