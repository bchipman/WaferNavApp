package com.nielsenninjas.wafernav.barcodereader;

import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;

/**
 Created by brian on 3/19/2017.
 */

public class BarcodeProcessor implements Detector.Processor<Barcode> {

    private static final String TAG = "BarcodeProcessor";
    private BarcodeCaptureActivity barcodeCaptureActivity;

    public BarcodeProcessor(BarcodeCaptureActivity barcodeCaptureActivity) {
        this.barcodeCaptureActivity = barcodeCaptureActivity;
    }

    @Override
    public void release() {

    }
    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        SparseArray<Barcode> detectedItems = detections.getDetectedItems();

        for (int i=0; i<detectedItems.size(); i++) {
            final Barcode barcode = detectedItems.valueAt(i);
            if (barcode != null) {
                Log.d(TAG, "Found barcode " + barcode.rawValue);
                barcodeCaptureActivity.myReturnToPreviousActivity(barcode);
            }
        }
    }
}
