package com.unicorn.shortvideocreation.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;


import com.gowtham.library.utils.TrimVideo;
import com.unicorn.shortvideocreation.R;
import com.unicorn.shortvideocreation.videofeatures.VideoEdit;
import com.unicorn.shortvideocreation.videofeatures.VideoMerger;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MergeVideo extends AppCompatActivity implements View.OnClickListener {
private Button btn_selectvideo;


    private static final int SELECT_VIDEOS=505;
    private static final int SELECT_VIDEOS_KITKAT=606;
    private List<String> selectedVideos;
    private VideoMerger videoMerger;
    private VideoView videoView;
    public String outputpath;
    private boolean saveEnabled=false,trimEnabled=false;
    private VideoEdit mvideoCompressor=new VideoEdit(this);
    private VideoEdit addWaterMark=new VideoEdit(this);
    private Uri finaluri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_merge_video);

        btn_selectvideo=findViewById(R.id.btn_video);
        videoView=findViewById(R.id.videoView);
        btn_selectvideo.setOnClickListener(this);

        videoMerger=new VideoMerger(this);


        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu2, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        if(item.getItemId()==R.id.action_save) {
            if(outputpath!=null&&finaluri!=null)
            { startMediaWatermarking(finaluri.toString());
            Toast.makeText(this, "File Saved to "+outputpath, Toast.LENGTH_SHORT).show();
            }
            else if(outputpath!=null)
            {

                startMediaWatermarking(getImageContentUri(this,outputpath).toString());
            }
        }
        if(item.getItemId()==R.id.action_trim) {
            if (outputpath != null) {
                TrimVideo.activity(getImageContentUri(MergeVideo.this, outputpath).toString())
                        .setFixedDuration(2 * 60) //2 minutes
                        .setAccurateCut(true)
                        .start((Activity) MergeVideo.this);
            }
            else
            {
                Toast.makeText(this, "Please Select Videos First", Toast.LENGTH_SHORT).show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View view) {
        if(view==btn_selectvideo)
        {
            choosemultiplevideo();
        }
    }

    private void choosemultiplevideo() {
        if (Build.VERSION.SDK_INT <19){
            Intent intent = new Intent();
            intent.setType("video/mp4");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select videos"),SELECT_VIDEOS);
        } else {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.setType("video/mp4");
            startActivityForResult(intent, SELECT_VIDEOS_KITKAT);
        }

    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK&&requestCode != TrimVideo.VIDEO_TRIMMER_REQ_CODE) {
            selectedVideos = getSelectedVideos(requestCode, data);

            if(selectedVideos.size()==2)
            {

                    outputpath=getFilename();
                    videoMerger.startMerging(selectedVideos, new VideoMerger.MergingCallback() {
                        @Override
                        public void onMergeComplete(String fileOutputPath) {
//                            Log.i("UNI", "onMergeComplete: "+fileOutputPath);
                            saveEnabled=true;
                            trimEnabled=true;

                        }

                        @Override
                        public void onFailure(String message) {
//                            Log.e("MergeListner",message);
                        }

                        @Override
                        public void onProgress(final int progress) {

                        }

                    },outputpath);

            }
            else
            {
                Toast.makeText(this, "Please Select Two Videos", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == TrimVideo.VIDEO_TRIMMER_REQ_CODE && data != null) {
            Uri uri = Uri.parse(TrimVideo.getTrimmedVideoPath(data));
            videoView.setVideoURI(uri);
            videoView.start();
            finaluri=uri;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

                }
            }

            @Override
            public void onFailure(String message) {
                Log.i("compression_watermark", "onFailure: "+message);
            }

            @Override
            public void onProgress(final int progress) {

            }
        },outputpath);
    }
    private List<String> getSelectedVideos(int requestCode, Intent data) {

        List<String> result = new ArrayList<>();

        ClipData clipData = data.getClipData();
        if(clipData != null) {
            for(int i=0;i<clipData.getItemCount();i++) {
                ClipData.Item videoItem = clipData.getItemAt(i);
                Uri videoURI = videoItem.getUri();
                String filePath = getPath(this, videoURI);
                result.add(filePath);
            }
        }
        else {
            Uri videoURI = data.getData();
            String filePath = getPath(this, videoURI);
            result.add(filePath);
        }

        return result;
    }

    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
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
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
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
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }



    public String getFilename() {

        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +"Short Video/"+ "videos/" + "merged_"+System.currentTimeMillis() + ".mp4";


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


}



