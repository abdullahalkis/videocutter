package webservisim.video_cutter;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.app.usage.ExternalStorageStats;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.TimeZoneFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.support.design.widget.Snackbar;
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
import java.lang.reflect.Array;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import webservisim.video_cutter.videoTrimmer.utils.FileUtils;

import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_TAKE_GALLERY_VIDEO = 100;
    private Runnable r;
    private FFmpeg ffmpeg;
    //    private K4LVideoTrimmer rangeSeekBar;
    protected static Uri selectedVideoUri;
    private static final String TAG = "BHUVNESH";
    private static final String POSITION = "position";
    private static final String FILEPATH = "filepath";
    private int stopPosition;
    private String filePath;
    private Context mContext;
    private ConstraintLayout mainlayout;
    private VideoView videoView;
    public static String[] complexCommand;
    public  static  File dest;
    public  static int timesToTime,durationVideo,bb=0;
    private static  String isExistOr,beforeExist;
    private Button wp,ins,bolBtn;
    private TextView tvLeft, tvRight;
    protected static boolean isWp=false,isInsStory=false,isIns60;
    private static Set<String> sharingVideosList;
    private static File path;
    static final String EXTRA_VIDEO_PATH = "EXTRA_VIDEO_PATH";
    static final String VIDEO_TOTAL_DURATION = "VIDEO_TOTAL_DURATION";
    public EditText bolNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView uploadVideo=findViewById(R.id.uploadVideo);
        final TextView cutVideo=findViewById(R.id.cropVideo);
        videoView =findViewById(R.id.videoView);


        tvLeft = findViewById(R.id.tvLeft);
        tvRight = findViewById(R.id.tvRight);
        ins=findViewById(R.id.instagram);
        wp=findViewById(R.id.wp);
        bolBtn=findViewById(R.id.bolBtn);
        bolNum=findViewById(R.id.bolText);
        mainlayout=findViewById(R.id.mainlayout);
        bolNum.setText("0");
        getPermission();

        uploadVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadVideo();
            }
        });

        wp.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                isWp=true;
                durationVideo=videoView.getDuration();
                if(selectedVideoUri!=null){
                    if(durationVideo/1000>30){
                        Toast.makeText(getApplicationContext(),valueOf(videoView.getDuration()/1000),Toast.LENGTH_LONG).show();
                        //whatsapp
                        timesToTime=islemler(videoView.getDuration());
                        startActivity(new Intent(MainActivity.this,GridLayoutWp.class));


                        Log.d("Abooo","3.si Cikti");
                    }else{
                        Log.d("dest :",String .valueOf(dest));
                        Log.d("selected :",String .valueOf(selectedVideoUri));
                        shareWhatsApp();
//                  executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);
                    }}else {
                    Snackbar.make(mainlayout,R.string.videoYukle,2000).show();
                }
            }
        });

        ins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Instagram")
                        .setMessage("Select 60 sec or 15 sec ")
                        .setCancelable(false)
                        .setPositiveButton("60sec", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isIns60=true;
                                durationVideo=videoView.getDuration();
                                if(selectedVideoUri!=null){
                                    if(durationVideo/1000>60){
                                        Toast.makeText(getApplicationContext(),valueOf(videoView.getDuration()/1000),Toast.LENGTH_LONG).show();
                                        //whatsapp
                                        timesToTime=islemler(videoView.getDuration());
                                        startActivity(new Intent(MainActivity.this,GridLayoutWp.class));

                                        Log.d("Abooo","3.si Cikti");
                                    }else{
                                        Log.d("dest :",String .valueOf(dest));
                                        Log.d("selected :",String .valueOf(selectedVideoUri));
                                        shareInsta();
//                  executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);
                                    }}else {
                                    Snackbar.make(mainlayout,R.string.videoYukle,2000).show();
                                }

                            }
                        })
                        .setNegativeButton("15sec", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                isInsStory=true;
                                durationVideo=videoView.getDuration();
                                if(selectedVideoUri!=null){
                                    if(durationVideo/1000>15){
                                        Toast.makeText(getApplicationContext(),valueOf(videoView.getDuration()/1000),Toast.LENGTH_LONG).show();
                                        //whatsapp
                                        timesToTime=islemler(videoView.getDuration());
                                        startActivity(new Intent(MainActivity.this,GridLayoutWp.class));

                                        Log.d("Abooo","3.si Cikti");
                                    }else{
                                        Log.d("dest :",String .valueOf(dest));
                                        Log.d("selected :",String .valueOf(selectedVideoUri));
                                        shareInsta();
//                  executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);
                                    }}else {
                                    Snackbar.make(mainlayout,R.string.videoYukle,2000).show();
                                }
                            }
                        })
                        .create()
                        .show();

            }
        });
        bolBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {

                int partNum=Integer.parseInt(String.valueOf(bolNum.getText()));
                if(partNum>0&&partNum<videoView.getDuration()/1000){
                    timesToTime=islemler(videoView.getDuration());
                    for (int i = 0; i < timesToTime+1; i++) {

                        if(i==timesToTime) {//videonun sonu icin
                            executeCutVideoCommand((i) * partNum * 1000, videoView.getDuration());
                            Toast.makeText(MainActivity.this, "İşleminiz Tamamlanmıştır. ", Toast.LENGTH_SHORT).show();

                        }else{
                            Toast.makeText(getApplicationContext(), valueOf(i), Toast.LENGTH_LONG).show();
                            executeCutVideoCommand(i * partNum * 1000, (((i) * partNum) + partNum) * 1000);
                            Log.d("Abooo", "2.si Cikti ve i degeri :" + String.valueOf(i));

                        }                    }

                }else if(videoView.getDuration()==-1){
                    Toast.makeText(MainActivity.this, "Lütfen Video Seçimi Yapınız.", Toast.LENGTH_SHORT).show();
                }
                else if(Integer.parseInt(String.valueOf(bolNum.getText()))>videoView.getDuration()/1000){
                    Toast.makeText(MainActivity.this, "Kesilecek süre videonun süresinden fazla olamaz.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Lütfen Geçerli Bir Değer yazınız.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        cutVideo.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View v) {
                if (selectedVideoUri != null) {

                    startTrimActivity(selectedVideoUri);

                    //                       executeCutVideoCommand(rangeSeekBar.getSelectedMinValue().intValue() * 1000, rangeSeekBar.getSelectedMaxValue().intValue() * 1000);
/*                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Ne Yapmak istersiniz.");
                        builder.setMessage("WhatsAppta paylaş ");
                        builder.setPositiveButton(" Paylaş ", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                /*if (rangeSeekBar.getSelectedMaxValue().intValue() - rangeSeekBar.getSelectedMinValue().intValue() <= 30) {
                                    shareWhatsApp();
                                } else if (rangeSeekBar.getSelectedMaxValue().intValue() - rangeSeekBar.getSelectedMinValue().intValue() > 30) {
                                    GridLayoutWp.dest = new File(String.valueOf(dest));
                                    final Uri sttr = Uri.parse(String.valueOf(dest));
                                    videoView.setVideoURI(sttr);

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            durationVideo = videoView.getDuration();
                                            timesToTime = islemler(videoView.getDuration());
                                            Log.d("Hoho", String.valueOf(videoView.getDuration()) + String.valueOf(sttr) + String.valueOf(timesToTime));
                                            startActivity(new Intent(MainActivity.this, GridLayoutWp.class));
                                        }
                                    }, 1000);


                                }
                            }
                        });
                        builder.setNegativeButton("Düzenle", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Uri sttr = Uri.parse(String.valueOf(dest));
//                                videoView.setVideoURI(sttr);
//                                startActivity(new Intent(MainActivity.this, PreviewActivity.class));
                            }
                        });
                        builder.show(); */
                } else
                    Toast.makeText(getApplicationContext(),"Ho weehhhhh",Toast.LENGTH_LONG).show();
                Snackbar.make(mainlayout, "Please upload a video aa", 2000).show();



            }


        });

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
        int fileNo=0;
        Toast.makeText(this, String.valueOf(path), Toast.LENGTH_SHORT).show();
        boolean aa=true;
        while (aa){
            if(bb==0){
                while (dest.exists()) {
                    fileNo++;
                    dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
                    aa=false;
                    path=dest;
                }
                aa=false;
                bb++;
            }else if(path.exists()){
                while (dest.exists()) {
                    fileNo++;
                    dest = new File(moviesDir, filePrefix + fileNo + fileExtn);
                    aa=false;
                    path=dest;
                }}

        }


        Log.d(TAG, "startTrim: src: " + yourRealPath);
        Log.d(TAG, "startTrim: dest: " + dest.getAbsolutePath());
        Log.d(TAG, "startTrim: startMs: " + startMs);
        Log.d(TAG, "startTrim: endMs: " + endMs);
        filePath = dest.getAbsolutePath();
        //String[] complexCommand = {"-i", yourRealPath, "-ss", "" + startMs / 1000, "-t", "" + endMs / 1000, dest.getAbsolutePath()};

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

    private void uploadVideo() {
        try{
            Intent intent =new Intent();
            intent.setType("video/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,getResources().getString(R.string.videoSecimi)),REQUEST_TAKE_GALLERY_VIDEO);
        }catch (Throwable throwable){

            throwable.printStackTrace();
        }

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
            ActivityCompat.requestPermissions(MainActivity.this,params,100);
        }

    }

    private void showUnsupportedExceptionDialog() {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Not Supported")
                .setMessage("Device Not Supported")
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.finish();
                    }
                })
                .create()
                .show();

    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                selectedVideoUri = data.getData();
                videoView.setVideoURI(selectedVideoUri);

//                if (selectedVideoUri != null) {
//                    startTrimActivity(selectedVideoUri);
//                } else {
//                    Toast.makeText(MainActivity.this, R.string.toast_cannot_retrieve_selected_video, Toast.LENGTH_SHORT).show();
//                }

                //videoView.start();

                /*videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            int duration2;
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        // TODO Auto-generated method stub
                        duration2 = mp.getDuration() / 1000;
                        tvLeft.setText("00:00:00");

                        tvRight.setText(getTime(mp.getDuration() / 1000));
                        mp.setLooping(true);
                        rangeSeekBar.setRangeValues(0, duration2);
                        rangeSeekBar.setSelectedMinValue(0);
                        rangeSeekBar.setSelectedMaxValue(duration2);
                        rangeSeekBar.setEnabled(true);

                        rangeSeekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener() {
                            @Override
                            public void onRangeSeekBarValuesChanged(RangeSeekBar bar, Object minValue, Object maxValue) {
                                videoView.seekTo((int) minValue * 1000);

                               tvLeft.setText(getTime((int)bar.getSelectedMinValue()));

                               tvRight.setText(getTime((int) bar.getSelectedMaxValue()));

                            }
                        });

                        final Handler handler = new Handler();
                        handler.postDelayed(r = new Runnable() {
                            @Override
                            public void run() {

                                if (videoView.getCurrentPosition() >= rangeSeekBar.getSelectedMaxValue().intValue() * 1000)
                                    videoView.seekTo(rangeSeekBar.getSelectedMinValue().intValue() * 1000);
                                handler.postDelayed(r, 1000);
                            }
                        }, 1000);

                    }
                });*/
/*                tvLeft.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    }
                });*/
//                }
            }
        }
    }
    private String getTime(int seconds) {
        int hr = seconds / 3600;
        int rem = seconds % 3600;
        int mn = rem / 60;
        int sec = rem % 60;
        return String.format("%02d", hr) + ":" + String.format("%02d", mn) + ":" + String.format("%02d", sec);
    }

    protected int islemler(int time){
        //whatsapp icin
        if(isWp){
            int a=time/30000;
            return a;
        }else if(isIns60){
            int a=time/60000;
            return a;
        }else if(isInsStory){
            int a=time/15000;
            return a;
        }else{
            int a=time/(Integer.parseInt(String.valueOf(bolNum.getText()))*1000);
            return a;
        }
    }
    protected void shareWhatsApp(){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        if(dest!=null){
            Uri screenshotUri = Uri.parse(String.valueOf(dest));
            sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        }else
        {
            sharingIntent.putExtra(Intent.EXTRA_STREAM, selectedVideoUri);
        }
        sharingIntent.setType("video/*");
        sharingIntent.setPackage("com.whatsapp");
        startActivity(Intent.createChooser(sharingIntent, "Share Video "));
    }
    protected void shareInsta(){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        if(dest!=null){
            Uri screenshotUri = Uri.parse(String.valueOf(dest));
            sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        }else
        {
            sharingIntent.putExtra(Intent.EXTRA_STREAM, selectedVideoUri);
        }
        sharingIntent.setType("video/*");
        sharingIntent.setPackage("com.instagram.android");
        startActivity(Intent.createChooser(sharingIntent, "Share Video "));
    }

    public static void startTrim(File src, File dst, int startMs, int endMs) throws IOException {

        FileDataSourceImpl file = new FileDataSourceImpl(src);
        Movie movie = MovieCreator.build(src.getAbsolutePath());
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
    private void startTrimActivity(@NonNull Uri uri) {
        Intent intent = new Intent(this, TrimmerActivity.class);
        intent.putExtra(EXTRA_VIDEO_PATH, FileUtils.getPath(this, uri));
        intent.putExtra(VIDEO_TOTAL_DURATION, getMediaDuration(uri));
        startActivity(intent);
    }
    private int  getMediaDuration(Uri uriOfFile)  {
        MediaPlayer mp = MediaPlayer.create(this,uriOfFile);
        int duration = mp.getDuration();
        return  duration;
    }


}
