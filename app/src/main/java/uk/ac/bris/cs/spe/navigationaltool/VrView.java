package uk.ac.bris.cs.spe.navigationaltool;


import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.vr.sdk.widgets.pano.VrPanoramaView;
import java.io.InputStream;


public class VrView extends AppCompatActivity {

    private VrPanoramaView mVRPanoramaView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vr_view);
        mVRPanoramaView = findViewById(R.id.vrPanoramaView);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent){
        String code = intent.getStringExtra("CODE");
        String path = intent.getStringExtra("PATH");
        String locCode = path + "images/" + code + ".jpg";
        loadPhotoSphere(locCode);
    }


    private void loadPhotoSphere(String name){
        VrPanoramaView.Options options = new VrPanoramaView.Options();
        InputStream inputStream = null;
        AssetManager assetManager = getAssets();

        try{

            inputStream = assetManager.open(name);
            options.inputType = VrPanoramaView.Options.TYPE_MONO;
            mVRPanoramaView.loadImageFromBitmap(BitmapFactory.decodeStream(inputStream),options);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        mVRPanoramaView.pauseRendering();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mVRPanoramaView.resumeRendering();
    }

    @Override
    protected void onDestroy() {
        mVRPanoramaView.shutdown();
        super.onDestroy();
    }

}
