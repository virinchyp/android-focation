package com.focation.android;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class MainActivity extends MapActivity implements LocationListener{

  private static final String TAG = "MainActivity=>>Info";
  
  private static final int TEN_SECONDS = 10000;
  private static final int TEN_METERS = 10;
  private static final int TWO_MINUTES = 1000 * 60 * 2;
  //UI handler codes.
  private static final int UPDATE_ADDRESS = 1;
  private static final int UPDATE_LATLNG = 2;
  
  LocationManager locMgr;
  MapController mapCtr;
  Geocoder geocoder;
  GeoPoint geopoint;
  
  MapView mapView;
  TextView locInfo;
  String providerName; 
  Handler mHandler;
  private Timer timer = new Timer();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    locInfo = (TextView) findViewById(R.id.locInfo);
    mapView = (MapView) findViewById(R.id.mapView);
    
    mapView.setBuiltInZoomControls(true);
    mapCtr = mapView.getController();
    mapCtr.setZoom(16);
    
    locMgr = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    final boolean gpsEnabled = locMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    // Location location = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    // Location location = locMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    geocoder = new Geocoder(this);
    
//    // Retrieve a list of location providers that have fine accuracy, no monetary cost, etc
//    Criteria criteria = new Criteria();
//    criteria.setAccuracy(Criteria.ACCURACY_FINE);
//    criteria.setCostAllowed(false);
//    providerName = locMgr.getBestProvider(criteria, true);
//    
//    if (!gpsEnabled) {
//      Log.i("gps enabled","false");
//      enableLocationSettings();
//    } else {
//      locMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0f, this);
//    }
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setAltitudeRequired(false);
    criteria.setBearingRequired(false);
    criteria.setCostAllowed(true);
    criteria.setPowerRequirement(Criteria.POWER_LOW);
    String provider = locMgr.getBestProvider(criteria, true);
    locMgr.requestLocationUpdates(provider, 10000, 10, this);
  }

  
  
  public void retrieveLocation() {
    Criteria criteria = new Criteria();
    criteria.setAccuracy(Criteria.ACCURACY_FINE);
    criteria.setAltitudeRequired(false);
    criteria.setBearingRequired(false);
    criteria.setCostAllowed(true);
    criteria.setPowerRequirement(Criteria.POWER_LOW);
    String provider = locMgr.getBestProvider(criteria, true);
    locMgr.requestLocationUpdates(provider, 0, 0, this);

}

  
  private void enableLocationSettings() {
    Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    startActivity(settingsIntent);
  }

  @Override
  protected boolean isRouteDisplayed() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void onLocationChanged(Location location) {
    // TODO Auto-generated method stub
    Log.d(TAG, "onLocationChanged with location " + location.toString());
    String text = String.format("Lat:\t %f\nLong:\t %f\nAlt:\t %f\nBearing:\t %f", location.getLatitude(), 
                  location.getLongitude(), location.getAltitude(), location.getBearing());
    locInfo.setText(text);
    
    try {
      List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 10); //<10>
      for (Address address : addresses) {
        this.locInfo.append("\n" + address.getAddressLine(0));
      }
      
      int latitude = (int)(location.getLatitude() * 1000000);
      int longitude = (int)(location.getLongitude() * 1000000);

      geopoint = new GeoPoint(latitude,longitude);
      
      //---Add a location marker---
      MapOverlay mapOverlay = new MapOverlay();
      List<Overlay> listOfOverlays = mapView.getOverlays();
      listOfOverlays.clear();
      listOfOverlays.add(mapOverlay); 
      
      mapCtr.animateTo(geopoint); 
      
    } catch (IOException e) {
      Log.e("LocateMe", "Could not get Geocoder data", e);
    }
  }

  @Override
  public void onProviderDisabled(String provider) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onProviderEnabled(String provider) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 10, this); 
  }

  @Override
  protected void onPause() {
    super.onPause();
    locMgr.removeUpdates(this); 
  }
  
  class MapOverlay extends com.google.android.maps.Overlay
  {
      @Override
      public boolean draw(Canvas canvas, MapView mapView, 
      boolean shadow, long when) 
      {
          super.draw(canvas, mapView, shadow);                   

          //---translate the GeoPoint to screen pixels---
          Point screenPts = new Point();
          mapView.getProjection().toPixels(geopoint, screenPts);

          //---add the marker---
          Bitmap bmp = BitmapFactory.decodeResource(
              getResources(), R.drawable.maker);            
          canvas.drawBitmap(bmp, screenPts.x, screenPts.y-34, null);         
          return true;
      }
  }

  
}
