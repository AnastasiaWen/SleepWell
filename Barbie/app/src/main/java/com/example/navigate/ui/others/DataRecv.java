package com.example.navigate.ui.others;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.navigate.ui.home.HomeFragment;
import com.example.navigate.ui.service.BLEService;
import com.example.navigate.ui.service.mgetFeature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//used for unpack \  store instantly  \
public class DataRecv   {

    //sample rates and the length of data in buffer
    public static int EEGfs = 512;
    public static int EEGPackfs = 1;
    public static int Spo2fs = 100;
    public static int Brefs = 25;
    public static int Tempfs = 1;
    public static int Humifs = 1;

    public static int Length_sec = 30;
    public static int Length_EEGraw = 128;  //个点  128*2 字节
    public static int Length_Afe = 8;   //8*4 = 32字节 两通道
    public static int Length_Bre = 10;   //10*2 = 20 字节
    //存60秒的历史数据
    //EEG
    public static ArrayList<Double> listEEG_raw = new ArrayList<>();
    public static ArrayList<Double> listEEG_alp_L = new ArrayList<>();
    public static ArrayList<Double> listEEG_alp_H = new ArrayList<>();
    public static ArrayList<Double> listEEG_beta_L = new ArrayList<>();
    public static ArrayList<Double> listEEG_beta_H = new ArrayList<>();
    public static ArrayList<Double> listEEG_delta = new ArrayList<>();
    public static ArrayList<Double> listEEG_theta = new ArrayList<>();
    public static ArrayList<Double> listEEG_gamma_L = new ArrayList<>();
    public static ArrayList<Double> listEEG_gamma_M = new ArrayList<>();
    public static ArrayList<Double> listEEG_power = new ArrayList<>();
    public static ArrayList<Double> listEEG_attention = new ArrayList<>();
    public static ArrayList<Double> listEEG_mediation = new ArrayList<>();

    public static ArrayList<Integer> listEEG_class = new ArrayList<>();


    //SPO2
    public static ArrayList<Double> listSp1 = new ArrayList<>();
    public static ArrayList<Double> listSp2 = new ArrayList<>();

    //breath
    public static ArrayList<Double> listBre = new ArrayList<>();

    //humidity and temperature
    public static ArrayList<Double> listTemp = new ArrayList<>();
    public static ArrayList<Double> listHumi = new ArrayList<>();

    //转化字典
    public static Map<Character, Integer> str2hexdic = new HashMap<Character , Integer>()
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


    //delete the history data in previous 60s
    public void clear(){
        listEEG_raw.clear();
        listEEG_alp_L.clear();
        listEEG_alp_H.clear();
        listEEG_beta_L.clear();
        listEEG_beta_H.clear();
        listEEG_delta.clear();
        listEEG_theta.clear();
        listEEG_gamma_L.clear();
        listEEG_gamma_M.clear();
        listEEG_power.clear();
        listEEG_attention.clear();
        listEEG_mediation.clear();

        listSp1.clear();
        listSp2.clear();
        listBre.clear();
        listTemp.clear();
        listHumi.clear();
    }
    //add the pack to old list
    public static void EEGpackAdd(int[] ans) {
        listEEG_power.add((double) ans[1]);
        listEEG_delta.add((double) (((int) ans[4] << 16) + ((int) ans[5] << 8) + (int) ans[6]));
        listEEG_theta.add((double) (((int) ans[7] << 16) + ((int) ans[8] << 8) + (int) ans[9]));
        listEEG_alp_L.add((double) (((int) ans[10] << 16) + ((int) ans[11] << 8) + (int) ans[12]));
        listEEG_alp_H.add((double) (((int) ans[13] << 16) + ((int) ans[14] << 8) + (int) ans[15]));
        listEEG_beta_L.add((double) (((int) ans[16] << 16) + ((int) ans[17] << 8) + (int) ans[18]));
        listEEG_beta_H.add((double) (((int) ans[19] << 16) + ((int) ans[20] << 8) + (int) ans[21]));
        listEEG_gamma_L.add((double) (((int) ans[22] << 16) + ((int) ans[23] << 8) + (int) ans[24]));
        listEEG_gamma_M.add((double) (((int) ans[25] << 16) + ((int) ans[26] << 8) + (int) ans[27]));
        listEEG_attention.add((double) ans[29]);
        listEEG_mediation.add((double) ans[31]);
    }
    //删除第一个点
    public static void packRemoveAt0() {
        listEEG_alp_L.clear();
        listEEG_alp_H.clear();
        listEEG_beta_L.clear();
        listEEG_beta_H.clear();
        listEEG_delta.clear();
        listEEG_theta.clear();
        listEEG_gamma_L.clear();
        listEEG_gamma_M.clear();
        listEEG_power.clear();
        listEEG_attention.clear();
        listEEG_mediation.clear();
    }





    //返回新添加的值
    public static AFE UpdataAFE(String tag)
    {
        AFE afe = new AFE();
        // skip 4 0e0e
        for(int i =4;i<tag.length();i+=16)
        {
            double ans1 = afe.getAfe1(tag, i);
            double ans2 = afe.getAfe2(tag, i);
            afe.afe1.add(ans1);
            afe.afe2.add(ans2);

            if(listSp1.size()<Spo2fs*Length_sec)
            {
                listSp1.add(ans1);
                listSp2.add(ans2);
            }
            else
            {
                listSp1.clear();
                listSp2.clear();
                listSp1.add(ans1);
                listSp2.add(ans2);
            }
        }
        return afe;  //包含了2通道的新数组
    }


    private static final String TAG = "DataRecv";
    public static ArrayList UpdataEEG_raw(String tag)
    {
        ArrayList<Double> every_EEGraw = new ArrayList<>();
        //Log.i(TAG, "UpdataEEG_raw: updata");
        // skip 4 0101
        for(int i =4;i<tag.length();i+=4)
        {
            int High = (str2hexdic.get(new Character(tag.charAt(i))) << 12) + (str2hexdic.get(tag.charAt(i + 1))<<8);
            int Low = (str2hexdic.get(tag.charAt(i+2)) << 4) + (str2hexdic.get(tag.charAt(i+3)));

            int ans = High | Low;
            if (ans > 32768)
            {
                ans = ans-65536;
            }
            every_EEGraw.add((double)ans);
            if(listEEG_raw.size()<EEGfs*Length_sec)
            {
                listEEG_raw.add((double) ans);

            }
            else
            {
                runnable.run();

                listEEG_raw.clear();
                listEEG_raw.add((double) ans);
            }
        }

        return every_EEGraw; //返回每次新增的
    }

    static Handler  handler=new Handler();
    static Runnable runnable=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            try {
                int sleepClass =  mgetfeature.callPythonCode1(listEEG_raw);
                listEEG_class.add(sleepClass);


                //todo pwm
                switch (sleepClass)
                {
                    case 0:
                        HomeFragment.MyService.startSendData("");
                        break;
                    case 1:
                        HomeFragment.MyService.startSendData("");
                        break;
                    case 2:
                        HomeFragment.MyService.startSendData("");
                        break;
                    case 3:
                        HomeFragment.MyService.startSendData("");
                        break;
                    case 4:
                        HomeFragment.MyService.startSendData("");
                        break;
                    case 5:
                        HomeFragment.MyService.startSendData("");
                        break;
                    default:
                        break;
                }



            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    };

    public static mgetFeature mgetfeature = new mgetFeature();

    
    public static EEGpack UpdataEEG_pack(String tag)
    {
        EEGpack pack1 = new EEGpack();  //用于返回
        boolean isOk = false;  //用于写文件

        int[] ans = new int[32];int k = 0;
        for(int i =4;i<tag.length();i+=2)
        {
            ans[k++] = (str2hexdic.get(tag.charAt(i)) << 4) + (str2hexdic.get(tag.charAt(i+1)));
        }

        if(listEEG_theta.size()>EEGPackfs*Length_sec)
        {


            packRemoveAt0();
        }
        EEGpackAdd(ans);

       pack1.packLoad(pack1, ans);  //装载新数据

        isOk = true;
        return pack1;

    }

    public static ArrayList<Double> UpdataBre(String tag)
    {
        ArrayList<Double> every_Bre = new ArrayList<>();
        for(int i =4;i<tag.length();i+=2)
        {
            double ans =((double) ((str2hexdic.get(tag.charAt(i)) << 4) + (str2hexdic.get(tag.charAt(i+1)))));

            if(listBre.size()>Brefs*Length_sec)
            {
                listBre.clear();
            }
            listBre.add(ans);
            every_Bre.add(ans);
        }

        return every_Bre;

    }

    public static int test ()
    {
        return 1;
    }


}
