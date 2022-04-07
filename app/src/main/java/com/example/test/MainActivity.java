///////// infowWindow viewAdapter구현성공 +  지도 선택시 버튼 사라짐 + 버튼 다시클릭시 버튼 사라짐

package com.example.test;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraPosition;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.UiSettings;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.Overlay;
import com.naver.maps.map.overlay.OverlayImage;
import com.naver.maps.map.style.light.Position;
import com.naver.maps.map.util.FusedLocationSource;
import com.naver.maps.map.util.MarkerIcons;
import com.naver.maps.map.widget.LocationButtonView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,Overlay.OnClickListener, NaverMap.OnMapClickListener {

    private static final String TAG = "MainActivity";

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    private FusedLocationSource mLocationSource;
    private NaverMap naverMap;
    private InfoWindow infoWindow;

    // 마커를 찍을 데이터
    //private ArrayList<PlaceInfo> mPlaceInfoList;

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

        // 위치를 반환하는 구현체인 FusedLocationSource 생성
        mLocationSource =
                new FusedLocationSource(this, PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        Log.d( TAG, "onMapReady");

        this.naverMap = naverMap;
        // 권한확인. 결과는 onRequestPermissionsResult 콜백 매서드 호출
        ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE);

        naverMap.setOnMapClickListener(this);

        // Load Database (Rest)
        List<Rest> restList = initLoadRestDatabase();
        // Add Rest Marker
        addRestMarker(restList);

        // NaverMap 객체 받아서 NaverMap 객체에 위치 소스 지정
        this.naverMap = naverMap;
        this.naverMap.setLocationSource(mLocationSource);

        // UI 컨트롤 재배치
        UiSettings uiSettings = this.naverMap.getUiSettings();
        uiSettings.setLocationButtonEnabled(false); // 기본값 : false

        LocationButtonView locationButtonView = findViewById(R.id.location);
        locationButtonView.setMap(this.naverMap);

    }

    @Override
    public boolean onClick(@NonNull Overlay overlay) {

        if (overlay instanceof Marker) {
            Marker marker = (Marker) overlay;
            if (marker.getInfoWindow() != null) {
                infoWindow.close();
            } else {
                infoWindow.open(marker);
            }
            return true;
        }
        return false;
    }

    public void addRestMarker(@NonNull List<Rest> restList) {


        for (int i = 0; i < restList.size(); i++) {

            String storeName = restList.get(i).storeName;     // 이름
            String address = restList.get(i).address;         // 주소
            double lat = restList.get(i).latitude;            // 위도
            double lon = restList.get(i).longitude;           // 경도
            int _code = restList.get(i)._code;

            Marker marker = new Marker();
            marker.setTag(restList.get(i));

            marker.setPosition(new LatLng(lat, lon));
            marker.setAnchor(new PointF(0.5f, 1.0f));
            marker.setCaptionText(storeName);
            marker.setMap(naverMap);
            marker.setOnClickListener(this);
            marker.getTag();
            if( _code >=  2017){
                marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_place_yellow));
            } else {
                marker.setIcon(OverlayImage.fromResource(R.drawable.ic_baseline_place_blue));
            }


        }


        infoWindow = new InfoWindow();
        infoWindow.setAdapter(new InfoWindow.DefaultViewAdapter(this) {
            @NonNull
            @Override
            protected View getContentView(@NonNull InfoWindow infoWindow) {
                Marker marker = infoWindow.getMarker();
                Rest restList = (Rest) marker.getTag();
                View view = View.inflate(MainActivity.this, R.layout.view_info_window, null);
                ((TextView) view.findViewById(R.id.storeName)).setText(restList.storeName);
                ((TextView) view.findViewById(R.id.address)).setText(restList.address);
                ((TextView) view.findViewById(R.id.time)).setText(restList.time);

                return view;
            }
        });

    }

    // 지도 클릭시
    @Override
    public void onMapClick(@NonNull PointF pointF, @NonNull LatLng latLng) {
        if (infoWindow.getMarker() != null) {
            infoWindow.close();
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




