package com.befun.service;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.befun.db.FriendProvider;
import com.befun.entity.Friend;
import com.befun.exception.XXException;
import com.befun.util.NetUtil;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;

public class MainService extends Service {
	private int randomValue;
	public boolean is_kaigeer;
	private BluetoothGatt mBluetoothGatt = null;
	public final static String kServiceUUID = "713D0000-503E-4C75-BA94-3148F18D941E";
	  //0x713D0002503E4C75BA943148F18D941E
	public final static String kUUIDReadDevStatus = "713D0002-503E-4C75-BA94-3148F18D941E";
	  //0x713D0003503E4C75BA943148F18D941E
	public final static String kUUIDWriteCommand = "713D0003-503E-4C75-BA94-3148F18D941E";
	private UUID kUUIDDeviceCommand = UUID.fromString(kServiceUUID);
	private UUID writeUUID = UUID.fromString(kUUIDWriteCommand);
	private BluetoothGattService gattService       = null;
	private BluetoothGattCharacteristic btGattChar = null;
	private Timer kaigeerTimer;
	private TimerTask kaiTimerTask;
	private IBinder binder = new XXBinder();
	private SmackImpl mSmackable;
	private Thread mConnectingThread;
	private IConnectionStatusCallback mConnectionStatusCallback;
	public static final int CONNECTED = 0;
	public static final int DISCONNECTED = -1;
	public static final int CONNECTING = 1;
	private static final int RECONNECT_AFTER = 5;
	private int mConnectedState = DISCONNECTED;
	private int mReconnectTimeout = RECONNECT_AFTER;	
	private Handler mMainHandler = new Handler();
	public void registerConnectionStatusCallback(IConnectionStatusCallback cb) {
		mConnectionStatusCallback = cb;
	}

	public void unRegisterConnectionStatusCallback() {
		mConnectionStatusCallback = null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return binder;
	}
	public class XXBinder extends Binder {
		public MainService getService() {
			Log.v("Service bind------>","success");
			return MainService.this;
		}
	}
	public void login(final String username,final String password){
		if(NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE){
			if(mConnectionStatusCallback != null){
				mConnectionStatusCallback.connectionStatusChanged(9, "网络状况不太好，连接失败了");
				return;
			}
		}
		if(TextUtils.isEmpty(username))return;
		if (mConnectingThread != null) {
			Log.v("------->","a connection is still goign on!");
			return;
		}
		mConnectingThread = new Thread() {
			@Override
			public void run() {
				try {
					postConnecting();
					mSmackable = new SmackImpl(MainService.this);
					if (mSmackable.login(username, password)) {						
						Log.i("登陆成功了", "登陆成功了");
						postConnectionScuessed();
					} else {
						postConnectionFailed("LOGIN_FAILED");
					}
				} catch (XXException e) {
					String message = e.getLocalizedMessage();
					if (e.getCause() != null)
						message += "\n" + e.getCause().getLocalizedMessage();
					//postConnectionFailed(message);
					Log.v("exception", "YaximXMPPException in doConnect():"+message);
					postConnectionFailed(message);
					e.printStackTrace();
				} finally {
					if (mConnectingThread != null)
						synchronized (mConnectingThread) {
							mConnectingThread = null;
						}
				}
			}

		};
		mConnectingThread.start();
	}
	private void postConnectionScuessed() {
		mMainHandler.post(new Runnable() {
			public void run() {
				connectionScuessed();
			}

		});
	}

	private void connectionScuessed() {
		mConnectedState = CONNECTED;
		mReconnectTimeout = RECONNECT_AFTER;{
		if (mConnectionStatusCallback != null)
			mConnectionStatusCallback.connectionStatusChanged(mConnectedState,"登陆成功");
		}
	}
	private void postConnecting() {
		// TODO Auto-generated method stub
		mMainHandler.post(new Runnable() {
			public void run() {
				connecting();
			}
		});		
	}
	public void postConnectionFailed(final String reason) {
		mMainHandler.post(new Runnable() {
			public void run() {
				connectionFailed(reason);
			}
		});
	}
	private void connectionFailed(String reason){
		mConnectedState = DISCONNECTED;
		if (mConnectionStatusCallback != null) {
			mConnectionStatusCallback.connectionStatusChanged(mConnectedState,
					reason);
		}
	}
	private void connecting() {
		// TODO Auto-generated method stub
		mConnectedState = CONNECTING;
		if (mConnectionStatusCallback != null){
			mConnectionStatusCallback.connectionStatusChanged(mConnectedState,"");}
	}
	
	@Override
	public void onDestroy() {
		Log.v("SERvice","service----destroy");
		logout();
	}
	public boolean logout(){
		if(mSmackable != null){
			mSmackable.logout();
			mSmackable = null;
		}
		return false;	
	}
	public boolean isAuthenticated() {
		if (mSmackable != null) {
			return mSmackable.isAuthenticated();
		}

		return false;
	}
	public void sendMessage(String user, String message,int type) {
		if (mSmackable != null){
				mSmackable.sendMessage(user, message,type);		
		if(!mSmackable.isAuthenticated())
			mConnectionStatusCallback.connectionStatusChanged(9, "糟糕，掉线了");}
			
	}
	public void addFriendtoDB(final Friend friend){				
		new Thread(){
			@Override
			public void run() {
				ContentValues values = new ContentValues();
				values.put("username", friend.befunId);
				values.put("gender", friend.gender);
				values.put("nickname", friend.nickName);
				values.put("is_read", 0);
				getContentResolver().insert(FriendProvider.CONTENT_URI, values);
			}			
		}.start();
	}
	public void newMessage(String Jid,String nickName){
		mMainHandler.post(new Runnable() {		
			@Override
			public void run() {
				Vibrator vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
				long[] pattern = {200,250,90,200};
				vibrator.vibrate(pattern, -1);
			}
		});	
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	public void startKaigeer(BluetoothGatt mBluetoothGattS){
		mBluetoothGatt = mBluetoothGattS;		
		if(kaigeerTimer == null)
			kaigeerTimer = new Timer();				
		if(kaiTimerTask == null)
			kaiTimerTask = new KaiTimerTask();
		Random random = new Random();
		randomValue = random.nextInt(3)%3 + 1;	
		kaigeerTimer.schedule(kaiTimerTask, 10, randomValue*1000+500);
	}
	public void cancelKaigeer(){
		if(kaigeerTimer != null){
			kaigeerTimer.cancel();
			kaigeerTimer = null;
		}
		if(kaiTimerTask != null){
			kaiTimerTask.cancel();
			kaiTimerTask = null;
		}
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	class KaiTimerTask extends TimerTask{
		@Override
		public void run() {
			if (mBluetoothGatt != null)
			{	
				SetModel(6);
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//SetSpeed(80);
				execut();				
			}
		}		
	}
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	void SetModel(int modelNo)
	{
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
    	final byte[] model_buffer = {0,0,0,0,0};
		for (int i=0; i<5; i++)
		{
			model_buffer[i] = 0;
		}
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
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void execut(){
		mMainHandler.postDelayed(new Runnable() {			
			@Override
			public void run() {
				if (mBluetoothGatt != null)
				{
					Boolean execWrite = mBluetoothGatt.executeReliableWrite();			
					Log.i("execWrite", execWrite.toString());
				}          
			}
		}, 80);
	}
}
