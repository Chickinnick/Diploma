package com.nlt.mobileteam.wifidirect.utils;

import android.net.wifi.p2p.WifiP2pDevice;

import com.nlt.mobileteam.wifidirect.controller.CommunicatorList;
import com.nlt.mobileteam.wifidirect.model.WiFiP2pService;

import java.util.ArrayList;
import java.util.List;

/**
 * The class provides opportunity work with the list of device connections  using familiar
 * the {@code java.util.List} methods.
 * <p>
 * The class contains {@code List<WiFiP2pService>} which considers as empty when all its elements
 * contain {@link #emptyCommunicator}.
 * <p>
 * List`s size sets in the constructor {@link #devicesCount}.
 */
public class DeviceList extends CommunicatorList<WiFiP2pService> {

    public DeviceList(int devicesCount) {
        super(devicesCount);
    }

    @Override
    public WiFiP2pService getEmptyCommunicator() {
        WiFiP2pService emptyService = new WiFiP2pService();
        emptyService.device = new WifiP2pDevice();
        emptyService.instanceName = "empty";

        return emptyService;
    }

    /**
     * Returns new List of Services what has all not empty devices
     */
    public List<WiFiP2pService> getTrimList() {
        List<WiFiP2pService> result = new ArrayList<>();
        for (WiFiP2pService service : this) {
            if (!this.isEmptyCommunicator(service)) {
                result.add(service);
            }
        }

        return result;
    }
}
