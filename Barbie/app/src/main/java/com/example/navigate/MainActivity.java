package com.example.navigate;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.example.navigate.databinding.ActivityMainBinding;
import com.example.navigate.ui.home.HomeFragment;
import com.example.navigate.ui.service.mgetFeature;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {

    public static boolean plot_EEG=false;
    public static boolean plot_PW=false;
    public static boolean plot_Breath=false;

    public static int Music_ctl=1;
    public static MediaPlayer mediaPlayer;;
    public static boolean mediaPlayer_ctl=false;
    public static boolean lineChart_ctl=false;
    public static int lineChart_data=0;
    static String recordTime;
    private ActivityMainBinding binding;

    FragmentManager Manager;
    Fragment frghome;
    Fragment frg1;
    Fragment frg2;


    String div;
    String[] list2;
    private static final String TAG = "MainActivity";
    String string;

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
    public static Double pwRate;
    public static Double spo2;
    public static ArrayList<Double> afe1 = new ArrayList<>();
    public static ArrayList<Double> afe2 = new ArrayList<>();

    //breath
    public static ArrayList<Double> listBre = new ArrayList<>();
    //battery
    public static ArrayList<Double> listBat = new ArrayList<>();


    //humidity and temperature
    public static ArrayList<Double> listTemp = new ArrayList<>();
    public static ArrayList<Double> listHumi = new ArrayList<>();

    public static Context mContext;

    public static boolean note_ctl=false;

    //转化字典
    public static Map<Character, Integer> str2hexdic = new HashMap<Character , Integer>()
    {
        {
            put('a', 10);
            put('b', 11);
            put('c', 12);
            put('d', 13);
            put('e', 14);
            put('f', 15);
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

    //血氧 + 脉率
    public static double getAfe2(String tag, int i) {
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
    public static double getAfe1(String tag, int i) {
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
    public static void UpdataAFE(String tag) throws IOException {
        //AFE afe = new AFE();
        // skip 4 0e0e

        double[] result;
        for(int i =4;i<tag.length();i+=16)
        {
            double ans1 = getAfe1(tag, i);
            double ans2 = getAfe2(tag, i);


            if(listSp1.size()<Spo2fs*Length_sec)
            {
                listSp1.add(ans1);
                //刷新图表
                int kk=(int)(Math.random()*100);

                if(plot_PW)HomeFragment.updateChart((int)ans1);
                //Log.i(TAG, "kk:"+kk);
                //if(HomeFragment.deviceOpen.isChecked())HomeFragment.updateChart((int)ans1);
                //if(HomeFragment.deviceOpen.isChecked())HomeFragment.addLine1Data((float) ans1);
                //Log.i(TAG, "listSp1.ans1:"+ans1);
                listSp2.add(ans2);
                afe1.add(ans1);
                afe2.add(ans2);


            }
            else
            {
                listSp1.clear();
                listSp2.clear();
            }
        }
        //Log.i(TAG, "afe length"+afe1.size());
        if(afe1.size()>100*3)  //2秒刷新一次
        {
            //Log.i(TAG, "UpdataAFEafe1         长度: "+afe1.size());
            //Log.i(TAG, "UpdataAFEafe2        长度: "+afe2.size());
            //mgetfeature.callSPO211(afe1,afe2);
            ArrayList<Double> afe1Af=Avgprocess(afe1,12);
            ArrayList<Double> afe2Af=Avgprocess(afe2,12);
            result =  mgetfeature.callSPO2andPhase(afe1Af,afe2Af);
            //添加到double变量
            spo2=result[0];
            pwRate=(result[1]);

            //Log.i(TAG, "spo2        spo2 "+spo2);
            //Log.i(TAG, "spo2        pwRate "+pwRate);

            afe1.clear();
            afe2.clear();
        }
    }



    //EEG
    public static ArrayList UpdataEEG_raw(String tag)
    {
        ArrayList<Double> every_EEGraw = new ArrayList<>();
        //Log.i(TAG, "UpdataEEG_raw: updata");
        // skip 4 0101
        if(tag.length()==4+64)
        {
            for(int i =4;i<tag.length();i+=4)
            {
                //Log.i(TAG, tag);
                //Log.i(TAG, String.valueOf(tag.length()));
                int High = (str2hexdic.get(tag.charAt(i)) << 12) + (str2hexdic.get(tag.charAt(i + 1))<<8);
                int Low = (str2hexdic.get(tag.charAt(i+2)) << 4) + (str2hexdic.get(tag.charAt(i+3)));

                int ans = High | Low;
                if (ans > 32768)
                {
                    ans = ans-65536;
                }
                every_EEGraw.add((double)ans);
                if(plot_EEG)HomeFragment.updateChart((int)ans);
                //Log.i(TAG, "UpdataEEG   ans : " + ans);
                if(listEEG_raw.size()<EEGfs*Length_sec)
                {
                    listEEG_raw.add((double) ans);


                }
                else
                {
                    //Log.i(TAG, "--------------------------------------");
                    //Log.i(TAG, "长度              "+listEEG_raw.toString());
                    //Log.i(TAG, "长度长度长度"+listEEG_raw.size());

                            int sleepClass = 0;
                            try {
                                sleepClass = mgetfeature.callPythonCode1(listEEG_raw);
                                listEEG_raw.clear();
                                Log.i(TAG, "长度长度长度"+listEEG_raw.size());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            listEEG_class.add(sleepClass);
                            Log.i(TAG, String.valueOf(sleepClass));



                            //todo pwm
                            /*
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

                             */


                }
            }



        }


        return every_EEGraw; //返回每次新增的
    }



    //脑电大包
    public static void UpdataEEG_pack(String tag)
    {

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



    //呼吸
    public static ArrayList<Double> UpdataBre(String tag)
    {
        ArrayList<Double> every_Bre = new ArrayList<>();
        for(int i =4;i<tag.length();i+=4)
        {
            double ans =((double) ((str2hexdic.get(tag.charAt(i)) << 12) + (str2hexdic.get(tag.charAt(i+1)) <<8)  +(str2hexdic.get(tag.charAt(i+2)) <<4) +(str2hexdic.get(tag.charAt(i+3)))  )    );

            if(listBre.size()>Brefs*Length_sec)
            {
                listBre.clear();
            }
            listBre.add(ans);
            every_Bre.add(ans);
            //Log.i(TAG, "UpdataBre: "+ans);
            //Log.i(TAG, "plot_Breath:"+plot_Breath);
            if(plot_Breath)HomeFragment.updateChart((int) ans);

        }
        double bre=listBre.get(0);

        return every_Bre;

    }


    //电池
    public static ArrayList<Double> UpdataBattery(String tag)
    {
        ArrayList<Double> every_Bat = new ArrayList<>();
        for(int i =4;i<tag.length();i+=4)
        {
            double ans =((double) ((str2hexdic.get(tag.charAt(i)) << 12) + (str2hexdic.get(tag.charAt(i+1)) <<8)  +(str2hexdic.get(tag.charAt(i+2)) <<4) +(str2hexdic.get(tag.charAt(i+3)))  )    );
            //Log.i(TAG, "UpdataBattery0: " + ((tag.charAt(i))));
            //Log.i(TAG, "UpdataBattery1: " + (tag.charAt(i+1)));
            if(listBat.size()>Brefs*Length_sec)
            {
                listBat.clear();
            }
            listBat.add(ans);
            every_Bat.add(ans);
        }
        //Log.i(TAG, "UpdataBattery0: " + every_Bat.get(0));
        //double Bat=0.3086*every_Bat.get(0)-689.2;
        //Log.i(TAG, "UpdataBatteryBAT: " + Bat);

        return every_Bat;

    }

    //温湿度
    public static Double UpdataTemp(String tag)
    {
        double ans =((double) ((str2hexdic.get(tag.charAt(4)) << 4) + (str2hexdic.get(tag.charAt(4+1)))));

        if(listTemp.size()>Length_sec)
        {
            listTemp.clear();
        }
        listTemp.add(ans);
        return ans;
    }
    public static Double UpdataHumi(String tag)
    {
        double ans =((double) ((str2hexdic.get(tag.charAt(6)) << 4) + (str2hexdic.get(tag.charAt(6+1)))));

        if(listHumi.size()>Length_sec)
        {
            listHumi.clear();
        }
        listHumi.add(ans);
        return ans;
    }





    public static int test ()
    {
        return 1;
    }


    public static mgetFeature mgetfeature = new mgetFeature();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

       // Manager = this.getSupportFragmentManager();

        mContext=this;

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        initPython();

        ArrayList<Integer> Data = new ArrayList<>();
        //分期数据生成
        for (int i =0;i<700;i++)
        {
            Data.add((int) Math.round(Math.random()*6)-1);
        }

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        String timenow =  d.format(date);

        String systemPath = getExternalFilesDir(null).getAbsolutePath() + "/"+"SLEEPDATA";
        //创建目录
        new File(systemPath).mkdirs();

        Date da = new Date(System.currentTimeMillis());
        SimpleDateFormat d1 = new SimpleDateFormat("HH:mm");
        recordTime = d1.format(da);

        saveinFile(Data,timenow,systemPath);
        ArrayList<Integer> cl = returnFileData(systemPath,timenow);
    }

    //读取分期结果的文件 返回int数组 舍去首尾的【int  ，int】和开始记录时间信息
    public static ArrayList<Integer>  returnFileData(String systemPath, String name) {
        //read
        StringBuilder sd =  eadSdcard(systemPath,name+".txt");
        //分割成单个结果
        String[] sdd =  sd.toString().split(",");
        //添加
        ArrayList<Integer> classf = new ArrayList<>();

        for(int i=2;i< sdd.length-1;i++)
        {
            classf.add(Integer.parseInt(sdd[i].replace(" ","")));

        }
        //Log.i(TAG, "文件读取结果: 长度" +classf.size()+"------"+classf);
        return classf;
    }

    public static String  returnRecordTime(String systemPath, String name) {
        //read
        StringBuilder sd =  eadSdcard(systemPath,name+".txt");
        //分割成单个结果
        String[] sdd =  sd.toString().split(",");
        //添加
        String ANS =  sdd[1];
        //Log.i(TAG, "文件读取结果: 长度" +classf.size()+"------"+classf);
        return ANS;
    }
    //用于写分期文件
    public static void riteFile(String s,String path,String name) {

        String s1 = s.toString();
        File file1 = new File(path, name);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file1);
            fileOutputStream.write(s1.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static StringBuilder eadSdcard(String path, String name) {
        InputStream inputStream = null;
        Reader reader = null;
        BufferedReader bufferedReader = null;
        StringBuilder result = new StringBuilder();
        try {

            File file = new File(path, name);
            inputStream = new FileInputStream(file);
            reader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(reader);
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                result.append(temp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //读取路径下的所有文件名 按日期降序排列
    public static ArrayList<String> getFilesAllName(String path) {
        File file=new File(path);
        File[] files=file.listFiles();
        if (files == null){Log.e("error","空目录");return null;}
        ArrayList<String> s = new ArrayList<>();
        for(int i =0;i<files.length;i++){
            s.add(files[i].getName().replace(".txt",""));
        }
        s.sort(Comparator.reverseOrder());
        return s;
    }

    public static void saveinFile(ArrayList<Integer> listClass,String timeNow,String path)
    {
        ArrayList<String> write = new ArrayList<>();
        write.add("0");
        write.add(recordTime);
        Log.i(TAG, "recordTime: " + recordTime);
        for(int i =0;i<listClass.size();i++)
        {
            write.add(String.valueOf(listClass.get(i)));
        }
        riteFile(write.toString(),path,timeNow+".txt");


    }

    public static ArrayList<Double> Avgprocess(ArrayList<Double> prodata, int windowL)
    {

        ArrayList<Double> ans = new ArrayList<>();
        for (int i = 0; i < prodata.size() - windowL - 1; i++)
        {
            if(prodata.subList(i, i+windowL).size()!=0)
            {
                List<Double> a =  prodata.subList(i, i+windowL);
                ans.add(a.stream().collect(Collectors.averagingDouble(Double::doubleValue)));
            }
            else
            {
                continue;
            }
        }
        return ans;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Toast.makeText(frghome.getActivity(), "------",Toast.LENGTH_LONG).show();
    }
    //函数：初始化Python环境
    void initPython() {
        if (!Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
}