package c.tlgbltcn.library

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.IntentFilter
import android.os.Build

/**
 * Created by tolga bolatcan on 24.01.2019
 */
class BluetoothHelper(private val context: Context, private val listener: BluetoothHelperListener) {

    private val bluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()?.let {
            return@lazy it
        }
    }

    private var isRequiredPermission = false

    private var isEnabled = bluetoothAdapter?.isEnabled

    private var isDiscovering = bluetoothAdapter?.isDiscovering

    private val mBluetoothStateChangeReceiver by lazy {
        object : BluetoothStateChangeReceiver() {
            override fun onStartDiscovering() {
                isDiscovering = true
                listener.onStartDiscovery()
            }

            override fun onFinishDiscovering() {
                isDiscovering = false
                listener.onFinishDiscovery()
            }

            override fun onEnabledBluetooth() {
                isEnabled = true
                listener.onEnabledBluetooth()
            }

            override fun onDisabledBluetooth() {
                isEnabled = false
                listener.onDisabledBluetooh()
            }
        }
    }

    private val mBluetoothDeviceFounderReceiver by lazy {
        object : BluetoothDeviceFounderReceiver() {
            override fun getFoundDevices(device: BluetoothDevice) {
                listener.getBluetoothDeviceList(device)
            }
        }
    }

    fun isBluetoothEnabled() = isEnabled

    fun isBluetoothScanning() = isDiscovering

    fun enableBluetooth() {
        if (isEnabled == false) bluetoothAdapter?.enable()
    }

    fun disableBluetooth() {
        if (isEnabled == true) bluetoothAdapter?.disable()
    }

    fun registerBluetoothStateChanged() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        context.registerReceiver(mBluetoothStateChangeReceiver, intentFilter)
    }

    fun unregisterBluetoothStateChanged() {
        context.unregisterReceiver(mBluetoothStateChangeReceiver)
    }

    fun startDiscovery() {
        if (isEnabled == true && isDiscovering == false) {
            bluetoothAdapter?.startDiscovery()
            val discoverDevicesIntent = IntentFilter(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(mBluetoothDeviceFounderReceiver, discoverDevicesIntent)
        }
    }

    fun stopDiscovery() {
        if (isEnabled == true && isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
            context.unregisterReceiver(mBluetoothDeviceFounderReceiver)
        }
    }

    private fun checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            var permissionCheck = context.checkSelfPermission(BluetoothHelperConstant.ACCESS_FINE_LOCATION)
            permissionCheck += context.checkSelfPermission(BluetoothHelperConstant.ACCESS_COARSE_LOCATION)

            if (permissionCheck != 0)
                (context as Activity).requestPermissions(
                        arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                        ), BluetoothHelperConstant.REQ_CODE
                )
        }
    }

    fun setPermissionRequired(isRequired: Boolean): BluetoothHelper {
        this.isRequiredPermission = isRequired
        return this
    }

    fun create(): BluetoothHelper {
        if (this.isRequiredPermission) checkBTPermissions()
        return BluetoothHelper(context, listener)
    }
}
