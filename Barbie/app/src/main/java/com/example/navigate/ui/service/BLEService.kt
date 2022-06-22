package com.example.navigate.ui.service

import android.app.ProgressDialog
import android.app.Service
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.content.ContentValues
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.navigate.MainActivity
import com.example.navigate.ui.notifications.NotificationsFragment
import com.example.navigate.ui.others.BluetoothLeClass
import com.example.navigate.ui.others.UUIDInfo
import com.example.navigate.ui.utilInfo.Utils
import java.util.*


private lateinit var getServerDialog: ProgressDialog
private var iSendLength = 0
private var iRecvLength = 0
private var getServerTimer = Timer()

var lastPack = ""
var packSign0 = false

 class BLEService : Service() {

    // 透传数据的相关服务与特征
    protected var strSerial_Server = "0000fff0-0000-1000-8000-00805f9b34fb"
    protected var strSerial_Read = "0000fff1-0000-1000-8000-00805f9b34fb"
    protected var strSerial_Write = "0000fff2-0000-1000-8000-00805f9b34fb"


    // 接收通知数据回调
    protected var BC_RecvData = "BC_RecvData"

    // 写数据回调
    protected var BC_WriteData = "BC_WriteData"

    // 读通道回调
    protected var BC_ReadData = "BC_ReadData"

    // MTU改变的回调
    protected var BC_ChangeMTU = "BC_ChangeMTU"

    // 连接状态变化的回调
    protected var BC_ConnectStatus = "BC_ConnectStatus"

    class MyBinder:Binder()
    {
        fun getService():BLEService? {
            return trxActivity
        }
    }


     override fun onBind(intent: Intent?): IBinder? {
         return MyBinder()
     }

     private val TAG = BLEService::class.java.simpleName

     override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG,"OPEN")
        initData()


        trxActivity = this
        return super.onStartCommand(intent, flags, startId)
    }

     companion object {
         var list2 = ArrayList<String>(20)
         private lateinit var trxActivity:BLEService
         fun getInstance():BLEService {
             return trxActivity
         }
     }



     /** 初始化数据 */
     private fun initData() {
         // 断开或连接 状态发生变化时调用
         NotificationsFragment.getInstance().mBLE.setOnConnectListener(OnConnectListener)
         // 发现BLE终端的Service时回调
         NotificationsFragment.getInstance().mBLE.setOnServiceDiscoverListener(mOnServiceDiscover)
         // 读操作的回调
         NotificationsFragment.getInstance().mBLE.setOnDataAvailableListener(OnDataAvailableListener)
         // 写操作的回调
         NotificationsFragment.getInstance().mBLE.setOnWriteDataListener(OnWriteDataListener)
         // 接收到硬件返回的数据
         NotificationsFragment.getInstance().mBLE.setOnRecvDataListener(OnRecvDataListerner)
         NotificationsFragment.getInstance().mBLE.setOnChangeMTUListener(onChangeMTUListener)

         // 设置MTU
         NotificationsFragment.getInstance().mBLE.requestMtu(512)

         getServerTimer = Timer()
         getServerTimer.schedule(object : TimerTask() {
             override fun run() {
                 NotificationsFragment.getInstance().mBLE.getServiceByGatt()
                 getServerTimer.cancel()             }
         }, 1000)


         //NotificationsFragment.getInstance().mBLE.getServiceByGatt()
     }

     // 断开或连接 状态发生变化时调用
     private val OnConnectListener = object : BluetoothLeClass.OnConnectListener {
         override fun onConnected(gatt: BluetoothGatt?, status: Int, newState: Int) {
             sendBroadcast(Intent(BC_ConnectStatus)
                 .putExtra("isConnectState",true)
                 .putExtra("strConnectState","Connected to GATT server."))
             var msgStop = Message()
             msgStop.what = iConnectState
             msgStop.obj = "Connected to GATT server."
             //MainActivity.lineChart_ctl=true
             Log.i(TAG, "linchrt_ctl "+MainActivity.lineChart_ctl)
             selfHandler.sendMessage(msgStop)
         }
         override fun onDisconnect(gatt: BluetoothGatt?, status: Int, newState: Int) {
             sendBroadcast(Intent(BC_ConnectStatus)
                 .putExtra("isConnectState",false)
                 .putExtra("strConnectState","Disconnected from GATT server."))

             var msgStop = Message()
             msgStop.what = iConnectState
             msgStop.obj = "Disconnected from GATT server."
             //MainActivity.lineChart_ctl=false
             selfHandler.sendMessage(msgStop)
         }
         override fun onConnectting(gatt: BluetoothGatt?, status: Int, newState: Int) {
             sendBroadcast(Intent(BC_ConnectStatus)
                 .putExtra("isConnectState",false)
                 .putExtra("strConnectState","Connectting to GATT..."))
             var msgStop = Message()
             msgStop.what = iConnectState
             msgStop.obj = "Connectting to GATT..."
             selfHandler.sendMessage(msgStop)
         }
     }

     var serverList = ArrayList<UUIDInfo>()
     var readCharaMap = HashMap<String, ArrayList<UUIDInfo>>()
     var writeCharaMap = HashMap<String, ArrayList<UUIDInfo>>()
     private var selectServer : UUIDInfo? = null
     private var selectWrite : UUIDInfo? = null
     private var selectRead : UUIDInfo? = null

     /**
      * 搜索到BLE终端服务的事件
      */
     private val mOnServiceDiscover = BluetoothLeClass.OnServiceDiscoverListener {
         Log.e("onConnected", "mOnServiceDiscover: ${it.services.size}")

         val gattlist = it.services
         serverList.clear()
         readCharaMap.clear()
         writeCharaMap.clear()
         for (bluetoothGattService in gattlist) {
             val serverInfo = UUIDInfo(bluetoothGattService.uuid)
             serverInfo.strCharactInfo = "[Server]"
             serverList.add(serverInfo)
             val readArray = ArrayList<UUIDInfo>()
             val writeArray = ArrayList<UUIDInfo>()
             val characteristics = bluetoothGattService.characteristics
             for (characteristic in characteristics) {
                 val charaProp = characteristic.properties
                 var isRead = false
                 var isWrite = false
                 // 具备读的特征
                 var strReadCharactInfo = ""
                 // 具备写的特征
                 var strWriteCharactInfo = ""
                 if (charaProp and BluetoothGattCharacteristic.PROPERTY_READ > 0) {  //判断读特征
                     isRead = true
                     strReadCharactInfo += "[PROPERTY_READ]"
                     Log.e(ContentValues.TAG, "read_chara=" + characteristic.uuid + "----read_service=" + bluetoothGattService.uuid)
                 }
                 if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE > 0) {  //判断写特征
                     isWrite = true
                     strWriteCharactInfo += "[PROPERTY_WRITE]"
                     Log.e(ContentValues.TAG, "write_chara=" + characteristic.uuid + "----write_service=" + bluetoothGattService.uuid)
                 }
                 if (charaProp and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE > 0) {  //判断写但不返回特征
                     isWrite = true
                     strWriteCharactInfo += "[PROPERTY_WRITE_NO_RESPONSE]"
                     Log.e(ContentValues.TAG, "write_chara=" + characteristic.uuid + "----write_service=" + bluetoothGattService.uuid)
                 }
                 if (charaProp and BluetoothGattCharacteristic.PROPERTY_NOTIFY > 0) {  //判断通知特征
                     isRead = true
                     strReadCharactInfo += "[PROPERTY_NOTIFY]"
                     Log.e(ContentValues.TAG, "notify_chara=" + characteristic.uuid + "----notify_service=" + bluetoothGattService.uuid)
                 }
                 if (charaProp and BluetoothGattCharacteristic.PROPERTY_INDICATE > 0) {
                     isRead = true
                     strReadCharactInfo += "[PROPERTY_INDICATE]"
                     Log.e(ContentValues.TAG, "indicate_chara=" + characteristic.uuid + "----indicate_service=" + bluetoothGattService.uuid)
                 }
                 if (isRead) {
                     val uuidInfo = UUIDInfo(characteristic.uuid)
                     uuidInfo.strCharactInfo = strReadCharactInfo  //特征标签添加
                     uuidInfo.bluetoothGattCharacteristic = characteristic
                     readArray.add(uuidInfo)  //可读队列
                 }
                 if (isWrite) {
                     val uuidInfo = UUIDInfo(characteristic.uuid)
                     uuidInfo.strCharactInfo = strWriteCharactInfo
                     uuidInfo.bluetoothGattCharacteristic = characteristic
                     writeArray.add(uuidInfo)   //可写队列
                 }
                 readCharaMap.put(bluetoothGattService.uuid.toString(), readArray)
                 writeCharaMap.put(bluetoothGattService.uuid.toString(), writeArray)
             }
         }

         for (serverInfo in serverList) {  //选服务
             if (serverInfo.uuidString.equals(strSerial_Server)) {
                 selectServer = serverInfo
                 break
             }
         }

         val readArray = readCharaMap[selectServer?.uuidString]   //读取当前服务的写和读特征队列
         val writeArray = writeCharaMap[selectServer?.uuidString]
         if (readArray != null) {  //选择读UUID
             for (readInfo in readArray) {
                 if (readInfo.uuidString.equals(strSerial_Read)) {
                     selectRead = readInfo
                     break
                 }
             }
         }
         if (writeArray != null) {//选择写UUID
             for (writeInfo in writeArray) {
                 if (writeInfo.uuidString.equals(strSerial_Write)) {
                     selectWrite = writeInfo
                     break
                 }
             }
         }

         bindServerSubNofify()
     }

     // 读操作的回调
     private val OnDataAvailableListener = object : BluetoothLeClass.OnDataAvailableListener {
         @RequiresApi(Build.VERSION_CODES.N)
         override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
             sendBroadcast(Intent(BC_ReadData).putExtra("UUID",characteristic!!.uuid).putExtra("status",status).putExtra("data",characteristic.value))
         }
     }

     // 写操作的回调
     private val OnWriteDataListener = object : BluetoothLeClass.OnWriteDataListener {
         @RequiresApi(Build.VERSION_CODES.N)
         override fun OnCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
             Log.e("OnWriteDataListener", "writeStatus:${status == 0}")
             sendBroadcast(Intent(BC_WriteData).putExtra("UUID",characteristic!!.uuid).putExtra("status",status))
         }
     }




     // 接收到硬件返回的数据
     private val OnRecvDataListerner = object : BluetoothLeClass.OnRecvDataListerner {
         @RequiresApi(Build.VERSION_CODES.N)
         override fun OnCharacteristicRecv(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
             if (characteristic!!.value.size == 0) return
             //Log.e("OnRecvDataListerner", "RECVDATA,to HEX:${Utils.bytesToHexString(characteristic!!.value)},to STR:${String(characteristic!!.value)}")
             sendBroadcast(Intent(BC_RecvData).putExtra("UUID",characteristic!!.uuid).putExtra("data",characteristic.value))
             //startSendData("BB")
             val iNowRecvLength = characteristic!!.value.size
             var string = ""
             // 字符
             // 十六进制

             string = Utils.bytesToHexString(characteristic!!.value)

             var s0=1;  //idle
             var s1=0;  //header getting
             var s2=0;  //pack fulling
             var s3=0;  //end vading
             var mhead = "B4EC8C8A"
             var mend = "97AFC259"
            //var startP =string.indexOf(mhead,0,true)


             var nextIndex=0;

             val p = ArrayList<String>(0)

             do{
                // Log.i(TAG, "str     果: " + string )
                 var upIndex=string.indexOf(mhead,nextIndex,true)
                 //Log.i(TAG, "截取结果: " + upIndex )
                 if(upIndex!=-1){
                     var endIndex=string.indexOf(mend,upIndex,true)
                    // Log.i(TAG, "end   取结果: " + endIndex )

                     if(endIndex!=-1&&(endIndex-upIndex==64+8+4))
                     {
                         var temp=string.substring(upIndex+8,endIndex)
                         list2.add(temp)
                         //Log.i(TAG, "截取结果: " + temp )
                         //Log.i(TAG, "截取结果: " + temp.length )
                     }
                    nextIndex=upIndex+1;
                  }
             }while(upIndex!=-1)



//             Thread {
//                 Looper.prepare() //增加部分
             var bundle = Bundle()
                // Log.i(TAG, list2.get(0))
             //Log.i(TAG, "list2结果: "+list2)
             for(str in list2)
             {
                 if(str!=null) {

                     if (str.startsWith("af") ) {    //血氧

                         MainActivity.UpdataAFE(str);


                     }
                     else if (str.startsWith("2a") ) {   //脑电raw

                         //Log.i(TAG, "Servic start长度" + MainActivity.listEEG_raw.size)
                         var EEGraw =  MainActivity.UpdataEEG_raw(str)
                         bundle.putSerializable("EEGraw",EEGraw)
                         var eeg = EEGraw.toString();


                     }
                     else if (str.startsWith("be") ) {  //电池
                         var battery = MainActivity.UpdataBattery(str)
                         bundle.putSerializable("battery",battery)


                     }
                     else if (str.startsWith("b2") ) {  //呼吸

                         var bre =  MainActivity.UpdataBre(str);
                         bundle.putSerializable("bre",bre)

                     }
                     else if (str.startsWith("ed") ) {  //温湿度
                         var temp = MainActivity.UpdataTemp(str)
                         var humi = MainActivity.UpdataHumi(str)
                         bundle.putSerializable("temp",temp)
                         bundle.putSerializable("humi",humi)

                     }
                     else if (str.startsWith("cd") ) {  //脑电大包

                         //添加两个特征值
                         MainActivity.UpdataEEG_pack(str);

                     }

                 }
             }
                list2.clear()

//                 Looper.loop() //增加部分
//             }.start()




         }
     }


     /** MTU改变监听 */
     private val onChangeMTUListener = object : BluetoothLeClass.OnChangeMTUListener {
         override fun onChangeMTUListener(isResult: Boolean?, strMsg: String?, iMTU: Int) {
             Log.e("onChangeMTUListener", "MTU设置结果：$strMsg")
             sendBroadcast(Intent(BC_ChangeMTU).putExtra("strMsg",strMsg).putExtra("iMTU",iMTU))
         }
     }

     /** 绑定服务，订阅通知 */
     private fun bindServerSubNofify() {
         // 订阅通知
         val isNotification = NotificationsFragment.getInstance().mBLE.setCharacteristicNotification(selectRead?.bluetoothGattCharacteristic, true)
         Log.e("TestDataActivity", "isNotification:$isNotification")
         if (isNotification) {
             val descriptors: List<BluetoothGattDescriptor> = selectRead?.bluetoothGattCharacteristic!!.descriptors
             for (descriptor in descriptors) {
                 // 读写开关操作，writeDescriptor 否则可能读取不到数据。
                 val b1 = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                 if (b1) {
                     val isB = NotificationsFragment.getInstance().mBLE.writeDescriptor(descriptor)
                     Log.e(ContentValues.TAG, "startRead: " + "监听收数据")

                 }
             }
         }

     }

     /*------------------------------------*/
     /*------------------------------------*/
     /*------------------------------------*/

     val iConnectState = 111
     val iRevDataLog = 222
     val iSendDataLog = 333
     val selfHandler = object : Handler() {
         override fun dispatchMessage(msg: Message) {
             super.dispatchMessage(msg)



             when(msg.what) {
                 iRevDataLog -> {
                     val dataBundle = msg.obj as Bundle
                     var bre = dataBundle["bre"]
                     var AFE =dataBundle["AFE"]
                     var EEGraw = dataBundle["EEGraw"]
                     var EEGpack = dataBundle["EEGpack"]



                 }
             }
         }

     }

     var handler = Handler()
     var runnable: Runnable = object : Runnable {
         override fun run() {
             // TODO Auto-generated method stub


             //要做的事情


         }
     }

     public fun startSendData(strSend: String) {
         if (selectWrite == null) return
         var mgattCharacteristic = selectWrite?.bluetoothGattCharacteristic!!
         mgattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE)
         // 字符发送
         mgattCharacteristic.setValue(Utils.hexStringToBytes(strSend))
         NotificationsFragment.getInstance().mBLE.writeCharacteristic(mgattCharacteristic)
         Log.i(TAG, "发送数据:$strSend")
     }

}


