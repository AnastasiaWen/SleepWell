package com.example.navigate.ui.others;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AFE implements Parcelable {

        public ArrayList<Double> afe1;
        public ArrayList<Double> afe2;

        public static Map<Character , Integer> str2hexdic = new HashMap<Character , Integer>()
        {
                {
                        put('A', 10);
                        put('B', 11);
                        put('C', 12);
                        put('D', 13);
                        put('E', 14);
                        put('F', 15);
                        put('1', 1);
                        put('2', 2);
                        put('3', 3);
                        put('4', 4);
                        put('5', 5);
                        put('6', 6);
                        put('7', 7);
                        put('8', 8);
                        put('9', 9);
                        put('0', 0);
                }
        };

        protected AFE(Parcel in) {
                afe1 = (ArrayList<Double>) in.readSerializable();
                afe2 = (ArrayList<Double>) in.readSerializable();
        }

        public static final Creator<AFE> CREATOR = new Creator<AFE>() {
                @Override
                public AFE createFromParcel(Parcel in) {
                        return new AFE(in);
                }

                @Override
                public AFE[] newArray(int size) {
                        return new AFE[size];
                }
        };

        public AFE() {}

        public   double getAfe2(String tag, int i) {
                double ans2 =  (double) (
                        (str2hexdic.get(tag.charAt(i + 4+8)) << 20)+
                                (str2hexdic.get(tag.charAt(i + 5+8)) << 16)  +
                                (str2hexdic.get(tag.charAt(i + 2+8)) << 12) +
                                (str2hexdic.get(tag.charAt(i + 3+8)) << 8) +
                                (str2hexdic.get(tag.charAt(i +8)) << 4) +
                                (str2hexdic.get(tag.charAt(i + 1+8)))
                );
                return ans2;
        }

        public   double getAfe1(String tag, int i) {
                double ans1 =  (double) (
                        (str2hexdic.get(tag.charAt(i + 4)) << 20)+
                                (str2hexdic.get(tag.charAt(i + 5)) << 16) +
                                (str2hexdic.get(tag.charAt(i + 2)) << 12) +
                                (str2hexdic.get(tag.charAt(i + 3)) << 8) +
                                (str2hexdic.get(tag.charAt(i)) << 4) +
                                (str2hexdic.get(tag.charAt(i + 1)))
                );
                return ans1;
        }

        @Override
        public int describeContents() {
                return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
                parcel.writeSerializable(afe1);
                parcel.writeSerializable(afe2);
        }

        /**
         * 适配器管理器
         * @author 210001001427
         *
         */
        public static class AdapterManager {
        protected static final String string2 = null;

        private Context mContext;


        private List<BluetoothDevice> mDeviceList;   //设备集合
        private List<File> mFileList;    //文件集合
        private Handler mainHandler;   //主线程Handler

        public AdapterManager(Context context){
                this.mContext = context;

        }

        /**
         * 取得设备列表adapter
         * @return


        /**
         * 取得文件列表adapter
         * @return
         */




        /**
         * 清空设备列表
         */
        public void clearDevice(){
                if(null != mDeviceList){
                mDeviceList.clear();
                }
        }

        /**
         * 添加设备
         * @param bluetoothDevice
         */
        public void addDevice(BluetoothDevice bluetoothDevice){
                mDeviceList.add(bluetoothDevice);
        }

        /**
         * 更新设备信息
         * @param listId
         * @param bluetoothDevice
         */
        public void changeDevice(int listId, BluetoothDevice bluetoothDevice){
                mDeviceList.remove(listId);
                mDeviceList.add(listId, bluetoothDevice);
        }
        /**
         * 更新文件列表
         * @param path
         */



        /**
         * 取得设备列表
         * @return
         */
        public List<BluetoothDevice> getDeviceList() {
                return mDeviceList;
        }

        }
}
