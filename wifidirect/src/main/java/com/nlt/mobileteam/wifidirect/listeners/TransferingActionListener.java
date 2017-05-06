package com.nlt.mobileteam.wifidirect.listeners;


import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorConnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.DirectorDisconnect;
import com.nlt.mobileteam.wifidirect.model.event.assistant.OwnerName;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Abort;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Progress;
import com.nlt.mobileteam.wifidirect.model.event.transfer.Success;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TransferingActionListener implements WifiDirectActionListener {
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void fileAborted(Abort event){};

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doInProgress(Progress progress){};

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSuccessed(Success event){};
}
