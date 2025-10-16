import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.os.Looper
import android.util.Log
import java.util.*

interface BLECallback {
    fun onDataReceived(buttonNumber: Int)
}

@SuppressLint("MissingPermission")
class BLEManager(context: Context, private val callback: BLECallback) {
    private val buttons = mapOf(
        "FB:39:42:FB:BC:DD" to 0,   // JukeButton1 = rock
        "F8:BB:E0:F2:1E:28" to 1,   // JukeButton2 = pop
        "C3:FC:D2:F4:08:53" to 2,   // JukeButton3 = hip hop
        "F9:3B:91:F8:1B:26" to 3,   // JukeButton4 = techno
        "F8:8B:42:BC:07:3F" to 4,   // JukeButton5 = disco
        "FA:1B:31:86:DD:F6" to 5   // JukeButton6 = slow
    )

    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
    }
    private val scanner: BluetoothLeScanner? = bluetoothAdapter?.bluetoothLeScanner

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord


            val buttonNumber = buttons[device.address] ?: return
            val serviceData = scanRecord?.serviceData?.values?.firstOrNull() ?: return
            val hexString = serviceData.joinToString("") { String.format("%02x", it) }

            val isPressed = hexString.endsWith("0100")

            if (isPressed) {
                Log.d("BLEManager", "Button $buttonNumber PRESSED")
                callback.onDataReceived(buttonNumber)
            }
        }
    }

    fun startScanning() {
        val scanFilters = buttons.keys.map { address ->
            ScanFilter.Builder().setDeviceAddress(address).build()
        }

        val scanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        scanner?.startScan(scanFilters, scanSettings, scanCallback)
        Log.d("BLEManager", "Scanning started")
    }

    fun stopScanning() {
        scanner?.stopScan(scanCallback)
        Log.d("BLEManager", "Scanning stopped")
    }
}
