package com.dnake.install;

import android.content.Context;
import android.util.Log;

public class ApkUtils {
    /**
     * Android9.0静默安装
     */
    /**
     * install app
     * the permission {#android.permission.INSTALL_PACKAGES} is only granted to
     * system apps
     * <p>
     * add #android:sharedUserId="android.uid.system"# to manifest
     *
     * @param appFilePath
     */
    public static boolean installPackage(Context context, String appFilePath) {
        NewPackageManager mPackageManager = new NewPackageManager(context);
        /**
         *@param appPath 需要安装的APP的绝对路径
         */
        boolean ret = mPackageManager.installPackage(appFilePath, new NewPackageManager.IInstallObserver() {
            /**
             *@param packageName 安装APP的包名
             *@param returnCode 0为安装成功，其余值为安装失败
             *@param errorMsg 失败的错误信息
             */
            @Override
            public void onPackageInstalled(String packageName, int returnCode, String errorMsg) {
                Log.i("install", "packageName：" + packageName + ",returnCode：" + returnCode + ",errorMsg：" + errorMsg);
            }
        });
        Log.i("install", "installPackage ret = 0为安装成功 ret " + ret);
        return ret;
    }

    public static String installPackageReturnPkgName(Context context, String appFilePath) {
        NewPackageManager mPackageManager = new NewPackageManager(context);
        final String[] pkgName = new String[1];
        /**
         *@param appPath 需要安装的APP的绝对路径
         */
        boolean ret = mPackageManager.installPackage(appFilePath, new NewPackageManager.IInstallObserver() {
            /**
             *@param packageName 安装APP的包名
             *@param returnCode 0为安装成功，其余值为安装失败
             *@param errorMsg 失败的错误信息
             */
            @Override
            public void onPackageInstalled(String packageName, int returnCode, String errorMsg) {
                pkgName[0] = packageName;
                Log.i("install", "packageName：" + packageName + ",returnCode：" + returnCode + ",errorMsg：" + errorMsg);
            }
        });
        Log.i("install", "installPackage ret = 0为安装成功 ret " + ret);
        if (ret) {
            return pkgName[0];
        } else {
            return "";
        }
    }
}
