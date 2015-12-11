// 监听wifi连接状态
private void logNetworkInfo() {
    IntentFilter intentFilter = new IntentFilter();
    intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo info = (NetworkInfo) intent.getParcelableExtra(
                        WifiManager.EXTRA_NETWORK_INFO);
                // DISCONNECTED
                // OBTAINING_IPADDR
                // VERIFYING_POOR_LINK
                // CAPTIVE_PORTAL_CHECK
                // CONNECTED
                // 具体见NetworkInfo.DetailedState
                Log.i("NetworkInfo", info.getDetailedState().name());
            }
        }
    };
    registerReceiver(receiver, intentFilter);
}


/**
 * 构建新的wifi配置
 * 代码参考：com.android.settings.wifi.WifiConfigController.getConfig()
 * 源码版本：android_5.1.51
 * @param ssid
 * @param password
 * @param securityMode
 * @param hiddenSSID
 * @return
 */
public static WifiConfiguration createConfig(String ssid, String password, int securityMode, boolean hiddenSSID) {
    int length = password.length();

    WifiConfiguration config = new WifiConfiguration();
    config.SSID = "\"" + ssid + "\"";
    config.hiddenSSID = hiddenSSID;

    switch (securityMode) {
        case SecurityMode.OPEN:
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            break;

        case SecurityMode.WEP:
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);

            if (length != 0) {
                // WEP-40, WEP-104, and 256-bit WEP (WEP-232?)
                if ((length == 10 || length == 26 || length == 58) &&
                        password.matches("[0-9A-Fa-f]*")) {
                    config.wepKeys[0] = password;
                } else {
                    config.wepKeys[0] = '"' + password + '"';
                }
            }
            break;

        case SecurityMode.WPA:
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            if (length != 0) {
                if (password.matches("[0-9A-Fa-f]{64}")) {
                    config.preSharedKey = password;
                } else {
                    config.preSharedKey = '"' + password + '"';
                }
            }
            break;

        case SecurityMode.WPA2:
            return null;
        default:
            return null;
    }
    return config;
}

private static WifiConfiguration searchConfiguration(WifiManager wifiManager, String ssid) {
    for(WifiConfiguration wifiConfiguration : wifiManager.getConfiguredNetworks()) {
        if(wifiConfiguration.SSID.equals(ssid)) {
            return wifiConfiguration;
        }
    }
    return null;
}

/**
 * 连接wifi
 * @param wifiManager
 * @param ssid
 * @param password
 * @param securityMode
 * @param hiddenSSID
 * @return
 */
public static boolean connect(WifiManager wifiManager, String ssid, String password, int securityMode, boolean hiddenSSID) {
    //添加新的网络配置
    WifiConfiguration newWifiConfig = createConfig(ssid, password, securityMode, hiddenSSID);

    WifiConfiguration preConfig = searchConfiguration(wifiManager, newWifiConfig.SSID);
    if (preConfig != null) {
        wifiManager.removeNetwork(preConfig.networkId); //从列表中删除指定的网络配置网络
    }

    //添加网络配置
    int networkID = wifiManager.addNetwork(newWifiConfig);

    if(networkID == -1) {
        return false;
    }

    //连接该网络
    return wifiManager.enableNetwork(networkID, true);
}
