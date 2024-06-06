package com.mirea.kt.ribo.contactscw;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class ImageStorage {
    public static String saveToSdCard(Bitmap bitmap, String filename) {

        String stored = null;
        File sdcard = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File folder = new File(sdcard.getAbsoluteFile(), ".ContactsCW");
        try {
            folder.mkdirs();
        }catch (Exception ex){
            Log.e("ImageStorage", "Make dir error: "+ex);
        }
        File file = new File(folder.getAbsoluteFile(), filename + ".jpg");
        if (file.exists())
            return stored;
        try {
            //outputStream = openFileOutput(fileName , Context.MODE_PRIVATE);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            Log.e("ImageStorage", "File output stream error: "+e);
        }
        return stored;
    }

    public static File getImage(String imagename) {
        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;
            mediaImage = new File(myDir.getPath() + "/Pictures/.ContactsCW/" + imagename);
        } catch (Exception e) {
            Log.e("ImageStorage", "getImages error: "+e);
        }
        return mediaImage;
    }

    public static boolean checkifImageExists(String imagename) {
        Bitmap b = null;
        File file = ImageStorage.getImage("/" + imagename + ".jpg");
        String path = file.getAbsolutePath();

        if ((path != null) && file.exists())
            try {
                b = BitmapFactory.decodeFile(path);
            } catch (Exception ex){
                Log.e("ImageStorage", "File not found: " + imagename);
            }

        if (b == null || b.equals("")) {
            return false;
        }
        return true;
    }
}
