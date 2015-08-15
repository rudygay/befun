package com.befun.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

import com.befun.util.PreferenceConstants;
import com.befun.util.PreferenceUtils;

public class AcountRelated {
	private HttpClient httpClient;
	private HttpParams httpParams;
	public String createUser(String nickname,String gender){
		List<NameValuePair> params = new ArrayList<NameValuePair>(); 
		String createUrl = PreferenceConstants.createUrlStr;
		params.add(new BasicNameValuePair("nickname", nickname));
		params.add(new BasicNameValuePair("gender", gender));
		HttpPost httpRequest = new HttpPost(createUrl);
		String strResult = "0";
		getHttpClient();
		try {  
	        /* 添加请求参数到请求对象 */  
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));  
			/* 发送请求并等待响应 */  
			HttpResponse httpResponse = httpClient.execute(httpRequest);  
	            /* 若状态码为200 ok */  
	            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
	                /* 读返回数据 */  
	                strResult = EntityUtils.toString(httpResponse.getEntity());  
	            } else {  
	                strResult = "0";
	            }  
	        }
			catch (ClientProtocolException e) {  
				strResult = "0";
				e.printStackTrace();  
			} catch (IOException e) {  
				strResult = "0";
				e.printStackTrace();  
			}
	        catch (Exception e) {  
	            strResult = "0";
	            e.printStackTrace();  
	        }  
		return strResult;		
	}
	public String deleteUser(String username,boolean is_delete){
		List<NameValuePair> params = new ArrayList<NameValuePair>(); 
		String url;
		if(is_delete){
			url = PreferenceConstants.deleteUrlStr;
			params.add(new BasicNameValuePair("username", username));}
		else {url = PreferenceConstants.uploadpic;
			params.add(new BasicNameValuePair("img", username));}				
		HttpPost httpRequest = new HttpPost(url);
		String strResult = "0";
		getHttpClient();
		try {  
	        /* 添加请求参数到请求对象 */  
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));  
			/* 发送请求并等待响应 */  
			HttpResponse httpResponse = httpClient.execute(httpRequest);  
			Log.v("AcountRelateed",httpResponse.getStatusLine().getStatusCode()+"");
	            /* 若状态码为200 ok */  
	            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
	                /* 读返回数据 */  
	                strResult = EntityUtils.toString(httpResponse.getEntity());  
	            } else {  
	                strResult = "0";
	            }  
	        }
			catch (ClientProtocolException e) {  
				strResult = "0";
				e.printStackTrace();  
			} catch (IOException e) {  
				strResult = "0";
				e.printStackTrace();  
			}
	        catch (Exception e) {  
	            strResult = "0";
	            e.printStackTrace();  
	        }  
	        Log.v("strResult", strResult);
		return strResult;		
	}
	public String getRandom(String username,boolean is_random){
		String url = null;
		if(is_random){
			url = PreferenceConstants.getRandomUrlStr+"?username="+username;
		}
		else{
			url = PreferenceConstants.byIdUrlStr+"?username="+username;
		}
		HttpGet httpRequest = new HttpGet(url);  
        String strResult = "0";  
        getHttpClient();
        try {  
            /* 发送请求并等待响应 */ 
            HttpResponse httpResponse = httpClient.execute(httpRequest);  
            /* 若状态码为200 ok */  
            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
                /* 读返回数据 */  
                strResult = EntityUtils.toString(httpResponse.getEntity());  
            } else {  
            	strResult = "0"; 
            }  
        } catch (ClientProtocolException e) {  
            strResult = e.getMessage().toString();  
            e.printStackTrace();
            strResult = "0";
        } catch (IOException e) {  
            strResult = e.getMessage().toString();  
            e.printStackTrace(); 
            strResult = "0";
        } catch (Exception e) {  
            strResult = e.getMessage().toString();  
            e.printStackTrace();
            strResult = "0";
        }    
        return strResult;  		
	}
	public String deleteMe(String username){
		String url = PreferenceConstants.deleteMe+"?username="+username;
		HttpGet httpRequest = new HttpGet(url);  
        String strResult = "0";  
        getHttpClient();
        try {  
            /* 发送请求并等待响应 */ 
            HttpResponse httpResponse = httpClient.execute(httpRequest); 
            Log.v("httpcode",httpResponse.getStatusLine().getStatusCode()+"");
            /* 若状态码为200 ok */  
            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
                /* 读返回数据 */  
                strResult = EntityUtils.toString(httpResponse.getEntity());  
            } else {  
            	strResult = "0"; 
            }  
        } catch (ClientProtocolException e) {  
            strResult = e.getMessage().toString();  
            e.printStackTrace();
            strResult = "0";
        } catch (IOException e) {  
            strResult = e.getMessage().toString();  
            e.printStackTrace(); 
            strResult = "0";
        } catch (Exception e) {  
            strResult = e.getMessage().toString();  
            e.printStackTrace();
            strResult = "0";
        }    
        return strResult;  		
	}
	 public HttpClient getHttpClient() {  
	        // 创建 HttpParams 以用来设置 HTTP 参数（这一部分不是必需的）  
	        this.httpParams = new BasicHttpParams();  
	        // 设置连接超时和 Socket 超时，以及 Socket 缓存大小  
	        HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);  
	        HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);  
	        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);  
	        // 设置重定向，缺省为 true  
	        HttpClientParams.setRedirecting(httpParams, true);  
	        // 设置 user agent  
	        String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";  
	        HttpProtocolParams.setUserAgent(httpParams, userAgent);  
	        // 创建一个 HttpClient 实例  
	        // 注意 HttpClient httpClient = new HttpClient(); 是Commons HttpClient  
	        // 中的用法，在 Android 1.5 中我们需要使用 Apache 的缺省实现 DefaultHttpClient  
	        httpClient = new DefaultHttpClient(httpParams);  
	        return httpClient;  
	    }  
	 public String deleteFriend(String username,String deleteUsername){
			List<NameValuePair> params = new ArrayList<NameValuePair>(); 
			String createUrl = PreferenceConstants.deleteFriend;
			params.add(new BasicNameValuePair("username", username));
			params.add(new BasicNameValuePair("deleteUsername", deleteUsername));
			HttpPost httpRequest = new HttpPost(createUrl);
			String strResult = "0";
			getHttpClient();
			try {  
		        /* 添加请求参数到请求对象 */  
				httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));  
				/* 发送请求并等待响应 */  
				HttpResponse httpResponse = httpClient.execute(httpRequest);  
		            /* 若状态码为200 ok */  
		            if (httpResponse.getStatusLine().getStatusCode() == 200) {  
		                /* 读返回数据 */  
		                strResult = EntityUtils.toString(httpResponse.getEntity());  
		            } else {  
		                strResult = "0";
		            }  
		        }
				catch (ClientProtocolException e) {  
					strResult = "0";
					e.printStackTrace();  
				} catch (IOException e) {  
					strResult = "0";
					e.printStackTrace();  
				}
		        catch (Exception e) {  
		            strResult = "0";
		            e.printStackTrace();  
		        }  
			return strResult;		
		}
}
