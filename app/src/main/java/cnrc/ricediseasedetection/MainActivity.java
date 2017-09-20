package cnrc.ricediseasedetection;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Button btn_openCam;
    TextView txt_topYpercent;
    TextView txt_topBpercent;
    TextView txt_midYpercent;
    TextView txt_midBpercent;
    TextView txt_botYpercent;
    TextView txt_botBpercent;
    ImageView imgView_original;
    String fileName = null;
    String mCurrentPhotoPath = null;
    private Mat originalMat;
    private int leafMatPixCount;
    private long topLeaf_yPixCount, topLeaf_bPixCount;
    private long midLeaf_yPixCount, midLeaf_bPixCount;
    private long botLeaf_yPixCount, botLeaf_bPixCount;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindLayouts();
        setOpenCamBtn_onClick();
    }

    //User-Interaction Activities:
    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "On Resume");
        Log.i(TAG, "Photo current Path: " + mCurrentPhotoPath);
        if(mCurrentPhotoPath == null){
            //doo nothing.
        }else{
            Log.i(TAG, "mCurrentPath is : " + mCurrentPhotoPath + " Proceed to calling openCV callback.");
                if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback))
                {
                    Log.e(TAG, "Cannot connect to OpenCV Manager");
                }
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "Here at BaseLoader Call back");
                    //perform calculations here.
                    Mat leafMat = rtnLeafMat(debugFilePath);
                    //test:
                    if(leafMat != null){
                        //Perform operations on this fucking leafMat:
                        //Count top:
                        performTopLeafActivities(leafMat);
                    }else{
                        Log.e(TAG,"Leaf mat is Null (empty).");
                    }
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }

        private void performTopLeafActivities(Mat leafMat) {
            //Cut the leaf into three parts: here is top:
            Mat topLeaf = cropTopLeaf(leafMat);
            Mat midLeaf = cropMidLeaf(leafMat);
            Mat botLeaf = cropBotLeaf(leafMat);
            //get pixel color counts; set it to xml widgets.
            topLeaf_yPixCount = countYellowPixel(topLeaf);
            topLeaf_bPixCount = countBrownPixel(topLeaf);
            midLeaf_yPixCount = countYellowPixel(midLeaf);
            midLeaf_bPixCount = countBrownPixel(midLeaf);
            botLeaf_yPixCount = countYellowPixel(botLeaf);
            botLeaf_bPixCount = countBrownPixel(botLeaf);

            //test:
            Log.i(TAG, "TopLeaf_yPixCount: " + topLeaf_yPixCount);
            Log.i(TAG, "TopLeaf_bPixCount: " + topLeaf_bPixCount);
            Log.i(TAG, "Middle Yellow Pixel COunt: " + midLeaf_yPixCount);
            Log.i(TAG, "Middle Brown Pixel COunt: " + midLeaf_bPixCount);
            Log.i(TAG, "Bot Yellow Pixel Count: " + botLeaf_yPixCount);
            Log.i(TAG, "Bot Brown Pixel Count: " + botLeaf_bPixCount);
        }

        private Mat cropBotLeaf(Mat leafMat) {
            Mat leafMatCopy = leafMat.clone();
            Mat bottomMask = new Mat (leafMatCopy, new Range( (leafMatCopy.rows() /2)+(leafMatCopy.rows()/4) -1, leafMatCopy.rows()-1), new Range( 0, leafMatCopy.cols()-1) );
            Mat bottomLeafMat = new Mat(originalMat, new Range(((originalMat.rows() /2)+(leafMatCopy.rows()/4)-1), originalMat.rows()-1), new Range( 0, originalMat.cols()-1));
            bottomLeafMat.copyTo(bottomLeafMat, bottomMask);
        return bottomLeafMat;
        }

        private Mat cropMidLeaf(Mat leafMat) {
            Mat leafMatCopy = new Mat();
            leafMatCopy = leafMat.clone();

            Mat middleMask = new Mat(leafMatCopy, new Range( ( (leafMatCopy.rows() / 2) - (leafMatCopy.rows() / 4) ) - 1 , ( (leafMatCopy.rows() / 2)+ leafMatCopy.rows() /4 )-1), new Range(0 , (leafMatCopy.cols()-1)) );
            Mat middleLeafMat = new Mat(originalMat, new Range( ((originalMat.rows()/2)-(originalMat.rows()/4)-1),(((originalMat.rows()/2)+originalMat.rows()/4)-1) ), new Range(0,(leafMatCopy.cols()-1) ) );
            middleLeafMat.copyTo(middleLeafMat, middleMask);
        return middleLeafMat;
        }

        private Mat cropTopLeaf(Mat leafMat) {
            //Create a copy of leafMat:
            Mat leafMatCopy = leafMat.clone();
            Mat topLeafMask = new Mat(leafMatCopy, new Range(0 , ((leafMatCopy.rows()/2)-(leafMatCopy.rows()/4)) -1 ) , new Range( 0 , (leafMatCopy.cols()-1) ) );
            //create an originalImagew ith the same size as the bottom and top masks:
            Mat topLeafMat = new Mat( originalMat, new Range(0 , ((originalMat.rows()/2) - (originalMat.rows()/4) -1)), new Range( 0,(originalMat.cols()-1) ) );
            topLeafMat.copyTo(topLeafMat, topLeafMask);
            return topLeafMat;
        }

        private long countBrownPixel(Mat thisMat) {
            Mat thisMatCopy = thisMat.clone();

            if(!isMatEmpty(thisMatCopy)){
                Mat thisMatHsv = thisMatCopy;
                Imgproc.cvtColor(thisMatCopy ,thisMatHsv , Imgproc.COLOR_BGR2HSV);

                Mat brownMat = new Mat();
                Core.inRange(thisMatHsv, new Scalar(8,100,20), new Scalar(17,255,200), brownMat);
                if(!isMatEmpty(brownMat)){
                    return Core.countNonZero(brownMat);
                }else{
                    Log.i(TAG, "BrownMat Empty");
                }
            }else{
                Log.e(TAG, "Empty thisMatCopy");
            }
            return 0;
        }

        private long countYellowPixel(Mat thisMat) {
            Mat thisMatCopy = thisMat.clone();

            if(!isMatEmpty(thisMatCopy)){
                Mat thisMatHsv = thisMatCopy;
                Imgproc.cvtColor(thisMatCopy ,thisMatHsv , Imgproc.COLOR_BGR2HSV);
                Mat yellowMat = new Mat();
                Core.inRange(thisMatCopy, new Scalar(18,100,100), new Scalar(30,255,255), yellowMat);
                if(!isMatEmpty(yellowMat)){
                    return Core.countNonZero(yellowMat);
                }else{
                    Log.i(TAG, "YellowMat Empty");
                }
            }else{
                Log.e(TAG, "Empty thisMatCopy");
            }
        return 0;
        }


        //Debug: To return Mat object
        private Mat rtnLeafMat(String thisFilePath) {
            originalMat = Imgcodecs.imread(thisFilePath);
            Mat grayScaleMat = Imgcodecs.imread(thisFilePath, Imgcodecs.IMREAD_GRAYSCALE);
            Log.i(TAG, "Current Photo Path:" + thisFilePath);
            if(isMatEmpty(grayScaleMat)){
                Log.e(TAG, "Empty originalMat");
            }else{
                //Log.i(TAG, "Original Mat:" + originalMat.total());
                return performWatershed(grayScaleMat, originalMat);
            }
        return null;
        }

        private Mat performWatershed(Mat grayScaleMat, Mat originalMat) {
            if(isMatEmpty(grayScaleMat)){
                Log.e(TAG, "Empty grayScaleMat ");
            }else{
                Mat binaryMat = new Mat();
                Imgproc.threshold(grayScaleMat, binaryMat, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
                if(isMatEmpty(binaryMat)){
                    Log.e(TAG, "Empty BinaryMat");
                }else{
                    //Real Watershedding:
                    Mat foregroundMat = new Mat();
                    Imgproc.erode(binaryMat, foregroundMat, new Mat(),new Point(-1,-1),2);
                    //Test Foreground Mat:
                    leafMatPixCount = Core.countNonZero(foregroundMat);

                    Mat backgroundMat = new Mat();
                    Imgproc.dilate(binaryMat, backgroundMat, new Mat(), new Point(-1,-1), 3);
                    Imgproc.threshold(backgroundMat, backgroundMat, 1,128, Imgproc.THRESH_BINARY_INV);

                    //Create Marker:
                    Mat markerMat = new Mat(binaryMat.size(), CvType.CV_8U, new Scalar(0));
                    Imgproc.connectedComponents(binaryMat, markerMat);
                    Core.add(foregroundMat, backgroundMat, markerMat);

                    //Segment Process:
                    Mat marker = new Mat();
                    //Set Marker:
                    markerMat.convertTo(marker, CvType.CV_32S);
                    //Process Marker:
                    Imgproc.watershed(originalMat, marker);
                    Mat result = marker;
                    result.convertTo(result, CvType.CV_8U);
                    return result;
                }
            }
        return null;
        }
    };

    //CAMERA ON ACTIVITY RESULT:
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
        }else{
            Log.i(TAG, "Cam Image catprue failed.");
            mCurrentPhotoPath = null;
        }
    }

/*
    HELPER METHODS:
*/
    private void setOpenCamBtn_onClick() {
    btn_openCam.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //open Cam intent:
            openCamIntent();
        }
    });
}

    private void openCamIntent() {
        Intent photoCamIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensure that there's a camera activity to handle the intent
        File phoneDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        fileName = getFileName();

        File imgFile = new File(phoneDirectory, fileName);
        Uri imgUri = Uri.fromFile(imgFile);
        photoCamIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, imgUri);
        startActivityForResult(photoCamIntent, REQUEST_IMAGE_CAPTURE);

        mCurrentPhotoPath = phoneDirectory.toString() + "/" + fileName;
    }

    private String getFileName(){
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return "RicePhoto_" + timeStamp + ".jpg";
    }

    private void bindLayouts(){
        imgView_original = (ImageView) findViewById(R.id.imgView_original);
        txt_topYpercent = (TextView) findViewById(R.id.txt_topYpercent);
        txt_topBpercent = (TextView) findViewById(R.id.txt_topBpercent);
        txt_midYpercent = (TextView) findViewById(R.id.txt_midYpercent);
        txt_midBpercent = (TextView) findViewById(R.id.txt_midBpercent);
        txt_botYpercent = (TextView) findViewById(R.id.txt_botYpercent);
        txt_botBpercent = (TextView) findViewById(R.id.txt_botBpercent);
        btn_openCam = (Button) findViewById(R.id.btn_openCam);
    }

    private boolean isMatEmpty(Mat thisMat) {
        return thisMat.empty();
    }
/*
STATIC CALLS FOR LIBRARIES AND STUFF
*/
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //Debug:
    //public static final String phoneDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
    public static final String debugFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/sheathblight1.jpg";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "MainActivity";
}
