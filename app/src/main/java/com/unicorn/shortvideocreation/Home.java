package com.unicorn.shortvideocreation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.unicorn.shortvideocreation.activity.Add_Video;
import com.unicorn.shortvideocreation.activity.MergeVideo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

public class Home extends AppCompatActivity implements View.OnClickListener {
private Button btn_addvideo,btn_mergevideo;
    private int MY_CAMERA_REQUEST_CODE=4040;
    private Bitmap bitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        btn_addvideo=findViewById(R.id.add_video);
        btn_mergevideo=findViewById(R.id.merge_video);
        bitmap = getBitmapFromVectorDrawable(this,R.drawable.watermark);

        if (checkPermission()) {

        } else {
            requestPermission();
        }

        btn_addvideo.setOnClickListener(this);
        btn_mergevideo.setOnClickListener(this);

        try {
            savelogo(bitmap);
        } catch (IOException e) {
            Log.i("SAM", "onCreate: "+e.toString());
            e.printStackTrace();
        }

    }

    private void savelogo(Bitmap bm) throws IOException {
        Log.i("SAM", "savelogo: "+"sam");
        OutputStream fos;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "short_video_logo_watermark.png");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "DCIM" + File.separator + "Short Video");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                System.out.println(getExternalCacheDir());
//                System.out.println(getExternalFilesDir());
                File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

            } else {
                Log.i("SAM", "onCreate: " + "LOgo SAved");
                String extStorageDirectory = Environment.getExternalStorageDirectory().toString() + "/Short Video";

                File file = new File(extStorageDirectory, "short_video_logo_watermark.png");
                fos = new FileOutputStream(file);
            }

            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            Objects.requireNonNull(fos).close();
        }
        catch (Exception e)
        {
            Log.i("UNI", "savelogo: "+e.toString());
        }
    }


    public static Bitmap getBitmapFromVectorDrawable(Context context, int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            drawable = (DrawableCompat.wrap(drawable)).mutate();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
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
    public void onClick(View view) {
        if(view.equals(btn_addvideo))
        {
            startActivity(new Intent(Home.this, Add_Video.class));

        }
        else if(view.equals(btn_mergevideo))
        {
//            chooseimages();
            startActivity(new Intent(Home.this, MergeVideo.class));

        }
    }






}