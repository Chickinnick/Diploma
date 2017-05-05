package com.nlt.mobileteam.wifidirect.listeners;

import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorConnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;


public class AssistantActionListener implements WifiDirectActionListener {

    @Subscribe
    public void directorConnected(DirectorConnect event){};

    @Subscribe
    public void directorDisconnected(DirectorDisconnect event){};

}

