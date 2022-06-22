package com.example.navigate.ui.dashboard;

import static com.example.navigate.MainActivity.mContext;
import static com.example.navigate.MainActivity.returnRecordTime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.navigate.MainActivity;
import com.example.navigate.R;
import com.example.navigate.databinding.FragmentDashboardBinding;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardFragment extends Fragment {

    
    //历史bar数据
    static List<BarEntry> barEntries1;
    static List<BarEntry> barEntries2;
    static List<BarEntry> barEntries3;
    static List<BarEntry> barEntries4;
    static List<BarEntry> barEntries5;
    static List<BarEntry> barEntries6;
    //List<BarEntry> barNowEntries;
    private static List<PieEntry> pieValue;

    private FragmentDashboardBinding binding;
    BarChart BarHistoryData;
    static PieChart pieNow;
    static TextView TextDate;
    static BarChart barNow;

    static TextView textAll;
    static TextView textwake;
    static TextView textSleep1;
    static TextView textSleep2;
    static TextView textREM;
    static TextView textSleep3;
    static TextView textSleep4;
    static TextView mscore;
    TextView txWake;
    TextView txREM;
    TextView sleep1;
    TextView sleep2;
    TextView sleep3;
    TextView sleep4;

    TextView sleepLabel;  //"浅睡期"
    static TextView timeofClass; //”时间显示“
    static String dateofSleep;

    //NOW条形图数据 0 1 2
    List<Integer> caData = new ArrayList<>();
    static List<Integer> cData = new ArrayList<>();

    //textview数据
    static float allSleepTimef;
    static float wakeTimef;
    static float slightTimef;
    static float deepTimef;

    static String mfileName;
    static ArrayList<String> fileName;
    static String systemPath = mContext.getExternalFilesDir(null).getAbsolutePath() + "/"+"SLEEPDATA";

    private static final String TAG = "DashboardFragment";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();


        TextDate  =binding.TextDate;
        textAll = binding.textAll;
        textwake = binding.textwake;
        textSleep1 = binding.textSleep1;
        textSleep2= binding.textSleep2;
        textSleep3=binding.textSleep3;
        textSleep4=binding.textSleep4;
        textREM=binding.textREM;

        sleep1=binding.sleep1;
        sleep2=binding.sleep2;
        sleep3=binding.sleep3;
        sleep4=binding.sleep4;
        txWake=binding.txWake;
        txREM=binding.txREM;
        barNow = binding.barNow;
        sleepLabel= binding.sleepLabel;
        timeofClass =binding.timeofClass;
        mscore=binding.score;

        //小时 分钟 入睡时间
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("HH:mm");
        //dateofSleep = d.format(date);


        fileName = MainActivity.getFilesAllName(systemPath);
        dateofSleep = returnRecordTime(systemPath,fileName.get(0));
        timeofClass.setText(dateofSleep);

        Calendar calendar = Calendar.getInstance();

        binding.mCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDatePickerDialog(getActivity(),0,calendar);
            }
        });



        return root;
    }


    //获取众数
    public static int majorityElement1(int[] nums) {
        if(nums.length == 1)
            return nums[0];
        Map<Integer,Integer> map = new HashMap<>();
        int result = 0;
        for(int i=0;i < nums.length;i ++)
        {
            if(!map.containsKey(nums[i]))
            {
                map.put(nums[i],1);
            }
            else
            {
                map.put(nums[i],map.get(nums[i]) + 1);
            }
        }
        result =  Collections.max(map.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
        return result;
    }


    //一天的睡眠条形图
    void initBARNow(View view){
        barNow =view.findViewById(R.id.barNow);
        barNow.getDescription().setEnabled(false); // 不显示描述
        barNow.setDoubleTapToZoomEnabled(false);  //禁止缩放
        //barNow.setDragEnabled(false);  //禁止拖拽
        barNow.setScaleEnabled(false);  //禁止双指操作

        List<IBarDataSet> sets = new ArrayList<>();

        String timenow =  fileName.get(0);

        //根据日期读取数据
        ArrayList<Integer> Data = MainActivity.returnFileData(systemPath,timenow);


        //数据整合
        for(int i= 0;i<Math.floor(Data.size()/7/2);i++)
        {
            int[] every7 = new int[7*2];
            for(int k =0;k<7*2;k++)
            {
                every7[k]=(Data.get(k+i*14));
            }

            // Log.i(TAG, "majorityElement1: "+every7);
            //得到了一组的14个值的众数 添加到caDate
            int re =majorityElement1(every7);
            if (re==0)
            {
                caData.add(0) ;
            }
            else if(re==1||re==2||re==3)
            {
                caData.add(1); //浅睡
            }
            else
            {
                caData.add(2); //深睡

            }

        }


        List<List<BarEntry>> listClassify = new ArrayList<>();
        for(int i =0;i<caData.size();i++)
        {
            listClassify.add(new ArrayList<BarEntry>(Arrays.asList(new BarEntry(i,1))));
        }

        List<IBarDataSet> set = new ArrayList<>();
        for (int i =0; i<caData.size();i++)
        {

            BarDataSet barDataSet;
            if(caData.get(i)==0)
            {
                barDataSet = new BarDataSet(listClassify.get(i),"W");
                barDataSet.setColor(Color.parseColor("#f5c7f7")); // 柱子的颜色

            }
            else if(caData.get(i)==1)
            {
                barDataSet = new BarDataSet(listClassify.get(i),"S");
                barDataSet.setColor(Color.parseColor("#a393eb")); // 柱子的颜色
            }
            else
            {
                barDataSet = new BarDataSet(listClassify.get(i),"D");
                barDataSet.setColor(Color.parseColor("#5e63b6")); // 柱子的颜色
            }

            barDataSet.setDrawValues(false);
            set.add(barDataSet);

        }

        barNow.setDoubleTapToZoomEnabled(false);
        YAxis AxisLeft = barNow.getAxisLeft();
        AxisLeft.setDrawGridLines(false);
        AxisLeft.setAxisMaximum(1);
        AxisLeft.setAxisMinimum(0);
        AxisLeft.setEnabled(false);

        YAxis AxisRight = barNow.getAxisRight();
        AxisRight.setDrawGridLines(false);
        AxisRight.setAxisMaximum(1);
        AxisRight.setAxisMinimum(0);
        AxisRight.setEnabled(false);

        XAxis x = barNow.getXAxis();
        x.setDrawGridLines(false);
        x.setEnabled(false);

        Legend l=barNow.getLegend();
        l.setEnabled(false);


        BarData bara = new BarData(set);
        bara.setBarWidth(1.02f); // 设置所有柱子的宽度
        barNow.setData(bara);
    }


    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initBar(view);
        initPie(view);
        initBARNow(view);

        barNow.animateY(1000, Easing.EaseOutQuad);
        TextDate.setText("<   "+xtoDate(0)+"   >");

        //BarHistoryData.animateY(1000, Easing.EaseOutQuad);
        //选中高亮条
        /*
        BarHistoryData.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                //返回x轴的坐标
                int a =  Math.round(h.getX());
                //返回饼图的值
                pieValue = new ArrayList<>();

                pieValue.add(new PieEntry(barEntries1.get(a).getY()));
                pieValue.add(new PieEntry(barEntries2.get(a).getY()));
                pieValue.add(new PieEntry(barEntries3.get(a).getY()));
                pieValue.add(new PieEntry(barEntries4.get(a).getY()));
                pieValue.add(new PieEntry(barEntries5.get(a).getY()));
                pieValue.add(new PieEntry(barEntries6.get(a).getY()));

                allSleepTimef =
                        barEntries1.get(a).getY() + barEntries2.get(a).getY()+barEntries3.get(a).getY()
                                +barEntries4.get(a).getY()+barEntries5.get(a).getY()+barEntries6.get(a).getY();
                deepTimef = barEntries5.get(a).getY()+barEntries6.get(a).getY();
                slightTimef =barEntries2.get(a).getY()+barEntries3.get(a).getY() +barEntries4.get(a).getY();
                wakeTimef =  barEntries1.get(a).getY();

                DecimalFormat df = new DecimalFormat("#,##0.#");

                int score = (int) ((allSleepTimef-wakeTimef)*100/allSleepTimef);
                Log.i(TAG, "得分: " + score);
                pieNow.setCenterText(String.valueOf(score)+"分");
                pieNow.setCenterTextSize(20);


                textAll.setText("总睡眠时间:    "+String.valueOf(df.format(Float.parseFloat(String.valueOf(allSleepTimef)))+"h"));
                textSleep2.setText("深睡:    "+String.valueOf(df.format(Float.parseFloat(String.valueOf(deepTimef)))+"h"));
                textSleep1.setText("浅睡:    "+String.valueOf(df.format(Float.parseFloat(String.valueOf(slightTimef)))+"h"));
                textwake.setText("觉醒:    "+String.valueOf(df.format(Float.parseFloat(String.valueOf(wakeTimef)))+"h"));

                //回复大小
                textSleep1.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                textwake.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                textSleep2.animate().setDuration(200).scaleX(1f).scaleY(1f).start();

                setPieNow();
                pieNow.animateY(1000,Easing.EaseOutCirc);

                //返回选中日期
                String b = fileName.get(a);
                TextDate.setText(b);



                //更新单日数据
                String timenow =  fileName.get(a);
                cData.clear();
                //根据日期读取数据
                ArrayList<Integer> Data = MainActivity.returnFileData(systemPath,timenow);
                dateofSleep = returnRecordTime(systemPath,timenow);
                timeofClass.setText("入睡时间   "+dateofSleep);

                //数据整合
                for(int i= 0;i<Math.floor(Data.size()/7/2);i++)
                {
                    int[] every7 = new int[7*2];
                    for(int k =0;k<7*2;k++)
                    {
                        every7[k]=(Data.get(k+i*14));
                    }

                    // Log.i(TAG, "majorityElement1: "+every7);
                    //得到了一组的14个值的众数 添加到caDate
                    int re =majorityElement1(every7);
                    if (re==0)
                    {
                        cData.add(0) ;
                    }
                    else if(re==1||re==2||re==3)
                    {
                        cData.add(1); //浅睡
                    }
                    else
                    {
                        cData.add(2); //深睡

                    }

                }


                List<List<BarEntry>> listClassify = new ArrayList<>();
                for(int i =0;i<cData.size();i++)
                {
                    listClassify.add(new ArrayList<BarEntry>(Arrays.asList(new BarEntry(i,1))));
                }

                List<IBarDataSet> set = new ArrayList<>();
                for (int i =0; i<cData.size();i++)
                {

                    BarDataSet barDataSet;
                    if(cData.get(i)==0)
                    {
                        barDataSet = new BarDataSet(listClassify.get(i),"W");
                        barDataSet.setColor(Color.parseColor("#f5c7f7")); // 柱子的颜色

                    }
                    else if(cData.get(i)==1)
                    {
                        barDataSet = new BarDataSet(listClassify.get(i),"S");
                        barDataSet.setColor(Color.parseColor("#a393eb")); // 柱子的颜色
                    }
                    else
                    {
                        barDataSet = new BarDataSet(listClassify.get(i),"D");
                        barDataSet.setColor(Color.parseColor("#5e63b6")); // 柱子的颜色
                    }

                    barDataSet.setDrawValues(false);
                    set.add(barDataSet);

                }
                BarData bara = new BarData(set);
                bara.setBarWidth(1.02f); // 设置所有柱子的宽度
                barNow.setData(bara);
                barNow.animateY(1000, Easing.EaseOutQuad);

            }

            @Override
            public void onNothingSelected() {

            }
        });
         */
        //选中结果显示
        barNow.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int a =  Math.round(h.getX());
                //高亮的下标
                Log.i(TAG, "onValueSelected: " +a);
                int result = caData.get(a);
                if (result==0)
                {
                    sleepLabel.setText("觉醒期");
                }
                else if(result==1)
                {
                    sleepLabel.setText("浅睡期");
                }
                else
                {
                    sleepLabel.setText("深睡期");
                }
                //时段改变
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                Date nowDate = null;

                try {
                    nowDate = df.parse(dateofSleep);
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }

                //如果需要向后计算日期 -改为+
                Date newDate2 = new Date(nowDate.getTime() + (long)a  * 7 * 60 * 1000);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                String dateOk = simpleDateFormat.format(newDate2);
                timeofClass.setText(dateOk);


            }

            @Override
            public void onNothingSelected()
            {
                timeofClass.setText("入睡时间   "+dateofSleep);
                sleepLabel.setText("睡眠状态");
            }
        });

        //饼图高亮动画
        pieNow.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                int a = (int) h.getX();


                if(a==0)
                {
                    textwake.animate().setDuration(200).scaleX(1.2f).scaleY(1.2f).start();
                    // textwake.setTextColor(Color.rgb(0,0,0));
                    textREM.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep1.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep2.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep3.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep4.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    //textwake.getPaint().setFakeBoldText(true);
                    txWake.setBackgroundColor(0xFFeacdd1);
                    txREM.setBackgroundColor(0xFFFFFFFF);
                    sleep1.setBackgroundColor(0xFFFFFFFF);
                    sleep2.setBackgroundColor(0xFFFFFFFF);
                    sleep3.setBackgroundColor(0xFFFFFFFF);
                    sleep4.setBackgroundColor(0xFFFFFFFF);
                }


                if(a==1)
                {
                    textREM.animate().setDuration(200).scaleX(1.2f).scaleY(1.2f).start();
                    // textSleep1.setTextColor(Color.rgb(0,0,0));
                    textwake.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep1.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep2.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep3.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep4.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    //textSleep1.getPaint().setFakeBoldText(true);
                    txWake.setBackgroundColor(0xFFFFFFFF);
                    txREM.setBackgroundColor(0xFFeac0ce);
                    sleep1.setBackgroundColor(0xFFFFFFFF);
                    sleep2.setBackgroundColor(0xFFFFFFFF);
                    sleep3.setBackgroundColor(0xFFFFFFFF);
                    sleep4.setBackgroundColor(0xFFFFFFFF);
                }
                if(a==2)
                {
                    textSleep1.animate().setDuration(200).scaleX(1.2f).scaleY(1.2f).start();
                    // textSleep1.setTextColor(Color.rgb(0,0,0));
                    textwake.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textREM.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep2.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep3.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep4.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    //textSleep1.getPaint().setFakeBoldText(true);
                    txWake.setBackgroundColor(0xFFFFFFFF);
                    txREM.setBackgroundColor(0xFFFFFFFF);
                    sleep1.setBackgroundColor(0xFFc3a6cb);
                    sleep2.setBackgroundColor(0xFFFFFFFF);
                    sleep3.setBackgroundColor(0xFFFFFFFF);
                    sleep4.setBackgroundColor(0xFFFFFFFF);
                }

                if(a==3)
                {
                    textSleep2.animate().setDuration(200).scaleX(1.2f).scaleY(1.2f).start();
                    // textSleep1.setTextColor(Color.rgb(0,0,0));
                    textwake.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textREM.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep1.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep3.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep4.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    //textSleep1.getPaint().setFakeBoldText(true);
                    txWake.setBackgroundColor(0xFFFFFFFF);
                    txREM.setBackgroundColor(0xFFFFFFFF);
                    sleep1.setBackgroundColor(0xFFFFFFFF);
                    sleep2.setBackgroundColor(0xFFa381ba);
                    sleep3.setBackgroundColor(0xFFFFFFFF);
                    sleep4.setBackgroundColor(0xFFFFFFFF);
                }
                if(a==4)
                {
                    textSleep3.animate().setDuration(200).scaleX(1.2f).scaleY(1.2f).start();
                    // textSleep1.setTextColor(Color.rgb(0,0,0));
                    textwake.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textREM.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep1.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep2.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep4.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    //textSleep1.getPaint().setFakeBoldText(true);
                    txWake.setBackgroundColor(0xFFFFFFFF);
                    txREM.setBackgroundColor(0xFFFFFFFF);
                    sleep1.setBackgroundColor(0xFFFFFFFF);
                    sleep2.setBackgroundColor(0xFFFFFFFF);
                    sleep3.setBackgroundColor(0xFF732e7e);
                    sleep4.setBackgroundColor(0xFFFFFFFF);
                }

                if(a==5)
                {
                    textSleep4.animate().setDuration(200).scaleX(1.2f).scaleY(1.2f).start();
                    // textSleep1.setTextColor(Color.rgb(0,0,0));
                    textwake.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textREM.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep1.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep2.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep3.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    //textSleep1.getPaint().setFakeBoldText(true);
                    txWake.setBackgroundColor(0xFFFFFFFF);
                    txREM.setBackgroundColor(0xFFFFFFFF);
                    sleep1.setBackgroundColor(0xFFFFFFFF);
                    sleep2.setBackgroundColor(0xFFFFFFFF);
                    sleep3.setBackgroundColor(0xFFFFFFFF);
                    sleep4.setBackgroundColor(0xFF423171);
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
    }

    void initPie(View view){
        pieNow = view.findViewById(R.id.pieNow);
        //默认画最近日期的数据
        pieValue = new ArrayList<>();
        pieValue.add(new PieEntry(barEntries1.get(0).getY()));
        pieValue.add(new PieEntry(barEntries2.get(0).getY()));
        pieValue.add(new PieEntry(barEntries3.get(0).getY()));
        pieValue.add(new PieEntry(barEntries4.get(0).getY()));
        pieValue.add(new PieEntry(barEntries5.get(0).getY()));
        pieValue.add(new PieEntry(barEntries6.get(0).getY()));
        pieNow.getDescription().setEnabled(false); // 不显示描述
        Legend l =pieNow.getLegend();
        l.setEnabled(false);

        allSleepTimef =
                barEntries1.get(0).getY() + barEntries2.get(0).getY()+barEntries3.get(0).getY()
                        +barEntries4.get(0).getY()+barEntries5.get(0).getY()+barEntries6.get(0).getY();
        deepTimef = barEntries5.get(0).getY()+barEntries6.get(0).getY();
        slightTimef =barEntries2.get(0).getY()+barEntries3.get(0).getY() +barEntries4.get(0).getY();
        wakeTimef =  barEntries1.get(0).getY();

        DecimalFormat df = new DecimalFormat("#,##0.#");
        textAll.setText(String.valueOf(df.format(Float.parseFloat(String.valueOf(allSleepTimef)))+"h"));
        textSleep2.setText("N2: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries4.get(0).getY())))+"h"));
        textSleep1.setText("N1: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries3.get(0).getY())))+"h"));
        textwake.setText("觉醒: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(wakeTimef)))+"h"));
        textREM.setText("REM: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries2.get(0).getY())))+"h"));
        textSleep3.setText("N3: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries5.get(0).getY())))+"h"));
        textSleep4.setText("N4: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries6.get(0).getY())))+"h"));

        int score = (int) ((allSleepTimef-wakeTimef)*100/allSleepTimef);
        Log.i(TAG, "得分: " + score);
        binding.score.setText(String.valueOf(score));

        //画图
        setPieNow();
        pieNow.animateY(1000,Easing.EaseOutCirc);
    }

    static void setPieNow(){

        //设置数据
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#eacdd1"));
        colors.add(Color.parseColor("#eac0ce"));
        colors.add(Color.parseColor("#c3a6cb"));
        colors.add(Color.parseColor("#a381ba"));
        colors.add(Color.parseColor("#732e7e"));
        colors.add(Color.parseColor("#423171"));

        PieDataSet pieDataSet = new PieDataSet(pieValue, "");
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        pieData.setValueTextSize(10);
        pieData.setValueTextColor(Color.rgb(255,255,255));

        pieNow.setData(pieData);
    }


    private void initBar(View view) {
        //BarHistoryData =view.findViewById(R.id.historySleepData);
        //BarHistoryData.getDescription().setEnabled(false); // 不显示描述

        setBarData();
        //setBarAxis();
    }
    /*
    void setBarAxis(){

        BarHistoryData.setDoubleTapToZoomEnabled(false);
        YAxis AxisLeft = BarHistoryData.getAxisLeft();
        AxisLeft.setDrawGridLines(false);
        AxisLeft.setAxisMaximum(3);
        AxisLeft.setAxisMinimum(0);
        AxisLeft.setEnabled(false);

        YAxis AxisRight = BarHistoryData.getAxisRight();
        AxisRight.setDrawGridLines(false);
        AxisRight.setAxisMaximum(3);
        AxisRight.setAxisMinimum(0);
        AxisRight.setEnabled(false);

        XAxis x = BarHistoryData.getXAxis();
        x.setDrawGridLines(false);

        x.setEnabled(true);
        x.setPosition(XAxis.XAxisPosition.BOTTOM);;

        BarHistoryData.setVisibleXRangeMaximum(3);

        x.setValueFormatter(new ValueFormatter() {
            public String getAxisLabel(float v, AxisBase axisBase) {

                //如果是整数 v是x轴的值
                if(Math.round(v)==v)
                {
                    return fileName.get((int) v);
                }
                return "";
            }



        });
        Legend l=BarHistoryData.getLegend();
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setWordWrapEnabled(true);
        l.setTextSize(15);

    }
     */

    public static String xtoDate(int v) {

        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd");
        return getDateStr(d.format(date),v);
    }


    void setBarData(){
        List<IBarDataSet> sets = new ArrayList<>();
        //历史数据 天数计算

        int days =1;
        //去除了后缀 可直接写入X轴

        ArrayList<ArrayList<Integer>> ALLDATA = new ArrayList<>();  //历史数据的集合
        ArrayList<double[]> DATA = new ArrayList<>();  //统计小时数的集合
        for (int i =0 ;i<1;i++)
        {
            ArrayList<Integer> Data = MainActivity.returnFileData(systemPath,fileName.get(0));

            //将读取的数据统计0-5出现的次数
            double[] cal = new double[6];
            for (int it : Data)
            {
                if(it==0) {cal[0]++;}
                else if(it==1) {cal[1]++; }
                else if(it==2) {cal[2]++; }
                else if(it==3) {cal[3]++; }
                else if(it==4) {cal[4]++; }
                else if(it==5) {cal[5]++; }
            }
            for (int k=0;k< cal.length;k++)
            {
                cal[k] = cal[k]/120;
            }

            DATA.add(cal);

            ALLDATA.add(Data);
            Log.i(TAG, "Data: " + Data);
            Log.i(TAG, "cal: " + DATA.get(i)[0] +"  "+ DATA.get(i)[1]);
        }


        // entries1是每个序列中的第一根柱子
        // x是横坐标，表示位置，y是纵坐标，表示高度
        barEntries1 = new ArrayList<>();
        float position1 = -0.25f;
        for (int i =0;i<days;i++)
        {
            barEntries1.add(new BarEntry(i+position1, (float) DATA.get(i)[0]));
        }

        BarDataSet barDataSet1 = new BarDataSet(barEntries1, "");
        barDataSet1.setDrawValues(false); // 不显示值
        barDataSet1.setValueTextColor(Color.RED); // 值的颜色
        barDataSet1.setValueTextSize(15f); // 值的大小
        barDataSet1.setColor(Color.parseColor("#f5c7f7")); // 柱子的颜色
        barDataSet1.setLabel("WAKE"); // 设置标签之后，图例的内容默认会以设置的标签显示
        sets.add(barDataSet1);

        barEntries2 = new ArrayList<>();
        float position2 = -0.15f;
        for (int i =0;i<days;i++)
        {
            barEntries2.add(new BarEntry(i+position2, (float) DATA.get(i)[1]));
        }
        BarDataSet barDataSet2 = new BarDataSet(barEntries2, "");

        barDataSet2.setDrawValues(false); // 不显示值
        barDataSet2.setColor(Color.parseColor("#a393eb"));
        barDataSet2.setLabel("REM");
        sets.add(barDataSet2);

        barEntries3 = new ArrayList<>();
        float position3 = -0.05f;
        for (int i =0;i<days;i++)
        {
            barEntries3.add(new BarEntry(i+position3, (float) DATA.get(i)[2]));
        }
        BarDataSet barDataSet3 = new BarDataSet(barEntries3, "");
        barDataSet3.setDrawValues(false); // 不显示值
        barDataSet3.setValueTextColor(Color.RED); // 值的颜色
        barDataSet3.setValueTextSize(15f); // 值的大小
        barDataSet3.setColor(Color.parseColor("#5e63b6")); // 柱子的颜色
        barDataSet3.setLabel("N1"); // 设置标签之后，图例的内容默认会以设置的标签显示
        sets.add(barDataSet3);

        barEntries4 = new ArrayList<>();
        float position4 = 0.05f;
        for (int i =0;i<days;i++)
        {
            barEntries4.add(new BarEntry(i+position4, (float) DATA.get(i)[3]));
        }
        BarDataSet barDataSet4 = new BarDataSet(barEntries4, "");
        barDataSet4.setDrawValues(false); // 不显示值
        barDataSet4.setColor(Color.parseColor("#27296d"));
        barDataSet4.setLabel("N2");
        sets.add(barDataSet4);

        barEntries5 = new ArrayList<>();
        float position5 = 0.15f;
        for (int i =0;i<days;i++)
        {
            barEntries5.add(new BarEntry(i+position5, (float) DATA.get(i)[4]));
        }
        BarDataSet barDataSet5 = new BarDataSet(barEntries5, "");

        barDataSet5.setDrawValues(false); // 不显示值
        barDataSet5.setValueTextColor(Color.RED); // 值的颜色
        barDataSet5.setValueTextSize(15f); // 值的大小
        barDataSet5.setColor(Color.parseColor("#a393eb")); // 柱子的颜色
        barDataSet5.setLabel("N3"); // 设置标签之后，图例的内容默认会以设置的标签显示
        sets.add(barDataSet5);

        barEntries6 = new ArrayList<>();
        float position6 = 0.25f;
        for (int i =0;i<days;i++)
        {
            barEntries6.add(new BarEntry(i+position6, (float) DATA.get(i)[5]));
        }
        BarDataSet barDataSet6 = new BarDataSet(barEntries6, "");
        // 不显示第二根柱子上的值
        barDataSet6.setDrawValues(false); // 不显示值
        barDataSet6.setColor(Color.parseColor("#5e63b6"));
        barDataSet6.setLabel("N4");
        sets.add(barDataSet6);

        BarData barData = new BarData(sets);
        barData.setBarWidth(0.1f); // 设置柱子的宽度
        //BarHistoryData.setData(barData);

    }


    //Day:日期字符串例如 2015-3-10  Num:需要减少的天数例如 7  转换为时间戳 计算后再转回来
    //真正使用的时候 将读取的文件日期列表排序 对应替换就行
    public static String getDateStr(String day,int Num)  {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date nowDate = null;
        try {
            nowDate = df.parse(day);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //如果需要向后计算日期 -改为+
        Date newDate2 = new Date(nowDate.getTime() - (long)Num * 24 * 60 * 60 * 1000);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateOk = simpleDateFormat.format(newDate2);
        return dateOk;
    }

    /**
     * 日期选择
     * @param activity
     * @param themeResId
     * @param calendar
     */
    public static void showDatePickerDialog(Activity activity, int themeResId, final Calendar calendar) {
        // 直接创建一个DatePickerDialog对话框实例，并将它显示出来
        new DatePickerDialog(activity, themeResId, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                // 此处得到选择的时间，可以进行你想要的操作
                //tv.setText("" + year + "-" + (month + 1) + "-" + dayOfMonth + "");
                Log.i(TAG, "year"+year);
                Log.i(TAG, "year"+month);
                Log.i(TAG, "year"+dayOfMonth);
                String dStr=year+"-"+ (month + 1) + "-" + dayOfMonth;
                long nd = 1000 * 24 * 60 * 60;// 一天的毫秒数
                //long nh = 1000 * 60 * 60;// 一小时的毫秒数
               // long nm = 1000 * 60;// 一分钟的毫秒数
               // long ns = 1000;// 一秒钟的毫秒数
                long diff;
                long day = 0;

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date curDate = new Date(System.currentTimeMillis());//获取当前时间
                try {
                    Date dateTemp=sdf.parse(dStr);
                    dStr=sdf.format(dateTemp);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "dStr:"+dStr);
                Log.i(TAG, "fileName"+fileName);
                if(fileName.contains(dStr)) {


                    String nowtime = sdf.format(curDate);
                    Log.i(TAG, "NowTime: " + nowtime);
                    Log.i(TAG, "dStr: " + dStr);
                    try {
                        diff = (sdf.parse(nowtime).getTime() - sdf.parse(dStr).getTime());
                        day = diff / nd;// 计算差多少天
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    //返回x轴的坐标
                    int a = Math.round(day);
                    Log.i(TAG, "a: " + a);
                    //返回饼图的值
                    pieValue = new ArrayList<>();

                    pieValue.add(new PieEntry(barEntries1.get(a).getY()));
                    pieValue.add(new PieEntry(barEntries2.get(a).getY()));
                    pieValue.add(new PieEntry(barEntries3.get(a).getY()));
                    pieValue.add(new PieEntry(barEntries4.get(a).getY()));
                    pieValue.add(new PieEntry(barEntries5.get(a).getY()));
                    pieValue.add(new PieEntry(barEntries6.get(a).getY()));

                    allSleepTimef =
                            barEntries1.get(a).getY() + barEntries2.get(a).getY() + barEntries3.get(a).getY()
                                    + barEntries4.get(a).getY() + barEntries5.get(a).getY() + barEntries6.get(a).getY();
                    deepTimef = barEntries5.get(a).getY() + barEntries6.get(a).getY();
                    slightTimef = barEntries2.get(a).getY() + barEntries3.get(a).getY() + barEntries4.get(a).getY();
                    wakeTimef = barEntries1.get(a).getY();

                    DecimalFormat df = new DecimalFormat("#,##0.#");

                    int score = (int) ((allSleepTimef - wakeTimef) * 100 / allSleepTimef);
                    Log.i(TAG, "得分: " + score);

                    mscore.setText(String.valueOf(score));
                    textAll.setText(String.valueOf(df.format(Float.parseFloat(String.valueOf(allSleepTimef))) + "h"));
                    textSleep2.setText("N2: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries4.get(0).getY())))+"h"));
                    textSleep1.setText("N1: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries3.get(0).getY())))+"h"));
                    textwake.setText("觉醒: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(wakeTimef)))+"h"));
                    textREM.setText("REM: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries2.get(0).getY())))+"h"));
                    textSleep3.setText("N3: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries5.get(0).getY())))+"h"));
                    textSleep4.setText("N4: "+String.valueOf(df.format(Float.parseFloat(String.valueOf(barEntries6.get(0).getY())))+"h"));

                    //回复大小
                    textSleep1.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textwake.animate().setDuration(200).scaleX(1f).scaleY(1f).start();
                    textSleep2.animate().setDuration(200).scaleX(1f).scaleY(1f).start();

                    setPieNow();
                    pieNow.animateY(1000, Easing.EaseOutCirc);

                    //返回选中日期
                    String b = dStr;
                    TextDate.setText("<   "+b+"   >");


                    //更新单日数据
                    String timenow = dStr;
                    cData.clear();
                    //根据日期读取数据
                    ArrayList<Integer> Data = MainActivity.returnFileData(systemPath, timenow);
                    dateofSleep = returnRecordTime(systemPath, timenow);
                    timeofClass.setText("入睡时间   " + dateofSleep);

                    //数据整合
                    for (int i = 0; i < Math.floor(Data.size() / 7 / 2); i++) {
                        int[] every7 = new int[7 * 2];
                        for (int k = 0; k < 7 * 2; k++) {
                            every7[k] = (Data.get(k + i * 14));
                        }

                        // Log.i(TAG, "majorityElement1: "+every7);
                        //得到了一组的14个值的众数 添加到caDate
                        int re = majorityElement1(every7);
                        if (re == 0) {
                            cData.add(0);
                        } else if (re == 1 || re == 2 || re == 3) {
                            cData.add(1); //浅睡
                        } else {
                            cData.add(2); //深睡

                        }

                    }


                    List<List<BarEntry>> listClassify = new ArrayList<>();
                    for (int i = 0; i < cData.size(); i++) {
                        listClassify.add(new ArrayList<BarEntry>(Arrays.asList(new BarEntry(i, 1))));
                    }

                    List<IBarDataSet> set = new ArrayList<>();
                    for (int i = 0; i < cData.size(); i++) {

                        BarDataSet barDataSet;
                        if (cData.get(i) == 0) {
                            barDataSet = new BarDataSet(listClassify.get(i), "W");
                            barDataSet.setColor(Color.parseColor("#f5c7f7")); // 柱子的颜色

                        } else if (cData.get(i) == 1) {
                            barDataSet = new BarDataSet(listClassify.get(i), "S");
                            barDataSet.setColor(Color.parseColor("#a393eb")); // 柱子的颜色
                        } else {
                            barDataSet = new BarDataSet(listClassify.get(i), "D");
                            barDataSet.setColor(Color.parseColor("#5e63b6")); // 柱子的颜色
                        }

                        barDataSet.setDrawValues(false);
                        set.add(barDataSet);

                    }
                    BarData bara = new BarData(set);
                    bara.setBarWidth(1.02f); // 设置所有柱子的宽度
                    barNow.setData(bara);
                    barNow.animateY(1000, Easing.EaseOutQuad);
                }
                else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle("错误");
                    builder.setMessage("该日期无数据！请选择其他日期！");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //Toast.makeText(AlertDialogActivity.this, "确定了",Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder.show();

                }
            }
        }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                , calendar.get(Calendar.MONTH)
                , calendar.get(Calendar.DAY_OF_MONTH)).show();

    }

    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}