package de.hs.stralsund.dartstracker.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.google.common.collect.EvictingQueue;
import com.google.common.util.concurrent.ListenableFuture;
import com.jakewharton.rxbinding4.view.RxView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.hs.stralsund.dartstracker.activities.dialogs.WinnerDialog;
import de.hs.stralsund.dartstracker.dartgame.GameInformation;
import de.hs.stralsund.dartstracker.dartgame.Player;
import de.hs.stralsund.dartstracker.dartgame.Tuple;
import de.hs.stralsund.dartstracker.imagerecognition.ColorMask;
import de.hs.stralsund.dartstracker.imagerecognition.ImageCalibration;
import de.hs.stralsund.dartstracker.imagerecognition.ImageUtils;
import de.hs.stralsund.dartstracker.imagerecognition.PointsFinder;
import de.hs.stralsund.dartstracker.utils.CalculatingLogic;


public class DartGameActivity extends AppCompatActivity {

    public final static int BOX_ID_INCREMENT = 100;
    public final static int POINT_ID_INCREMENT = 200;
    public final static int NAME_ID_INCREMENT = 300;
    public final static int LEG_ID_INCREMENT = 400;
    public final static int SET_ID_INCREMENT = 500;

    // camera analysis
    private final Executor analyzerExecutor = Executors.newSingleThreadExecutor();
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private TextView calcDartValueBox;
    private final PointsFinder pointsFinder = new PointsFinder(new Point(851,851), 642); // this matches our dartboard_abmessungen.png
    private final Map<Integer, TextView> listOfNumberFields = new HashMap<>();

    GameInformation gameInformation;
    Map<String, Integer> listOfArrowResults = new HashMap<>();
    LinearLayout playerStats;
    Toolbar toolBar;
    private ImageView previewImage;
    TextView arrowOne;
    TextView arrowTwo;
    TextView arrowThree;
    int arrowsCounter = 0;
    private int activeMultiplier;
    private CalculatingLogic calculateLogic;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dart_game);
        this.toolBar = findViewById(R.id.toolbarGame);
        previewImage = findViewById(R.id.previewImage);
        calcDartValueBox = findViewById(R.id.calcDartValueBox);
        this.arrowOne = findViewById(R.id.arrowOne);
        this.arrowTwo = findViewById(R.id.arrowTwo);
        this.arrowThree = findViewById(R.id.arrowThree);
        this.playerStats = findViewById(R.id.gameDartPlayerStats);
        this.gameInformation = StartMenueActivity.dataManager.getGameinformationFromFile();

        boolean playerIsTurnIsSet = false;
        for(Player player : this.gameInformation.getPlayerList()){
            if(player.getIsTurn()){
                playerIsTurnIsSet = true;
                break;
            }
        }
        if(!playerIsTurnIsSet){
            this.gameInformation.getPlayer(0).setIsTurn(true);
            StartMenueActivity.dataManager.writeData(gameInformation);
        }

        this.calculateLogic = new CalculatingLogic(this);

        buildListOfNumberFields();
        this.activeMultiplier = 1;
        updateListOfNumbers();

        initializeClickEvents();

        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout.LayoutParams cardViewParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        /*#TODO Player UIs erstellen*/

        preparePlayerContentIds();
        calculateLogic.resetPlayerScores(gameInformation);

        RxView.clicks(findViewById(R.id.quitGame))
            .subscribe(restart -> {
                Intent intent = getBaseContext().getPackageManager().
                        getLaunchIntentForPackage(getBaseContext().getPackageName());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                startActivity(intent);
            });

        startCamera();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void preparePlayerContentIds() {

        for (Player player : gameInformation.getPlayerList()) {

            View playerStatsLayout = getLayoutInflater().inflate(R.layout.player_ui,null);

            TextView playerPoints = playerStatsLayout.findViewById(R.id.playerPoints);
            playerPoints.setId(POINT_ID_INCREMENT + player.getPlayerID());
            playerPoints.setText(String.valueOf(player.getPointsInLeg()));

            TextView playerName = playerStatsLayout.findViewById(R.id.playerName);
            playerName.setId(NAME_ID_INCREMENT + player.getPlayerID());
            playerName.setText(player.getPlayerName());

            playerStatsLayout.findViewById(R.id.playerBox).setId(BOX_ID_INCREMENT + player.getPlayerID());
            playerStatsLayout.findViewById(R.id.playerLeg).setId(LEG_ID_INCREMENT + player.getPlayerID());
            playerStatsLayout.findViewById(R.id.playerSet).setId(SET_ID_INCREMENT + player.getPlayerID());

            this.playerStats.addView(playerStatsLayout);
        }

        this.findViewById(DartGameActivity.BOX_ID_INCREMENT).setBackgroundResource(R.drawable.rounded_highlighted_rectangle);
    }

    public void buildListOfNumberFields() {
        this.listOfNumberFields.put(1, (TextView) findViewById(R.id.numberOne));
        this.listOfNumberFields.put(2, (TextView) findViewById(R.id.numberTwo));
        this.listOfNumberFields.put(3, (TextView) findViewById(R.id.numberThree));
        this.listOfNumberFields.put(4, (TextView) findViewById(R.id.numberFour));
        this.listOfNumberFields.put(5, (TextView) findViewById(R.id.numberFive));
        this.listOfNumberFields.put(6, (TextView) findViewById(R.id.numberSix));
        this.listOfNumberFields.put(7, (TextView) findViewById(R.id.numberSeven));
        this.listOfNumberFields.put(8, (TextView) findViewById(R.id.numberEight));
        this.listOfNumberFields.put(9, (TextView) findViewById(R.id.numberNine));
        this.listOfNumberFields.put(10, (TextView) findViewById(R.id.numberTen));
        this.listOfNumberFields.put(11, (TextView) findViewById(R.id.numberEleven));
        this.listOfNumberFields.put(12, (TextView) findViewById(R.id.numberTwelve));
        this.listOfNumberFields.put(13, (TextView) findViewById(R.id.numberThirtheen));
        this.listOfNumberFields.put(14, (TextView) findViewById(R.id.numberFortheen));
        this.listOfNumberFields.put(15, (TextView) findViewById(R.id.numberFiftheen));
        this.listOfNumberFields.put(16, (TextView) findViewById(R.id.numberSixteen));
        this.listOfNumberFields.put(17, (TextView) findViewById(R.id.numberSeventheen));
        this.listOfNumberFields.put(18, (TextView) findViewById(R.id.numberEighteen));
        this.listOfNumberFields.put(19, (TextView) findViewById(R.id.numberNinetheen));
        this.listOfNumberFields.put(20, (TextView) findViewById(R.id.numberTwenty));
    }

    private void setDoubleMultiplier() {

        if (activeMultiplier != 2) {
            activeMultiplier = 2;
        } else {
            activeMultiplier = 1;
        }

        updateListOfNumbers();
    }

    private void setTripleMultiplier() {

        if (activeMultiplier != 3) {
            activeMultiplier = 3;
        } else {
            activeMultiplier = 1;
        }

        updateListOfNumbers();
    }

    private void updateListOfNumbers() {

        for (Map.Entry<Integer, TextView> numberView : this.listOfNumberFields.entrySet()) {
            numberView.getValue().setText(String.valueOf(numberView.getKey() * activeMultiplier));
        }
    }

    private void initializeClickEvents() {

        RxView.clicks(findViewById(R.id.numberOneField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation,1 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberTwoField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 2 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberThreeField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 3 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberFourField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 4 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberFiveField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 5 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberSixField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 6 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberSevenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 7 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberEightField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 8 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberNineField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 9 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberTenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 10 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberElevenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 11 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberTwelveField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 12 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberThirtheenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 13 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberFourtheenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 14 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberFiftheenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 15 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberSixteenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 16 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberSeventheenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 17 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberEighteenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 18 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberNinetheenField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 19 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberTwentyField))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 20 * activeMultiplier));

        RxView.clicks(findViewById(R.id.numberNullCard))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 0));

        RxView.clicks(findViewById(R.id.numberFiftyCard))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 50));

        RxView.clicks(findViewById(R.id.numberTwentyFiveCard))
                .subscribe(Number -> this.calculateLogic.determineLegPoints(this, gameInformation, 25));

        RxView.clicks(findViewById(R.id.changeToDoubleCardView))
                .subscribe(Double -> setDoubleMultiplier());

        RxView.clicks(findViewById(R.id.changeToTripleCardView))
                .subscribe(Double -> setTripleMultiplier());

        //TODO Dringend überarbeiten !!!
        RxView.clicks(findViewById(R.id.numberBackCard))
                .subscribe(numberBack -> {
                    for(Player player : this.gameInformation.getPlayerList()){
                        if(!this.listOfArrowResults.containsKey("ArrowOne") && player.getArrowFinished()){
                            TextView arrowThree = findViewById(R.id.arrowThree);
                            arrowThree.setText("Pfeil 3");
                            player.setPointsInLeg(-this.listOfArrowResults.get("ArrowThree"));
                            TextView playersTextView = findViewById(player.getPlayerID());
                            playersTextView.setText(String.valueOf(player.getPointsInLeg()));
                            this.listOfArrowResults.remove("ArrowThree");
                            player.getListOfArrowResults().remove("ArrowThree");
                            this.listOfArrowResults.put("ArrowOne",player.getListOfArrowResults().get("ArrowOne"));
                            this.listOfArrowResults.put("ArrowTwo",player.getListOfArrowResults().get("ArrowTwo"));
                            TextView arrowOneTextView = findViewById(R.id.arrowOne);
                            arrowOneTextView.setText(String.valueOf(player.getListOfArrowResults().get("ArrowOne")));
                            for(Player otherPlayer : this.gameInformation.getPlayerList()){
                                if(otherPlayer != player){
                                    if(otherPlayer.getIsTurn()){
                                        otherPlayer.setIsTurn(false);
                                    }
                                }
                            }
                            player.setArrowFinished(false);
                            player.setIsTurn(true);
                            StartMenueActivity.dataManager.writeData(gameInformation);
                            return;
                        }
                    }

                    if (this.listOfArrowResults.size() > 0) {
                        if (this.listOfArrowResults.containsKey("ArrowTwo")) {
                            TextView arrowTwo = findViewById(R.id.arrowTwo);
                            arrowTwo.setText("Pfeil 2");
                            for (Player player : this.gameInformation.getPlayerList()) {
                                if (player.getIsTurn()) {
                                    player.setPointsInLeg(-this.listOfArrowResults.get("ArrowTwo"));
                                    TextView playersTextView = findViewById(player.getPlayerID());
                                    playersTextView.setText(String.valueOf(player.getPointsInLeg()));
                                }
                            }
                            this.listOfArrowResults.remove("ArrowTwo");
                            StartMenueActivity.dataManager.writeData(gameInformation);
                            return;
                        }
                        else {
                            TextView arrowOne = findViewById(R.id.arrowOne);
                            arrowOne.setText("Pfeil 1");
                            for (Player player : this.gameInformation.getPlayerList()) {
                                if (player.getIsTurn()) {
                                    player.setPointsInLeg(-this.listOfArrowResults.get("ArrowOne"));
                                    TextView playersTextView = findViewById(player.getPlayerID());
                                    playersTextView.setText(String.valueOf(player.getPointsInLeg()));
                                }
                            }
                            this.listOfArrowResults.remove("ArrowOne");
                            StartMenueActivity.dataManager.writeData(gameInformation);
                            return;
                        }
                    }
                });
    }

    private void startCamera() {

        ImageCalibration imageCalibration = ImageCalibration.getInstance();
        ImageAnalysis imageAnalysis = imageCalibration.getImageAnalysis();
        imageAnalysis.setAnalyzer(analyzerExecutor, getDartgameImageAnalyzer());

        // Used to bind the lifecycle of cameras to the lifecycle owner
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                cameraProvider.unbindAll();
                //Binden der Aktivitäten an die Kamera => Funktionalitäten
                Camera camera = cameraProvider.bindToLifecycle(this, imageCalibration.getCameraSelector(), imageAnalysis);
                camera.getCameraControl().setLinearZoom(imageCalibration.getZoomLevel());
            }
            catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    int frameNumber=0;
    int frameSkip=2;
    private ImageAnalysis.Analyzer getDartgameImageAnalyzer() {

        return image -> {
            try {
                frameNumber++;
                // only analyzing n-th Frame due to performance issues
                if(frameNumber%frameSkip!=0){
                    image.close();
                    return;
                }
                String dartValue="";
                frameNumber=0;
                final ImageCalibration imageCalibration = ImageCalibration.getInstance();
                final int rotationDegrees = image.getImageInfo().getRotationDegrees();

                // Create cv::mat(RGB888) from image(NV21)
                Mat mat = ImageUtils.ImageToRgbaMat(image);
                // Fix image rotation if given image is taken with a rotation
                mat = ImageUtils.matRotate(mat, rotationDegrees);

                /* Do some image processing */
                List<Point> markers = ImageUtils.findMarkers(mat, ColorMask.YellowNew);

                int pointQuality = imageCalibration.calculateNewPoints(markers, rotationDegrees);
                // TODO fix quality points - passiert hier anders als in der qualibierung, kann auch komplett ausgelagert werden
                //Tuple<Long, MatOfPoint2f> lastValidPoints = imageCalibration.getLastValidPoints();
                Point dartTip = ImageUtils.findAndMarkDartTip(mat);
                if (dartTip != null) {
                    Mat transformMatrix = ImageUtils.getTransformMatrixFromMarkers(markers, mat);
                    if (transformMatrix != null) {
                        MatOfPoint2f transformedPoint = new MatOfPoint2f(dartTip);
                        Core.perspectiveTransform(transformedPoint, transformedPoint, transformMatrix);
                        dartValue = String.valueOf(pointsFinder.getPointsFromXYCoordinates((int) Math.round(transformedPoint.get(0, 0)[0]), (int) Math.round(transformedPoint.get(0, 0)[1])));
                    }
                }
                String finalDartValue = checkDartValue4Player(dartValue);

                // draw found markers on original image
                mat = ImageUtils.drawYellowMarkers(markers, mat);

                // Convert cv::mat to bitmap for drawing
                Bitmap previewImageBitmap=Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(mat, previewImageBitmap);
                // Display the result onto ImageView - horrible slow. and stucks often.. but it's possible
                // https://blog.undabot.com/camerax-to-rescue-fa32db4523f8
                // möglicher ausweg, PreviewView und ein bitmap image drüber legen, das möglichst wenig inhalt hat? irgend ein canvas draw?
                calcDartValueBox.setText(dartValue.equals("0")? "":dartValue);
                runOnUiThread(() -> {
                    if(!finalDartValue.equals("") && !finalDartValue.equals("0")){
                        calculateLogic.determineLegPoints(this, gameInformation, Integer.parseInt(finalDartValue));
                    }
                    previewImage.setImageBitmap(previewImageBitmap);
                });
            }
            catch (Exception e) {
                Log.e("analyze", "broken", e);
            }
            // close image or we won't get next frame
            image.close();
        };
    }

    // FIFO EvictingQueue - collects last calculated timestamp+dartvalue up to specific numer of frames
    private final Queue<Tuple<Long,String>> lastDartValues = EvictingQueue.create(60/frameSkip); // 30 is maximal frames in 1 seconds
    private String lastValuedDart = "";

    /**
     * check frames. if there are more then 90% the same value and wasn't seen before = value the dart for actual player
     * furthermore, clear older entries
     *
     * @param newDartValue
     * @return
     */
    private String checkDartValue4Player(String newDartValue) {
        long now = System.currentTimeMillis();
        lastDartValues.offer(new Tuple<>(now, newDartValue));
        int sameValue = 0;

        Iterator<Tuple<Long,String>> lastDartValueIter = lastDartValues.iterator();
        while(lastDartValueIter.hasNext()){
            Tuple<Long,String> lastDartValue = lastDartValueIter.next();
            if(now - lastDartValue.a > TimeUnit.SECONDS.toMillis(3)){
                //lastDartValueIter.remove();
            }
            else if(newDartValue.equals(lastDartValue.b)) {
                sameValue++;
            }
        }
        double percentageSameValue = sameValue / lastDartValues.size();
        if(percentageSameValue >= 0.9 && !lastValuedDart.equals(newDartValue)) {
            lastValuedDart = newDartValue;
            if(!newDartValue.equals("")){
                return newDartValue;
            }
        }
        return "";
    }

    public void openWinnerDialog(String name) {

        WinnerDialog dialog = WinnerDialog.newInstance(name);
        dialog.show(getSupportFragmentManager(), "Winner Dialog");
    }
}
