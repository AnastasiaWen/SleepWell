package com.example.navigate.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HomeViewModel extends ViewModel {

    //声明存储的变量
    private final MutableLiveData<String> mText = new MutableLiveData<>();
    private final MutableLiveData<Integer> barValueP = new MutableLiveData<>();
    private final MutableLiveData<Integer> barValueE = new MutableLiveData<>();
    private final MutableLiveData<Integer> barValueS = new MutableLiveData<>();
    private final MutableLiveData<Boolean> HelpState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> AutoState = new MutableLiveData<>();
    private final MutableLiveData<Integer> sleepTime = new MutableLiveData<>();

    public HomeViewModel() {


    }

    public void seekbarSave(int processB,String whichone){
        if (whichone=="seekbarSound")
        {
            barValueS.setValue(processB);
        }
        if (whichone=="seekbarEletronic")
        {
            barValueE.setValue(processB);
        }
       if(whichone=="seekbarPhoto")
       {
           barValueP.setValue(processB);
       }
    }
    public LiveData<Integer> getbarValue(String whichone) {
        if (whichone=="seekbarSound")
        {
            return barValueS;
        }
        if (whichone=="seekbarEletronic")
        {
            return barValueE;
        }
        if(whichone=="seekbarPhoto")
        {
            return barValueP;
        }
        else
            return barValueE;

    }
    public LiveData<Boolean>  getswitchState(String name){
        if (name=="openHelp"){
            return HelpState;
        }
        else
        {
            return AutoState;
        }
    }
    public void setSwitchState(Boolean state,String name){
        if(name =="openHelp")
        {
            HelpState.setValue(state);
        }

        else
        {
            AutoState.setValue(state);
        }
    }
    public void setSleepTime(int position)
    {
        sleepTime.setValue(position);
    }

    public LiveData<Integer> getsleepTime()
    {
        return sleepTime;
    }
}