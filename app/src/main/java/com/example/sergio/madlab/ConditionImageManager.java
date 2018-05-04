package com.example.sergio.madlab;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ConditionImageManager {

    private static final String filepath = "bookDir";

    public String saveToInternalStorage(Bitmap bitmapImage, String filename, Context applicationContext){
        ContextWrapper contextWrapper = new ContextWrapper(applicationContext);
        File directory = contextWrapper.getDir(filepath, Context.MODE_PRIVATE);
        File imagePath = new File(directory, filename);

        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(imagePath);
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
        } catch (Exception e){
            e.getMessage();
        } finally {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.getMessage();
            }
        }

        return directory.getAbsolutePath();
    }

    public Bitmap loadImageFromInternalStorage(String path, String filename){
        try{
            File file = new File(path, filename);
            return BitmapFactory.decodeStream(new FileInputStream(file));
        } catch (FileNotFoundException e){
            e.getMessage();
        }
        return null;
    }

    public Uri getUri(String path, String filename){
        File image = new File(path, filename);
        return Uri.fromFile(image);
    }

}
