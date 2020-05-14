package com.kwai.opensdk.demo;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;


public class HistoryOpenIdActivity extends AppCompatActivity implements View.OnClickListener {

  static final String TAG = "HistoryOpenIdActivity";
  static final String KEY = "ids";

  public static String sTargetOpenId;

  private EditText mInputView;
  private Button mAddBtn;
  private EditText mChooseInput;
  private Button mChooseBtn;
  private TextView mOpenIds;

  private final ArrayList<String> mList = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history_open_id);
    mInputView = findViewById(R.id.input);
    mAddBtn = findViewById(R.id.add_btn);
    mChooseInput = findViewById(R.id.choose_input);
    mChooseBtn = findViewById(R.id.choose_btn);
    mOpenIds = findViewById(R.id.openids);
    mAddBtn.setOnClickListener(this);
    mChooseBtn.setOnClickListener(this);
    loadIds();
    mOpenIds.setText(getIdString());
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.add_btn) {
      String text = mInputView.getText().toString().trim();
      if (!TextUtils.isEmpty(text)) {
        mInputView.setText("");
        mList.add(text.toLowerCase());
        saveIds();
        mOpenIds.setText(getIdString());
      } else {
          Toast.makeText(this, "请正确输入targetId!", Toast.LENGTH_SHORT).show();
      }
    } else if (v.getId() == R.id.choose_btn) {
      int pos = -1;
      try {
        pos = Integer.parseInt(mChooseInput.getText().toString().trim());
      } catch (Exception e) {}
      if (pos >= 0) {
        sTargetOpenId = mList.get(pos);
        Toast.makeText(this, "sTargetOpenId=" + sTargetOpenId, Toast.LENGTH_SHORT).show();
        finish();
      }else {
        Toast.makeText(this, "请正确选择targetId!", Toast.LENGTH_SHORT).show();
      }

    }
  }

  String getIdString() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < mList.size(); i++) {
      sb.append(i).append("    ").append(mList.get(i)).append("\n");
    }
    return sb.toString();
  }

  void saveIds() {
    String text = TextUtils.join(",", mList);
    setDefaultString(this, KEY, text);
  }

  void loadIds() {
    String text = getDefaultString(this, KEY, null);
    if (!TextUtils.isEmpty(text)) {
      String[] ids = text.split(",");
      if (ids != null && ids.length > 0) {
        for (String id : ids) {
          if (!TextUtils.isEmpty(id)) {
            mList.add(id);
          }
        }
      }
    }
  }

  public static String getDefaultString(Context c, String key, String defaultValue) {
    return PreferenceManager.getDefaultSharedPreferences(c).getString(key, defaultValue);
  }

  public static void setDefaultString(Context c, String key, String value) {
    PreferenceManager.getDefaultSharedPreferences(c).edit().putString(key, value).commit();
  }
}
