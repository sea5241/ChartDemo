package com.example.chartdemo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.example.chartdemo.bean.ChartData;
import com.example.chartdemo.util.CommonUtils;
import com.example.chartdemo.view.ChartView;
import com.google.gson.Gson;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * MainActivity
 *
 * @author sea
 * @date 2019/9/28
 */
public class MainActivity extends AppCompatActivity {

    /**
     * 折线图表自定义view
     */
    private ChartView mChartView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
    }

    /**
     * 初始化view
     */
    private void initView(){
        mChartView = findViewById(R.id.cv_chart);
    }

    /**
     * 初始化数据
     */
    private void initData(){
        Map<Long, Float> map = new TreeMap<>();
        Gson gson=new Gson();
        ChartData result=gson.fromJson(CommonUtils.getJson(MyApp.getAppContext(),"sample_points_1.json"),ChartData.class);
        for (List<Float> l:result.getPoints()){
            map.put(l.get(0).longValue(),l.get(1));
        }
        mChartView.setMapChartData(map);
        mChartView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //父容器不需要拦截
                mChartView.getParent().requestDisallowInterceptTouchEvent(true);
                mChartView.setIsConsumeEvent(true);
                return true;
            }
        });
    }
}
