package ca.uwo.eng.se3313.lab2;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by andyk on 2016-10-20.
 */

/**
 * Downloads an image in the background
 */
public class myImageDownloader implements IImageDownloader {

    /**
     *
     */
    public ErrorHandler ehandle = error -> Log.e("Error", error.getMessage());

    /**
     *
     * @param url the Url of image to download
     * @param handler Code to execute in the UI thread on success (accepts a {@link Bitmap}).
     *
     */
    public void download(String url, IImageDownloader.SuccessHandler handler) {
        //Behind the scenes...
        new DownloadImageTask(){

            /**
             * Runs after the {@link #doInBackground(String...)} function has finished.
             * @param result the downloaded image
             */
            @Override
            protected void onPostExecute(Bitmap result) {
                super.onPostExecute(result);
                System.out.println("PostExecute, Bitmap: "+result);
                handler.onComplete(result);
            }
        }.execute(url);
    }

    /**
     * Task that connects and downloads the image
     */
    public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        /**
         *
         * @param urls Array of urls to download. in this case only one url
         * @return
         */
        protected Bitmap doInBackground(String... urls) {
            System.out.println("Download Image Task With URL: "+urls[0]);
            String urldisplay = urls[0];
            Bitmap myBitmapImage;
            try {
                URL url = new URL(urldisplay);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                InputStream input = con.getInputStream();
                myBitmapImage = BitmapFactory.decodeStream(input);//Makes the Bitmap from data stream.
                con.disconnect();//closes connection
                return myBitmapImage;
            } catch (Exception e) {
                ehandle.onError(e);
            }
            return null;
        }
    }

}
