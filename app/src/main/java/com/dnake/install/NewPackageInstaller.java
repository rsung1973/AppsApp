package com.dnake.install;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInstaller;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class NewPackageInstaller implements NewPackageManager.IPackageInstall {
    private Context mContext;
    private String mAction;

    public NewPackageInstaller(Context context) {
        this(context, "com.snack.content.INSTALL");
    }

    public NewPackageInstaller(Context context, String action) {
        this.mContext = context;
        this.mAction = action;
    }

    private int createSession(PackageInstaller packageInstaller, PackageInstaller.SessionParams sessionParams) {
        int sessionId = -1;

        try {
            sessionId = packageInstaller.createSession(sessionParams);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return sessionId;
    }

    private boolean copyInstallFile(PackageInstaller packageInstaller, int sessionId, String appPath) {
        InputStream inputStream = null;
        OutputStream outputStream = null;
        PackageInstaller.Session session = null;
        boolean isSuccess = false;
        File appFile = new File(appPath);

        try {
            inputStream = new FileInputStream(appFile);
            session = packageInstaller.openSession(sessionId);
            outputStream = session.openWrite("my_app_session", 0L, appFile.length());
            int total = 0;

            int len;
            for (byte[] buffer = new byte[4096]; (len = inputStream.read(buffer)) != -1; total += len) {
                outputStream.write(buffer, 0, len);
            }

            session.fsync(outputStream);
            boolean var12 = true;
            return var12;
        } catch (IOException var16) {
            var16.printStackTrace();
        } finally {
            this.close(outputStream);
            this.close(inputStream);
            this.close(session);
        }

        return false;
    }

    @SuppressLint("WrongConstant")
    private boolean installPackage(PackageInstaller packageInstaller, int sessionId, NewPackageManager.IInstallObserver observer) {
        PendingIntent pendingIntent = null;
        PackageInstaller.Session session = null;

        try {
            session = packageInstaller.openSession(sessionId);
            IntentFilter ifliter = new IntentFilter();
            ifliter.addAction(this.mAction);
            NewPackageInstaller.InstallReceiver installReceiver = new NewPackageInstaller.InstallReceiver(observer);
            this.mContext.registerReceiver(installReceiver, ifliter);
            Intent intent = new Intent(this.mAction);
            pendingIntent = PendingIntent.getBroadcast(this.mContext, sessionId, intent, 134217728);
            session.commit(pendingIntent.getIntentSender());
            boolean var9 = true;
            return var9;
        } catch (IOException var13) {
            var13.printStackTrace();
        } finally {
            this.close(session);
        }

        return false;
    }

    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

    }

    public boolean installPackage(String appPath, NewPackageManager.IInstallObserver observer) {
        File appFile = new File(appPath);
        if (appFile.exists() && appFile.isFile() && appFile.canRead()) {
            PackageInstaller packageInstaller = this.mContext.getPackageManager().getPackageInstaller();
            PackageInstaller.SessionParams sessionParams = new PackageInstaller.SessionParams(1);
            sessionParams.setSize(appFile.length());
            int sessionId = this.createSession(packageInstaller, sessionParams);
            if (sessionId != -1) {
                boolean isCopySuccess = this.copyInstallFile(packageInstaller, sessionId, appPath);
                if (isCopySuccess) {
                    return this.installPackage(packageInstaller, sessionId, observer);
                }
            }

            return false;
        } else {
            return false;
        }
    }

    class InstallReceiver extends BroadcastReceiver {
        private NewPackageManager.IInstallObserver mInstallObserver;

        public InstallReceiver(NewPackageManager.IInstallObserver observer) {
            this.mInstallObserver = observer;
        }

        public void onReceive(Context context, Intent intent) {
            Log.d("install", "receiver :" + intent.getAction() + ",intent.toString:" + intent.toString());
            String pkgName = intent.getStringExtra("android.content.pm.extra.PACKAGE_NAME");
            String result = intent.getStringExtra("android.content.pm.extra.STATUS_MESSAGE");
            int resultCode = intent.getIntExtra("android.content.pm.extra.LEGACY_STATUS", 0);
            if (this.mInstallObserver != null) {
                this.mInstallObserver.onPackageInstalled(pkgName, resultCode, result);
            }

            context.unregisterReceiver(this);
        }
    }
}
