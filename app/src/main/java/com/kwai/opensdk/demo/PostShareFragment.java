package com.kwai.opensdk.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.kwai.opensdk.sdk.constants.KwaiOpenSdkCmdEnum;
import com.kwai.opensdk.sdk.constants.KwaiOpenSdkConstants;
import com.kwai.opensdk.sdk.model.postshare.AICutMedias;
import com.kwai.opensdk.sdk.model.postshare.MultiMediaClip;
import com.kwai.opensdk.sdk.model.postshare.PostShareMediaInfo;
import com.kwai.opensdk.sdk.model.postshare.SinglePictureEdit;
import com.kwai.opensdk.sdk.model.postshare.SinglePicturePublish;
import com.kwai.opensdk.sdk.model.postshare.SingleVideoClip;
import com.kwai.opensdk.sdk.model.postshare.SingleVideoEdit;
import com.kwai.opensdk.sdk.model.postshare.SingleVideoPublish;
import com.kwai.opensdk.sdk.openapi.IKwaiOpenAPI;
import com.kwai.opensdk.sdk.openapi.KwaiOpenAPI;
import com.kwai.opensdk.sdk.utils.LogUtil;
import com.kwai.opensdk.sdk.utils.VerifyAppUtil;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;

public class PostShareFragment extends Fragment {
  private static final String TAG = "PostShareFragment";

  private static final int REQUEST_CODE_IMAGE = 10;
  private static final int REQUEST_CODE_SINGLE_VIDEO_COVER = 11;
  private static final int REQUEST_CODE_SINGLE_VIDEO = 12;
  private static final int REQUEST_CODE_MULTI_VIDEO = 13;

  private TextView mSdkInfoTv;
  private View mShowOptionData;
  private View mTagInput;
  private TextView mTagList;
  private View mSelectPic;
  private TextView mPicPath;
  private View mSingleVideoCoverPic;
  private TextView mSingleVideocoverPath;
  private View mSingleVideoPic;
  private TextView mSingleVideoPath;
  private TextView mMultiVideoPath;
  private View multiSelectVideo;
  private View mPublishMultiVideo;
  private View mPublishAICutVideo;
  private Spinner mSpinnerSharePage;
  private Spinner mPicSpinnerSharePage;
  private Spinner mPackageSpinner;
  private View mTagClear;
  private View mMultiVideoClear;
  private EditText mExtraEdit;
  private CheckBox mDisableFallBack;
  private View mMultiSelectPic;
  private TextView mCallbackTv;

  private ArrayList<String> mTags = new ArrayList<>();
  private ArrayList<String> mMultiVideos = new ArrayList<>();
  boolean mIsSelectePicIng = false;

  private IKwaiOpenAPI mKwaiOpenAPI;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View view = inflater.inflate(R.layout.post_share_layout, container, false);
    mSpinnerSharePage = view.findViewById(R.id.spinner_share_page);
    mSdkInfoTv = view.findViewById(R.id.sdk_info);
    mSelectPic = view.findViewById(R.id.select_pic);
    mShowOptionData = view.findViewById(R.id.show_option_data);
    mTagInput = view.findViewById(R.id.tag_input);
    mTagList = view.findViewById(R.id.tag_list);
    mPicPath = view.findViewById(R.id.pic_path);
    mSingleVideoCoverPic = view.findViewById(R.id.cover_pic);
    mSingleVideocoverPath = view.findViewById(R.id.cover_path);
    mSingleVideoPic = view.findViewById(R.id.select_video);
    mSingleVideoPath = view.findViewById(R.id.video_path);
    mMultiVideoPath = view.findViewById(R.id.multi_video_path);
    multiSelectVideo = view.findViewById(R.id.multi_select_video);
    mPublishMultiVideo = view.findViewById(R.id.publish_multi_video);
    mPublishAICutVideo = view.findViewById(R.id.publish_ai_cut_video);
    mTagClear = view.findViewById(R.id.tag_clear);
    mMultiVideoClear = view.findViewById(R.id.multi_video_clear);
    mMultiSelectPic = view.findViewById(R.id.multi_select_pic);
    mPicSpinnerSharePage = view.findViewById(R.id.picSpinnerSharePage);
    mExtraEdit = view.findViewById(R.id.user_info);
    mDisableFallBack = view.findViewById(R.id.disableFallBack);
    mPackageSpinner = view.findViewById(R.id.spinner_package);
    mCallbackTv = view.findViewById(R.id.callback_tips);

    mExtraEdit.setFocusable(true);

    //基本信息
    mSdkInfoTv.setText("open sdk version:" + KwaiOpenSdkConstants.SDK_VERSION + "\n" +
      "快手是否安装:" + VerifyAppUtil.isKwaiAppInstalled(getContext()));

    //查看配置信息
    mShowOptionData.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        new AlertDialog.Builder(getContext())
          .setMessage(queryShareAuth(getContext()))
          .setNegativeButton("关闭", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
          }).create().show();
      }
    });

    //添加标签 tag
    mTagInput.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("请输入需要添加的tags");

        final EditText input = new EditText(getContext());
        builder.setView(input);
        builder.setPositiveButton("添加", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
            mTags.add(input.getText().toString());
            StringBuilder stringBuilder = new StringBuilder();
            for (String item : mTags) {
              stringBuilder.append("#").append(item);
            }
            mTagList.setText(stringBuilder);
          }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            dialog.cancel();
          }
        });
        builder.show();
      }
    });


    //清除tag
    mTagClear.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mTags.clear();
        mTagList.setText("");
      }
    });

    //选择并发布图片
    mSelectPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mIsSelectePicIng = true;
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(albumIntent, REQUEST_CODE_IMAGE);
      }
    });

    //选择封面图片
    mSingleVideoCoverPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mIsSelectePicIng = true;
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(albumIntent, REQUEST_CODE_SINGLE_VIDEO_COVER);
      }
    });

    //选择单个视频并发布
    mSingleVideoPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mIsSelectePicIng = true;
        Intent albumIntent = new Intent(Intent.ACTION_PICK);
        albumIntent.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*");
        startActivityForResult(albumIntent, REQUEST_CODE_SINGLE_VIDEO);
      }
    });

    //增加多段视频
    multiSelectVideo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mIsSelectePicIng = true;
        PictureSelector.create(PostShareFragment.this)
          .openGallery(PictureMimeType.ofVideo())
          .forResult(REQUEST_CODE_MULTI_VIDEO);
      }
    });

    //增加多段图片
    mMultiSelectPic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mIsSelectePicIng = true;
        PictureSelector.create(PostShareFragment.this)
          .openGallery(PictureMimeType.ofImage())
          .forResult(REQUEST_CODE_MULTI_VIDEO);
      }
    });

    //发布多段视频
    mPublishMultiVideo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        clipMultiMedia(mMultiVideos);
      }
    });

    //发布智能剪辑视频
    mPublishAICutVideo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        aiCutMedias(mMultiVideos);
      }
    });

    //清除所有多段视频
    mMultiVideoClear.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mMultiVideos.clear();
        mMultiVideoPath.setText("");
      }
    });


    mKwaiOpenAPI = new KwaiOpenAPI(getContext());
    // 使用sdk的loading界面，设置false第三方应用可以自定义实现loading
    mKwaiOpenAPI.setShowDefaultLoading(false);
    // 使用sdk的loading界面，设置true则自动跳转应用市场下载
    mKwaiOpenAPI.setAutoGotoMarket(true, true);
    registerListener();
    // sdk的log设置
    LogUtil.setLogLevel(LogUtil.LOG_LEVEL_ALL);
    return view;
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    mIsSelectePicIng = false;
    if (resultCode == Activity.RESULT_OK) {
      if (requestCode == REQUEST_CODE_IMAGE) {
        Uri uri = data.getData();//从图片的Uri是以cotent://格式开头的
        //获取到图片
        try {
          File file = new File(parseFilePath(getActivity(), uri));
          //mPicPath.setText(file.getAbsolutePath());
          KwaiOpenSdkCmdEnum cmd = getPicShareCmd(mPicSpinnerSharePage.getSelectedItemPosition());
          if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_PUBLISH)) {
            publishPicture(file);
          } else if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_EDIT)) {
            editPicture(file);
          }

        } catch (Exception e) {
          Log.e("testData", e.getMessage());
        }
      } else if (requestCode == REQUEST_CODE_SINGLE_VIDEO_COVER) {
        Uri uri = data.getData();//从图片的Uri是以cotent://格式开头的
        //获取到图片
        File file = new File(parseFilePath(getActivity(), uri));
        mSingleVideocoverPath.setText(file.getAbsolutePath());
      } else if (requestCode == REQUEST_CODE_SINGLE_VIDEO) {
        Uri uri = data.getData();//从图片的Uri是以cotent://格式开头的
        //获取到图片
        try {
          File file = new File(parseFilePath(getActivity(), uri));
          //mSingleVideoPath.setText(file.getAbsolutePath());
          KwaiOpenSdkCmdEnum cmd = getVideoShareCmd(mSpinnerSharePage.getSelectedItemPosition());
          if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_PUBLISH)) {
            publishSingleVideo(file);
          } else if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_EDIT)) {
            editSingleVideo(file);
          } else if (cmd.equals(KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_CLIP)) {
            clipSingleVideo(file);
          }

        } catch (Exception e) {
          Log.e("testData", e.getMessage());
        }
      } else if (requestCode == REQUEST_CODE_MULTI_VIDEO) {
        List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
        if (selectList != null) {
          for (LocalMedia item : selectList) {
            String path = item.getRealPath();
            if (TextUtils.isEmpty(path)) {
              path = item.getPath();
            }
            mMultiVideos.add(path);
          }
        }
        mMultiVideoPath.setText("已选视频或图片数量:" + mMultiVideos.size());
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mKwaiOpenAPI.removeKwaiAPIEventListerer();
  }

  public String getPackageName(String defaultName) {
    //低版本没有mock 的必要
    if (mIsSelectePicIng || !VerifyAppUtil.validateKwaiApp(getContext())) {
      return defaultName;
    }
    if (mPackageSpinner != null) {
      if (mPackageSpinner.getSelectedItemPosition() == 1) {
        return "com.kwai.videoeditor";
      } else if (mPackageSpinner.getSelectedItemPosition() == 2) {
        return "com.kwai.m2u";
      }
    }
    return defaultName;
  }

  private String parseFilePath(Activity activity, Uri uri) {
    String[] filePathColumn = {MediaStore.Images.Media.DATA};
    Cursor cursor = null;
    String picturePath = null;
    try {
      cursor = activity.getContentResolver().query(uri, filePathColumn, null, null, null);
      cursor.moveToFirst();
      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      picturePath = cursor.getString(columnIndex);
    } catch (Exception e) {
      Log.e("AllInSdkUtil", e.getMessage());
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return picturePath;
  }

  //获取主app配置信息
  private String queryShareAuth(Context context) {
    if (context == null) {
      return null;
    }
    // 读取content provider之前确认是我们的app
    if (!VerifyAppUtil.isKwaiAppInstalled(context)) {
      return null;
    }
    String authConfig = "";
    try {
      Uri uri = Uri.parse("content://com.yxcorp.gifshow.ShareAuthProvider/data?appId=" + context.getPackageName());
      Cursor cursor = context.getContentResolver().query(uri, new String[]{"thirdPartyConfig"}, null, null, null);
      if (cursor != null) {
        try {
          if (cursor.moveToNext()) {
            authConfig = cursor.getString(cursor.getColumnIndex("thirdPartyConfig"));
          }
        } finally {
          try {
            cursor.close();
          } catch (Throwable e) {

          }
        }
      }
    } catch (Throwable e) {
    }
    return authConfig;
  }

  private KwaiOpenSdkCmdEnum getPicShareCmd(int position) {
    if (position == 0) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_EDIT;
    } else if (position == 1) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_PUBLISH;
    }
    return KwaiOpenSdkCmdEnum.CMD_SINGLE_PICTURE_EDIT;
  }

  private KwaiOpenSdkCmdEnum getVideoShareCmd(int position) {
    if (position == 0) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_CLIP;
    } else if (position == 1) {
      return KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_EDIT;
    }
    return KwaiOpenSdkCmdEnum.CMD_SINGLE_VIDEO_PUBLISH;
  }

  //发布图片
  public void publishPicture(File file) {
    SinglePicturePublish.Req req = new SinglePicturePublish.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SinglePicturePublish";

    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> imageFile = new ArrayList<>();
    imageFile.add(file.getAbsolutePath());
    req.mediaInfo.mMultiMediaAssets = imageFile;
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraEdit.getText().toString();
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  //编辑图片
  public void editPicture(File file) {
    SinglePictureEdit.Req req = new SinglePictureEdit.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SinglePictureEdit";

    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> imageFile = new ArrayList<>();
    imageFile.add(file.getAbsolutePath());
    req.mediaInfo.mMultiMediaAssets = imageFile;
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraEdit.getText().toString();
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  //发布单个视频
  public void publishSingleVideo(File file) {
    SingleVideoPublish.Req req = new SingleVideoPublish.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoPublish";

    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> imageFile = new ArrayList<>();
    imageFile.add(file.getAbsolutePath());
    req.mediaInfo.mMultiMediaAssets = imageFile;
    if (!TextUtils.isEmpty(mSingleVideocoverPath.getText())) {
      req.mCover = mSingleVideocoverPath.getText().toString();
    }
    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraEdit.getText().toString();
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  //编辑单个视频
  public void editSingleVideo(File file) {
    SingleVideoEdit.Req req = new SingleVideoEdit.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoEdit";

    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> imageFile = new ArrayList<>();
    imageFile.add(file.getAbsolutePath());
    req.mediaInfo.mMultiMediaAssets = imageFile;

    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraEdit.getText().toString();
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  //裁剪单个视频
  public void clipSingleVideo(File file) {
    SingleVideoClip.Req req = new SingleVideoClip.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoClip";

    req.mediaInfo = new PostShareMediaInfo();
    ArrayList<String> imageFile = new ArrayList<>();
    imageFile.add(file.getAbsolutePath());
    req.mediaInfo.mMultiMediaAssets = imageFile;

    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraEdit.getText().toString();
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  //clipMultiMedia
  public void clipMultiMedia(ArrayList<String> multiMedia) {
    MultiMediaClip.Req req = new MultiMediaClip.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "MultiMediaClip";

    req.mediaInfo = new PostShareMediaInfo();
    req.mediaInfo.mMultiMediaAssets = multiMedia;

    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraEdit.getText().toString();
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  //aiCutMedias
  public void aiCutMedias(ArrayList<String> multiMedia) {
    AICutMedias.Req req = new AICutMedias.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "AICutMedias";

    req.mediaInfo = new PostShareMediaInfo();
    req.mediaInfo.mMultiMediaAssets = multiMedia;

    if (!TextUtils.isEmpty(mTagList.getText().toString())) {
      req.mediaInfo.mTag = mTagList.getText().toString();
    }
    req.mediaInfo.mDisableFallback = mDisableFallBack.isChecked();
    if (!TextUtils.isEmpty(mExtraEdit.getText().toString())) {
      req.mediaInfo.mExtraInfo = mExtraEdit.getText().toString();
    }
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  private void registerListener() {
    mKwaiOpenAPI.addKwaiAPIEventListerer(resp -> {
      Log.i(TAG, "resp=" + resp);
      if (resp != null) {
        Log.i(TAG, "errorCode=" + resp.errorCode + ", errorMsg="
          + resp.errorMsg + ", cmd=" + resp.getCommand()
          + ", transaction=" + resp.transaction);
        mCallbackTv.setText("CallBackResult: errorCode=" + resp.errorCode + ", errorMsg="
            + resp.errorMsg + ", cmd=" + resp.getCommand()
            + ", transaction=" + resp.transaction);
      } else {
        mCallbackTv.setText("CallBackResult: resp is null");
      }
    });
  }
}
