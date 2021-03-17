package com.kwai.opensdk.demo;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager.widget.ViewPager;

public class TabMainActivity extends FragmentActivity {
  private LinearLayout mSocialTab;
  private LinearLayout mPostTab;
  private LinearLayout mGenSignatureTab;

  private TextView mSocialShareTextView;
  private TextView mPostShareTextView;
  private TextView mGenSignatureTextView;

  private ImageView mTabLine;
  private int screenWidth;

  private ViewPager mViewPager;
  private FragmentAdapter mAdapter;
  private List<Fragment> fragments = new ArrayList<Fragment>();

  private SocialShareFragment mSocialFragment;
  private PostShareFragment mPostFragment;
  private GenSignatureFragment mGenSignatureFragment;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.activity_main_tab);
    initView();
    mViewPager = findViewById(R.id.id_viewpager);
    mAdapter = new FragmentAdapter(getSupportFragmentManager(), fragments);
    mViewPager.setAdapter(mAdapter);
    mViewPager.addOnPageChangeListener(new TabOnPageChangeListener());
    initTabLine();
  }

  private void initTabLine() {
    mTabLine = findViewById(R.id.id_tab_line);
    DisplayMetrics outMetrics = new DisplayMetrics();
    getWindow().getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
    screenWidth = outMetrics.widthPixels;

    LinearLayout.LayoutParams lp =
        (LinearLayout.LayoutParams) mTabLine.getLayoutParams();
    lp.width = screenWidth / 3;
    mTabLine.setLayoutParams(lp);
  }

  private void initView() {
    mSocialShareTextView = findViewById(R.id.id_tab01_info);
    mPostShareTextView = findViewById(R.id.id_tab02_info);
    mGenSignatureTextView = findViewById(R.id.id_tab03_info);

    mSocialTab = findViewById(R.id.id_tab01);
    mPostTab = findViewById(R.id.id_tab02);
    mGenSignatureTab = findViewById(R.id.id_tab03);

    mSocialTab.setOnClickListener(new TabOnClickListener(0));
    mPostTab.setOnClickListener(new TabOnClickListener(1));
    mGenSignatureTab.setOnClickListener(new TabOnClickListener(2));

    mSocialFragment = new SocialShareFragment();
    fragments.add(mSocialFragment);
    mPostFragment = new PostShareFragment();
    fragments.add(mPostFragment);
    mGenSignatureFragment = new GenSignatureFragment();
    fragments.add(mGenSignatureFragment);

  }

  private void resetTextView() {
    mSocialShareTextView.setTextColor(getResources().getColor(R.color.black));
    mPostShareTextView.setTextColor(getResources().getColor(R.color.black));
    mGenSignatureTextView.setTextColor(getResources().getColor(R.color.black));
  }

  public class TabOnClickListener implements View.OnClickListener {
    private int index = 0;

    public TabOnClickListener(int i) {
      index = i;
    }

    public void onClick(View v) {
      mViewPager.setCurrentItem(index);
    }

  }

  public class TabOnPageChangeListener implements ViewPager.OnPageChangeListener {

    public void onPageScrollStateChanged(int state) {

    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      LinearLayout.LayoutParams lp =
          (LinearLayout.LayoutParams) mTabLine.getLayoutParams();
      lp.leftMargin = (int) ((positionOffset + position) * screenWidth / 3);
      mTabLine.setLayoutParams(lp);
    }

    public void onPageSelected(int position) {
      resetTextView();
      switch (position) {
        case 0:
          mSocialShareTextView.setTextColor(getResources().getColor(R.color.colorAccent));
          break;
        case 1:
          mPostShareTextView.setTextColor(getResources().getColor(R.color.colorAccent));
          break;
        case 2:
          mGenSignatureTextView.setTextColor(getResources().getColor(R.color.colorAccent));
          break;
      }
    }
  }
}
