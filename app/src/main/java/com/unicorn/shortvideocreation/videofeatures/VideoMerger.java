package com.unicorn.shortvideocreation.videofeatures;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;


import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.unicorn.shortvideocreation.R;
import com.unicorn.shortvideocreation.helper.ProgressCalculator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class VideoMerger {

    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int NONE = 3;
    public static final int RUNNING = 4;

    private final Context context;
    private boolean isFinished;
    private int status = NONE;
    private String errorMessage = "Merge Failed!";
    private ProgressCalculator mProgressCalculator;
    private ProgressDialog progressdialog;
    private VideoView videoView;
    public VideoMerger(Context context) {
        this.context = context;

    }

    public void startMerging(List<String> videoFilenameList, MergingCallback listener,String outputPath) {

        List<String> cmdList = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        Log.i("UNI", "startMerging: "+videoFilenameList.get(0));
        for (int i = 0; i < videoFilenameList.size(); i++) {
            cmdList.add("-i");
            cmdList.add(videoFilenameList.get(i));

            sb.append("[").append(i).append(":0] [").append(i).append(":1]");
        }
        sb.append(" concat=n=").append(videoFilenameList.size()).append(":v=1:a=1 [v] [a]");
        cmdList.add("-filter_complex");
        cmdList.add(sb.toString());
        cmdList.add("-map");
        cmdList.add("[v]");
        cmdList.add("-map");
        cmdList.add("[a]");
        cmdList.add("-preset");
        cmdList.add("ultrafast");
        cmdList.add(outputPath);

        sb = new StringBuilder();
        for (String str : cmdList) {
            sb.append(str).append(" ");
        }

        String[] cmd = cmdList.toArray(new String[cmdList.size()]);

        videoView=((Activity)context).findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(context);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        compressVideo(cmd, outputPath, listener);

    }

    public String getAppDir() {
        String outputPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        outputPath += "/" + "vvvvv";
        File file = new File(outputPath);
        if (!file.exists()) {
            file.mkdir();
        }
        outputPath += "/" + "videocompress";
        file = new File(outputPath);
        if (!file.exists()) {
            file.mkdir();
        }
        return outputPath;
    }

    private void compressVideo(String[] command, final String outputFilePath, final MergingCallback listener) {
        FFmpeg fFmpeg=FFmpeg.getInstance(context);
        try {
            fFmpeg.loadBinary(new FFmpegLoadBinaryResponseHandler() {
                @Override
                public void onFailure() {

                }

                @Override
                public void onSuccess() {

                }

                @Override
                public void onStart() {

                }

                @Override
                public void onFinish() {

                }
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
        }
        try {

            fFmpeg.getInstance(context).execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    status = SUCCESS;
                    progressdialog.dismiss();
                    Uri uri=getImageContentUri(context,outputFilePath);
                    videoView.setVideoURI(uri);
                    videoView.start();
                    Log.i("UNI", "onSuccess: "+uri);
                }

                @Override
                public void onProgress(String message) {
                    status = RUNNING;
                    Log.e("VideoCronProgress", message);
                    int progress = mProgressCalculator.calcProgress(message);
                    Log.e("VideoCronProgress == ", progress + "..");

                    progressdialog.setProgress(progress);
                    if (progress != 0 && progress <= 100) {
                        if (progress >= 99) {
                            progress = 100;
                        }
                        listener.onProgress(progress);
                    }
                }

                @Override
                public void onFailure(String message) {
                    status = FAILED;
                    progressdialog.dismiss();
                    Log.e("VideoCompressor", message);
                    if (listener != null) {
                        listener.onFailure("Error : " + message);
                    }
                }

                @Override
                public void onStart() {
                    mProgressCalculator=new ProgressCalculator();
                    progressdialog = new ProgressDialog(context);
                    progressdialog.setIndeterminate(false);
                    progressdialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressdialog.setCancelable(false);
                    progressdialog.setMessage("Merging Videos Please Wait..");
                    progressdialog.setMax(100);
                    progressdialog.show();
                }

                @Override
                public void onFinish() {
                    Log.e("VideoCronProgress", "finnished");
                    isFinished = true;
                    if (listener != null) {
                        listener.onMergeComplete(outputFilePath);
                    }
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            status = FAILED;
            errorMessage = e.getMessage();
            if (listener != null) {
                listener.onFailure("Error : " + e.getMessage());
            }
        }
    }

    public interface MergingCallback {
        void onMergeComplete(String fileOutputPath);

        void onFailure(String message);

        void onProgress(int progress);
    }
    public static Uri getImageContentUri(Context context, String absPath) {
        Log.v("UNI", "getImageContentUri: " + absPath);

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , new String[] { MediaStore.Images.Media._ID }
                , MediaStore.Images.Media.DATA + "=? "
                , new String[] { absPath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , Integer.toString(id));

        } else if (!absPath.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
        }
    }
    public boolean isDone() {
        return status == SUCCESS || status == NONE;
    }

}
