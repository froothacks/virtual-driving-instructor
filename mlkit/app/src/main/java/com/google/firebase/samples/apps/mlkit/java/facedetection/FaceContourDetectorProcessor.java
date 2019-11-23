package com.google.firebase.samples.apps.mlkit.java.facedetection;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.graphics.Point;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.samples.apps.mlkit.common.CameraImageGraphic;
import com.google.firebase.samples.apps.mlkit.common.FrameMetadata;
import com.google.firebase.samples.apps.mlkit.common.GraphicOverlay;
import com.google.firebase.samples.apps.mlkit.java.VisionProcessorBase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Face Contour Demo.
 */
public class FaceContourDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceContourDetectorProc";

    private final FirebaseVisionFaceDetector detector;

    public FaceContourDetectorProcessor() {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    public FirebaseVisionPoint centroid(List<FirebaseVisionPoint> points)  {
        float centroidX = 0, centroidY = 0;

        for (FirebaseVisionPoint point : points) {
            centroidX += point.getX();
            centroidY += point.getY();
        }

        return new FirebaseVisionPoint(centroidX / points.size(), centroidY / points.size(), null);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face);
            graphicOverlay.add(faceGraphic);
            int boxWidth = face.getBoundingBox().width();
            FirebaseVisionPoint centerPoint = new FirebaseVisionPoint((float)face.getBoundingBox().centerX(), (float)face.getBoundingBox().centerY(), null);
            FirebaseVisionPoint centerEyePoint = centroid(Arrays.asList(
                    centroid(face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints()),
                    centroid(face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints())));

            Log.d(TAG, "CENTER " + centerPoint.toString() + " CENTER EYE " + centerEyePoint.toString());
            Log.d(TAG, "IS RIGHT: " + (centerPoint.getX() < centerEyePoint.getX()));
            Float diff = centerEyePoint.getX() - centerPoint.getX();
            if (diff > boxWidth/10) {
                Log.d(TAG, "LOOKING LEFT LEFT LEFT LEFT LEFT");
            } else if (diff < -(boxWidth/10)) {
                Log.d(TAG, "LOOKING RIGHT RIGHT RIGHT RIGHT RIGHT");
            } else {
                Log.d(TAG, "LOOKING NOWHERE");
            }
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
