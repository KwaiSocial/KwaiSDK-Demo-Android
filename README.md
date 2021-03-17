# 一、设计目标
开放平台sdk的设计目标是：第三方应用使用开放平台sdk能够方便、快速的调起快手主app的功能页面，从而将第三方内容发布到快手平台上或者使用快手APP提供的其他开放功能。

| 开放能力 |  |  |  |  |  |  |  |
| ------ | ------ | ------ | ------ | ------ | ------ | ------ | ------ |
| 账号授权 | 快手主站授权 | 快手极速版授权 |  |  |  | |  |
| 社交功能 | 分享私信 | 分享私信到指定人 | 打开指定用户主页 |  |  | |  |
| 生产功能 | 单图编辑 | 单图发布 | 单视频编辑 | 单视频裁剪 | 单视频发布 |多视频图片裁剪 | 智能裁剪视频 |

# 二、项目地址库

准备工作
开发者需要在快手开放平台完成注册，新建一个网站应用，并获取应用标识appId 和 appSecret，详细参考申请注册流程，官网地址：https://open.kuaishou.com/platform

外网访问git库：https://github.com/KwaiSocial/KwaiSDK-Demo-Android

快手内网访问git库：http://git.corp.kuaishou.com/android/open_sdk_social

# 三、第三方接入说明
## 1、接入aar

- 版本要求 minsdkversion:19

- 快手外网引用aar：外网版本仅提供带auth认证的aar(当前最新版本：3.0.2)
```
dependencies {
   // 版本号建议设置成最新的版本
   implementation "com.github.kwaisocial:kwai-opensdk-withauth:3.0.2"
}
```

- 快手内网引用aar:

  配置maven库
```
allprojects {
    repositories {
	maven {
           url "http://nexus.corp.kuaishou.com:88/nexus/content/repositories/releases/" }
    }
}
```

仅使用auth认证的依赖，支持快手登录认证获取openId(当前最新版本：3.0.0)
```
dependencies {
   // 版本号请设置最新的版本
   implementation "com.kwai.opensdk.sdk:kwai-auth:3.0.0"
}

```
不带auth认证的依赖，需要第三方应用自行获取openId并且已经登录快手(当前最新版本：3.0.1)
```
dependencies {
   // 版本号请设置最新的版本
   implementation "com.kwai.opensdk.sdk:kwai-opensdk:3.0.1"
}

```
合并上面两个依赖库，带有auth认证逻辑的依赖，可以通过提供的接口操作登录并获取openId(当前最新版本：3.0.2)
```
dependencies {
   // 版本号请设置最新的版本
   implementation "com.kwai.opensdk.sdk:kwai-opensdk-withauth:3.0.2"
}
```
- 混淆配置
```
-keep class com.kwai.opensdk.sdk.** {*;}
-keep class com.kwai.auth.** {*;}

如果第三方应用需要混淆资源，请将下面文件加入白名单不要混淆防止找不到资源
/kwai-opensdk/src/main/res/layout/activity_loading.xml
/kwai-auth/src/main/res/layout/activity_kwai_login_h5.xml
```

## 2、api使用说明
### （1）应用配置
接入方应用需要在build.gradle中配置如下信息（必须）
```
android {
    defaultConfig {
        applicationId "com.kwai.chat.demo" // 接入方的包名
        manifestPlaceholders = [
            "KWAI_APP_ID": "ks703687443040312600", // 申请分配的appId
            "KWAI_SCOPE" : "user_info" // 账号授权需要申请的scope权限，多个scope可以使用","分割，代表需要用户授权什么能力
        ]
    }
}
```
### （2）登录认证并获取用户的openId
接入方app需要通过渠道获取分配给接入方应用的appId，当使用需要openId的功能时，请先获取必须的参数openId，当前只有分享私信到人的功能需要必须参数openId。

```
// 快手授权登录前需要预先初始化，若不使用授权登录功能请忽略
public class MyApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    KwaiAuthAPI.init(this);
  }
}
```

```
final ILoginListener loginListener = new ILoginListener() { // 登录回调的监听
    @Override
    public void onSuccess(@NonNull InternalResponse response) {
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
// STATE安全参数，标识和用户或者设备相关的授权请求。建议开发者实现
// KwaiConstants.LoginType.APP通过快手App登录授权，KwaiConstants.LoginType.H5通过H5页面登录授权
// 请求授权，支持两个平台KwaiConstants.Platform.KWAI_APP（快手主站）、KwaiConstants.Platform.NEBULA_APP（快手极速版），未设置的默认通过快手主站授权
// 设置了两个平台且同时安装了快手主站和快手极速版，则按传入顺序调起
// KwaiConstants.LoginType.APP使用快手应用授权，KwaiConstants.LoginType.H5使用前端页面通过手机号和验证码授权
  KwaiAuthRequest request = new KwaiAuthRequest.Builder()
    .setState(Config.STATE)
    .setAuthMode(KwaiConstants.AuthMode.AUTHORIZE)
    .setLoginType(KwaiConstants.LoginType.APP)
    .setPlatformArray(platformList.toArray(new String[]
      {KwaiConstants.Platform.KWAI_APP，KwaiConstants.Platform.NEBULA_APP}))
    .build();
  KwaiAuthAPI.getInstance().sendRequest(getActivity(), request, loginListener);

  // 服务器使用接口，获取openId的网络请求，为了安全性，建议放在第三方客户端的服务器中，由接入方服务器实现这个请求接口后将openid返回接入方的客户端
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
```
### （3）开放平台api初始化和回调设置

```
private IKwaiOpenAPI mKwaiOpenAPI; // 声明使用接口
mKwaiOpenAPI = new KwaiOpenAPI(getContext()); // 初始化

// 设置平台功能的配置选项
 OpenSdkConfig openSdkConfig = new OpenSdkConfig.Builder()
        .setGoToMargetAppNotInstall(true) // 应用未安装，是否自动跳转应用市场
        .setGoToMargetAppVersionNotSupport(true) // 应用已安装但版本不支持，是否自动跳转应用市场
        .setSetNewTaskFlag(true) // 设置启动功能页面是否使用新的页面栈
        .setSetClearTaskFlag(true) // 设置启动功能页面是否清除当前页面栈，当isSetNewTaskFlag为true时生效
        .setShowDefaultLoading(false) // 是否显示默认的loading页面作为功能启动的过渡
        .build();
mKwaiOpenAPI.setOpenSdkConfig(openSdkConfig);

// 业务请求回调结果监听
mKwaiOpenAPI.addKwaiAPIEventListerer(new IKwaiAPIEventListener() {

  @Override
  public void onRespResult(@NonNull BaseResp resp) {
    Log.i(TAG, "resp=" + resp);
    if (resp != null) {
      Log.i(TAG, "errorCode=" + resp.errorCode + ", errorMsg="
          + resp.errorMsg + ", cmd=" + resp.getCommand()
          + ", transaction=" + resp.transaction + ", platform=" + resp.platform);
      mCallbackTv.setText("CallBackResult: errorCode=" + resp.errorCode + ", errorMsg="
          + resp.errorMsg + ", cmd=" + resp.getCommand()
          + ", transaction=" + resp.transaction + ", platform=" + resp.platform);
    } else {
      mCallbackTv.setText("CallBackResult: resp is null");
    }
  }
});

// 移除对回调结果的监听，请及时移除不用的监听避免内存泄漏问题
mKwaiOpenAPI.removeKwaiAPIEventListerer();
```
### （4）社交方向相关的业务请求示例代码：
```
// 通过选择人或者群组分享私信
  public void shareMessage() {
    // base params
    ShareMessage.Req req = new ShareMessage.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "sharemessage";
   // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
   // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
   req.setPlatformArray(new String[] {KwaiPlatform.Platform.KWAI_APP, KwaiPlatform.Platform.NEBULA_APP});

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
```
```
// 通过TargetOpenId分享私信给个人，openId是必须参数
  public void shareMessageToBuddy() {
    if (TextUtils.isEmpty(HistoryOpenIdActivity.sTargetOpenId)) {
      Toast.makeText(getActivity(), "sTargetOpenId is null, 请先设置",
         Toast.LENGTH_SHORT).show();
      return;
    }

    ShareMessageToBuddy.Req req = new ShareMessageToBuddy.Req();
    req.openId = mOpenId;
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "sharemessageToBuddy";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.NEBULA_APP, KwaiPlatform.Platform.KWAI_APP});

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
```

```
// 打开TargetOpenId指向的个人主页
  public void showProfile() {
    if (TextUtils.isEmpty(HistoryOpenIdActivity.sTargetOpenId) && getActivity() !=
         null && !getActivity().isFinishing()) {
      Toast.makeText(getActivity(), "sTargetOpenId is null, 请先设置",
         Toast.LENGTH_SHORT).show();
      return;
    }

    ShowProfile.Req req = new ShowProfile.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "showProfile";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.KWAI_APP});

    req.targetOpenId = HistoryOpenIdActivity.sTargetOpenId;

    mKwaiOpenAPI.sendReq(req, getActivity());
  }
```
### （5）生产方向相关的业务请求示例代码：
- 生产方向的操作具有权限控制，请在demo中查询接入方是否获取了对应操作的权限
- 设置封面时需要封面图与视频大小保持一致
```
//发布图片
  public void publishPicture(File file) {
    SinglePicturePublish.Req req = new SinglePicturePublish.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SinglePicturePublish";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.KWAI_APP, KwaiPlatform.Platform.NEBULA_APP});

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
```

```
//编辑图片
  public void editPicture(File file) {
    SinglePictureEdit.Req req = new SinglePictureEdit.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SinglePictureEdit";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.NEBULA_APP});

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
```

```
//发布单个视频
  public void publishSingleVideo(File file) {
    SingleVideoPublish.Req req = new SingleVideoPublish.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoPublish";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.KWAI_APP,KwaiPlatform.Platform.NEBULA_APP});

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
```

```
//编辑单个视频
  public void editSingleVideo(File file) {
    SingleVideoEdit.Req req = new SingleVideoEdit.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoEdit";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.KWAI_APP, KwaiPlatform.Platform.NEBULA_APP});

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

```

```
//裁剪单个视频
  public void clipSingleVideo(File file) {
    SingleVideoClip.Req req = new SingleVideoClip.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "SingleVideoClip";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.KWAI_APP, KwaiPlatform.Platform.NEBULA_APP});

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
```

```
// 多图和视频裁剪
  public void clipMultiMedia(ArrayList<String> multiMedia) {
    MultiMediaClip.Req req = new MultiMediaClip.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "MultiMediaClip";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.NEBULA_APP});

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

```

```
//智能剪辑的api使用
  public void aiCutMedias(ArrayList<String> multiMedia) {
    AICutMedias.Req req = new AICutMedias.Req();
    req.sessionId = mKwaiOpenAPI.getOpenAPISessionId();
    req.transaction = "AICutMedias";
    // 设置功能调起快手支持应用，KwaiPlatform.Platform.KWAI_APP（快手主站），KwaiPlatform.Platform.NEBULA_APP（快手极速版）
    // 按数组顺序检查应用安装和版本情况，从中选择满足条件的第一个应用调起，若不设置则默认启动快手主站应用
    req.setPlatformArray(new String[] {KwaiPlatform.Platform.KWAI_APP});

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
```
## 3、错误码定义

```
public interface KwaiOpenSdkErrorCode {

  //这些值不要随意改变，和ios统一了，和主站保持一致了
  int ERR_KWAI_APP_NOT_LOGIN = -1011;
  int ERR_INVALID_PARAMETERS = -1010;
  int ERR_KWAI_APP_UNSUPPORT = -1006;
  int ERR_NO_KWAI_APP = -1005;
  int ERR_CANCEL = -1;
  int ERR_OK = 1;
  // add new
  int ERR_NO_AUTH_AND_OPENID = -1012;
  int ERR_FALL_BACK_REJECT = -1013;
  int ERR_TEENAGER_MODE = -1014;
  int ERR_NETWORK = -1015;
  // post share
  int ERR_NO_PERMISSION = -1016;
  int ERR_COMPRESS_PICTURE = -1017;

  //server api 返回的errorcode
  int ERR_UNAUTHORIZED_CMD = 20088;  //未授权的cmd
  int ERR_UNACCESSIBLE_USERID = 20089; //无法获取openId对应的userId
  int ERR_INVALID_CMD = 20090; //无效的cmd
  int ERR_INCONSISTEN_OPENID_LOGIN_USERID  = 20091; //登陆userId 和 openId对应的userId不一致
  int ERR_TARGET_NOT_BUDDY  = 20092; //targetOpenId不是好友
  int ERR_INVALID_TARGET_OPEN_ID = 20094; // 无效的targetOpenId

  /**
   * 100200100, //请求缺少参数或参数类型错误
   * 100200101, //未授权的 client，无效的 app 或 developer
   * 100200102, //请求被拒绝，可能是无效的 token 等
   * 100200103, //请求的 responseType 错误
   * 100200104, //请求的 grantType 不支持
   * 100200105, //请求的 code 错误
   * 100200106, //请求的 scope 错误
   * 100200107, //无效的 openid
   * 100200108, // access_token 或者 refresh_token 过期
   * 100200109, // 用户取消该 app 授权
   * 100200110, // 用户授权过期
   * 100200111, // 用户未授权过
   */
  int ERR_SERVER_CHECK_INVALID_PARAMETER  = 100200100; //SERVER端检查发现无效的参数
  
  // 作品发布到快手上传成功通知
  int PUBLISH_WORK_SUCCESS = 100;
}
```