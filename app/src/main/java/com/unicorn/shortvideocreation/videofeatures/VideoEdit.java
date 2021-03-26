package com.unicorn.shortvideocreation.videofeatures;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpegLoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;
import com.unicorn.shortvideocreation.helper.ProgressCalculator;

import java.io.File;


public class VideoEdit {

    public static final int SUCCESS = 1;
    public static final int FAILED = 2;
    public static final int NONE = 3;
    public static final int RUNNING = 4;

    private final Context context;
    private boolean isFinished;
    private int status = NONE;
    private String errorMessage = "Compression Failed!";
    private ProgressDialog progressdialog;
    String outputPath = "";
    private ProgressCalculator mProgressCalculator;

    public VideoEdit(Context context) {
        this.context = context;
    }

    public void watermarking(String inputPath, WaterMarkListner listener,String outputPath) {
        this.outputPath=outputPath;
        if (inputPath == null || inputPath.isEmpty()) {
            status = NONE;
            if (listener != null) {
                listener.watermarkingfinished(NONE, false, null);
            }
            return;
        }

        String[] commands = new String[30];
        commands[0] = "-i";
        commands[1] = inputPath;
        commands[2] = "-i";
        commands[3] = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Short Video/short_video_logo_watermark.PNG";
        commands[4] = "-filter_complex";
        commands[5] = "overlay=x=(main_w-overlay_w)/(main_w-overlay_w):y=(main_h-overlay_h)";
        commands[6] = "-s";
        commands[7] = "240x320";
        commands[8] = "-r";
        commands[9] = "20";
        commands[10] = "-c:v";
        commands[11] = "libx264";
        commands[12] = "-preset";
        commands[13] = "ultrafast";
        commands[14] = "-c:a";
        commands[15] = "copy";
        commands[16] = "-me_method";
        commands[17] = "zero";
        commands[18] = "-tune";
        commands[19] = "fastdecode";
        commands[20] = "-tune";
        commands[21] = "zerolatency";
        commands[22] = "-strict";
        commands[23] = "-2";
        commands[24] = "-b:v";
        commands[25] = "1000k";
        commands[26] = "-pix_fmt";
        commands[27] = "yuv420p";
        commands[28] = "-y";
        commands[29] = outputPath;

        watermarkVideo(commands, outputPath, listener);

    }

    private void watermarkVideo(String[] command, final String outputFilePath, final WaterMarkListner listener) {
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
            fFmpeg.execute(command, new FFmpegExecuteResponseHandler() {
                @Override
                public void onSuccess(String message) {
                    status = SUCCESS;
                    progressdialog.dismiss();
                    Toast.makeText(context, "Video saved to "+outputFilePath, Toast.LENGTH_SHORT).show();
                    Log.i("success", "onSuccess: "+status);
                }

                @Override
                public void onProgress(String message) {
                    status = RUNNING;
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
                    progressdialog.setMessage("Saving Video Please Wait..");
                    progressdialog.setMax(100);
                    progressdialog.show();
                }

                @Override
                public void onFinish() {
                    Log.e("VideoCronProgress", "finnished");
                    isFinished = true;
                    if (listener != null) {
                        listener.watermarkingfinished(status, true, outputFilePath);
                    }
                    progressdialog.dismiss();
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

    public interface WaterMarkListner {
        void watermarkingfinished(int status, boolean isVideo, String fileOutputPath);

        void onFailure(String message);

        void onProgress(int progress);
    }

    public boolean isDone() {
        return status == SUCCESS || status == NONE;
    }

}
