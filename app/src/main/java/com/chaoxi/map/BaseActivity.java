package com.chaoxi.map;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import static com.chaoxi.map.MyApp.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMapOptions;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class BaseActivity extends AppCompatActivity implements BaiduMap.OnMapClickListener {

    private MapView mapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initMap();
        initLocation();
        requestPermission();
    }

    private void initLocation() {
        // 定位初始化
        try {
            mLocationClient = new LocationClient(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 通过LocationClientOption设置LocationClient相关参数
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true); // 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(2000);
        //设置定位模式，三选一
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        // option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        // option.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);

        // 设置locationClientOption
        mLocationClient.setLocOption(option);

        // 注册LocationListener监听器
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);

        MyLocationConfiguration configuration = new MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.FOLLOWING
                , true
                , null
                , 0
                , 0
        );
        mBaiduMap.setMyLocationConfiguration(configuration);
    }

    private void initMap() {
        // 获取地图控件引用
        // MapView mMapView = (MapView) findViewById(R.id.bmapView);

        // 百度地图SDK支持不用通过layout文件中添加MapView控件
        BaiduMapOptions options = new BaiduMapOptions();
        // 设置地图模式为卫星地图
        options.mapType(BaiduMap.MAP_TYPE_NORMAL);
        mapView = new MapView(this, options);
        mBaiduMap = mapView.getMap();
        // 开启地图的定位图层
        mBaiduMap.setMyLocationEnabled(true);
        setContentView(mapView);
        mBaiduMap.setOnMapClickListener(this);
    }

    @Override
    protected void onResume() {
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mapView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        // 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mapView.onDestroy();
        mapView = null;
        super.onDestroy();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        Log.d(TAG, "当前点击经度：" + latLng.latitude + "\n 纬度" + latLng.longitude);

    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {
        Log.d(TAG, "mapPoi.getName()：" + mapPoi.getName() + "mapPoi.getUid()" + mapPoi.getUid() + "mapPoi.getPosition()" + mapPoi.getPosition());
    }

    private class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            Log.d(TAG, "onReceiveLocation: " + location.toString());
            // mapView 销毁后不在处理新接收的位置
            if (mapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);

        }
    }
    // 请求权限码
    private static final int REQUEST_PERMISSIONS = 8888;
    /**
     * 动态请求权限
     */
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void requestPermission() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                // android.Manifest.permission.READ_PHONE_STATE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {
            // true 有权限 开始定位
            Toast.makeText(this, "已获得权限，开始定位！", Toast.LENGTH_SHORT).show();
            // 开启地图定位图层
            mLocationClient.start();
        } else {
            // false 无权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, permissions);
        }
    }

    /**
     * 请求权限结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // 设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
