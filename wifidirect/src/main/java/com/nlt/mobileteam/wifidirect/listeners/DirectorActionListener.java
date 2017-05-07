package com.nlt.mobileteam.wifidirect.listeners;


import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public abstract class DirectorActionListener implements WifiDirectActionListener {


    @Subscribe
    public  void handleDeviceList(NotifyDeviceList event){

    };
}
