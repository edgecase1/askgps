package at.catnet.askgps;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

public class Journal implements Runnable {
	/** android TAG (logging) */
	public static final String TAG = "journal";
	/** locations */
	private Map<Long, Location> locations = new HashMap<Long, Location>();
	/** last known location */
	private Location top;
	
	/** min distance */
	static float MIN_DISTANCE = 10;
	/** upload interval in seconds */
	static int UPLOAD_INTERVAL = 10;
	/** URL */
	static String URL = "http://69.162.82.11:10080/ccc/post";
	
	private Sender sender;
	
	private StateManager stateManager;
	
	/** constructor
	 * thread preparation
	 */
	public Journal() {
		Thread t = new Thread(this);
		Log.v(TAG, "thread initialized");
		t.start();
	}
	
	/** add data to list */
	private synchronized void addData(Location coord){
		Log.i(TAG, "new data! adding to list");
		locations.put(coord.getTime(), coord);
		top = coord;
	}
	
	/** return a set of the current keys */
	private synchronized Map<Long, Location> getLocations(){
		HashMap<Long, Location> copy = new HashMap<Long, Location>();
		for(Entry<Long, Location> e : locations.entrySet()){
			copy.put(e.getKey(), e.getValue());
		}
		return copy;
	}	
	
	public void put(Location coord){
		Log.v(TAG, "new coord");
		// only check last position if one is known
		if(top != null){
			float dist = coord.distanceTo(top);
			// check if it is in the range
			if(dist < MIN_DISTANCE){
				// ignore new coord
				Log.v(TAG, "distance " + dist + "m too small (min=" + MIN_DISTANCE + ")");
				return;
			}else{
				Log.d(TAG, "distance " + dist + "m acceptable");
			}
		}else{
			// first data
			Log.v(TAG, "first data => top");
		}
		// new data!
		
		// update state
		stateManager.move();
		
		// update data
		addData(coord);
	}
	
	/** mark last known position as point of interest */
	public void putPoi() {
		if(top == null){
			Log.e(TAG, "no top coord, poi not possible");
			return;
		}
		
		Log.v(TAG, "putPoi");
		Bundle b = new Bundle();
		b.putChar("type", 's');
		Location l = new Location(top);
		l.setExtras(b);
		
		addData(l);
	}
	
	private synchronized void remove(Set<Long> list){
		Log.i(TAG, "cleanup: removing items");
		for(Long i : list){
			locations.remove(i);
		}
	}
	
	/** upload data to mothership */
	public boolean upload(){
		Log.v(TAG, "upload initialized");
		Map<Long, Location> locToRepl = getLocations();
		if(locToRepl.isEmpty()){
			Log.i(TAG, "no locations to upload");
			return true;
		}
		
		// upload to mothership
		try {
			// encode list for sending
			Log.i(TAG, "encoding for upload");
			sender.encode(locToRepl);
			// upload
			Log.i(TAG, "upload over http");
			HttpResponse response = sender.send();
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode != HttpStatus.SC_OK){
				Log.e(TAG, "error sending: bad status code: " + statusCode);
				return false;
			}
		} catch (JSONException e){
			Log.e(TAG, "error encoding: " + e.getMessage());
		} catch (ClientProtocolException e) {
			Log.e(TAG, "error sending: ClientProtocolException " + e.getMessage());
			return false;
		} catch (IOException e) {
			Log.e(TAG, "error sending: IOException " + e.getMessage());
			return false;
		}
		
		// remove uploaded keys
		Log.v(TAG, "cleanup");
		remove(locToRepl.keySet());
		
		// successful
		Log.v(TAG, "upload done");
		return true;
	}

	@Override
	public void run() {
		Log.i(TAG, "journal running");
		stateManager =  new StateManager(this);
		
		Log.i(TAG, "connecting to server");
		sender = new Sender(URL);
		sender.connect();
		
		while(true){
			Log.v(TAG, "journal wakeup; upload");
			upload();
			try {
				Log.v(TAG, "sleeping");
				Thread.currentThread().sleep(UPLOAD_INTERVAL*1000);
			} catch (InterruptedException e) {
				Log.e(TAG, "InterruptedException " + e.getMessage());
			}
		}
	}
	
}
