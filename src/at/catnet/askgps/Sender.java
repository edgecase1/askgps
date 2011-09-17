package at.catnet.askgps;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;



public class Sender {
	public static final String TAG = "sender";
	
	DefaultHttpClient httpclient;
	private String URL;
	
	JSONObject list;
	
	public Sender(String URL) {
		this.URL = URL;
	}
	
	public void connect(){
		HttpParams params = new BasicHttpParams();
	    HttpConnectionParams.setStaleCheckingEnabled(params, false);
	    HttpConnectionParams.setConnectionTimeout(params, 20000);
	    HttpConnectionParams.setSoTimeout(params, 20000);
		httpclient = new DefaultHttpClient(params);
	}
	
	public void disconnect(){
		
	}
	
	public void reconnect(){
		disconnect();
		connect();
	}
	
	public HttpResponse send() throws ClientProtocolException, IOException {
		if(list == null){
			Log.e(TAG, "json is null");
			throw new NullPointerException();
		}
		
		HttpPost httpost = new HttpPost(URL);
		StringEntity se = new StringEntity(list.toString());
		httpost.setEntity(se);
		httpost.setHeader("Accept", "application/json");
		httpost.setHeader("Content-type", "application/json");
		//ResponseHandler<String> responseHandler = new BasicResponseHandler();
		HttpResponse response = httpclient.execute(httpost);
		return response;
	}

	public void encode(Map<Long, Location> locToRepl) throws JSONException {
		list = new JSONObject();
		for(Entry<Long, Location> i : locToRepl.entrySet()){
			JSONObject json = new JSONObject();
			// encode to JSON
			json.put("alt", i.getValue().getAltitude());
			json.put("lon", i.getValue().getLongitude());
			json.put("lat", i.getValue().getLatitude());
			json.put("type", i.getValue().getExtras().getChar("type"));
			list.put(String.valueOf(i.getValue().getTime()/1000), json);
		}
	}
}
