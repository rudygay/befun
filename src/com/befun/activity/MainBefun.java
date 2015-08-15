package com.befun.activity;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.befun.db.FriendProvider;
import com.befun.entity.Friend;
import com.befun.entity.MessageAadpter;
import com.befun.http.AcountRelated;
import com.befun.service.IConnectionStatusCallback;
import com.befun.service.MainService;
import com.befun.test.R;
import com.befun.util.MyFriendParser;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.AsyncQueryHandler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainBefun extends Activity implements IConnectionStatusCallback{
//////////蓝牙模块/////////
	private int mConnectionState = STATE_DISCONNECTED;
	private boolean is_connected = false;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_SCAN = 3;
    private static final int STATE_ServicesDiscovered  = 4;
    private static final int STATE_STOP_EXECUTE_DEVICE = 5;
    private static final int STATE_EXECUTE_DEVICE = 6;    
    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    private BluetoothAdapter mBluetoothAdapter  = null;
    private boolean mScanning = false;
    BluetoothDevice mDevice = null;
    BluetoothGatt mBluetoothGatt = null;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int DEFAULTHEART = 50;
	private static final int DEFAULTHIGH = 99;
	private static final int DEFAULTKAI = 80;
	private UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
	private UUID writeUUID = UUID.fromString(kUUIDWriteCommand);
	private BluetoothGattService gattService       = null;
	private BluetoothGattCharacteristic btGattChar = null;
	private SeekBar speedProgress;
	private ImageView renewImg;
//////////蓝牙模块/////////
	private AcountRelated acountRelated;
	public static MainBefun instance = null;	 
	private ViewPager mTabPager;	
	private ImageView mTab1,mTab2,mTab3,mTab4,offImage;
	private int currIndex = 0;
	private LinearLayout heshe_layout,message_layout,mine_layout,more_layout,search_lay;
    private TextView heshe_txt,message_txt,mine_txt,more_txt,offName,offId;
    public final ArrayList<View> views = new ArrayList<View>();
    public final ArrayList<View> mineViews = new ArrayList<View>();
    public View view1,view2,view3,view4,viewSetting;
    public View mineView0,mineView1;
    public ImageView mine_page0,mine_page1;
    public ArrayList<Friend> friends = new ArrayList<Friend>();
    public MessageAadpter messageAdapter;
    public ListView meList;//消息区listview！！！！
    public EditText searchEdit;
    public LayoutInflater mLi;
    public Button h1,h2,h3,h4,h5;
    private Handler mainHandler = new Handler();
	private MainService mainService;
	private MyFriendParser myParser;
	private ProgressBar renewPro;
	private ContentObserver mContactObserver = new ContactObserver();
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
			public int getItemPosition(Object object) {  
			    View view = (View)object;  
			    if(currIndex == (Integer)view.getTag()){  
			         return POSITION_NONE;    
			    }else{  
			         return POSITION_UNCHANGED;  
			      }  
			        // return super.getItemPosition(object);  
			    }  
			
			@Override
			public Object instantiateItem(View container, int position) {
				views.get(position).setTag(position); 
				((ViewPager)container).addView(views.get(position));
				return views.get(position);
			}
		};
		
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mainService = ((MainService.XXBinder) service).getService();
			mainService.registerConnectionStatusCallback(MainBefun.this);
			if (!mainService.isAuthenticated()) {
				String usr = PreferenceUtils.getPrefString(MainBefun.this,
						PreferenceConstants.USERNAME, "");
				String password = PreferenceUtils.getPrefString(
						MainBefun.this, PreferenceConstants.PASSWORD, "");
				mainService.login(usr, password);
				}else {			
			}
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			mainService.unRegisterConnectionStatusCallback();
			mainService = null;
		}
	};
    @SuppressLint("InflateParams")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.befun);
        //getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        instance = this;
        initView();
        getContentResolver().registerContentObserver(
    			FriendProvider.CONTENT_URI, true, mContactObserver);
        setChatWindowAdapter();
		searchEdit.setOnKeyListener(new SearchKeylistener());
		acountRelated = new AcountRelated();		
		getDelete(PreferenceUtils.getPrefString
				(MainBefun.this, PreferenceConstants.USERNAME, ""));
    }
    private void getDelete(final String username){
    	new Thread(){

			@Override
			public void run() {
				String deleteStr = acountRelated.deleteMe(username);
				Log.v("删除的json",deleteStr);
				try {
					JSONObject jsonObject = new JSONObject(deleteStr);
					JSONArray jsonArray = jsonObject.getJSONArray("result");			
					for(int i = 0;i < jsonArray.length();i++){
						String username = jsonArray.getString(i);
						if(username != null){
							MainBefun.this.getContentResolver().delete(FriendProvider.CONTENT_URI
									, "username = ?", new String[]{username});
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
    		
    	}.start();
    }
    public void getfriend(final String username,final boolean is_random){
    	new Thread(){
			@Override
			public void run() {
				String random;
				if(is_random)
				{random = acountRelated.getRandom(username,true);
				}
				else random = acountRelated.getRandom(username,false);
				try {
					JSONObject jsonObject = new JSONObject(random);
					String code = jsonObject.getString("code");						
					Log.v("code",code);					
					if(code.equals("1")){
						if(is_random)failtoCreate();
						else failtoSearch();}
					if(code.equals("0")){
						Log.v("随机好友", "-----成功----");
						String randUsername = jsonObject.getJSONObject("result").getString("username");
						String randXML = jsonObject.getJSONObject("result").getString("vcard"); 
						successtoCreate(randXML,randUsername);
					}
					
				}catch (JSONException e) {
					Log.v("json----->","json解析失败");
					if(is_random)failtoCreate();
					else failtoSearch();
					e.printStackTrace();
				}
			}		
    	}.start();
    }
    public void successtoCreate(final String xmlStr,final String randomUsername){
    	mainHandler.post(new Runnable() {			
			@Override
			public void run() {
				Toast.makeText(MainBefun.this, "获取成功", Toast.LENGTH_SHORT).show();
				try {
					Friend friend= myParser.parse(xmlStr);
					friend.setBefunId(randomUsername);
					sethesheFriend(friend);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
    }
    public void sethesheFriend(final Friend friend){
    	View view = mLi.inflate(R.layout.found, null);
    	LinearLayout friend_lay = (LinearLayout)view.findViewById(R.id.found_layout);
    	TextView foundtxt = (TextView)view.findViewById(R.id.found_txt);
    	ImageView foundImag = (ImageView)view.findViewById(R.id.found_img);
    	ImageButton addtalk = (ImageButton)view.findViewById(R.id.talkadd);
    	ImageButton changetalk = (ImageButton)view.findViewById(R.id.talkchange);
    	foundtxt.setText(friend.nickName);
    	switch (friend.gender) {
		case "0":
			foundImag.setImageResource(R.drawable.male_big);
			break;
		case "1":
			foundImag.setImageResource(R.drawable.female_big);
			break;
		case "2":
			foundImag.setImageResource(R.drawable.les_big);
			break;
		case "3":
			foundImag.setImageResource(R.drawable.gay_big);
			break;
		default:
			break;
		}
    	addtalk.setOnClickListener(new OnClickListener() {		
			@Override
			public void onClick(View v) {
				mainService.addFriendtoDB(friend);
				Intent intent = new Intent(MainBefun.this, ChatActivity.class);
				intent.putExtra("friend", friend);
				startActivity(intent);
			}
		});
		changetalk.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getfriend(PreferenceUtils.getPrefString
						(MainBefun.this, PreferenceConstants.USERNAME, "10035"),true);
			}
		});
		search_lay.removeAllViews();
		search_lay.addView(friend_lay);
    }
    public void failtoCreate(){
    	mainHandler.post(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(MainBefun.this, "暂时无人在线", Toast.LENGTH_SHORT).show();
			}
		});
    }
    public void failtoSearch(){
    	mainHandler.post(new Runnable() {			
			@Override
			public void run() {
				//Toast.makeText(MainBefun.this, "未能成功获取好友", Toast.LENGTH_SHORT).show();
				View notfindView = mLi.inflate(R.layout.heshe_dialog, null);
				Button po = (Button)notfindView.findViewById(R.id.heshepo);
				final Dialog alert = new Dialog(MainBefun.this,R.style.myDialog);
				alert.setContentView(notfindView);
		        alert.show();
		        po.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						alert.dismiss();
					}
				});				
			}
		});
    }
    public class MyOnClickListener implements View.OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}
		@Override
		public void onClick(View v) {
			mTabPager.setCurrentItem(index);
		}
	};
	private void bindXMPPService() {
		Log.i("LoginActivity.class", "[SERVICE] bind");
		bindService(new Intent(MainBefun.this, MainService.class),
				mServiceConnection, Context.BIND_AUTO_CREATE
						+ Context.BIND_DEBUG_UNBIND);
	}
    public void initView(){
    	mTabPager = (ViewPager)findViewById(R.id.tabpager);
        mTabPager.setOnPageChangeListener(new MyOnPageChangeListener());
        heshe_layout = (LinearLayout)findViewById(R.id.main_heshe_layout);
        message_layout = (LinearLayout)findViewById(R.id.main_message_layout);
        more_layout = (LinearLayout)findViewById(R.id.main_more_layout); 
        mine_layout = (LinearLayout)findViewById(R.id.main_mine_layout);
        mTab1 = (ImageView) findViewById(R.id.heorshe);
        mTab2 = (ImageView) findViewById(R.id.message);
        mTab3 = (ImageView) findViewById(R.id.mine);
        mTab4 = (ImageView) findViewById(R.id.more);
        heshe_txt =(TextView)findViewById(R.id.heshe_txt);
        message_txt =(TextView)findViewById(R.id.message_txt);
        mine_txt =(TextView)findViewById(R.id.mine_txt);
        more_txt =(TextView)findViewById(R.id.more_txt);
        heshe_layout.setOnClickListener(new MyOnClickListener(0));
        message_layout.setOnClickListener(new MyOnClickListener(1));
        more_layout.setOnClickListener(new MyOnClickListener(3));
        mine_layout.setOnClickListener(new MyOnClickListener(2));       
        mLi = LayoutInflater.from(this);        
        view1 = mLi.inflate(R.layout.main_tab_heshe, null);
        view2 = mLi.inflate(R.layout.main_tab_message, null);
        view3 = mLi.inflate(R.layout.main_tab_mine, null);
        view4 = mLi.inflate(R.layout.main_tab_more, null);
        h1 = (Button)view3.findViewById(R.id.h1);
        h2 = (Button)view3.findViewById(R.id.h2);
        h3 = (Button)view3.findViewById(R.id.h3);
        h4 = (Button)view3.findViewById(R.id.h4);
        h5 = (Button)view3.findViewById(R.id.h5);
        speedProgress = (SeekBar)view3.findViewById(R.id.speedProgress);
        renewImg = (ImageView)view3.findViewById(R.id.renewimg);
        renewPro = (ProgressBar)view3.findViewById(R.id.renewPro);
        speedProgress.setOnSeekBarChangeListener(new BluetoothSeek());
        h1.setOnClickListener(new MoshiOnclickListener());
        h2.setOnClickListener(new MoshiOnclickListener());
        h3.setOnClickListener(new MoshiOnclickListener());
        h4.setOnClickListener(new MoshiOnclickListener());
        h5.setOnClickListener(new MoshiOnclickListener());       
        mineView0 = mLi.inflate(R.layout.mine_view0, null);
        mineView1 = mLi.inflate(R.layout.mine_view1, null);        
        meList = (ListView)view2.findViewById(R.id.message_list);
        searchEdit = (EditText)view1.findViewById(R.id.edit_Text2);
        search_lay = (LinearLayout)view1.findViewById(R.id.search_layout);
        offImage = (ImageView)view4.findViewById(R.id.img_more_id);
        offName = (TextView)view4.findViewById(R.id.off_name);
        offId = (TextView)view4.findViewById(R.id.off_id);
        offName.setText(PreferenceUtils.getPrefString(this,PreferenceConstants.NICKNAME
        		, "null"));
        offId.setText("被窝ID："+PreferenceUtils.getPrefString(this,PreferenceConstants.USERNAME
        		, "null"));
        switch (PreferenceUtils.getPrefString(this,PreferenceConstants.GENDER
        		, "null")) {
        		case "0":
        			offImage.setImageResource(R.drawable.male_more);
        			break;
        		case "1":
        			offImage.setImageResource(R.drawable.female_more);
        			break;
        		case "2":
        			offImage.setImageResource(R.drawable.les_more);
        			break;
        		case "3":
        			offImage.setImageResource(R.drawable.gay_more);
        			break;
        		default:
        			break;
		}
        viewSetting = mLi.inflate(R.layout.setting, null);
        mineViews.add(mineView0);
        mineViews.add(mineView1);
        views.add(view1);
        views.add(view2);
        views.add(view3);
        views.add(view4);      		
		mTabPager.setAdapter(mPagerAdapter);
		myParser = new MyFriendParser();		
    }
	
	public class MyOnPageChangeListener implements OnPageChangeListener {
		@Override
		public void onPageSelected(int arg0) {
			switch (arg0) {
			case 0:
				mTab1.setImageDrawable(getResources().getDrawable(R.drawable.heshe_pressed));
				heshe_txt.setTextColor(Color.parseColor("#ff548d"));
				if (currIndex == 1) {
					message_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab2.setImageDrawable(getResources().getDrawable(R.drawable.message));
				} else if (currIndex == 2) {
					mine_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab3.setImageDrawable(getResources().getDrawable(R.drawable.mine));
				}
				else if (currIndex == 3) {
					more_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.more));
				}
				break;
			case 1:
				mTab2.setImageDrawable(getResources().getDrawable(R.drawable.message_pressed));
				message_txt.setTextColor(Color.parseColor("#ff548d"));
				if (currIndex == 0) {
					heshe_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab1.setImageDrawable(getResources().getDrawable(R.drawable.heshe));
				} else if (currIndex == 2) {
					mine_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab3.setImageDrawable(getResources().getDrawable(R.drawable.mine));
				}
				else if (currIndex == 3) {
					more_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.more));
				}
				break;
			case 2:
				mTab3.setImageDrawable(getResources().getDrawable(R.drawable.mine_pressed));
				mine_txt.setTextColor(Color.parseColor("#ff548d"));
				if (currIndex == 0) {
					heshe_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab1.setImageDrawable(getResources().getDrawable(R.drawable.heshe));
				} else if (currIndex == 1) {
					message_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab2.setImageDrawable(getResources().getDrawable(R.drawable.message));
				}
				else if (currIndex == 3) {
					more_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab4.setImageDrawable(getResources().getDrawable(R.drawable.more));
				}
				break;
			case 3:
				mTab4.setImageDrawable(getResources().getDrawable(R.drawable.more_pressed));
				more_txt.setTextColor(Color.parseColor("#ff548d"));
				if (currIndex == 0) {
					heshe_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab1.setImageDrawable(getResources().getDrawable(R.drawable.heshe));
				} else if (currIndex == 1) {
					message_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab2.setImageDrawable(getResources().getDrawable(R.drawable.message));
				}
				else if (currIndex == 2) {
					mine_txt.setTextColor(Color.parseColor("#ffffff"));
					mTab3.setImageDrawable(getResources().getDrawable(R.drawable.mine));
				}
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
	public void more_setting(View v){
		views.remove(3);  
	    views.add(3, viewSetting);  
	    mTabPager.getAdapter().notifyDataSetChanged();
	}
	public void problemhelp(View v){
		Intent intent = new Intent(this, WentiActivity.class);
		startActivity(intent);
	}
	public void zhuxiao(View v){
		Intent intent = new Intent(this, AcountCancel.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		bindXMPPService();
		getfriend(PreferenceUtils.getPrefString
				(MainBefun.this, PreferenceConstants.USERNAME, "10171"),true);
	}
	@Override
	protected void onDestroy() {
		Log.v("MainBefun","destroy!");
		getContentResolver().unregisterContentObserver(mContactObserver);
		mainService.stopSelf();
		super.onDestroy();
	}
	public class SearchKeylistener implements OnKeyListener{			
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(keyCode == KeyEvent.KEYCODE_ENTER){
				if(searchEdit.getText().toString().length() > 0)
				getfriend(searchEdit.getText().toString(), false);			
				searchEdit.setText("");
			}
			return false;
		}
	}	
	public void gao(View v){
		   SetModel(7);
		   try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		   SetSpeed(DEFAULTHIGH);
		   execut();
	   } 
	   public void jingzhi(View v){
		   SetModel(0);
		   execut();
	   }
	   public void kaige(View v){
		  /*SetModel(6);
		   try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		   if (mBluetoothGatt == null)
			{
				return;
			}
		   SetSpeed(DEFAULTKAI);
		   mainService.startKaigeer(mBluetoothGatt);
	   }
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void execut(){
		mainHandler.postDelayed(new Runnable() {			
			@Override
			public void run() {
				if (mBluetoothGatt != null)
				{
					Boolean execWrite = mBluetoothGatt.executeReliableWrite();			
					Log.i("execWrite", execWrite.toString());
				}
		 	   	Message msg = new Message();          
			    msg.what = STATE_EXECUTE_DEVICE;
			    mHandler1.sendMessage(msg);
			}
		}, 80);
	}
	private void unbindXMPPService(){
		try {
			unbindService(mServiceConnection);
			Log.i("MainBefun", "[SERVICE] Unbind");
		} catch (IllegalArgumentException e) {
			Log.e("MainBefun", "Service wasn't bound!");
		}
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		unbindXMPPService();
	}
	private class ContactObserver extends ContentObserver {
		public ContactObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {
			Log.d("ContactObserver.onChange: ","contentchange");
			updateContactStatus();}
	}
	private void updateContactStatus(){
		setChatWindowAdapter();
		Log.d("测试数据库","好友数发生变化");
	}
	private void setChatWindowAdapter() {
		new AsyncQueryHandler(getContentResolver()) {

			@Override
			protected void onQueryComplete(int token, Object cookie,
					Cursor cursor) {
				Log.v("cursor",cursor.getCount()+"");
				ListAdapter adapter = new MessageAadpter(cursor, MainBefun.this,mainService);
				meList.setAdapter(adapter);
				//meList.setSelection(adapter.getCount() - 1);
			}
		}.startQuery(0,null,FriendProvider.CONTENT_URI,null,null,null,"is_read");
	}
	@Override
	public void connectionStatusChanged(int connectedState, String reason) {
		if(connectedState == 9 || connectedState == 0)
		Toast.makeText(this, reason, Toast.LENGTH_SHORT).show();
	}
	
	////////////蓝牙模块///////////
	public class MoshiOnclickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			int seekInt = speedProgress.getProgress();
			switch (v.getId()) {
			case R.id.h1:
				h1.setBackgroundResource(R.drawable.h1_pressed);
				h2.setBackgroundResource(R.drawable.h2);
				h3.setBackgroundResource(R.drawable.h3);
				h4.setBackgroundResource(R.drawable.h4);
				h5.setBackgroundResource(R.drawable.h5);
				if (mBluetoothGatt != null)
				{
					SetModel(1);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SetSpeed(seekInt);
					execut();
				}								
				break;
			case R.id.h2:
				h1.setBackgroundResource(R.drawable.h1);
				h2.setBackgroundResource(R.drawable.h2_pressed);
				h3.setBackgroundResource(R.drawable.h3);
				h4.setBackgroundResource(R.drawable.h4);
				h5.setBackgroundResource(R.drawable.h5);
				if (mBluetoothGatt != null)
				{
					SetModel(2);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SetSpeed(seekInt);
					execut();
				}	
				break;
			case R.id.h3:
				h1.setBackgroundResource(R.drawable.h1);
				h2.setBackgroundResource(R.drawable.h2);
				h3.setBackgroundResource(R.drawable.h3_pressed);
				h4.setBackgroundResource(R.drawable.h4);
				h5.setBackgroundResource(R.drawable.h5);
				if (mBluetoothGatt != null)
				{
					SetModel(3);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SetSpeed(seekInt);
					execut();
				}	
				break;
			case R.id.h4:
				h1.setBackgroundResource(R.drawable.h1);
				h2.setBackgroundResource(R.drawable.h2);
				h3.setBackgroundResource(R.drawable.h3);
				h4.setBackgroundResource(R.drawable.h4_pressed);
				h5.setBackgroundResource(R.drawable.h5);
				if (mBluetoothGatt != null)
				{
					SetModel(4);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SetSpeed(seekInt);
					execut();
				}	
				break;
			case R.id.h5:
				h1.setBackgroundResource(R.drawable.h1);
				h2.setBackgroundResource(R.drawable.h2);
				h3.setBackgroundResource(R.drawable.h3);
				h4.setBackgroundResource(R.drawable.h4);
				h5.setBackgroundResource(R.drawable.h5_pressed);
				if (mBluetoothGatt != null)
				{
					SetModel(5);
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					SetSpeed(seekInt);
					execut();
				}	
				break;	
			default:
				break;
			}
		}	
	}
	/**
	 * 
	 */
	@SuppressLint("HandlerLeak")
	protected Handler mHandler1 = new Handler()
	{         
		// @Override         
		@SuppressLint("NewApi")
		public void handleMessage(Message msg) 
		{         
			super.handleMessage(msg);
			switch(msg.what)
			{
			case STATE_SCAN:
				String deviceName = (String)msg.obj;
				if (deviceName.equalsIgnoreCase("Biscuit"))
				{
					connectGatt();
					mScanning = false;
				}				
				break;
			case STATE_CONNECTING:
				break;
			case STATE_CONNECTED:
				is_connected = true;
				renewPro.setVisibility(View.GONE);
				renewImg.setVisibility(View.VISIBLE);
				renewImg.setImageResource(R.drawable.renew_pressed);
				View bluView = mLi.inflate(R.layout.device_success_dialog, null);
				Button po = (Button)bluView.findViewById(R.id.success_po);				
				final Dialog alert = new Dialog(MainBefun.this,R.style.myDialog);
				alert.setContentView(bluView);
		        alert.show();
		        po.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						alert.dismiss();						
					}
				});
				break;
			case STATE_DISCONNECTED:
				is_connected = false;
				renewImg.setImageResource(R.drawable.renew);
				toas("设备失去连接！");
				break;
			case STATE_ServicesDiscovered:
				//toas("Services Discovered");
				break;
			case STATE_STOP_EXECUTE_DEVICE:
				toas("Stop Execute Device!");
				if (mBluetoothGatt != null)
				{
					Boolean execWrite = mBluetoothGatt.executeReliableWrite();
					Log.i("execWrite", execWrite.toString());
				}
				break;
			case STATE_EXECUTE_DEVICE:
				//toas("Start Execute Device!");
				break;
				
			default:
				break;
			}
			
		}
	};
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	void SetModel(int modelNo)
	{
		mainService.cancelKaigeer();
		if (mBluetoothGatt == null)
		{
			return;
		}
		List<BluetoothGattService> listService = mBluetoothGatt.getServices();   	
    	for (int i=0; i<listService.size(); i++)
    	{
    		BluetoothGattService tempgattService = listService.get(i);
    		UUID deviceUUID = tempgattService.getUuid();
        	if (deviceUUID.compareTo(kUUIDDeviceCommand) ==0) 
        	{
        		gattService = tempgattService;
        		break;
        	}
    	}
    	
    	if (gattService == null)
    	{
    		return;
    	}
    
    	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
    	for (int i=0; i<gattChacterList.size(); i++)
    	{
    		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
    		if (tempbtGattChar.getUuid().compareTo(writeUUID) == 0 )
    		{
    			btGattChar = tempbtGattChar;
    			break;
    		}
    		
    	}
    	if (btGattChar == null)
    	{
    		return;
    	}
    	mBluetoothGatt.setCharacteristicNotification(btGattChar, true);
		//byte[] model_buffer = new byte[6];
    	final byte[] model_buffer = {0,0,0,0,0};
		for (int i=0; i<5; i++)
		{
			model_buffer[i] = 0;
		}
		
		//0xFA, 0x05, 0x03, 0x00, 0xFC
		
		model_buffer[0] = (byte) 0xfa;
		model_buffer[1] = 0x05;
		model_buffer[2] = (byte)modelNo;
		model_buffer[3] = 0x00;
		model_buffer[4] = (byte) (model_buffer[0] ^ model_buffer[1] ^ model_buffer[2] ^ model_buffer[3]);
		//model_buffer[4] = (byte) 0xfc;		
		mBluetoothGatt.beginReliableWrite();
		btGattChar.setValue(model_buffer);
		btGattChar.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
		Boolean writechar = mBluetoothGatt.writeCharacteristic(btGattChar);
		mBluetoothGatt.readCharacteristic(btGattChar);
		Log.i("writechar", writechar.toString());
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void SetSpeed(int nSpeedPos){
		if (mBluetoothGatt == null)
		{
			return;
		}			
    	List<BluetoothGattService> listService = mBluetoothGatt.getServices();   	
    	for (int i=0; i<listService.size(); i++)
    	{
    		BluetoothGattService tempgattService = listService.get(i);
    		UUID deviceUUID = tempgattService.getUuid();
        	if (deviceUUID.compareTo(kUUIDDeviceCommand) == 0) 
        	{
        		gattService = tempgattService;
        		break;
        	}
    	}
    	
    	if (gattService == null)
    	{
    		return;
    	}
    
    	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
    	for (int i=0; i<gattChacterList.size(); i++)
    	{
    		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
    		if (tempbtGattChar.getUuid().compareTo(writeUUID) == 0 )
    		{
    			btGattChar = tempbtGattChar;
    			break;
    		}
    	}
    	if (btGattChar == null)
    	{
    		return;
    	}
    	mBluetoothGatt.setCharacteristicNotification(btGattChar, true);


		byte[] speedbuffer = {0,0,0,0,0};
		for (int i=0; i<5; i++)
		{
			speedbuffer[i] = 0;
		}
		// middle speed 0xFA, 0x07, 0x32, 0x00, 0xCF
		
		speedbuffer[0] = (byte)0xfa;
		speedbuffer[1] = 0x07;
		speedbuffer[2] = (byte)nSpeedPos;
		speedbuffer[3] = 0x00;
		speedbuffer[4] = (byte)(speedbuffer[0] ^ speedbuffer[1] ^ speedbuffer[2] ^ speedbuffer[3]);
		//speedbuffer[4] = (byte) 0xcf;
		
		mBluetoothGatt.beginReliableWrite();
		btGattChar.setValue(speedbuffer);
		btGattChar.setWriteType(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE);
		Boolean writechar = mBluetoothGatt.writeCharacteristic(btGattChar);
		mBluetoothGatt.readCharacteristic(btGattChar);			
	}
	class BluetoothSeek implements OnSeekBarChangeListener{

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
			
		}
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			int nSpeedPos = seekBar.getProgress(); 
			SetSpeed(nSpeedPos);
			Log.i("modelSpiner", "onStopTrackingTouch");			
			execut();										
		}		
	}	
	//******************************************************
		// Method:     scanLeDevice
		// Access:     public 
		// Returns:    void
		// Parameter:  
		// Note:	  开始扫描设备
		// Author      muzongcun  2012/11/12 create
		//*******************************************************
	    
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
		private void scanLeDevice(final boolean enable) {
	    	
			// Use this check to determine whether BLE is supported on the device. Then
			// you can selectively disable BLE-related features.
			if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			    Toast.makeText(this, "抱歉，本机不支持蓝牙连接设备功能", Toast.LENGTH_LONG).show();
			    return;
			}
			// Initializes Bluetooth adapter.
			final BluetoothManager bluetoothManager =
			        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
			 mBluetoothAdapter = bluetoothManager.getAdapter();
			 
			// Ensures Bluetooth is available on the device and it is enabled. If not,
			// displays a dialog requesting user permission to enable Bluetooth.
			if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
				View bluView = mLi.inflate(R.layout.bluetooth_dialog, null);
				Button po = (Button)bluView.findViewById(R.id.bluset);
				Button can = (Button)bluView.findViewById(R.id.blucancle);
				final Dialog alert = new Dialog(MainBefun.this,R.style.myDialog);
				alert.setContentView(bluView);
				if(!alert.isShowing())
		        alert.show();
		        po.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						alert.dismiss();
						Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
					}
				});
		        can.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						alert.dismiss();
					}
				});			    
			}
			else{
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2){
					final BluetoothAdapter.LeScanCallback mLeScanCallback =
				            new BluetoothAdapter.LeScanCallback() {
				    	//******************************************************
				    	// Method:     onLeScan
				    	// Access:     public 
				    	// Returns:    void
				    	// Parameter:  
				    	// Note:	  扫描设备回调函数
				    	//*******************************************************
				        @Override
				        public void onLeScan(final BluetoothDevice device, int rssi,
				                byte[] scanRecord) {
				            runOnUiThread(new Runnable() {
				               @Override
				               public void run() {
				                   //mLeDeviceListAdapter.addDevice(device);
				                  // mLeDeviceListAdapter.notifyDataSetChanged();
				            	   mDevice = device;
				            	   Log.i("SCAN", mDevice.getName());
				            	   Message msg = new Message();
				            	   msg.obj = mDevice.getName();
				            	   msg.what = STATE_SCAN;
				            	   mHandler1.sendMessage(msg);	            	
				               }
				           });
				       }
				    };
	        if (enable)
	        {	        	
	            // Stops scanning after a pre-defined scan period.
	            mainHandler.postDelayed(new Runnable() 
	            {
	                @Override
	                public void run() {
	                    mScanning = false;
	                    renewImg.setVisibility(View.VISIBLE);
	    	    		renewPro.setVisibility(View.GONE);
	                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
	                    if(!is_connected){
	                    	View bluView = mLi.inflate(R.layout.device_fail_dialog, null);
	        				Button po = (Button)bluView.findViewById(R.id.fail_po);
	        				final Dialog alert = new Dialog(MainBefun.this,R.style.myDialog);
	        				alert.setContentView(bluView);
	        		        alert.show();
	        		        po.setOnClickListener(new OnClickListener() {					
	        					@Override
	        					public void onClick(View v) {
	        						alert.dismiss();
	        					}
	        				});
	                    }
	                }
	            }, SCAN_PERIOD);

	            mScanning = true;
	            Log.v("scanLeDevice开始了","startLeScan");
	            renewImg.setVisibility(View.GONE);
	    		renewPro.setVisibility(View.VISIBLE);
	            mBluetoothAdapter.startLeScan(mLeScanCallback);
	        } else {
	            mScanning = false;
	            Log.v("stopLeScan开始了","startLeScan");
	            mBluetoothAdapter.stopLeScan(mLeScanCallback);
	        }
			}
			}
	    }

	    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
		void connectGatt()
		{
			final BluetoothGattCallback mGattCallback =
		            new BluetoothGattCallback() {
				@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
				@Override
		        public void onConnectionStateChange(BluetoothGatt gatt, int status,
		                int newState) {

		            String intentAction;
		            if (newState == BluetoothProfile.STATE_CONNECTED) {
		                intentAction = ACTION_GATT_CONNECTED;
		                mConnectionState = STATE_CONNECTED;
		                //broadcastUpdate(intentAction);
		                Log.i("conn", "Connected to GATT server.");
		                Log.i("conn", "Attempting to start service discovery:" +
		                        mBluetoothGatt.discoverServices());
		         	   	Message msg = new Message();
		        
		        	    msg.what = STATE_CONNECTED;
		        	    mHandler1.sendMessage(msg);
		                
		            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
		                intentAction = ACTION_GATT_DISCONNECTED;
		                mConnectionState = STATE_DISCONNECTED;
		                Log.i("disconn", "Disconnected from GATT server.");
		                broadcastUpdate(intentAction);
		                Message msg = new Message();
		        	    msg.what = STATE_DISCONNECTED;
		        	    mHandler1.sendMessage(msg);
		            }
		        }
		        @Override
		        //
		        public void onServicesDiscovered(BluetoothGatt gatt, int status) {	        	
		        	Log.i("discovered", "my onServicesDiscovered received: " + status);
		            if (status == BluetoothGatt.GATT_SUCCESS) {
		                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
		                Message msg = new Message();
		        	    msg.what = STATE_ServicesDiscovered;
		        	    mHandler1.sendMessage(msg);
		        	    //*************************************************************/
		        	    //*************************************************************/
		        	    //*************************************************************/
		        	    //*************************************************************/
		        	    
		            	List<BluetoothGattService> listService = gatt.getServices();
			        	UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
			        	UUID readUUID = UUID.fromString(kUUIDReadDevStatus);
			        	BluetoothGattService gattService       = null;
			        	BluetoothGattCharacteristic btGattChar = null;
			        	for (int i=0; i<listService.size(); i++)
			        	{
			        		BluetoothGattService tempgattService = listService.get(i);
			        		UUID deviceUUID = tempgattService.getUuid();
				        	if (deviceUUID.compareTo(kUUIDDeviceCommand) ==0) 
				        	{
				        		gattService = tempgattService;
				        		break;
				        	}
			        	}
			        	
			        	if (gattService == null)
			        	{
			        		return;
			        	}
			        
			        	List<BluetoothGattCharacteristic> gattChacterList = gattService.getCharacteristics();
			        	for (int i=0; i<gattChacterList.size(); i++)
			        	{
			        		BluetoothGattCharacteristic tempbtGattChar = gattChacterList.get(i);
			        		if (tempbtGattChar.getUuid().compareTo(readUUID) == 0 )
			        		{
			        			btGattChar = tempbtGattChar;
			        			break;
			        		}
			        		
			        	}
			        	if (btGattChar == null)
			        	{
			        		return;
			        	}
			        	
			        	byte []readValue = btGattChar.getValue();
			        	//Log.i("ReadValue", readValue.toString());
			        	
		        	    //*************************************************************/
		        	    //*************************************************************/
		        	    //*************************************************************/
		        	    //*************************************************************/
		            } else {
		                Log.i("discovered", "onServicesDiscovered received: " + status);
		            }
		        }
		    	//******************************************************
		    	// Method:     onCharacteristicRead
		    	// Access:     public 
		    	// Returns:    void
		    	// Parameter:  
		    	// Note:	  Result of a characteristic read operation
		    	//*******************************************************
		        @Override
		        public void onCharacteristicRead(BluetoothGatt gatt,
		                BluetoothGattCharacteristic characteristic,
		                int status) {
		        	Log.i("onCharacteristicRead", "onCharacteristicRead: " + status + characteristic);
		            if (status == BluetoothGatt.GATT_SUCCESS) {
		                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		            	Log.i("discovered", "onServicesDiscovered received: " + status + characteristic);
		            }
		        }
		    	//******************************************************
		    	// Method:     onDescriptorWrite
		    	// Access:     public 
		    	// Returns:    void
		    	// Parameter:  
		    	// Note:	  Result of a characteristic read operation
		    	//*******************************************************
		        @Override
		        public void  onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) 
		        {
		        	Log.i("onDescriptorWrite", "onDescriptorWrite: " + status + descriptor);
		        }
		    	//******************************************************
		    	// Method:     onCharacteristicChanged
		    	// Access:     public 
		    	// Returns:    void
		    	// Parameter:  
		    	// Note:	  Result of a characteristic read operation
		    	//*******************************************************
		        @Override
		        public void onCharacteristicChanged(BluetoothGatt gatt,
		                BluetoothGattCharacteristic characteristic) {
		           	Log.i("onCharacteristicChanged", "onCharacteristicChanged");
		            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
		        }
		        
		    };
			mBluetoothGatt = mDevice.connectGatt(this, false, mGattCallback);			
		}
		
		//******************************************************
		// Method:     BluetoothGattCallback
		// Access:     public 
		// Returns:    void
		// Parameter:  
		// Note:	    Various callback methods defined by the BLE API.
		//*******************************************************
	    // 
	    
		
		private Object UUID_HEART_RATE_MEASUREMENT;

		private Object UUID_HEART_RATE_MEASUREMENT1;
	    
	    private void broadcastUpdate(final String action) {
	        final Intent intent = new Intent(action);
	        sendBroadcast(intent);
	    }
	    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
		private void broadcastUpdate(final String action,
	            final BluetoothGattCharacteristic characteristic) {
	    		final Intent intent = new Intent(action);

	    		// This is special handling for the Heart Rate Measurement profile. Data
	    		// parsing is carried out as per profile specifications.
	    		UUID uid = characteristic.getUuid();
	    		Log.i("uuid", uid.toString());
	    		if (UUID_HEART_RATE_MEASUREMENT1.equals(characteristic.getUuid())) {
	    			int flag = characteristic.getProperties();
	    			int format = -1;
	    			if ((flag & 0x01) != 0) {
	    				format = BluetoothGattCharacteristic.FORMAT_UINT16;
	    				Log.d("broadcastUpdate", "Heart rate format UINT16.");
	    			} else {
	    				format = BluetoothGattCharacteristic.FORMAT_UINT8;
	    				Log.d("broadcastUpdate", "Heart rate format UINT8.");
	    			}
	    			final int heartRate = characteristic.getIntValue(format, 1);
	    			Log.d("", String.format("Received heart rate: %d", heartRate));
	    			//intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
	    		} else {
	    			// For all other profiles, writes the data formatted in HEX.
	    			final byte[] data = characteristic.getValue();
	    			if (data != null && data.length > 0) {
	    				final StringBuilder stringBuilder = new StringBuilder(data.length);
	    				for(byte byteChar : data)
	    					stringBuilder.append(String.format("%02X ", byteChar));
	    				//intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
	    				//		stringBuilder.toString());
	    			}
	    		}
	    		sendBroadcast(intent);
	    }
	    
		//******************************************************
		// Method:     close
		// Access:     public 
		// Returns:    void
		// Parameter:  
		// Note:	  close ble
		//*******************************************************
	    public void close() {
	        if (mBluetoothGatt == null) {
	            return;
	        }
	        mBluetoothGatt.close();
	        mBluetoothGatt = null;
	    }
    
	    
		private void toas(String content){
			Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
		}
		
	    public void blue_connect(View v){
	    	if(!mScanning){
	    		toas("开始搜索设备");
	    		scanLeDevice(true);	    		
	    	}			
		}	   
}
