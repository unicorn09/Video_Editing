package com.unicorn.shortvideocreation.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;


import android.content.Intent;
import android.content.pm.PackageManager;

import android.net.Uri;

import android.os.Bundle;

import android.os.Environment;
import android.provider.MediaStore;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import androidx.appcompat.widget.Toolbar;


import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gowtham.library.utils.TrimVideo;
import com.unicorn.shortvideocreation.R;
import com.unicorn.shortvideocreation.videofeatures.VideoEdit;


import java.io.File;





public class Add_Video extends AppCompatActivity {

    VideoView videoView;
    private static final int REQUEST_CODE_VIDEO_CAPTURE = 2607;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    FloatingActionButton record_btn;
    private  String final_filepath;
    private VideoEdit mvideoCompressor=new VideoEdit(this);
    private VideoEdit addWaterMark=new VideoEdit(this);
    private ProgressBar progressBar;
    public String outputPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_video);
        videoView = findViewById(R.id.videoView);
        record_btn=findViewById(R.id.fab);


        progressBar=findViewById(R.id.progressbar);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        if (checkPermission()) {

        } else {
            requestPermission();
        }

        String extStorageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Short Video/Videos";

        record_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                     Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);


                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, REQUEST_CODE_VIDEO_CAPTURE);
                    }

            }
        });
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);

    }



    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED )
            return false;
        else if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED )
        return false;

        return true;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                MY_CAMERA_REQUEST_CODE);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            Log.i("UNicorn", "onActivityResult: "+videoUri);
            Log.i("UNicorn", "onActivityResult: "+Environment.getExternalStorageDirectory().getAbsolutePath());
            outputPath=getFilename();
            TrimVideo.activity(String.valueOf(videoUri))
                    .setFixedDuration(2*60)
                    .setAccurateCut(true)
                    .start(this);
        }

        if (requestCode == TrimVideo.VIDEO_TRIMMER_REQ_CODE && data != null) {
            Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(data));
            Log.d("UNicorn","Trimmed path:: "+uri);
            videoView.setVideoURI(uri);
            videoView.start();
            final_filepath=uri.toString();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if(item.getItemId()==R.id.action_save)
        {
            if(final_filepath!=null)
            {
                progressBar.setVisibility(View.VISIBLE);
                startMediaWatermarking(final_filepath);

            }
            else
                Toast.makeText(this, "Please Create a Video First", Toast.LENGTH_SHORT).show();
        }


        return super.onOptionsItemSelected(item);
    }
    private void startMediaWatermarking(String mInputPath) {
        addWaterMark.watermarking(mInputPath, new VideoEdit.WaterMarkListner() {
            @Override
            public void watermarkingfinished(int status, boolean isVideo, String fileOutputPath) {

                if (mvideoCompressor.isDone()) {
                    File outputFile = new File(fileOutputPath);
                    long outputCompressVideosize = outputFile.length();
                    long fileSizeInKB = outputCompressVideosize / 1024;
                    long fileSizeInMB = fileSizeInKB / 1024;

                    String s = "Output video path : " + fileOutputPath + "\n" +
                            "Output video size : " + fileSizeInKB + "mb";
                    Log.i("compression", "compressionFinished: "+s);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(String message) {
                Log.i("compression_watermark", "onFailure: "+message);
            }

            @Override
            public void onProgress(final int progress) {

            }
        },outputPath);
    }
    public String getFilename() {

        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"Short Video/"+ "videos/" + System.currentTimeMillis() + ".mp4";


    }


}