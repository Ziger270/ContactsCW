package com.mirea.kt.ribo.contactscw;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import java.io.File;

public class SetAvatar implements Runnable {
    private Person person;
    private ImageView imageView;

    public SetAvatar(Person person, ImageView imageView) {
        this.person = person;
        this.imageView = imageView;
    }

    @Override
    public void run(){
        String imagename = person.getPhone();
        if(ImageStorage.checkifImageExists(imagename)) {
            File file = ImageStorage.getImage("/"+imagename+".jpg");
            String path = file.getAbsolutePath();
            if (path != null){
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(bitmap);
            }
        } else {
            new GetImages(person.getAvatar(), imageView, imagename).execute() ;
        }
    }
}