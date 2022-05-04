package de.hs.stralsund.dartstracker.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import de.hs.stralsund.dartstracker.dartgame.Tuple;
import de.hs.stralsund.dartstracker.imagerecognition.ColorMask;
import de.hs.stralsund.dartstracker.imagerecognition.ImageCalibration;
import de.hs.stralsund.dartstracker.imagerecognition.ImageUtils;

/**
 * The type Camera calibration activity.
 */
public class CameraCalibrationActivity extends AppCompatActivity {

    private final Executor analyzerExecutor = Executors.newSingleThreadExecutor();
    // camera analysis
    private Camera camera;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    // surface components of this activity
    private ImageView camPreview;
    private ProgressBar calibrationProgressBar;
    private SeekBar zoomSeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_calibration);

        // Surface
        camPreview = findViewById(R.id.imageView);
        calibrationProgressBar = findViewById(R.id.progressBar);
        zoomSeekBar = findViewById(R.id.zoomSeekBar);
        zoomSeekBar.setOnSeekBarChangeListener(getSeekBarChangeListener());
    }

    @Override
    protected void onResume() {
        ImageCalibration.getInstance().resetValidPoints();
        super.onResume();
        startCamera();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void startCamera() {

        ImageCalibration imageCalibration = ImageCalibration.getInstance();
        ImageAnalysis imageAnalysis = imageCalibration.getImageAnalysis();
        imageAnalysis.setAnalyzer(analyzerExecutor, getCamCalibrationAnalyzer());

        // Used to bind the lifecycle of cameras to the lifecycle owner
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                //Binden der Aktivitäten an die Kamera => Funktionalitäten
                camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, imageCalibration.getCameraSelector(), imageAnalysis);
                camera.getCameraControl().setLinearZoom(imageCalibration.getZoomLevel());
            }
            catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private ImageAnalysis.Analyzer getCamCalibrationAnalyzer() {

        return image -> {
            try {
                ImageCalibration imageCalibration = ImageCalibration.getInstance();
                final int rotationDegrees = image.getImageInfo().getRotationDegrees();

                // Create cv::mat(RGB888) from image(NV21)
                Mat mat = ImageUtils.ImageToRgbaMat(image);
                // Fix image rotation if given image is taken with a rotation
                mat = ImageUtils.matRotate(mat, rotationDegrees);

                /* Do some image processing */
                List<Point> markers = ImageUtils.findMarkers(mat, ColorMask.YellowNew);
                calculateCalibrationProgress(markers, rotationDegrees, imageCalibration);

                // draw found markers on original image
                mat = ImageUtils.drawYellowMarkers(markers, mat);

                // Convert cv::mat to bitmap for drawing
                Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, bitmap);
                // Display the result onto ImageView - not really fast but it's possible
                runOnUiThread(() -> camPreview.setImageBitmap(bitmap));

                // if actual calculation is good for some seconds. save calculation and go on
                Tuple<Long, MatOfPoint2f> lastValidPoints = imageCalibration.getLastValidPoints();
                if (lastValidPoints != null && System.currentTimeMillis() - lastValidPoints.a > 3000) {
                    saveCalibration(imageCalibration);
                }

            }
            catch (Exception e) {
                Log.e("analyze", "broken", e);
            }
            finally {
                // close image or we won't get next frame
                image.close();
            }
        };
    }

    /**
     *  Einfaches tracking der gefundenen Punkte und Anzeige über die ProgressBar.
     *  3 Sekunden 4 Punkte halten reicht.
     * @param markers
     * @param rotationDegrees
     * @param imageCalibration
     */
    private void calculateCalibrationProgress(List<Point> markers, int rotationDegrees, ImageCalibration imageCalibration) {
        Tuple<Long, MatOfPoint2f> lastValidPoints = imageCalibration.getLastValidPoints();
        int numMarkers = markers == null ? 0 : markers.size();
        switch (numMarkers) {
            case 1:
                // better than nothing
                calibrationProgressBar.setProgress(1);
                break;
            case 2:
                // better
                calibrationProgressBar.setProgress(2);
                break;
            case 3:
                // good - but still to less points
                calibrationProgressBar.setProgress(3);
                break;
            case 4:
                // only this is good enough and has to keep up to some seconds
                if (lastValidPoints == null) {
                    imageCalibration.calculateNewPoints(markers, rotationDegrees);
                    imageCalibration.setInitialSourcePoints(markers, rotationDegrees);
                    calibrationProgressBar.setProgress(4);
                }
                else if (System.currentTimeMillis() - lastValidPoints.a > 1000) {
                    // good for some seconds. seems to be stable now
                    calibrationProgressBar.setProgress(5);
                }
                imageCalibration.calculateNewPoints(markers, rotationDegrees);
                return;
            case 5:
                // good - but this are to much points
                calibrationProgressBar.setProgress(3);
                break;
            case 6:
                // get's worser
                calibrationProgressBar.setProgress(2);
                break;
            case 7:
                // to baaad
                calibrationProgressBar.setProgress(1);
                break;
            default:
                // all or nothing
                calibrationProgressBar.setProgress(0);
        }
        // keep old points up to 1 seconds
        if (lastValidPoints != null && System.currentTimeMillis() - lastValidPoints.a > 1000) {
            imageCalibration.resetValidPoints();
        }
    }

    /**
     * Save calibration.
     * @param imageCalibration
     */
    public void saveCalibration(ImageCalibration imageCalibration) {
        imageCalibration.setZoomLevel(zoomSeekBar.getProgress() / (float) zoomSeekBar.getMax());
        Intent intent = new Intent(this, DartGameActivity.class);
        startActivity(intent);
//        finish();
    }

    /**
     * Save calibration.
     * @param view
     */
    public void saveCalibration(View view) {
        saveCalibration(ImageCalibration.getInstance());
    }

    private SeekBar.OnSeekBarChangeListener getSeekBarChangeListener() {
        return new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                camera.getCameraControl().setLinearZoom((progress / (float) zoomSeekBar.getMax()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
    }
}