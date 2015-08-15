package com.befun.activity;

import java.util.ArrayList;

import com.befun.test.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

public class Leading extends Activity {
	
	private ViewPager mViewPager;	
	private ImageView mPage0;
	private ImageView mPage1;
	private ImageView mPage2;
	private ImageView mPage3;
		
	private int currIndex = 0;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.leading);
        mViewPager = (ViewPager)findViewById(R.id.whatsnew_viewpager);        
        mViewPager.setOnPageChangeListener(new MyOnPageChangeListener());
       
        
        mPage0 = (ImageView)findViewById(R.id.page0);
        mPage1 = (ImageView)findViewById(R.id.page1);
        mPage2 = (ImageView)findViewById(R.id.page2);
        mPage3 = (ImageView)findViewById(R.id.page3);

      //将要分页显示的View装入数组中
        LayoutInflater mLi = LayoutInflater.from(this);
        View view1 = mLi.inflate(R.layout.leadpage1, null);
        View view2 = mLi.inflate(R.layout.leadpage2, null);
        View view3 = mLi.inflate(R.layout.leadpage3, null);
        View view4 = mLi.inflate(R.layout.leadpage4, null);
    
      //每个页面的view数据
        final ArrayList<View> views = new ArrayList<View>();
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);
        
        //填充ViewPager的数据适配器
        PagerAdapter mPagerAdapter = new PagerAdapter() {
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return views.size();
			}

			@Override
			public void destroyItem(View container, int position, Object object) {
				((ViewPager)container).removeView(views.get(position));
			}
			
			
			
			@Override
			public Object instantiateItem(View container, int position) {
				((ViewPager)container).addView(views.get(position));
				return views.get(position);
			}
		};
		
		mViewPager.setAdapter(mPagerAdapter);
    }    
    

    public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:				
				mPage0.setImageDrawable(getResources().getDrawable(R.drawable.dotl_now));
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.dotl));
				break;
			case 1:
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.dotl_now));
				mPage0.setImageDrawable(getResources().getDrawable(R.drawable.dotl));
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.dotl));
				break;
			case 2:
				mPage0.setVisibility(View.VISIBLE);
				mPage1.setVisibility(View.VISIBLE);
				mPage2.setVisibility(View.VISIBLE);
				mPage3.setVisibility(View.VISIBLE);
				mPage2.setImageDrawable(getResources().getDrawable(R.drawable.dotl_now));
				mPage1.setImageDrawable(getResources().getDrawable(R.drawable.dotl));
				mPage3.setImageDrawable(getResources().getDrawable(R.drawable.dotl));
				break;
			case 3:
				mPage0.setVisibility(View.GONE);
				mPage1.setVisibility(View.GONE);
				mPage2.setVisibility(View.GONE);
				mPage3.setVisibility(View.GONE);
				break;
			}
			currIndex = arg0;
		}
		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	}
    public void startbutton(View v) {  
      	Intent intent = new Intent();
		intent.setClass(Leading.this,WelcomActivity.class);
		startActivity(intent);
		this.finish();
      }  
    
}
