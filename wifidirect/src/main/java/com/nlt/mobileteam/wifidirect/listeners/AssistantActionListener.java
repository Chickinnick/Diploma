package com.nlt.mobileteam.wifidirect.listeners;

import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorConnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.model.event.director.NotifyDeviceList;

import org.greenrobot.eventbus.Subscribe;

/**
 * Created by Nick on 02.05.2017.
 */

public abstract class AssistantActionListener implements WifiDirectActionListener {

    @Subscribe
    public abstract void directorConnected(DirectorConnect event);

    @Subscribe
    public abstract void directorDisconnected(DirectorDisconnect event);

}

