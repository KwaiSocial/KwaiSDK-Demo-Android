package com.kwai.opensdk.demo;

import java.security.MessageDigest;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class GenSignatureFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    View view = inflater.inflate(R.layout.gen_signature_layout, container, false);

    EditText editText = view.findViewById(R.id.editText);
    TextView signatureText = view.findViewById(R.id.signature_text);

    view.findViewById(R.id.gen_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        String packageName = editText.getText().toString().trim();
        if (TextUtils.isEmpty(packageName)) {
          Toast.makeText(getContext(), "请输入应用包名", Toast.LENGTH_SHORT).show();
          return;
        }
        String sign = getSignature(packageName, getContext());
        if (TextUtils.isEmpty(sign)) {
          Toast.makeText(getContext(), "生成签名失败", Toast.LENGTH_SHORT).show();
        } else {
          signatureText.setVisibility(View.VISIBLE);
          signatureText.setText(sign);
        }
      }
    });

    view.findViewById(R.id.copy_button).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (setPrimaryClipText(signatureText.getText())) {
          Toast.makeText(getContext(), "成功复制签名到黏贴版", Toast.LENGTH_SHORT).show();
        }
      }
    });
    return view;
  }

  private boolean setPrimaryClipText(CharSequence text) {
    final ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
    if (clipboard == null || TextUtils.isEmpty(text)) {
      return false;
    }
    try {
      ClipData clip = ClipData.newPlainText("", text);
      clipboard.setPrimaryClip(clip);
    } catch (SecurityException e) {
      return false;
    }
    return true;
  }

  @Nullable
  private String getSignature(@Nullable String packageName, @NonNull Context context) {
    if (TextUtils.isEmpty(packageName)) {
      return null;
    }
    try {
      Signature[] signatures = context.getPackageManager().getPackageInfo(packageName,
          PackageManager.GET_SIGNATURES).signatures;
      if (signatures == null || signatures.length == 0) {
        return null;
      } else {
        return md5Hex(signatures[0].toByteArray());
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private String md5Hex(byte[] data) {
    try {
      if (data == null || data.length == 0)
        return null;
      MessageDigest digester = MessageDigest.getInstance("MD5");
      if (digester == null)
        return null;
      digester.update(data);
      byte[] d = digester.digest();
      if (d == null || d.length < 1)
        return null;
      return toHexString(d, 0, d.length);
    } catch (Exception e) {
      return null;
    }
  }

  static final char[] HEX_CHARS = {
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'a', 'b', 'c', 'd', 'e', 'f'
  };

  /**
   * get hex string of specified bytes
   */
  private String toHexString(byte[] bytes, int off, int len) {
    if (bytes == null)
      throw new NullPointerException("bytes is null");
    if (off < 0 || (off + len) > bytes.length)
      throw new IndexOutOfBoundsException();
    char[] buff = new char[len * 2];
    int v;
    int c = 0;
    for (int i = 0; i < len; i++) {
      v = bytes[i + off] & 0xff;
      buff[c++] = HEX_CHARS[(v >> 4)];
      buff[c++] = HEX_CHARS[(v & 0x0f)];
    }
    return new String(buff, 0, len * 2);
  }

}
