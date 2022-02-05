package com.example.myapplication;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.List;

public class GPSTracker implements LocationListener {
    Context context;
    public GPSTracker(Context c){
        context = c;
    }

    public Location getLocation(){
        Location bestLocation = null;

        if((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                &&(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            Toast.makeText(context,"Разрешение не предоставлено",Toast.LENGTH_LONG).show();
        }else{
            LocationManager  mLocationManager = (LocationManager)context.getSystemService(LOCATION_SERVICE);
            List<String> providers = mLocationManager.getProviders(true);
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,0,this);


            for (String provider : providers) {
                Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }
            }
        }

        return bestLocation;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

