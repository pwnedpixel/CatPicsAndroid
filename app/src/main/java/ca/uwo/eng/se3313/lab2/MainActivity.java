package ca.uwo.eng.se3313.lab2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Random;
import java.util.logging.Handler;

public class MainActivity extends AppCompatActivity {

    /**
     * View that showcases the image
     */
    private ImageView ivDisplay;

    /**
     * Skip button
     */
    private ImageButton skipBtn;

    /**
     * Progress bar showing how many seconds left (percentage).
     */
    private ProgressBar pbTimeLeft;

    /**
     * Label showing the seconds left.
     */
    private TextView tvTimeLeft;

    /**
     * Control to change the interval between switching images.
     */
    private SeekBar sbWaitTime;

    /**
     * Editable text to change the interval with {@link #sbWaitTime}.
     */
    private EditText etWaitTime;

    /**
     * Used to download images from the {@link #urlList}.
     */
    private IImageDownloader imgDownloader;

    /**
     * The next image that will be displayed
     */
    public Bitmap nextImage;

    /**
     * Timer until the image changes
     */
    private CountDownTimer myTimer;

    /**
     * List of image URLs of cute animals that will be displayed.
     */
    private static final List<String> urlList = new ArrayList<String>() {{
        add("http://i.imgur.com/CPqbVW8.jpg");
        add("http://i.imgur.com/Ckf5OeO.jpg");
        add("http://i.imgur.com/3jq1bv7.jpg");
        add("http://i.imgur.com/8bSITuc.jpg");
        add("http://i.imgur.com/JfKH8wd.jpg");
        add("http://i.imgur.com/KDfJruL.jpg");
        add("http://i.imgur.com/o6c6dVb.jpg");
        add("http://i.imgur.com/B1bUG2K.jpg");
        add("http://i.imgur.com/AfxvVuq.jpg");
        add("http://i.imgur.com/DSDtm.jpg");
        add("http://i.imgur.com/SAVYw7S.jpg");
        add("http://i.imgur.com/4HznKil.jpg");
        add("http://i.imgur.com/meeB00V.jpg");
        add("http://i.imgur.com/CPh0SRT.jpg");
        add("http://i.imgur.com/8niPBvE.jpg");
        add("http://i.imgur.com/dci41f3.jpg");
    }};

    /**
     * loads the next image in the background
     */
    private void loadNextImage() {
        Random rand = new Random();
        int index = rand.nextInt(urlList.size());
        //calls the download function, passes a newly defined success handler which saves the
        //aquired image to be loaded next.
        imgDownloader.download(urlList.get(index), image -> nextImage = image);
    }

    /**
     * Replaces the current image with {@link #nextImage}.
     */
    private void changeImage() {
        try {
            ivDisplay.setImageBitmap(nextImage);//will be blank if there was an error getting the image
            //if the image cannot be retrieved by the image downloader, null is returned. so
            // we check for null! if its nullm we show the cat
            if (nextImage == null) {
                System.out.println("Using Default image");
                ivDisplay.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.cat_error));
            }
        } catch (Exception e) {

            System.err.println("Error loading image: " + e);
            //  ivDisplay.setImageResource(R.drawable.cat_error);
        }
    }

    /**
     * Creates a new timer with a duration set by the value of {@link #sbWaitTime}.
     */
    public void createTimer() {
        System.out.println("New Timer");
        myTimer = new CountDownTimer(((sbWaitTime.getProgress() + 5) * 1000), 100) {

            public void onTick(long millisUntilFinished) {
                pbTimeLeft.incrementProgressBy(1);
                tvTimeLeft.setText((millisUntilFinished / 1000) + "");

            }

            public void onFinish() {
                System.out.println("Timer Finished");
                pbTimeLeft.setProgress(0);
                changeImage();
                loadNextImage();
                myTimer.start();
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pbTimeLeft = (ProgressBar) findViewById(R.id.pbTimeLeft);
        sbWaitTime = (SeekBar) findViewById(R.id.sbWaitTime);
        etWaitTime = (EditText) findViewById(R.id.etWaitTime);
        tvTimeLeft = (TextView) findViewById(R.id.tvTimeLeft);
        ivDisplay = (ImageView) findViewById(R.id.ivDisplay);
        skipBtn = (ImageButton) findViewById(R.id.btnSkip);

        //Event Listeners//

        sbWaitTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                etWaitTime.setText(sbWaitTime.getProgress() + 5 + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                myTimer.cancel();
                pbTimeLeft.setProgress(0);
                pbTimeLeft.setMax((sbWaitTime.getProgress() + 5) * 10);

                tvTimeLeft.setText(sbWaitTime.getProgress() + 5 + "");
                createTimer();

            }
        });

        /**this function will listen for when the enter or submit button n the keyboard is pressed.
         *   once enter is pressed, the number is validated.
         */
        etWaitTime.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN) &&
                            (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                //if (!event.isShiftPressed()) {
                int input = (etWaitTime.getText().length() != 0) ? Integer.valueOf(etWaitTime.getText().toString()) : 0;
                //Check if the number isnot between 5 and 60
                if (!(etWaitTime.getText().length() != 0 && input >= 5 && input <= 60)) {
                    etWaitTime.setError("Enter a number from 5 to 60");
                }
                //if it is valid...
                else {
                    System.out.println("textGood");
                    myTimer.cancel();
                    int progress = Integer.valueOf(etWaitTime.getText().toString()) - 5;
                    pbTimeLeft.setMax((progress + 5) * 10);
                    pbTimeLeft.setProgress(0);
                    tvTimeLeft.setText((progress + 5) + "");
                    sbWaitTime.setProgress(progress);
                    createTimer();
                }

                return true; // consume.
                // }
            }
            return false; // pass on to other listeners.

        });

        //Defines what the skip button will do when clicked
        skipBtn.setOnClickListener(v -> {
            myTimer.cancel();
            pbTimeLeft.setProgress(0);
            changeImage();
            loadNextImage();
            createTimer();
        });


        //Running code
        createTimer();
        imgDownloader = new myImageDownloader();

        //load the first image and display it as soon as it is available
        Random rand = new Random();
        int index = Math.abs(rand.nextInt() % urlList.size());
        imgDownloader.download(urlList.get(index), image -> {
            //success handler
            nextImage = image;
            changeImage();
        });
        loadNextImage();

    }
}