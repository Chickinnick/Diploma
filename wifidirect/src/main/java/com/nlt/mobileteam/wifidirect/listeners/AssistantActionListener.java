package com.nlt.mobileteam.wifidirect.listeners;

import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorConnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


public class AssistantActionListener implements WifiDirectActionListener {

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void directorConnected(DirectorConnect event){};

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void directorDisconnected(DirectorDisconnect event){};

}

