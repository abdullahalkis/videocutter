package webservisim.video_cutter;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.util.Matrix;
import com.googlecode.mp4parser.util.Path;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.lang.String.valueOf;

public class GridLayoutWp extends AppCompatActivity {
    Button bttn;
    GridLayout gridLayout;

    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private Runnable r;
    private FFmpeg ffmpeg;
    private static final String TAG = "BHUVNESH";
    private static final String POSITION = "position";
    private static final String FILEPATH = "filepath";
    private int stopPosition;
    private String filePath;
    private Context mContext;
    private ScrollView mainlayout;
    private VideoView videoView;
    public  static  File dest;
    private static  String isExistOr;
    private Button wp;
    private TextView tvLeft, tvRight;
    private static Set<String> sharingVideosList;
    protected static int socialTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      //  setContentView(R.layout.grid_layout_wp);

        getPermission();
        loadFFMpegBinary();
        social();

        gridLayout=new GridLayout(GridLayoutWp.this);
        gridLayout.setBackgroundColor(Color.parseColor("#00bfff"));
        gridLayout.setColumnCount(3);
        gridLayout.setRowCount(3);


        for (int i = 1; i<MainActivity.timesToTime+2; i++) {
            bttn = new Button(GridLayoutWp.this);
            bttn.setText(""+i+".video Paylas");
            GridLayout.LayoutParams param =new GridLayout.LayoutParams();
            param.height = GridLayout.LayoutParams.WRAP_CONTENT;
            param.width = GridLayout.LayoutParams.WRAP_CONTENT;
            bttn.setLayoutParams(param);
            gridLayout.addView(bttn,i-1);

            final int finalI = i;
            bttn.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"ahoo btn "+String.valueOf(finalI),Toast.LENGTH_LONG).show();
                    if(finalI==MainActivity.timesToTime+1) {//videonun sonu icin
                        executeCutVideoCommand((finalI - 1) * socialTime * 1000, MainActivity.durationVideo);
                        sharing();

                    }else{
                        Toast.makeText(getApplicationContext(), valueOf(finalI), Toast.LENGTH_LONG).show();
                        executeCutVideoCommand((finalI - 1) * socialTime * 1000, (((finalI - 1) * socialTime) + socialTime) * 1000);
                        Log.d("Abooo", "2.si Cikti ve i degeri :" + String.valueOf(finalI));
                        sharing();
                    }


                }
            });

        }
        RelativeLayout.LayoutParams layoutparams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
        );
        wp=new Button(GridLayoutWp.this);
        wp.setText(" Hepsini Paylaş ");
        wp.setBackgroundColor(Color.GRAY);
        wp.setGravity(Gravity.TOP);
        wp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Ho Weh Law",Toast.LENGTH_LONG).show();
            }
        });


        RelativeLayout outsideLayout = new RelativeLayout(GridLayoutWp.this);
        outsideLayout.setLayoutParams(layoutparams) ;
        outsideLayout.addView(gridLayout);
        outsideLayout.setGravity(Gravity.CENTER);
        // outsideLayout.addView(wp);
        setContentView(outsideLayout);
        MainActivity.isInsStory=false;
        MainActivity.isWp=false;
        MainActivity.isIns60=false;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void executeCutVideoCommand(int startMs, int endMs ) {
        //File moviesDir=Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File moviesDir = new File(Environment.getExternalStorageDirectory() + "/Movies/CutVideo");
        boolean success = true;
        if (!moviesDir.exists()) {
            //Toast.makeText(MainActivity.this, "Directory Does Not Exist, Create It", Toast.LENGTH_SHORT).show();
            success = moviesDir.mkdirs();
        }
        if (success) {
            //Toast.makeText(MainActivity.this, "Directory Created", Toast.LENGTH_SHORT).show();
        } else {
            //Toast.makeText(MainActivity.this, "Failed - Error", Toast.LENGTH_SHORT).show();
        }
        String filePrefix="cut_video";
        String fileExtn=".mp4";
        String yourRealPath=getPath(this,MainActivity.selectedVideoUri);
        dest=new File(moviesDir,filePrefix+fileExtn);
        View v =new View(this);
        int fileNo=0;
        while (dest.exists()) {
            fileNo++;
            dest = new File(moviesDir, filePrefix + fileNo + fileExtn);

        }



        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);
        filePath = dest.getAbsolutePath();

        try {
            startTrim(new File(yourRealPath),dest,startMs,endMs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private String getPath(final Context context, final Uri uri) {
        final boolean isKitKat=Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT;
        if(isKitKat&&DocumentsContract.isDocumentUri(context,uri)){
            //externalStorage
            if(isExternalStorageDocument(uri)){
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }


            }//DownloadProvider
            else if(isDownloadsDocument(uri)){
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);

            }// MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }    // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;


    }
    /**
     * Get the value of the data column for this Uri.
     */
    private String getDataColumn(Context context, Uri uri, String selection,
                                 String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }
    private boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());

    }

    private boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }



    private void getPermission() {
        String[] params =null;
        String writeExStorage=Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readExStorage=Manifest.permission.READ_EXTERNAL_STORAGE;

        int hasWrite=ActivityCompat.checkSelfPermission(this,writeExStorage);
        int hasRead=ActivityCompat.checkSelfPermission(this,readExStorage);
        if(hasWrite!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{writeExStorage},100);
        }
        if(hasRead!=PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{readExStorage},100);        }
        if(params!=null&& params.length>0){
            ActivityCompat.requestPermissions(GridLayoutWp.this,params,100);
        }

    }
    private void loadFFMpegBinary() {
        try {
            if (ffmpeg == null) {
                Log.d(TAG, "ffmpeg : era nulo");
                ffmpeg = FFmpeg.getInstance(this);
            }
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {
                @Override
                public void onFailure() {
                    showUnsupportedExceptionDialog();
                }

                @Override
                public void onSuccess() {
                    Log.d(TAG, "ffmpeg : correct Loaded");
                }
            });
        } catch (FFmpegNotSupportedException e) {
            showUnsupportedExceptionDialog();
        } catch (Exception e) {
            Log.d(TAG, "EXception no controlada : " + e);
        }
    }
    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(GridLayoutWp.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GridLayoutWp.this.finish();
                    }
                })
                .create()
                .show();

    }




    protected void shareWhatsApp(){
                    Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                    Uri screenshotUri = Uri.parse(String.valueOf(dest));
                    sharingIntent.setType("video/*");
                    sharingIntent.setPackage("com.whatsapp");
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
                    startActivity(Intent.createChooser(sharingIntent, "Share Video "));

    }
    protected void shareInsta(){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri screenshotUri = Uri.parse(String.valueOf(dest));
        sharingIntent.setType("video/*");
        sharingIntent.setPackage("com.instagram.android");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        startActivity(Intent.createChooser(sharingIntent, "Share Video "));
    }
    public static void startTrim(File src, File dst, int startMs, int endMs) throws IOException {
        Log.d("Hoho",String.valueOf(src)+" "+String.valueOf(dst)+" "+String.valueOf(startMs)+" "+String.valueOf(endMs));
        FileDataSourceImpl file = new FileDataSourceImpl(src);
        Movie movie = MovieCreator.build(file);
        // remove all tracks we will create new tracks from the old
        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList<Track>());
        double startTime = startMs / 1000;
        double endTime = endMs / 1000;

        for (Track track : tracks) {
            long currentSample = 0;
            double currentTime = 0;
            long startSample = -1;
            long endSample = -1;

            for (int i = 0; i < track.getSampleDurations().length; i++) {
                if (currentTime <= startTime) {

                    // current sample is still before the new starttime
                    startSample = currentSample;
                }
                if (currentTime <= endTime) {
                    // current sample is after the new start time and still before the new endtime
                    endSample = currentSample;
                } else {
                    // current sample is after the end of the cropped video
                    break;
                }
                currentTime += (double) track.getSampleDurations()[i] / (double) track.getTrackMetaData().getTimescale();
                currentSample++;
            }
            movie.addTrack(new CroppedTrack(track, startSample, endSample));
        }

        Container out = new DefaultMp4Builder().build(movie);
        MovieHeaderBox mvhd = Path.getPath(out, "moov/mvhd");
        mvhd.setMatrix(Matrix.ROTATE_180);
        if (!dst.exists()) {
            dst.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(dst);
        WritableByteChannel fc = fos.getChannel();
        try {
            out.writeContainer(fc);
        } finally {
            fc.close();
            fos.close();
            file.close();
        }

        file.close();


    }

    public static void social(){
        if(MainActivity.isWp){
        socialTime =30;
        }else if(MainActivity.isInsStory){
            socialTime=15;
        }else if(MainActivity.isIns60){
            socialTime=60;
        }else{
        }
    }
    public void sharing(){
        if(socialTime==30){
            shareWhatsApp();
        }else if(socialTime==60){
            shareInsta();
        }else if(socialTime==15){
            shareInsta();
        }else{

        }

    }

}
