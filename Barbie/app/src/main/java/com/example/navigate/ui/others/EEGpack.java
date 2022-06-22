package com.example.navigate.ui.others;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class EEGpack implements Parcelable {

    public  ArrayList<Double> listEEG_alp_L ;
    public  ArrayList<Double> listEEG_alp_H;
    public  ArrayList<Double> listEEG_beta_L ;
    public  ArrayList<Double> listEEG_beta_H ;
    public  ArrayList<Double> listEEG_delta ;
    public  ArrayList<Double> listEEG_theta ;
    public  ArrayList<Double> listEEG_gamma_L ;
    public  ArrayList<Double> listEEG_gamma_M ;
    public  ArrayList<Double> listEEG_power ;
    public  ArrayList<Double> listEEG_attention ;
    public  ArrayList<Double> listEEG_mediation ;

    //read  使用Parcel时候的构造函数
    protected EEGpack(Parcel in) {
        listEEG_power = (ArrayList<Double>) in.readSerializable();
        listEEG_delta = (ArrayList<Double>) in.readSerializable();
        listEEG_theta = (ArrayList<Double>) in.readSerializable();
        listEEG_alp_L = (ArrayList<Double>) in.readSerializable();
        listEEG_alp_H = (ArrayList<Double>) in.readSerializable();
        listEEG_beta_L = (ArrayList<Double>) in.readSerializable();
        listEEG_beta_H = (ArrayList<Double>) in.readSerializable();
        listEEG_gamma_L = (ArrayList<Double>) in.readSerializable();
        listEEG_gamma_M = (ArrayList<Double>) in.readSerializable();
        listEEG_attention = (ArrayList<Double>) in.readSerializable();
        listEEG_mediation = (ArrayList<Double>) in.readSerializable();


    }

    //不动他
    public static final Creator<EEGpack> CREATOR = new Creator<EEGpack>() {
        @Override
        public EEGpack createFromParcel(Parcel in) {
            return new EEGpack(in);
        }

        @Override
        public EEGpack[] newArray(int size) {
            return new EEGpack[size];
        }
    };

    //声明对象的时候的构造函数
    public EEGpack() {}

    @Override
    public int describeContents() {
        return 0;
    }

    //write  写顺序和读顺序要一致
    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(listEEG_power);
        parcel.writeSerializable(listEEG_delta);
        parcel.writeSerializable(listEEG_theta);
        parcel.writeSerializable(listEEG_alp_L);
        parcel.writeSerializable(listEEG_alp_H);
        parcel.writeSerializable(listEEG_beta_L);
        parcel.writeSerializable(listEEG_beta_H);
        parcel.writeSerializable(listEEG_gamma_L);
        parcel.writeSerializable(listEEG_gamma_M);
        parcel.writeSerializable(listEEG_attention);
        parcel.writeSerializable(listEEG_mediation);
    }

    //提供装包工作
    public EEGpack packLoad(EEGpack pack1, int[] ans) {
        pack1.listEEG_power.add((double) ans[1]);
        pack1.listEEG_delta.add((double) (((int)ans[4] << 16) + ((int)ans[5] << 8) + (int)ans[6]));
        pack1.listEEG_theta.add((double) (((int)ans[7] << 16) + ((int)ans[8] << 8) + (int)ans[9]));
        pack1.listEEG_alp_L.add((double) (((int)ans[10] << 16) + ((int)ans[11] << 8) + (int)ans[12]));
        pack1.listEEG_alp_H.add((double) (((int)ans[13] << 16) + ((int)ans[14] << 8) + (int)ans[15]));
        pack1.listEEG_beta_L.add((double) (((int)ans[16] << 16) + ((int)ans[17] << 8) + (int)ans[18]));
        pack1.listEEG_beta_H.add((double) (((int)ans[19] << 16) + ((int)ans[20] << 8) + (int)ans[21]));
        pack1.listEEG_gamma_L.add((double) (((int)ans[22] << 16) + ((int)ans[23] << 8) + (int)ans[24]));
        pack1.listEEG_gamma_M.add((double) (((int)ans[25] << 16) + ((int)ans[26] << 8) + (int)ans[27]));
        pack1.listEEG_attention.add((double) ans[29]);
        pack1.listEEG_mediation.add((double) ans[31]);

        return  pack1;
    }


}
