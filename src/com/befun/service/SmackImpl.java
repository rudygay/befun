package com.befun.service;

import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Message.Type;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.ServiceDiscoveryManager;
import org.jivesoftware.smackx.carbons.Carbon;
import org.jivesoftware.smackx.forward.Forwarded;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.ping.provider.PingProvider;
import org.jivesoftware.smackx.provider.DelayInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

import com.befun.activity.ChatActivity;
import com.befun.db.FriendProvider;
import com.befun.db.MessageProvider;
import com.befun.entity.DownloadImageTask;
import com.befun.entity.Friend;
import com.befun.exception.XXException;
import com.befun.util.ImageParser;
import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;
import com.befun.util.VcardParser;

public class SmackImpl {
	public static final String XMPP_IDENTITY_NAME = "x";
	public static final String XMPP_IDENTITY_TYPE = "phone";
	private MainService mainService;
	private ConnectionConfiguration mXMPPConfig;
	private XMPPConnection mXMPPConnection;
	private PacketListener mPacketListener; 
	private PacketListener mSendFailureListener;
	private TaxiConnectionListener connectionListener;
	private final ContentResolver mContentResolver;
	
	static {
		registerSmackProviders();
	}

	static void registerSmackProviders() {
		ProviderManager pm = ProviderManager.getInstance();
		// add IQ handling
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// add delayed delivery notifications
		pm.addExtensionProvider("delay", "urn:xmpp:delay",
				new DelayInfoProvider());
		pm.addExtensionProvider("x", "jabber:x:delay", new DelayInfoProvider());
		// add carbons and forwarding
		pm.addExtensionProvider("forwarded", Forwarded.NAMESPACE,
				new Forwarded.Provider());
		pm.addExtensionProvider("sent", Carbon.NAMESPACE, new Carbon.Provider());
		pm.addExtensionProvider("received", Carbon.NAMESPACE,
				new Carbon.Provider());
		// add delivery receipts
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT,
				DeliveryReceipt.NAMESPACE, new DeliveryReceipt.Provider());
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT,
				DeliveryReceipt.NAMESPACE,
				new DeliveryReceiptRequest.Provider());
		// add XMPP Ping (XEP-0199)
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());
		pm.addIQProvider("ping", "urn:xmpp:ping", new PingProvider());
		ServiceDiscoveryManager.setIdentityName(XMPP_IDENTITY_NAME);
		ServiceDiscoveryManager.setIdentityType(XMPP_IDENTITY_TYPE);
	}

	
	public SmackImpl(MainService mainService){
		String post = PreferenceConstants.xmppHost;
		int port = PreferenceConstants.xmppPort;
		this.mXMPPConfig = new ConnectionConfiguration(post, port);
		this.mXMPPConfig.setReconnectionAllowed(false);
		this.mXMPPConfig.setSendPresence(false);
		this.mXMPPConfig.setCompressionEnabled(false);
		this.mXMPPConfig.setSASLAuthenticationEnabled(false);  
		this.mXMPPConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled); 
		this.mXMPPConnection = new XMPPConnection(mXMPPConfig);
		this.mainService = mainService;
		mContentResolver = mainService.getContentResolver();
	}
	public boolean login(String username,String password) throws XXException{
		try {
			if (mXMPPConnection.isConnected()) {
				try {
					mXMPPConnection.disconnect();
				} catch (Exception e) {
					Log.v("connect----->","conn.disconnect() failed: ");
				}
			}
				SmackConfiguration.setPacketReplyTimeout(30000);
				SmackConfiguration.setKeepAliveInterval(-1);
				SmackConfiguration.setDefaultPingInterval(0);
				mXMPPConnection.connect();
				if (!mXMPPConnection.isConnected()) {
					Log.v("mXMPPConnection","连接失败");
					throw new XXException("SMACK connect failed without exception!");					
				}
				connectionListener = new TaxiConnectionListener();    
				mXMPPConnection.addConnectionListener(connectionListener);
				if (!mXMPPConnection.isAuthenticated()) {					
					mXMPPConnection.login(username, password);
					Log.v("SmackImpl","mXMPPConnection.login(username, password);");
				}
		} catch (XMPPException e) {
			throw new XXException(e.getLocalizedMessage(),
					e.getWrappedThrowable());
		} catch (Exception e) {
			// actually we just care for IllegalState or NullPointer or XMPPEx.
			Log.e("exception-----", "login_eexception");
			throw new XXException(e.getLocalizedMessage(), e.getCause());
		}
		Presence presence = new Presence(Presence.Type.available);       //通知为登录状态  
        mXMPPConnection.sendPacket(presence);
		registerAllListener();
		return mXMPPConnection.isAuthenticated();
		}
	private void registerAllListener(){		
		if (isAuthenticated()) {
			Log.d("registerlistener----->","success");
			registerMessageListener();
			registerMessageSendFailureListener();
			if (mainService == null) {
				mXMPPConnection.disconnect();
				return;
			}
		}
	} 
	private void registerMessageListener() {
		// do not register multiple packet listeners
		if (mPacketListener != null)
			mXMPPConnection.removePacketListener(mPacketListener);
		PacketTypeFilter filter = new PacketTypeFilter(Message.class);

		mPacketListener = new PacketListener() {			
			public void processPacket(Packet packet) {
				try {
					if (packet instanceof Message) {						
						Message msg = (Message)packet;
						String fromJID = msg.getFrom();
						Log.v("chat!!!", msg.toXML());
						long ts;
						if(msg.getProperty("date") != null){
							Double tscon = Double.parseDouble((String)msg.getProperty("date"));
							ts = tscon.longValue();
						}
						else
							ts = System.currentTimeMillis();
						if(TextUtils.equals(msg.getProperty("message_type").toString(),"0")){
							String chatMessage = msg.getBody();
							if (chatMessage == null) {
								return;
							}
							if (msg.getType() == Message.Type.error) {
								chatMessage = "<Error> " + chatMessage;
							}
											
							Log.v("fromJID",fromJID);
							is_friend(fromJID.split("@")[0],fromJID,true);						
							addChatMessageToDB(MessageProvider.INCOMING, fromJID.split("@")[0], chatMessage,
									MessageProvider.MESSAGE_TXT, ts);}
						if(TextUtils.equals(msg.getProperty("message_type").toString(),"999")){
							mainService.getContentResolver().delete(FriendProvider.CONTENT_URI
									, "username = ?",  new String[]{fromJID.split("@")[0]});
						}
						if(TextUtils.equals(msg.getProperty("message_type").toString(),"1")){
							String chatMessage = msg.getBody();
							if (chatMessage == null) {
								return;
							}
							if (msg.getType() == Message.Type.error) {
								chatMessage = "<Error> " + chatMessage;
								return;
							}
							String url = new ImageParser().parse(chatMessage);
							DownloadImageTask task = new DownloadImageTask
									(ts, mainService.getContentResolver(),mainService,fromJID); 
							task.execute(url);
							is_friend(fromJID.split("@")[0],fromJID,false);
						}
					}
				} catch (Exception e) {
					// SMACK silently discards exceptions dropped from
					// processPacket :(
					Log.e("message_listener","failed to process packet:");
					e.printStackTrace();
				}
			}
		};
		mXMPPConnection.addPacketListener(mPacketListener,filter);
	}
	private void is_friend(String username,String fromJID,boolean is_instant) throws XMPPException{
		Cursor cursor = mainService.getContentResolver().query(FriendProvider.CONTENT_URI,
				null, "username = ?", new String[]{username}, null);
		if(cursor.getCount() == 0){
			ContentValues values = new ContentValues();
			VCard vcard = new VCard();
			vcard.load(mXMPPConnection,fromJID.split("/")[0]);
			Friend friend = null;
			try {
				friend = new VcardParser().parse(vcard.toXML());
			} catch (Exception e) {				
				e.printStackTrace();
			}
			if(friend != null){
			values.put("username", username);
			values.put("gender", friend.gender);
			values.put("nickname", friend.nickName);
			values.put("is_read", 1);
			mContentResolver.insert(FriendProvider.CONTENT_URI, values);
			if(is_instant)
			mainService.newMessage(fromJID, vcard.getNickName());}
		}
		else{
			if(ChatActivity.instance != null){
				if(!TextUtils.equals(username, ChatActivity.instance.friend.befunId)){
					ContentValues values = new ContentValues();
					values.put("is_read", 1);
					mContentResolver.update(FriendProvider.CONTENT_URI
					, values, "username = ?", new String[]{username});
					}
				}
			else{
				ContentValues values = new ContentValues();
				values.put("is_read", 1);
				mContentResolver.update(FriendProvider.CONTENT_URI
				, values, "username = ?", new String[]{username});
			}
			cursor.moveToFirst();
			String nickname = cursor.getColumnName(4);
			if(is_instant)
			mainService.newMessage(fromJID, nickname);
		}
	}
	private void registerMessageSendFailureListener(){
		// do not register multiple packet listeners
		if (mSendFailureListener != null)
			mXMPPConnection.removePacketSendFailureListener(mSendFailureListener);

		PacketTypeFilter filter = new PacketTypeFilter(Message.class);

		mSendFailureListener = new PacketListener() {
			public void processPacket(Packet packet) {
				try {
					if (packet instanceof Message) {
						Message msg = (Message) packet;
						String chatMessage = msg.getBody();

						Log.d("SmackableImp",
								"message "
										+ chatMessage
										+ " could not be sent (ID:"
										+ (msg.getPacketID() == null ? "null"
												: msg.getPacketID()) + ")");
						//changeMessageDeliveryStatus(msg.getPacketID(),ChatConstants.DS_NEW);
					}
				} catch (Exception e) {
					// SMACK silently discards exceptions dropped from
					// processPacket :(
					Log.e("send--fail","failed to process packet:");
					e.printStackTrace();
				}
			}
		};

		mXMPPConnection.addPacketSendFailureListener(mSendFailureListener,
				filter);
	}
	public void sendMessage(String toJID, String message,int type) {
		// TODO Auto-generated method stub
		Log.v("send",toJID);
		//final Message newMessage = new Message(toJID, Message.Type.chat);	
		Message newMessage = new Message();
		newMessage.setTo(toJID);
		newMessage.setBody(message);
		newMessage.setType(Type.chat);
		newMessage.setProperty("date", String.valueOf(System.currentTimeMillis()));		
		switch (type) {
		case MessageProvider.MESSAGE_TXT:
			newMessage.setProperty("message_type", "0");
			break;
		case MessageProvider.MESSAGE_PIC:
			newMessage.setProperty("message_type", "1");
			break;
		case MessageProvider.MESSAGE_CONTROL:
			newMessage.setProperty("message_type", "2");
			break;
		case MessageProvider.MESSAGE_YY:
			newMessage.setProperty("message_type", "3");
			break;
		case MessageProvider.MESSAGE_BIAOQING:
			newMessage.setProperty("message_type", "4");
			break;
		case MessageProvider.MESSAGE_DELETE:
			newMessage.setProperty("message_type", "999");
			break;
		default:
			break;
		}
		newMessage.addExtension(new DeliveryReceiptRequest());
		if (isAuthenticated()) {
			Log.v("time",System.currentTimeMillis()+"");
			if(type == MessageProvider.MESSAGE_TXT)
			addChatMessageToDB(MessageProvider.OUTGOING, toJID.split("@")[0], message,
					type, System.currentTimeMillis());
			mXMPPConnection.sendPacket(newMessage);
			Log.d("send-----success",newMessage.getBody());
		} else {
			Log.v("sendMessage","断线了");
			// send offline -> store to DB
			/*addChatMessageToDB(ChatConstants.OUTGOING, toJID, message,
					ChatConstants.DS_NEW, System.currentTimeMillis(),
					newMessage.getPacketID());*/
		}
	}
	public boolean isAuthenticated() {
		if (mXMPPConnection != null) {
			return (mXMPPConnection.isConnected() && mXMPPConnection
					.isAuthenticated());
		}
		return false;
	}
	private void addChatMessageToDB(int direction, String username, String message,int type
			, long ts){
		ContentValues values = new ContentValues();
		values.put(MessageProvider.MESSAGE_DIREC, direction);
		values.put("username", username);
		values.put(MessageProvider.MESSAGE_CONTENT, message);
		values.put(MessageProvider.MESSAGE_TYPE, type);
		values.put(MessageProvider.MESSAGE_DATE, ts);
		mContentResolver.insert(MessageProvider.CONTENT_URI,values);
	}
	public boolean logout(){
		try {
			mXMPPConnection.removePacketListener(mPacketListener);
			mXMPPConnection.removePacketListener(mSendFailureListener);
			mXMPPConnection.removeConnectionListener(connectionListener);
		} catch (Exception e) {
			// TODO: handle exception
			return false;
		}
		if(mXMPPConnection.isConnected()){
			new Thread(){
				@Override
				public void run() {
					mXMPPConnection.disconnect();
					Log.v("log_out","登出成功");
				}				
			}.start();
		}
		this.mainService = null;
		return true;		
	}
	class TaxiConnectionListener implements ConnectionListener {  
	    private Timer tExit; 
	    private ReconnectTimetask timerTask;
	    private String username;  
	    private String password;  
	    private int logintime = 3000;  
	    @Override  
	    public void connectionClosed() {  
	        Log.i("TaxiConnectionListener", "連接關閉");  
	        // 重连服务器
	        if(tExit == null)
	        	tExit = new Timer();
	        if(timerTask == null)
	        	timerTask = new ReconnectTimetask();
	        tExit.schedule(timerTask, logintime);  
	    }  
	  
	    @Override  
	    public void connectionClosedOnError(Exception e) {  
	        Log.i("TaxiConnectionListener", "連接關閉異常");  
	        // 判斷為帳號已被登錄  
	        boolean error = e.getMessage().equals("stream:error (conflict)");  
	        if (!error) {  
	            // 關閉連接  
	        	if(mXMPPConnection.isConnected())
	        	mXMPPConnection.disconnect();
	            // 重连服务器  
	        	if(tExit == null)
		        	tExit = new Timer();
		        if(timerTask == null)
		        	timerTask = new ReconnectTimetask();
		        tExit.schedule(timerTask, logintime);   
	        }  
	    }  
	  
	    class ReconnectTimetask extends TimerTask {  
	        @Override  
	        public void run() {  
	            username = PreferenceUtils.getPrefString(mainService, PreferenceConstants.USERNAME, "");
	            password = PreferenceUtils.getPrefString(mainService, PreferenceConstants.PASSWORD, ""); 
	            if (username != null && password != null) {  
	                Log.i("TaxiConnectionListener", "嘗試登錄@"+username+"@"+password);  
	                // 连接服务器  
	                boolean is_login = false;
	                try {
						is_login = login(username, password);  
						Log.i("TaxiConnectionListener", "登錄成功");  					      
						}
					catch (XXException e) {
						is_login = false;
						e.printStackTrace();
					}
	                if(!is_login){
	                	Log.i("TaxiConnectionListener", "重新登錄");  
	                	if(tExit == null)
	    		        	tExit = new Timer();
	    		        //if(timerTask == null)
	    		        	//timerTask = new ReconnectTimetask();
	    		        tExit.schedule(new ReconnectTimetask(), logintime); 
	                }
	                else{
	                	if(tExit != null){
	                		tExit.cancel();
	                		tExit = null;
	            		}
	            		if(timerTask != null){
	            			timerTask.cancel();
	            			timerTask = null;
	            		}	             
	                }
	            }  
	        }  
	    }  
	  
	    @Override  
	    public void reconnectingIn(int arg0) {  
	    }  
	  
	    @Override  
	    public void reconnectionFailed(Exception arg0) {  
	    }  
	  
	    @Override  
	    public void reconnectionSuccessful() { 
	    	if(tExit != null){
        		tExit.cancel();
        		tExit = null;
    		}
    		if(timerTask != null){
    			timerTask.cancel();
    			timerTask = null;
    		}	             
	    }  
	  
	}  
}
