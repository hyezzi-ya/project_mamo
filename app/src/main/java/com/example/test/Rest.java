package com.example.test;


// 데이터베이스 테이블을 저장할 Class 작성
public class Rest {

    public int _id;
    public String storeName;
    public String address;
    public double longitude;
    public double latitude;
    public String time;
    public String h_day;
    public String  _code;



    // set function
    public void setId(int _id){ this._id = _id; }
    public void setStoreName(String storeName){ this.storeName = storeName; }
    public void setAddress(String address){ this.address = address; }
    public void setLongitude(double longitude){ this.longitude = longitude;}
    public void setLatitude(double latitude){this.latitude = latitude;}
    public void setTime(String time){ this.time = time; }
    public void setH_day(String h_day){ this.h_day = h_day; }
    public void set_code(String _code){ this._code = _code; }

    // get function
    public int getId(){ return this._id; }
    public String getStoreName(){ return this.storeName; }
    public String getAddress(){ return this.address; }
    public double getLatitude(){ return this.latitude;}
    public double getLongitude(){return this.longitude;}
    public String getTime(){ return this.time; }
    public String getH_day(){ return this.h_day; }
    public String get_code(){ return this._code; }


}