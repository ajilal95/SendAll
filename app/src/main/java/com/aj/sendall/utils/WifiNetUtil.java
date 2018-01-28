package com.aj.sendall.utils;


import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import org.greenrobot.greendao.annotation.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

final public class WifiNetUtil {

    private static final String TAG = "WifiApControl";

    private WifiManager.WifiLock wifiLock = null;
    private Context context;
    private WifiConfiguration systemConfiguration = null;

    private static Method getWifiApConfigurationMethod;
    private static Method getWifiApStateMethod;
    private static Method isWifiApEnabledMethod;
    private static Method setWifiApEnabledMethod;

    static {
        for (Method method : WifiManager.class.getDeclaredMethods()) {
            switch (method.getName()) {
                case "getWifiApConfiguration":
                    getWifiApConfigurationMethod = method;
                    break;
                case "getWifiApState":
                    getWifiApStateMethod = method;
                    break;
                case "isWifiApEnabled":
                    isWifiApEnabledMethod = method;
                    break;
                case "setWifiApEnabled":
                    setWifiApEnabledMethod = method;
                    break;
                default: continue;
            }
            method.setAccessible(true);
        }
    }

    private static final int WIFI_AP_STATE_DISABLING = 10;
    private static final int WIFI_AP_STATE_DISABLED  = 11;
    private static final int WIFI_AP_STATE_ENABLING  = 12;
    private static final int WIFI_AP_STATE_ENABLED   = 13;
    private static final int WIFI_AP_STATE_FAILED    = 14;

    public static final int STATE_DISABLING = WIFI_AP_STATE_DISABLING;
    public static final int STATE_DISABLED  = WIFI_AP_STATE_DISABLED;
    public static final int STATE_ENABLING  = WIFI_AP_STATE_ENABLING;
    public static final int STATE_ENABLED   = WIFI_AP_STATE_ENABLED;
    public static final int STATE_FAILED    = WIFI_AP_STATE_FAILED;

    private static boolean isSoftwareSupported() {
        return (getWifiApStateMethod != null
                && isWifiApEnabledMethod != null
                && setWifiApEnabledMethod != null
                && getWifiApConfigurationMethod != null);
    }

    private static boolean isHardwareSupported() {
        return true;
    }

    // isSupported reports whether Wi-Fi APs are supported by this device.
    /*public static boolean isSupported() {
        return isSoftwareSupported() && isHardwareSupported();
    }*/

    private static final String FALLBACK_DEVICE = "wlan0";

    private final WifiManager wm;
    private final String deviceName;

    private static WifiNetUtil instance = null;

    private WifiNetUtil(Context context) {
        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        deviceName = getDeviceName(wm);
        this.context = context;
    }

    // getInstance is a standard singleton instance getter, constructing
    // the actual class when first called.
    @NotNull
    public static WifiNetUtil getInstance(Context context) {
        if (instance == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(context)) {
                Log.e(TAG, "6.0 or later, but haven't been granted WRITE_SETTINGS!");
                return null;
            }
            instance = new WifiNetUtil(context);
        }
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private static String getDeviceName(WifiManager wifiManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            Log.w(TAG, "Older device - falling back to the default device name: " + FALLBACK_DEVICE);
            return FALLBACK_DEVICE;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.w(TAG, "6.0 or later, unaccessible MAC - falling back to the default device name: " + FALLBACK_DEVICE);
            return FALLBACK_DEVICE;
        }

        String macString = wifiManager.getConnectionInfo().getMacAddress();
        if (macString == null) {
            Log.w(TAG, "MAC Address not found - Wi-Fi disabled? Falling back to the default device name: " + FALLBACK_DEVICE);
            return FALLBACK_DEVICE;
        }
        byte[] macBytes = macAddressToByteArray(macString);

        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();

                byte[] hardwareAddress = iface.getHardwareAddress();
                if (hardwareAddress != null && Arrays.equals(macBytes, hardwareAddress)) {
                    return iface.getName();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }

        Log.w(TAG, "None found - falling back to the default device name: " + FALLBACK_DEVICE);
        return FALLBACK_DEVICE;
    }

    private static byte[] macAddressToByteArray(String macString) {
        String[] mac = macString.split("[:\\s-]");
        byte[] macAddress = new byte[6];
        for (int i = 0; i < mac.length; i++) {
            macAddress[i] = Integer.decode("0x" + mac[i]).byteValue();
        }
        return macAddress;
    }

    private static Object invokeQuietly(Method method, Object receiver, Object... args) {
        try {
            return method.invoke(receiver, args);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    // isWifiApEnabled returns whether the Wi-Fi AP is currently enabled.
    // If an error occured invoking the method via reflection, false is
    // returned.
    public boolean isWifiApEnabled() {
        Object result = invokeQuietly(isWifiApEnabledMethod, wm);
        if (result == null) {
            return false;
        }
        return (Boolean) result;
    }

    // isEnabled is a commodity function alias for isWifiApEnabled.
    public boolean isEnabled() {
        return isWifiApEnabled();
    }

    // newStateNumber adapts the state constants to the current values in
    // the SDK. They were changed on 4.0 to have higher integer values.
    public static int newStateNumber(int state) {
        if (state < 10) {
            return state + 10;
        }
        return state;
    }

    // getWifiApState returns the current Wi-Fi AP state.
    // If an error occured invoking the method via reflection, -1 is
    // returned.
    public int getWifiApState() {
        Object result = invokeQuietly(getWifiApStateMethod, wm);
        if (result == null) {
            return -1;
        }
        return newStateNumber((Integer) result);
    }

    // getState is a commodity function alias for getWifiApState.
    public int getState() {
        return getWifiApState();
    }

    // getWifiApConfiguration returns the current Wi-Fi AP configuration.
    // If an error occured invoking the method via reflection, null is
    // returned.
    public WifiConfiguration getWifiApConfiguration() {
        Object result = invokeQuietly(getWifiApConfigurationMethod, wm);
        if (result == null) {
            return null;
        }
        return (WifiConfiguration) result;
    }

    // getConfiguration is a commodity function alias for
    // getWifiApConfiguration.
    public WifiConfiguration getConfiguration() {
        return getWifiApConfiguration();
    }

    // setWifiApEnabled starts a Wi-Fi AP with the specified
    // configuration. If one is already running, start using the new
    // configuration. You should call WifiManager.setWifiEnabled(false)
    // yourself before calling this method.
    // If an error occured invoking the method via reflection, false is
    // returned.
    boolean setWifiApEnabled(WifiConfiguration config, boolean enabled) {
        Object result = invokeQuietly(setWifiApEnabledMethod, wm, config, enabled);
        if (result == null) {
            return false;
        }
        return (Boolean) result;
    }

    public boolean startHotspot(WifiConfiguration config){
        enableWifi(false);
        stopHotspot();
        systemConfiguration = getConfiguration();
        return setWifiApEnabled(config, true);
    }

    public void stopHotspot(){
        disableHotspot();
        if(systemConfiguration != null){
            setWifiApEnabled(systemConfiguration, true);
        }
        disableHotspot();
        systemConfiguration = null;
    }

    // disableHotspot stops any currently running Wi-Fi AP.
    public boolean disableHotspot() {
        return setWifiApEnabled(null, false);
    }

    // getInet6Address returns the IPv6 address that the device has in its
    // own Wi-Fi AP local network. Will return null if no Wi-Fi AP is
    // currently enabled.
    public Inet6Address getInet6Address() {
        if (!isEnabled()) {
            return null;
        }
        return getInetAddress(Inet6Address.class);
    }

    // getInet4Address returns the IPv4 address that the device has in its
    // own Wi-Fi AP local network. Will return null if no Wi-Fi AP is
    // currently enabled.
    public Inet4Address getInet4Address() {
        if (!isEnabled()) {
            return null;
        }
        return getInetAddress(Inet4Address.class);
    }


    private <T extends InetAddress> T getInetAddress(Class<T> addressType) {
        try {
            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface iface = ifaces.nextElement();

                if (!iface.getName().equals(deviceName)) {
                    continue;
                }

                Enumeration<InetAddress> addrs = iface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();

                    if (addressType.isInstance(addr)) {
                        return addressType.cast(addr);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "", e);
        }
        return null;
    }

    // Client describes a Wi-Fi AP device connected to the network.
    public static class Client {

        // ipAddr is the raw string of the IP Address client
        public String ipAddr;

        // hwAddr is the raw string of the MAC of the client
        public String hwAddr;

        public Client(String ipAddr, String hwAddr) {
            this.ipAddr = ipAddr;
            this.hwAddr = hwAddr;
        }
    }

    // getClients returns a getListableDirs of all clients connected to the network.
    // Since the information is pulled from ARP, which is cached for up to
    // five minutes, this method may yield clients that disconnected
    // minutes ago.
    public List<Client> getClients() {
        if (!isEnabled()) {
            return null;
        }
        List<Client> result = new ArrayList<>();

        // Basic sanity checks
        Pattern macPattern = Pattern.compile("..:..:..:..:..:..");

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" +");
                if (parts.length < 6) {
                    continue;
                }

                String ipAddr = parts[0];
                String hwAddr = parts[3];
                String device = parts[5];

                if (!device.equals(deviceName)) {
                    continue;
                }

                if (!macPattern.matcher(parts[3]).find()) {
                    continue;
                }

                result.add(new Client(ipAddr, hwAddr));
            }
        } catch (IOException e) {
            Log.e(TAG, "", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "", e);
            }
        }

        return result;
    }

    // ReachableClientListener is an interface to collect the results
    // provided by getReachableClients via callbacks.
    public interface ReachableClientListener {

        // onReachableClient is called each time a reachable client is
        // found.
        void onReachableClient(Client c);

        // onComplete is called when we are done looking for reachable
        // clients
        void onComplete();
    }

    // getReachableClients fetches the clients connected to the network
    // much like getClients, but only those which are reachable. Since
    // checking for reachability requires network I/O, the reachable
    // clients are returned via callbacks. All the clients are returned
    // like in getClients so that the callback returns a subset of the
    // same objects.
    public List<Client> getReachableClients(final int timeout,
                                            final ReachableClientListener listener) {
        List<Client> clients = getClients();
        if (clients == null) {
            return null;
        }
        final CountDownLatch latch = new CountDownLatch(clients.size());
        ExecutorService es = Executors.newCachedThreadPool();
        for (final Client c : clients) {
            es.submit(new Runnable() {
                public void run() {
                    try {
                        InetAddress ip = InetAddress.getByName(c.ipAddr);
                        if (ip.isReachable(timeout)) {
                            listener.onReachableClient(c);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "", e);
                    }
                    latch.countDown();
                }
            });
        }
        new Thread() {
            public void run() {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    Log.e(TAG, "", e);
                }
                listener.onComplete();
            }
        }.start();
        return clients;
    }



    public boolean enableWifi(boolean enable){
        boolean success = true;
        if(enable != isWifiEnabled()) {
            if (enable) {
                aquireWifiLock();
            } else {
                releaseWifiLock();
            }
            success = wm.setWifiEnabled(enable);
            if (success) {
                //wait for the operation to take effect
                if (enable) {
                    while (!wm.isWifiEnabled()) {
                    }
                } else {
                    while (wm.isWifiEnabled()) {
                    }
                }
            }
        }
        return success;
    }

    public boolean isWifiEnabled(){
        return wm.isWifiEnabled();
    }

    private void aquireWifiLock(){
        if(!isWifiLocked()) {
            if (wifiLock == null) {
                wifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, this.getClass().getSimpleName());
            }
            wifiLock.acquire();
        }
    }

    private void releaseWifiLock(){
        if(isWifiLocked()){
            wifiLock.release();
            wifiLock = null;
        }
    }

    private boolean isWifiLocked(){
        return wifiLock != null && wifiLock.isHeld();
    }

    private BroadcastReceiver wifiScanBr;
    public void startScanningWifi(BroadcastReceiver br) {
        enableWifi(true);
        wifiScanBr = br;
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanBr, intentFilter);
        wm.disconnect();
        wm.startScan();
    }

    public void stopScanningWifi(){
        if(wifiScanBr != null){
            try {
                context.unregisterReceiver(wifiScanBr);
                wifiScanBr = null;
            } catch (IllegalArgumentException e){
                wifiScanBr = null;
            }
        }
    }

    private int connectAndGetServerAddress(WifiConfiguration wifiConfiguration, int remAttempts){
        int res = wm.addNetwork(wifiConfiguration);
        wm.disconnect();
        try {
            Thread.sleep(3 * 1000);//time to disconnect
        } catch(Exception e){
            e.printStackTrace();
        }
        wm.enableNetwork(res, true);
        wm.reconnect();

        try {
            Thread.sleep(3 * 1000);//time to reconnect
        } catch(Exception e){
            e.printStackTrace();
        }
        int serverAddress = wm.getDhcpInfo().serverAddress;
        if(serverAddress > 0 || remAttempts <= 0){
            return serverAddress;
        } else {
            //Try again
            return connectAndGetServerAddress(wifiConfiguration, remAttempts - 1);
        }
    }

    public int connectAndGetServerAddress(WifiConfiguration wifiConfiguration) {
        return connectAndGetServerAddress(wifiConfiguration, 3);//try a max of three times to get connected
    }
}
