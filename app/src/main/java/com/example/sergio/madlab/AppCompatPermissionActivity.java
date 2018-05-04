package com.example.sergio.madlab;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseIntArray;
import android.view.View;

import com.example.sergio.madlab.R;

public abstract class AppCompatPermissionActivity extends AppCompatActivity {

    private SparseIntArray mErrorString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mErrorString = new SparseIntArray();

    }

    public abstract void onPermissionGranted(int requestCode);

    public void requestAppPermission(final String[] requestedPermissions, final int stringId, final int requestCode){
        mErrorString.put(requestCode, stringId);

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        boolean showRequestPermissions = false;
        for(String permission : requestedPermissions){
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            showRequestPermissions = showRequestPermissions || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);

        }

        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            if(showRequestPermissions){
                Snackbar.make(findViewById(android.R.id.content), stringId, Snackbar.LENGTH_INDEFINITE).setAction("grant", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ActivityCompat.requestPermissions(AppCompatPermissionActivity.this, requestedPermissions, requestCode);
                    }
                }).show();
            } else {
                ActivityCompat.requestPermissions(this, requestedPermissions, requestCode);
            }
        } else {
            onPermissionGranted(requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = PackageManager.PERMISSION_GRANTED;

        for (int permission : grantResults){
            permissionCheck = permissionCheck + permission;
        }

        if(grantResults.length > 0 && PackageManager.PERMISSION_GRANTED == permissionCheck){
            onPermissionGranted(requestCode);
        } else {
            Snackbar.make(findViewById(android.R.id.content), mErrorString.get(requestCode),
                    Snackbar.LENGTH_INDEFINITE).setAction("enable", new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    startActivity(intent);
                }
            }).show();
        }

    }
}
