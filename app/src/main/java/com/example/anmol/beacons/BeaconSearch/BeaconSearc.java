package com.example.anmol.beacons.BeaconSearch;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.anmol.beacons.MainActivity;
import com.example.anmol.beacons.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;

public class BeaconSearc extends Fragment implements BeaconConsumer{

    RelativeLayout rl;
    private RecyclerView rv;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    private BeaconManager beaconManager;
    private ProgressBar pb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        beaconManager = BeaconManager.getInstanceForApplication(getActivity());
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.bind(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.beacon_search, container, false);

        // intializing
        rl = v.findViewById(R.id.Relative_One);
        rv = v.findViewById(R.id.search_recycler);
        pb = v.findViewById(R.id.pb);
        return v;
    }

    @Override
    public void onBeaconServiceConnect() {

        final Region region = new Region("myBeaons",null, null, null);


        //Moniotor Notifier

        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                System.out.println("ENTER ------------------->");
                try {
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didExitRegion(Region region) {
                System.out.println("EXIT----------------------->");
                try {
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                System.out.println( "I have just switched from seeing/not seeing beacons: "+state);
            }
        });

        //Range Notifier
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    try{
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.INVISIBLE);
                                rl.setVisibility(View.GONE);
                                rv.setVisibility(View.VISIBLE);
                                layoutManager = new LinearLayoutManager(getActivity());
                                rv.setLayoutManager(layoutManager);
                            }
                       });
                    }
                    catch(Exception e){

                    }
                    final ArrayList<ArrayList<String>> arrayList = new ArrayList<ArrayList<String>>();
                    for (Beacon b:beacons){
                        //UUID
                        String uuid = String.valueOf(b.getId1());
                        //Major
                        String major = String.valueOf(b.getId2());
                        //Minor
                        String minor = String.valueOf(b.getId3());
                        //Distance
                        double distance1 =b.getDistance();
                        String distance = String.valueOf(Math.round(distance1*100.0)/100.0);
                        ArrayList<String> arr = new ArrayList<String>();
                        arr.add(uuid);
                        arr.add(major);
                        arr.add(minor);
                        arr.add(distance + " meters");
                        arrayList.add(arr);
                    }
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                adapter = new RecyclerAdapter(arrayList);
                                rv.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }catch(Exception e){

                    }
                }
                else if (beacons.size()==0) {
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                pb.setVisibility(View.INVISIBLE);
                                rl.setVisibility(View.VISIBLE);
                                rv.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {

                    }
                }
            }
        });
        try {
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {    }
    }

    @Override
    public Context getApplicationContext() {
        return getActivity().getApplicationContext();
    }

    @Override
    public void unbindService(ServiceConnection serviceConnection) {
        getActivity().unbindService(serviceConnection);
    }

    @Override
    public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
        return getActivity().bindService(intent, serviceConnection, i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }
}
