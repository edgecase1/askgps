package at.catnet.askgps;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.TextView;

public class Main extends Activity {
    
	/** status-Text field */
	TextView status;
	
	Journal journal = new Journal();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        status = (TextView) findViewById(R.id.status);
        
        enableTracking();
    }
    
    public void enableTracking(){
    	// Acquire a reference to the system Location Manager
    	LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

    	// Define a listener that responds to location updates
    	LocationListener locationListener = new LocationListener() {
    	    public void onLocationChanged(Location location) {
    	      // Called when a new location is found by the network location provider.
    	      //makeUseOfNewLocation(location);
    	    	journal.put(location);
    	    }

    	    public void onStatusChanged(String provider, int status, Bundle extras) {
    	    	
    	    }

    	    public void onProviderEnabled(String provider) {
    	    	
    	    }

    	    public void onProviderDisabled(String provider) {
    	    	
    	    }
    	  };

    	// Register the listener with the Location Manager to receive location updates
    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
    }
}