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
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

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
                    //Mat leafMat =
                            rtnLeafMat(mCurrentPhotoPath);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
        //Debug: To return Mat object
        private void rtnLeafMat(String mCurrentPhotoPath) {
            Mat originalMat = Imgcodecs.imread(mCurrentPhotoPath);
            Log.i(TAG, "Current Photo Path:" + mCurrentPhotoPath);
            if(isMatEmpty(originalMat)){
                Log.e(TAG, "Empty originalMat");
            }else{
                //Log.i(TAG, "Original Mat:" + originalMat.total());
                Bitmap testBmp = Bitmap.createBitmap(originalMat.cols(), originalMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(originalMat, testBmp);
                imgView_original.setImageBitmap(testBmp);
            }
        }
    };



    private void setOpenCamBtn_onClick() {
        btn_openCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //open Cam intent:
                openCamIntent();
            }
        });
    }

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
    public static final String phoneDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "MainActivity";
}
