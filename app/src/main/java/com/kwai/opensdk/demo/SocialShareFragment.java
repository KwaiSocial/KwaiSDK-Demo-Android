package com.kwai.opensdk.demo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.kwai.auth.common.InternalResponse;
import com.kwai.auth.common.KwaiConstants;
import com.kwai.opensdk.auth.IKwaiAuthListener;
import com.kwai.opensdk.auth.IKwaiOpenSdkAuth;
import com.kwai.opensdk.auth.KwaiOpenSdkAuth;
import com.kwai.opensdk.sdk.model.base.BaseResp;
import com.kwai.opensdk.sdk.model.socialshare.KwaiMediaMessage;
import com.kwai.opensdk.sdk.model.socialshare.KwaiWebpageObject;
import com.kwai.opensdk.sdk.model.socialshare.ShareMessage;
import com.kwai.opensdk.sdk.model.socialshare.ShareMessageToBuddy;
import com.kwai.opensdk.sdk.model.socialshare.ShowProfile;
import com.kwai.opensdk.sdk.openapi.IKwaiAPIEventListener;
import com.kwai.opensdk.sdk.openapi.IKwaiOpenAPI;
import com.kwai.opensdk.sdk.openapi.KwaiOpenAPI;
import com.kwai.opensdk.sdk.utils.LogUtil;
import com.kwai.opensdk.sdk.utils.NetworkUtil;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class SocialShareFragment extends Fragment {

  private static final String TAG = "SocialShareFragment";
  private static final String URL_HOST = "https://open.kuaishou.com";
  private static final int NETWORK_MAX_RETRY_TIMES = 5;
  // 测试demo用的的appId和appSecret，第三方客户端请使用分配的数据
  private static final String APP_ID = "ks703687443040312600";
  private static final String APP_SECRET = "cAQmb4gjTeCW3Sf4enQDbQ";

  private String mOpenId;

  private TextView mOpenIdTv;
  private TextView mCallbackTv;
  private IKwaiOpenAPI mKwaiOpenAPI;
  private IKwaiOpenSdkAuth mKwaiOpenSdkAuth;
  private CheckBox mKwaiCheck;
  private CheckBox mNebulaCheck;
  private TextView mLoginPlatform;
  private ArrayList<String> platformList = new ArrayList<>(2);

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View view = inflater.inflate(R.layout.social_share_layout, container, false);
    mOpenIdTv = view.findViewById(R.id.open_id);
    mCallbackTv = view.findViewById(R.id.callback_tips);
    mLoginPlatform = view.findViewById(R.id.login_platform);

    view.findViewById(R.id.login_app_btn).setOnClickListener(v -> appLogin());

    view.findViewById(R.id.share).setOnClickListener(v -> shareMessage());

    view.findViewById(R.id.shareToBuddy).setOnClickListener(v -> shareMessageToBuddy());

    view.findViewById(R.id.show_profile).setOnClickListener(v -> showProfile());

    view.findViewById(R.id.set_target_openId).setOnClickListener(v -> setTargetOpenId());

    mKwaiCheck = view.findViewById(R.id.kwai_app_checkbox);
    mKwaiCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      platformList.clear();
      if (compoundButton.isChecked()) {
        if (mNebulaCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
          platformList.add(KwaiConstants.Platform.KWAI_APP);
        } else {
          platformList.add(KwaiConstants.Platform.KWAI_APP);
        }
      } else {
        if (mNebulaCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
        }
      }
      refreshLoginText();
    });

    mNebulaCheck = view.findViewById(R.id.nebula_app_checkbox);
    mNebulaCheck.setOnCheckedChangeListener((compoundButton, b) -> {
      platformList.clear();
      if (compoundButton.isChecked()) {
        if (mKwaiCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.KWAI_APP);
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
        } else {
          platformList.add(KwaiConstants.Platform.NEBULA_APP);
        }
      } else {
        if (mKwaiCheck.isChecked()) {
          platformList.add(KwaiConstants.Platform.KWAI_APP);
        }
      }
      refreshLoginText();
    });

    mOpenIdTv.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (getActivity() == null || getActivity().isFinishing()) {
          return true;
        }
        if (!TextUtils.isEmpty(mOpenId)) {
          // 获取剪贴板管理器：
          ClipboardManager cm = (ClipboardManager) (getActivity().getSystemService(getActivity().CLIPBOARD_SERVICE));
          // 创建普通字符型ClipData
          ClipData data = ClipData.newPlainText("OpenId", mOpenId);
          // 将ClipData内容放到系统剪贴板里。
          cm.setPrimaryClip(data);
          Toast.makeText(getActivity(), "已复制到剪切板", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(getActivity(), "请先授权获取openId", Toast.LENGTH_SHORT).show();
        }
        return true;
      }
    });
    mKwaiOpenAPI = new KwaiOpenAPI(getContext());
    mKwaiOpenSdkAuth = new KwaiOpenSdkAuth();
    // 使用sdk的loading界面，设置false第三方应用可以自定义实现loading
    mKwaiOpenAPI.setShowDefaultLoading(true);
    registerListener();
    // sdk的log设置
    LogUtil.setLogLevel(LogUtil.LOG_LEVEL_ALL);
    return view;
  }

  private void refreshLoginText() {
    StringBuffer buffer = new StringBuffer();
    for (String platform : platformList) {
      buffer.append(platform);
      buffer.append(" ");
    }
    mLoginPlatform.setText(buffer.toString());
  }

  private void registerListener() {

    mKwaiOpenAPI.addKwaiAPIEventListerer(new IKwaiAPIEventListener() {

      @Override
      public void onRespResult(@NonNull BaseResp resp) {
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
      }
    });
  }

  // 获取openId的网络请求，为了安全性，建议放在第三方客户端的服务器中，由第三方服务器实现这个请求接口后将openid返回第三方客户端
  private String getOpenIdByNetwork(final String code) {
    String url = getRequestOpenIdUrl("code", APP_ID, APP_SECRET, code);
    String result = NetworkUtil.get(url, null, null);
    String openId = null;
    try {
      LogUtil.i(TAG, "result=" + result);
      JSONObject obj = new JSONObject(result);
      openId = obj.getString("open_id");
      LogUtil.i(TAG, "openId=" + openId);
    } catch (Throwable t) {
      LogUtil.e(TAG, "getOpenId exception");
    }
    return openId;
  }

  private String getRequestOpenIdUrl(String grantType, String appId, String appKey, String code) {
    StringBuilder builder = new StringBuilder();
    builder.append(URL_HOST);
    builder.append("/oauth2/access_token?");
    builder.append("grant_type=" + grantType);
    builder.append("&app_id=" + appId);
    builder.append("&app_secret=" + appKey);
    builder.append("&code=" + code);
    return builder.toString();
  }

  // app调起登录
  public void appLogin() {

    IKwaiAuthListener kwaiAuthListener = new IKwaiAuthListener() {

      @Override
      public void onSuccess(InternalResponse response) {
        new Thread(new Runnable() {
          public void run() {
            String result = null;
            int retry = 0;
            while (null == result && retry < NETWORK_MAX_RETRY_TIMES) {
              result = getOpenIdByNetwork(response.getCode());
              retry++;
              LogUtil.i(TAG, "retry=" + retry);
            }
            final String openId = result;
            Handler mainHandler = new Handler(Looper.getMainLooper());
            mainHandler.post(new Runnable() {
              @Override
              public void run() {
                mOpenId = openId;
                if (TextUtils.isEmpty(mOpenId)) {
                  mOpenIdTv.setText("当前openId:" + "get openId error");
                } else {
                  mOpenIdTv.setText("当前openId:" + mOpenId);
                }
              }
            });
          }
        }).start();
      }

      @Override
      public void onFailed(String state, int errCode, String errMsg) {
        mOpenIdTv.setText("code error is " + errCode + " and msg is " + errMsg);
      }

      @Override
      public void onCancel() {
        mOpenIdTv.setText("login is canceled");
      }
    };

    mKwaiOpenSdkAuth.sendAuthReqToKwai(getActivity(), Config.SCOPE, Config.STATE,
        kwaiAuthListener, new String[]{KwaiConstants.Platform.KWAI_APP});
  }

  // 通过选择人或者群组分享私信
  public void shareMessage() {
    // base params
    ShareMessage.Req req = new ShareMessage.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "sharemessage";

    // business params
    req.message = new KwaiMediaMessage();
    req.message.mediaObject = new KwaiWebpageObject();
    ((KwaiWebpageObject) req.message.mediaObject).webpageUrl =
        "https://blog.csdn.net/a249900679/article/details/51386660";
    req.message.title = "test";
    req.message.description = "webpage test share, hahahah";
    Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    b.compress(Bitmap.CompressFormat.PNG, 100, baos);
    req.message.thumbData = baos.toByteArray();

    // send request
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 通过TargetOpenId分享私信给个人
  public void shareMessageToBuddy() {
    if (TextUtils.isEmpty(HistoryOpenIdActivity.sTargetOpenId)) {
      Toast.makeText(getActivity(), "sTargetOpenId is null, 请先设置", Toast.LENGTH_SHORT).show();
      return;
    }

    ShareMessageToBuddy.Req req = new ShareMessageToBuddy.Req();
    req.openId = mOpenId;
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "sharemessageToBuddy";

    req.targetOpenId = HistoryOpenIdActivity.sTargetOpenId;
    req.message = new KwaiMediaMessage();
    req.message.mediaObject = new KwaiWebpageObject();
    ((KwaiWebpageObject) req.message.mediaObject).webpageUrl =
        "https://blog.csdn.net/a249900679/article/details/51386660";
    req.message.title = "test";
    req.message.description = "webpage test share, hahahah";
    Bitmap b = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    b.compress(Bitmap.CompressFormat.PNG, 100, baos);
    req.message.thumbData = baos.toByteArray();
    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  // 打开TargetOpenId指向的个人主页
  public void showProfile() {
    if (TextUtils.isEmpty(HistoryOpenIdActivity.sTargetOpenId) && getActivity() != null && !getActivity().isFinishing()) {
      Toast.makeText(getActivity(), "sTargetOpenId is null, 请先设置", Toast.LENGTH_SHORT).show();
      return;
    }

    ShowProfile.Req req = new ShowProfile.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "showProfile_1";

    req.targetOpenId = HistoryOpenIdActivity.sTargetOpenId;

    mKwaiOpenAPI.sendReq(req, getActivity());
  }

  public void setTargetOpenId() {
    Intent intent = new Intent(getActivity(), HistoryOpenIdActivity.class);
    this.startActivity(intent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mKwaiOpenAPI.removeKwaiAPIEventListerer();
  }
}
