package com.google.firebase.samples.apps.mlkit.java.facedetection;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
import java.util.Arrays;
import java.util.List;

/**
 * Face Contour Demo.
 */
public class FaceContourDetectorProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    private static final String TAG = "FaceContourDetectorProc";
    public boolean leftFaceTurn = false;
    public boolean rightFaceTurn = false;
    public boolean eyesClosed = false;
    public boolean notSmiling = false;



    private final FirebaseVisionFaceDetector detector;

    public FaceContourDetectorProcessor() {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
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
        int bestSize = 0;
        int bestFace = -1;
        for (int i = 0; i < faces.size(); ++i) {
            FirebaseVisionFace face = faces.get(i);
            if (bestSize < face.getBoundingBox().height()*face.getBoundingBox().width()) {
                bestSize = face.getBoundingBox().height()*face.getBoundingBox().width();
                bestFace = i;
            }
        }
        if (bestFace != -1) {
            FirebaseVisionFace face = faces.get(bestFace);
            FaceContourGraphic faceGraphic = new FaceContourGraphic(graphicOverlay, face);
            graphicOverlay.add(faceGraphic);
            int boxWidth = face.getBoundingBox().width();
            FirebaseVisionPoint centerPoint = new FirebaseVisionPoint((float)face.getBoundingBox().centerX(), (float)face.getBoundingBox().centerY(), null);
            FirebaseVisionPoint centerEyePoint = centroid(Arrays.asList(
                    centroid(face.getContour(FirebaseVisionFaceContour.LEFT_EYE).getPoints()),
                    centroid(face.getContour(FirebaseVisionFaceContour.RIGHT_EYE).getPoints())));

           // Log.d(TAG, "CENTER " + centerPoint.toString() + " CENTER EYE " + centerEyePoint.toString());
            //Log.d(TAG, "IS RIGHT: " + (centerPoint.getX() < centerEyePoint.getX()));
            Float diff = centerEyePoint.getX() - centerPoint.getX();
            if (diff > boxWidth/10 && !leftFaceTurn) {
                leftFaceTurn = true;
                Log.d(TAG, "LOOKING LEFT LEFT LEFT LEFT LEFT");
            } else if (diff < -(boxWidth/10) && !rightFaceTurn) {
                rightFaceTurn = true;
                Log.d(TAG, "LOOKING RIGHT RIGHT RIGHT RIGHT RIGHT");
            } else {
                leftFaceTurn = false;
                rightFaceTurn = false;
                Log.d(TAG, "LOOKING NOWHERE");
            }
            if (face.getLeftEyeOpenProbability() < 0.5 && face.getLeftEyeOpenProbability() < 0.5 && !eyesClosed ){
                Log.d(TAG, "eyes closed");
                eyesClosed = true;
            }
                else {
                Log.d(TAG, "eyes open");
                eyesClosed = false;
            }
            if (face.getSmilingProbability() < 0.5 && !notSmiling) {
                Log.d(TAG, "**** Smiling ***");
                notSmiling = true;
            }
        }
        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face detection failed " + e);
    }
}
