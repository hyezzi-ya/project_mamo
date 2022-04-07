///////// 정보창 생성 (mini) 빌드성공

package com.example.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.widget.LocationButtonView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,Overlay.OnClickListener  {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FusedLocationSource locationSource;
    private NaverMap naverMap;
    private InfoWindow infoWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 지도 객체 생성
        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map);
        if (mapFragment == null) {
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map, mapFragment).commit();

        }

        // getMapAsync를 호출하여 비동기로 onMapReady 콜백 메서드 호출
        // onMapReady에서 NaverMap 객체를 받음
        mapFragment.getMapAsync(this);

        // 위치를 반환하는 구현체, FusedLocationSource 생성
        locationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d( TAG, "onMapReady");
        this.naverMap = naverMap;

        // 권한확인
        // onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        // 데이터베이스
        List<Rest> restList = initLoadRestDatabase();
        // 마커,정보창 구현
        addRestMarker(restList);
        addInfoWindow(restList);

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        this.naverMap = naverMap;
        this.naverMap.setLocationSource(locationSource);

        // UI 컨트롤 재배치
        UiSettings uiSettings = this.naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(false); // 기본값 : false

        LocationButtonView locationButtonView = findViewById(R.id.location);
        locationButtonView.setMap(this.naverMap);


    }
    // 정보창 함수 구현
    public void addInfoWindow(@NonNull List<Rest> restList) {

        // infowindow 객체 생성
        infoWindow = new InfoWindow();

        for (int i = 0; i < restList.size(); i++) {

            String storeName = restList.get(i).storeName;     // 이름
            String address = restList.get(i).address;         // 휴게소 방향, 주
            String time = restList.get(i).time;               // 이시간

            // ViewAdapter 지정
            infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
                @NonNull
                @Override
                protected View getContentView(@NonNull InfoWindow infoWindow) {
                    View view = View.inflate(MainActivity.this, R.layout.view_info_window, null);
                    ((TextView) view.findViewById(R.id.name)).setText(storeName);
                    ((TextView) view.findViewById(R.id.address)).setText(address);
                    ((TextView) view.findViewById(R.id.time)).setText(time);

                    return view;
                }
            });


            }
    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {
            Marker marker = (Marker) overlay;
            infoWindow.open(marker);

        return false;
    }

    public void addRestMarker(@NonNull List<Rest> restList) {

        for (int i = 0; i < restList.size(); i++) {

            String storeName = restList.get(i).storeName;     // 이름
            double lat = restList.get(i).latitude;            // 위도
            double lon = restList.get(i).longitude;           // 경도

            Marker marker = new Marker();
            // 두개 동시에 해야 레드색
            //marker.setIcon(MarkerIcons.BLACK);
            marker.setIconTintColor(Color.RED);
            marker.setPosition(new LatLng(lat, lon));
            marker.setAnchor(new PointF(0.5f, 1.0f));
            marker.setCaptionText(storeName);
            marker.setMap(naverMap);
            marker.setOnClickListener(this);

            //markerList.add(marker);

        }
    }

    public List<Rest> initLoadRestDatabase(){
        DatabaseHelper databaseHelper = new DatabaseHelper(getApplicationContext());
        databaseHelper.OpenDatabaseFile();

        List<Rest> restList = databaseHelper.getTableData();
        Log.e("test", String.valueOf(restList.size()));

        databaseHelper.close();
        return restList;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // request code와 권한획득 여부 확인
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
            }
        }
    }


}


