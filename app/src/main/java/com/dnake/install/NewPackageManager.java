package com.dnake.install;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.Iterator;
import java.util.List;

public class NewPackageManager {
    private Context mContext;
    private PackageManager mPackageManager;
    private NewPackageManager.IAppObserver mDefaultAppObserver;
    private NewPackageManager.IPackageInstall mPackageInstaller;

    public NewPackageManager(Context context) {
        this.mContext = context.getApplicationContext();
        this.mPackageManager = context.getPackageManager();
        this.mDefaultAppObserver = new NewPackageManager.DefaultAppObserver();
        if (android.os.Build.VERSION.SDK_INT > 27) {
            this.mPackageInstaller = new NewPackageInstaller(this.mContext);
        }

    }

    public boolean installPackage(String appFilePath, NewPackageManager.IInstallObserver installObserver) {
        return this.mPackageInstaller.installPackage(appFilePath, installObserver);
    }


    public static boolean isAvailable(PackageManager packageManager, String packageName) {
        List<PackageInfo> infos = packageManager.getInstalledPackages(0);
        Iterator infoIterator = infos.iterator();

        PackageInfo info;
        do {
            if (!infoIterator.hasNext()) {
                return false;
            }

            info = (PackageInfo) infoIterator.next();
        } while (!info.packageName.equals(packageName));

        return true;
    }


    public interface IPackageInstall {
        boolean installPackage(String var1, NewPackageManager.IInstallObserver var2);
    }

    public interface IDeleteObserver {
        void onPackageDeleted(String var1, int var2, String var3);
    }

    public interface IInstallObserver {
        void onPackageInstalled(String var1, int var2, String var3);
    }

    public interface IAppObserver extends NewPackageManager.IInstallObserver, NewPackageManager.IDeleteObserver {
    }

    public static class DefaultAppObserver implements NewPackageManager.IAppObserver {
        public static final String INSTALL_TAG = "install";
        public static final String DELETE_TAG = "delete";

        public DefaultAppObserver() {
        }

        public void onPackageInstalled(String packageName, int returnCode, String msg) {
            if (returnCode == 1) {
                Log.d("install", "App install success: " + packageName);
            } else {
                Log.d("install", "Apk install fail! package:" + packageName + ".   Error code:" + returnCode + "," + msg);
            }

        }

        public void onPackageDeleted(String packageName, int returnCode, String msg) {
            if (returnCode == 1) {
                Log.d("delete", "App delete success:" + packageName);
            } else {
                Log.d("install", "Apk delete fail! package:" + packageName + ".   Error code:" + returnCode + "," + msg);
            }

        }
    }
}
