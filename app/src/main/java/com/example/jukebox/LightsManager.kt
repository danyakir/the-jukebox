import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.Context
import android.util.Log
import java.util.UUID

@SuppressLint("MissingPermission")
class LightsManager(private val context: Context) {
    private var writeCharacteristic: BluetoothGattCharacteristic? = null

    private val deviceAddress = "BE:30:69:00:1C:73"
    private var bluetoothGatt: BluetoothGatt? = null

    fun connect() {
        val bluetoothAdapter: BluetoothAdapter? by lazy {
            (context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter
        }
        val device = bluetoothAdapter?.getRemoteDevice(deviceAddress)

        if (device != null) {
            bluetoothGatt = device.connectGatt(context, false, gattCallback)
            Log.d("LightsManager", "Connecting to device at $deviceAddress...")
        } else {
            Log.e("LightsManager", "Device not found!")
        }
    }

    private fun sendCommand(command: ByteArray) {
        val characteristic = writeCharacteristic
        if (characteristic != null) {
        Log.d("LightsManager", "writeCharacteristic: $characteristic")
            characteristic.value = command
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            bluetoothGatt?.writeCharacteristic(characteristic)
            Log.d("LightsManager","Command sent: ${command.joinToString(" ") { String.format("%02X", it) }}"
            )
        } else {
            Log.e("LightsManager", "No writable characteristic found!")
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("LightsManager", "Connected to device")
                bluetoothGatt?.discoverServices()
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("LightsManager", "Disconnected from device")
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val serviceUUID = UUID.fromString("0000eea0-0000-1000-8000-00805f9b34fb")
                val characteristicUuid = UUID.fromString("0000ee01-0000-1000-8000-00805f9b34fb")
                val service = bluetoothGatt?.getService(serviceUUID)
                Log.d("LightsManager", "Changing to: ${service?.getCharacteristic(characteristicUuid)}")
                writeCharacteristic = service?.getCharacteristic(characteristicUuid)

                Log.d("LightsManager", "Services discovered, ready to send")
                sendCommand(getPacketFromOrdinal(null))

            } else {
                Log.e("LightsManager", "Service discovery failed, status: $status")
            }
        }
    }

    private fun getPacketFromOrdinal(ordinal: Number? = null): ByteArray {
        val packetChooseGrey = byteArrayOf(0x69.toByte(), 0x96.toByte(), 0x05, 0x02, 0x3F, 0x3F, 0x3F, 0x7F)

        val packetRockBlue = byteArrayOf(0x69.toByte(), 0x96.toByte(), 0x05, 0x02, 0x00, 0x4C.toByte(), 0x86.toByte(), 0x7F.toByte())
        val packetPopPink = byteArrayOf(0x69.toByte(), 0x96.toByte(), 0x05, 0x02, 0x7E, 0x60, 0x6C, 0x7F.toByte())
        val packetHipHopYellow = byteArrayOf(0x69.toByte(), 0x96.toByte(), 0x05, 0x02, 0x7F.toByte(), 0xB3.toByte(), 0x09, 0x7F.toByte())
        val packetTechnoGreen = byteArrayOf(0x69.toByte(), 0x96.toByte(), 0x05, 0x02, 0x49, 0x9C.toByte(), 0x6B, 0x7F.toByte())
        val packetDiscoBlue = byteArrayOf(0x69.toByte(), 0x96.toByte(), 0x05, 0x02, 0x1F.toByte(), 0x75, 0x72, 0x7F.toByte())
        val packetSlowRed = byteArrayOf(0x69.toByte(), 0x96.toByte(), 0x05, 0x02, 0x7A.toByte(), 0x1F, 0x3F, 0x7F.toByte())

        val packets = listOf(
            packetRockBlue,
            packetPopPink,
            packetHipHopYellow,
            packetTechnoGreen,
            packetDiscoBlue,
            packetSlowRed
        )

        return if (ordinal != null && ordinal.toInt() in packets.indices) {
            packets[ordinal.toInt()]
        } else {
            packetChooseGrey
        }
    }

    fun changeLightsColor(ordinal: Number?) {
        if (writeCharacteristic != null) {
            val packet = getPacketFromOrdinal(ordinal)
            sendCommand(packet)
        } else {
            Log.d("LightsManager", "Not ready yet")
        }
    }
}
