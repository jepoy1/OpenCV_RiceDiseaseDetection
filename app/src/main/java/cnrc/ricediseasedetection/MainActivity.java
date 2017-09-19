package cnrc.ricediseasedetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    Button btn_openCam;
    TextView txt_topYpercent;
    TextView txt_topBpercent;
    TextView txt_midYpercent;
    TextView txt_midBpercent;
    TextView txt_botYpercent;
    TextView txt_botBpercent;
    ImageView imgView_original;
    String imageFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindLayouts();
    }

    //User-Interaction Activities:


    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "On Rsume");
        setOpenCamBtn_onClick();
    }

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
            //Test by setting the data into the bitmap:
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imgView_original.setImageBitmap(imageBitmap);
        }else{
            Log.i(TAG, "Cam Image catprue failed.");
        }

    }

    /*
            HELPER METHODS:
        */
    private void openCamIntent() {
        Intent photoCamIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(photoCamIntent, REQUEST_IMAGE_CAPTURE);
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
/*
STATIC CALLS FOR LIBRARIES AND STUFF
*/
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    //Debug:
    private static final String phonePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String TAG = "MainActivity";
    static{
        if(!OpenCVLoader.initDebug()){
            Log.e(TAG, "Init Debug Failed to load");
        }else{
            Log.i(TAG, "OpenCV on Debug mode.");
        }
    }

}
