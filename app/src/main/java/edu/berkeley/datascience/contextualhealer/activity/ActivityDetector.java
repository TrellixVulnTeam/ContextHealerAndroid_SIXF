package edu.berkeley.datascience.contextualhealer.activity;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;

public class ActivityDetector {
    static {
        System.loadLibrary("tensorflow_context");
    }

    private native int init(AssetManager assetManager, String model);

    /**
     * draw pixels
     */

    public native int getActivityClass(int featurescount, double[] inputs);
    public native double getTestPrediction(double[] inputs);

    public boolean setup(Context context) {
        Log.v("Test", "In the setup method");
        AssetManager assetManager = context.getAssets();


        // model from activityModel
        int ret = init(assetManager, "file:///android_asset/activityModelMLP2.pb");
        //int ret = init(assetManager, "file:///android_asset/activityModelLR.pb");

        return ret >= 0;
    }
}
