package com.example.navigate.ui.home;

import static com.example.navigate.MainActivity.Music_ctl;
import static com.example.navigate.MainActivity.mContext;
import static com.example.navigate.MainActivity.mediaPlayer;
import static com.example.navigate.MainActivity.mediaPlayer_ctl;
import static com.example.navigate.MainActivity.plot_Breath;
import static com.example.navigate.MainActivity.plot_EEG;
import static com.example.navigate.MainActivity.plot_PW;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetFileDescriptor;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.navigate.MainActivity;
import com.example.navigate.R;
import com.example.navigate.databinding.FragmentHomeBinding;
import com.example.navigate.ui.service.BLEService;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet;
import com.github.mikephil.charting.model.GradientColor;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class HomeFragment extends Fragment {
    private final static String TAG = HomeFragment.class.getSimpleName();


    private static int addX = -1;
    private static int addY;

    public static Switch deviceOpen;
    private static GraphicalView chart;
    public static XYSeries series;
    static XYMultipleSeriesDataset mDataset;
    static  XYMultipleSeriesRenderer renderer;
    public static LinearLayout EEGplot;
    private FragmentHomeBinding binding;
    BarChart chartBar;
    BarChart chartBar2;
    RadarChart chartRader;
    TextView    spo2Text;
    TextView    breathText;
    TextView    pulseText;
    TextView    stateText;

    Switch openHelp;
    HomeViewModel homeViewModel;
    public AudioManager mAudioManager;
    public static BLEService MyService;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

       //使model绑定到activity
        HomeViewModel homeViewModel =
                new ViewModelProvider(getActivity()).get(HomeViewModel.class);


        binding = FragmentHomeBinding.inflate(inflater, container, false);
        ConstraintLayout root = binding.getRoot();



        //找到控件 binding里面包含了id

        SeekBar seekbarPhoto  =binding.seekbarPhoto;
        SeekBar seekbarSound =binding.seekbarSound;
        Switch openHelp= binding.openHelp;
        Switch autowake =binding.autowake;
        Spinner autowakeSpinner =binding.autowakeSpinner;
        spo2Text=binding.spo2Text;
        pulseText=binding.pulseText;
        breathText=binding.breathText;
        stateText=binding.stateText;

        deviceOpen=binding.deviceOpen;
        spo2Text.setText("稳定");
        pulseText.setText("稳定");
        breathText.setText("--"+"次/分");
        stateText.setText("当前状态良好，适宜入睡");


        //获取系统的Audio管理者
        mAudioManager = (AudioManager) getActivity().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        //最大音量
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekbarSound.setProgress(currentVolume);
        //mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (seekbarSound.getProgress()/100f*maxVolume),AudioManager.FLAG_SHOW_UI);

        //初始状态

        seekbarPhoto.setEnabled(false);
        seekbarSound.setEnabled(false);
        autowakeSpinner.setEnabled(false);
        autowake.setEnabled(false);
        binding.musicDown.setEnabled(false);
        binding.musicUp.setEnabled(false);
        binding.musicPlay.setEnabled(false);




        Log.i(TAG,"Service State:"+isServiceRunning(getContext(),"BLEService"));
        if(isServiceRunning(getActivity(),"BLEService")) {
            binding.openHelp.setEnabled(true);
            binding.deviceOpen.setEnabled(true);
        }
        else {
            binding.openHelp.setEnabled(false);
            binding.deviceOpen.setEnabled(false);
            binding.openHelp.setChecked(false);
            seekbarPhoto.setEnabled(false);
            seekbarSound.setEnabled(false);
            autowake.setEnabled(false);
            autowakeSpinner.setEnabled(false);
            binding.musicDown.setEnabled(false);
            binding.musicUp.setEnabled(false);
            binding.musicPlay.setEnabled(false);
        }

        if(!binding.openHelp.isChecked())
        {



        }



        //继承上一次状态
        homeViewModel.getsleepTime().observe(getViewLifecycleOwner(), autowakeSpinner::setSelection);
        homeViewModel.getswitchState("openHelp").observe(getViewLifecycleOwner(), openHelp::setChecked);
        homeViewModel.getswitchState("autowake").observe(getViewLifecycleOwner(), autowake::setChecked);

        homeViewModel.getbarValue("seekbarSound").observe(getViewLifecycleOwner(), seekbarSound::setProgress);
        homeViewModel.getbarValue("seekbarPhoto").observe(getViewLifecycleOwner(), seekbarPhoto::setProgress);


        final BLEService.MyBinder[] MyBinder = {null};//service绑定
        Intent serviceIntent=new Intent(getActivity(),BLEService.class);
        ServiceConnection con=new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MyBinder[0] =(BLEService.MyBinder)service;
                MyService=MyBinder[0].getService();
                Log.i(TAG,"获取到了BLEService的Binder");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                //重新连接操作
            }
        };

        Switch a2 = binding.deviceOpen;
        a2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(a2.isChecked()) {
                    Log.i(TAG, "is Checked");
                    handler.postDelayed(runnable, 100);
                    getActivity().bindService(serviceIntent,con,getActivity().BIND_AUTO_CREATE);
                    plot_EEG=true;
                    plot_Breath=false;
                    plot_PW=false;
                }
                else{
                    plot_EEG=false;
                    plot_Breath=false;
                    plot_PW=false;
                    handler.removeCallbacks(runnable);
                }
            }
        });


        seekbarSound.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                {
                    homeViewModel.seekbarSave(seekBar.getProgress(),"seekbarSound");
                    mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (progress/100f*maxVolume),AudioManager.FLAG_SHOW_UI);

                    //Log.i(TAG, "onProgressChanged: "+currentVolume);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekbarPhoto.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser)
                    homeViewModel.seekbarSave(seekBar.getProgress(),"seekbarPhoto");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        openHelp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                homeViewModel.setSwitchState(openHelp.isChecked(),"openHelp");
                if(openHelp.isChecked()&&isServiceRunning(getActivity(),"BLEService"))
                {
                    Log.i(TAG, "mediaPlayer_ctl"+mediaPlayer_ctl);
                    if(!mediaPlayer_ctl) {
                        mediaPlayer=new MediaPlayer();
                        initPlayer();//播放音乐
                        Log.i(TAG, "mediaPlayer1");

                        MainActivity.mediaPlayer_ctl=true;


                        Log.i(TAG, "onClick: ok");
                    }

                    seekbarPhoto.setEnabled(true);
                    seekbarSound.setEnabled(true);
                    autowake.setEnabled(true);
                    autowakeSpinner.setEnabled(true);
                    binding.musicDown.setEnabled(true);
                    binding.musicUp.setEnabled(true);
                    binding.musicPlay.setEnabled(true);
                    binding.musicPlay.setImageResource(R.mipmap.music_stop);
                }
                else
                {
                    if(mediaPlayer_ctl){
                        mediaPlayer.stop();
                        mediaPlayer_ctl=false;
                    }

                    seekbarPhoto.setEnabled(false);
                    seekbarSound.setEnabled(false);
                    autowake.setEnabled(false);
                    autowakeSpinner.setEnabled(false);
                    binding.musicDown.setEnabled(false);
                    binding.musicUp.setEnabled(false);
                    binding.musicPlay.setEnabled(false);
                    binding.musicPlay.setImageResource(R.mipmap.music_play);

                }
            }
        });
        autowake.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                homeViewModel.setSwitchState(autowake.isChecked(),"autowake");

                if (autowake.isChecked())
                {
                    autowakeSpinner.setEnabled(true);
                    Toast.makeText(getActivity(), "请设置睡眠的最短时长", Toast.LENGTH_LONG).show();
                }
                else
                {
                    autowakeSpinner.setEnabled(false);
                }
            }
        });
        autowakeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String content = parent.getItemAtPosition(position).toString();
                homeViewModel.setSleepTime(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        binding.musicPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    binding.musicPlay.setImageResource(R.mipmap.music_play);
                }
                else{
                    mediaPlayer.start();
                    binding.musicPlay.setImageResource(R.mipmap.music_stop);
                }
            }
        });

        binding.musicUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                if(Music_ctl+1>3)Music_ctl=1;
                else Music_ctl++;
                initPlayer();
            }
        });
        binding.musicDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer.stop();
                if(Music_ctl-1<1)Music_ctl=3;
                else Music_ctl--;
                initPlayer();
            }
        });

        //画图按钮
        binding.btnBreathPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mDataset.removeSeries(series);
                //series.clear();
                addX=-1;
                plot_PW=false;
                plot_EEG=false;
                plot_Breath=true;
                //mDataset.addSeries(series);
                renderer.setXAxisMin(0);//显示范围为100
                renderer.setXAxisMax(240);
            }
        });

        binding.btnPWPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               // mDataset.removeSeries(series);
                //series.clear();
                addX=-1;
               // mDataset.addSeries(series);
                plot_PW=true;
                plot_EEG=false;
                plot_Breath=false;
                renderer.setXAxisMin(0);//显示范围为100
                renderer.setXAxisMax(240);
            }
        });

        binding.btnEEGPlot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //mDataset.removeSeries(series);
                //series.clear();
                Log.i(TAG, "onClick: "+series.getItemCount());
                addX=-1;
                //mDataset.addSeries(series);
                plot_PW=false;
                plot_EEG=true;
                plot_Breath=false;
                renderer.setXAxisMin(0);//显示范围为100
                renderer.setXAxisMax(240);
            }
        });

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRadar(view);
        initLine(view);
        initChar();
        setData();
        //if((!MainActivity.mediaPlayer_ctl)&&MainActivity.lineChart_ctl)initPlayer();
    }


    Handler handler=new Handler();
    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub
            //要做的事情
            setData();


            Log.i(TAG, "series.len: "+HomeFragment.series.getItemCount());
            //mDataset.removeSeries(series);
            Log.i(TAG, "addX:"+addX);
            //mDataset.addSeries(series);
// 视图更新，没有这一步，曲线不会呈现动态
// 如果在非UI主线程中，需要调用postInvalidate()，具体参考api
            chart.postInvalidate();
            if(addX>1) {
                renderer.setYAxisMax(series.getY(addX - 1) + 10000);
                renderer.setYAxisMin(series.getY(addX - 1) - 20000);
            }
            if(addX>240){//如果超出了屏幕边界,实现坐标轴自动移动的方法
                renderer.setXAxisMin(addX-240);//显示范围为100
                renderer.setXAxisMax(addX);
            }


            //Log.i(TAG, "linChart_ctl"+MainActivity.lineChart_ctl);
            //Log.i(TAG, "listSp1"+MainActivity.listSp1);
            //if(MainActivity.lineChart_ctl)lineChart_invalidate();
            handler.postDelayed(this, 50);
        }
    };



    private void initPlayer()
    {
        //选歌
        AssetFileDescriptor file;
        switch (Music_ctl){
            case 1:file = getResources().openRawResourceFd(R.raw.dream3);
                break;
            case 2:file = getResources().openRawResourceFd(R.raw.dream1);
                break;
            case 3:file = getResources().openRawResourceFd(R.raw.dream2);
                break;
            default:file = getResources().openRawResourceFd(R.raw.dream3);
                break;
        }

        try {
            mediaPlayer.reset();
            MainActivity.mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        }
        catch (Exception e){
                e.printStackTrace();
            }

        try {
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MainActivity.mediaPlayer.setLooping(true); //循环播放
    }

    private void setData() {
        float mul = 20;   //上下限映射不管他
        float min = 70;

        int cnt = 5;  //控制维度

        //Y坐标最小值要放在这里 不然会报错
        chartRader.getYAxis().setAxisMinimum(0);
        chartRader.getYAxis().setAxisMaximum(100);
        // 多组数据值   添加到  多组ArrayList<RadarEntry>
        // 一组ArrayList<RadarEntry> 添加到  一组RadarDataSet 并设置属性
        // 各组RadarDataSet 合并到  一个ArrayList<IRadarDataSet>
        // 一个ArrayList<IRadarDataSet>  添加到  RadarData


        //控制组数
        ArrayList<RadarEntry> entries1 = new ArrayList<>();
        ArrayList<RadarEntry> entries2 = new ArrayList<>();



        // NOTE: The order of the entries when being added to the entries array determines their position around the center of
        //
        //Log.i(TAG, "spo2 " + MainActivity.spo2);
        //Log.i(TAG, "pwRate " + MainActivity.pwRate);
        //if(MainActivity.spo2!=null) {
            for (int i = 0; i < cnt; i++) {
                if(i==0){//放松度
                    if(MainActivity.listEEG_mediation.size()>0) {
                        int len = MainActivity.listEEG_mediation.size();
                        double k = MainActivity.listEEG_mediation.get((len - 1));
                        float val1 = (float) k;
                        //float val1 = (float) (MainActivity.spo2 + min);
                        entries1.add(new RadarEntry(val1));
                    }
                }
                if(i==1){//专注度
                    if(MainActivity.listEEG_attention.size()>0) {
                        int len = MainActivity.listEEG_attention.size();
                        double k = MainActivity.listEEG_attention.get((len - 1));
                        float val1 = (float) k;
                        //float val1 = (float) (MainActivity.spo2 + min);
                        entries1.add(new RadarEntry(val1));
                    }
                }
                if(i==2) {//血氧
                    if (MainActivity.spo2!=null) {
                        double k = MainActivity.spo2;
                        float val1 = (float) k;
                        //float val1 = (float) (MainActivity.spo2 + min);
                        entries1.add(new RadarEntry(val1));
                    }
                }
                if(i==3){//呼吸
                    //double k=MainActivity.listBre;
                    //float val1 = (float) k;
                    //float val1 = (float) (MainActivity.spo2 + min);
                   // entries1.add(new RadarEntry(val1));
                }
                if(i==4){//脉率
                    if(MainActivity.pwRate!=null) {
                        double k = MainActivity.pwRate;
                        float val1 = (float) k;
                        //float val1 = (float) (MainActivity.spo2 + min);
                        entries1.add(new RadarEntry(val1));
                    }
                }

            }
        //}

        //图例
        RadarDataSet set1 = new RadarDataSet(entries1, "Last Week");
        //边线和填充颜色
        set1.setColor(Color.rgb(103, 110, 129));
        set1.setFillColor(Color.rgb(103, 110, 129));
        set1.setDrawFilled(true);
        //不透明度
        set1.setFillAlpha(180);
        set1.setLineWidth(2f);
        set1.setDrawHighlightCircleEnabled(true);
        set1.setDrawHighlightIndicators(false);



        //图例2
        RadarDataSet set2 = new RadarDataSet(entries2, "This Week");
        //边线和填充颜色
        set2.setColor(Color.rgb(121, 162, 175));
        set2.setFillColor(Color.rgb(121, 162, 175));
        set2.setDrawFilled(true);
        set2.setFillAlpha(180);
        set2.setLineWidth(2f);
        set2.setDrawHighlightCircleEnabled(true);
        set2.setDrawHighlightIndicators(false);

        ArrayList<IRadarDataSet> sets = new ArrayList<>();
        sets.add(set1);
        // sets.add(set2);

        RadarData data = new RadarData(sets);
        //data.setValueTypeface(tfLight);
        data.setValueTextSize(8f);
        data.setDrawValues(false);
        data.setValueTextColor(Color.WHITE);

        Legend legend=chartRader.getLegend();
        legend.setEnabled(false);    //是否显示图例

        chartRader.setData(data);
        chartRader.invalidate();
        //chartRader.animateY(100, Easing.EaseOutCirc);、


        //________________________________________________________________________________________________
        //________________________________________________________________________________________________
        //___________________________BarChart_____________________________________________________________

        //添加line数据
        List<BarEntry> list=new ArrayList<>(); //上面的BarChart1
        int va = (int)(8*5+50);
        list.add(new BarEntry(0,va));

        List<BarEntry> list2=new ArrayList<>();//下面的BarChart2
        int va2 = (int)(8*5+50);
        list2.add(new BarEntry(0,va2));


        BarDataSet barDataSet=new BarDataSet(list, "温度");
        BarDataSet barDataSet2=new BarDataSet(list2, "湿度");

        //渐变色设置
        int bottomColor = ContextCompat.getColor(getContext(),R.color.Bar_start);
        int topColor = ContextCompat.getColor(getContext(),R.color.Bar_end);
        List<GradientColor> gradientColors = new ArrayList<>();
        gradientColors.add(new GradientColor(bottomColor, topColor));
        barDataSet.setGradientColors(gradientColors);//设置渐变色值，在barChart.setData前调用
        barDataSet2.setGradientColors(gradientColors);//设置渐变色值，在barChart.setData前调用

        BarData barData=new BarData(barDataSet);
        BarData barData2=new BarData(barDataSet2);
        chartBar.setData(barData);
        chartBar2.setData(barData2);
        chartBar.invalidate();
        chartBar2.invalidate();

        Legend bar = chartBar.getLegend();
        bar.setEnabled(false);
        chartBar2.getLegend().setEnabled(false);
        chartBar.getDescription().setEnabled(false);//隐藏右下角英文
        chartBar2.getDescription().setEnabled(false);

    }
    public void initRadar(View view) {
        chartRader =  view.findViewById(R.id.chart);
        //chartLine = findViewById(R.id.chart2);

        // 背景色
        chartRader.setBackgroundColor(Color.TRANSPARENT);

        // 图表的文本描述
        chartRader.getDescription().setEnabled(false);

        // 手势设置
        chartRader.setTouchEnabled(true);

        YAxis yAxis=chartRader.getYAxis();
        //是否绘制Y轴坐标点  和雷达框数据一般不同时存在 否则显得很挤 默认为true
        yAxis.setDrawLabels(true);
        yAxis.setTextColor(Color.GRAY);//Y轴坐标数据的颜色
        yAxis.setAxisMaximum(100);   //Y轴最大数值
        yAxis.setAxisMinimum(0);   //Y轴最小数值
        //Y轴坐标的个数    第二个参数一般填false     true表示强制设置标签数 可能会导致X轴坐标显示不全等问题
        yAxis.setLabelCount(5,true);
        yAxis.setEnabled(true);

        XAxis xAxis=chartRader.getXAxis();
        xAxis.setTextColor(Color.rgb(121, 162, 175));//X轴字体颜色
        xAxis.setTextSize(8);     //X轴字体大小
        //自定义X轴坐标描述（也就是五个顶点上的文字,默认是0、1、2、3、4）
        //和网上的不一样 用setValueFormatter方法 ValueFormatter类里面写getAxisLabel方法
        xAxis.setValueFormatter(new ValueFormatter() {
            public String getAxisLabel(float v, AxisBase axisBase) {
                if (v==0.0){
                    return "疲劳度";
                }
                if (v==1.0){
                    return "专注度";
                }
                if (v==2.0){
                    return "血氧";
                }
                if (v==3.0){
                    return "呼吸";
                }
                if (v==4.0){
                    return "脉率";
                }
                return "";
            }
        });
        chartRader.animateY(2000);  //


    }
    private void initLine(View view) {
        chartBar = view.findViewById(R.id.chartLine);
        chartBar2=view.findViewById(R.id.chartLine2);

        chartBar.setRotation(90);
        chartBar2.setRotation(90);
        XAxis barx = chartBar.getXAxis();
        XAxis barx2=chartBar2.getXAxis();
        /*
        barx.setValueFormatter(new ValueFormatter() {
            public String getAxisLabel(float v, AxisBase axisBase) {
                if (v==0.0){
                    return "温度";
                }
               // if (v==1.0){
                   // return "湿度";
               // }
                return "";
            }
        });

         */

        barx.setEnabled(false);
        barx2.setEnabled(false);

        YAxis AxisLeft = chartBar.getAxisLeft();
        YAxis AxisLeft2 = chartBar2.getAxisLeft();

        AxisLeft.setDrawGridLines(false);
        AxisLeft.setAxisMaximum(100);
        AxisLeft.setAxisMinimum(0);
        AxisLeft.setEnabled(false);
        AxisLeft2.setDrawGridLines(false);
        AxisLeft2.setAxisMaximum(100);
        AxisLeft2.setAxisMinimum(0);
        AxisLeft2.setEnabled(false);

        YAxis AxisRight = chartBar.getAxisRight();
        YAxis AxisRight2 = chartBar2.getAxisRight();
        AxisRight.setEnabled(false);
        chartBar.animateY(3000);  //绘制速度 动画效果
        AxisRight2.setEnabled(false);
        chartBar2.animateY(3000);  //绘制速度 动画效果



    }

    private  void initChar(){
        EEGplot= binding.EEGPlot;

        series = new XYSeries("title");
        // 创建一个数据集的实例，这个数据集将被用来创建图表
        mDataset = new XYMultipleSeriesDataset();
        // 将点集添加到这个数据集中
        mDataset.addSeries(series);
        // 以下都是曲线的样式和属性等等的设置，renderer相当于一个用来给图表做渲染的句柄
        int color = R.color.linChart_line;
        PointStyle style = PointStyle.CIRCLE;
        renderer = buildRenderer(color, style, true);

        // 设置好图表的样式（横轴时间分钟值，纵轴电压毫伏值）
        setChartSettings(renderer, "X", "Y", 0, 240, 1100000, 1150000, Color.TRANSPARENT,Color.TRANSPARENT);
        // 生成图表
        chart = ChartFactory.getLineChartView(mContext, mDataset, renderer);
        // 将图表添加到布局中去
        EEGplot.addView(chart, new ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));

    }

    protected XYMultipleSeriesRenderer buildRenderer(int color, PointStyle style, boolean fill)// 配置绘图属性
    {
        XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
        // 设置图表中曲线本身的样式，包括颜色、点的大小以及线的粗细等
        XYSeriesRenderer r = new XYSeriesRenderer();
        r.setColor(color);
        r.setPointStyle(style);
        //r.setFillBelowLine(true);
        //Yaohui.setYaohui(true);
        //Yaohui.setLg(new LinearGradient(0, 0, 240, 0,new int[]{Color.RED,Color.WHITE},null, Shader.TileMode.CLAMP));

        r.setFillPoints(fill);
        r.setLineWidth((float) 1);// 线粗尺寸
        renderer.addSeriesRenderer(r);
        return renderer;
    }

    protected void setChartSettings(XYMultipleSeriesRenderer renderer,
                                    String xTitle, String yTitle, double xMin, double xMax,
                                    double yMin, double yMax, int axesColor, int labelsColor) {
// 有关对图表的渲染可参看api文档
        renderer.setChartTitle("title");
        renderer.setXTitle("");
        renderer.setYTitle("");
        renderer.setXAxisMin(xMin);
        renderer.setXAxisMax(xMax);
        renderer.setYAxisMin(yMin);
        renderer.setYAxisMax(yMax);
        renderer.setAxesColor(axesColor);
        renderer.setLabelsColor(labelsColor);
        renderer.setMarginsColor(Color.TRANSPARENT);
        renderer.setMargins(new int[] {0, 0, -30, 0});
        renderer.setXLabelsColor(Color.TRANSPARENT);
        renderer.setShowGrid(false);
        renderer.setBackgroundColor(Color.TRANSPARENT);
        renderer.setGridColor(R.color.linChart_line);// 曲线颜色
        renderer.setXLabels(20);
        renderer.setYLabels(10);
        //renderer.setChartTitle("时间/电压变化曲线图");// 图表名称
        //renderer.setXTitle("时间(min)");// 横坐标名称
        //renderer.setYTitle("电压(mv)");// 纵坐标名称
        renderer.setYLabelsAlign(Paint.Align.RIGHT);
        renderer.setPointSize((float) 1);// 设置点的大小
        renderer.setShowLegend(false);
        renderer.setPanEnabled(true, false);// 设置滑动,这边是横向可以滑动,纵向不可滑动
        renderer.setZoomEnabled(true, false);// 设置缩放，横向可以，纵向不可以
// renderer.setZoomLimits(new double[] { 0, 720, 3400, 4400 });//设置缩放的范围
// renderer.setPanLimits(new double[] { -0.5, 720, 3400, 4400
// });//设置拉动的范围
    }

    public static void updateChart(int BatteryV)// 更新绘图方法</strong>
    {

// 设置好下一个需要增加的节点
        //Log.i(TAG, "addX "+addX);
        addX = (int) (addX + 1);

        addY = BatteryV;
        if(addX==0)series.clear();
        series.add(addX, addY);
        //Log.i(TAG, "addY "+addY);
// 移除数据集中旧的点集
        //mDataset.removeSeries(series);
        //Log.i(TAG, "mDataSet remove");
// 判断当前点集中到底有多少点，因为屏幕总共只能容纳240个，所以当点数超过240时，长度永远是240
        /*
        int length = series.getItemCount();
        if (length > 240) {
            length = 240;
        }
        Log.i(TAG, "length"+length);
// 将旧的点集中x和y的数值取出来放入backup中，并且将x的值加1，造成曲线向右平移的效果
        for (int i = 0; i < length; i++) {
            xv[i] = (int) series.getX(i);
            yv[i] = (int) series.getY(i);
        }
// 点集先清空，为了做成新的点集而准备
        series.clear();
// 将新产生的点首先加入到点集中，然后在循环体中将坐标变换后的一系列点都重新加入到点集中
// 这里可以试验一下把顺序颠倒过来是什么效果，即先运行循环体，再添加新产生的点
        series.add(addX, addY);
        for (int k = 0; k < length; k++) {
            series.add(xv[k], yv[k]);
        }
        Log.i(TAG, "series length"+series.getItemCount());
// 在数据集中添加新的点集

         */


    }


    public static boolean isServiceRunning(Context mContext, String className) {

        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);

        if (!(serviceList.size() > 0)) {
            return false;
        }
        Log.e("OnlineService：",className);
        for (int i = 0; i < serviceList.size(); i++) {
            //Log.e("serviceName：",serviceList.get(i).service.getClassName());
            if (serviceList.get(i).service.getClassName().contains(className) == true) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;

    }
}