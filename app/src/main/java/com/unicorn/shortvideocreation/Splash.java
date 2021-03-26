package com.unicorn.shortvideocreation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Splash extends AppCompatActivity {
private ImageView imgView_logo;Bitmap bitmap;
    private int MY_CAMERA_REQUEST_CODE=404;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        imgView_logo=findViewById(R.id.img_logo);
        bitmap = getBitmapFromVectorDrawable(this,R.drawable.watermark);
        Log.i("logo", "onCreate: "+bitmap);

        Animation fadeOut = new AlphaAnimation(0, 1);
        fadeOut.setInterpolator(new AccelerateInterpolator()); //and this
        fadeOut.setStartOffset(1000);
        fadeOut.setDuration(1000);




        AnimationSet animation1 = new AnimationSet(false); //change to false
        animation1.addAnimation(fadeOut);
        imgView_logo.setAnimation(animation1);



    }

    private void savelogo(Bitmap bm) throws IOException {

        Log.i("SAM", "onCreate: "+"LOgo SAved");
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString()+"/Short Video";

        File file = new File(extStorageDirectory, "short_video_logo_watermark.PNG");
        FileOutputStream outStream = new FileOutputStream(file);
        bm.compress(Bitmap.CompressFormat.PNG, 100, outStream);
        outStream.flush();
        outStream.close();
    }



    @Override
    public void onStart() {
        super.onStart();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Splash.this, Home.class));
                finish();
            }
        },4000);


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
}