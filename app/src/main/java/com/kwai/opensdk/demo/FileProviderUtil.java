package com.kwai.opensdk.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import com.kwai.opensdk.sdk.model.base.BaseReq;
import com.kwai.opensdk.sdk.openapi.IKwaiOpenAPI;
import com.kwai.opensdk.sdk.utils.KwaiPlatformUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

import androidx.annotation.WorkerThread;
import androidx.core.content.FileProvider;

public class FileProviderUtil {

  private static final String FILEPROVIDER_POSTFIX = ".fileprovider";

  @WorkerThread
  public static File copyFileToShareDir(Context context, File shareFile) {
    @SuppressLint("SimpleDateFormat")
    SimpleDateFormat dateFormat = new SimpleDateFormat();
    dateFormat.applyPattern("yyyy-MM-dd_HH-mm-ss_");
    String name = dateFormat.format(new Date()) + shareFile.getName();
    File destFile = new File(getDefaultShareDir(context), name);
    try {
      if (destFile.exists()) {
        destFile.delete();
      }
      copyFile(shareFile, destFile);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return destFile;
  }

  private static File getDefaultShareDir(Context context) {
    File result = null;
    if (!(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()))) {
      result = context.getCacheDir();
      if (result == null) {
        result =
            new File(Environment.getDataDirectory().getPath() + "/data/" + context.getPackageName() + "/cache/");
      }
    } else if (Build.VERSION.SDK_INT >= 29) {
      result = context.getExternalMediaDirs()[0];
    } else {
      try {
        result = context.getExternalCacheDir();
      } catch (NullPointerException e) {
        // 一些机型可能会在拿externalCacheDir的时候崩溃
        e.printStackTrace();
      }
      if (result == null) {
        result =
            new File(Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + context.getPackageName() + "/cache/");
      }
    }
    try {
      result.mkdirs();
    } catch (Exception e) {
      e.printStackTrace();
    }
    return result;
  }

  private static void copyFile(File source, File dest) throws IOException {
    FileChannel inputChannel = null;
    FileChannel outputChannel = null;
    try {
      inputChannel = new FileInputStream(source).getChannel();
      outputChannel = new FileOutputStream(dest).getChannel();
      outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
    } finally {
      if (inputChannel != null) {
        inputChannel.close();
      }
      if (outputChannel != null) {
        outputChannel.close();
      }
    }
  }

  public static String generateFileUriPath(Activity activity, File file, BaseReq req,
      IKwaiOpenAPI kwaiOpenAPI) {
    String filePath = null;
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
          && kwaiOpenAPI.isAppSupportUri(activity.getApplicationContext(), req)) {
        String providerName =  activity.getPackageName() + FILEPROVIDER_POSTFIX;
        Uri fileUri = FileProvider.getUriForFile(activity.getApplicationContext(),
            providerName, file);
        activity.grantUriPermission(KwaiPlatformUtil.getPackageNameByReq(activity, req), fileUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION);
        filePath = fileUri.toString();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    return filePath;
  }
}
