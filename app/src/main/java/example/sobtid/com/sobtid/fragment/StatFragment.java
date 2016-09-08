package example.sobtid.com.sobtid.fragment;


import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import example.sobtid.com.sobtid.R;
import example.sobtid.com.sobtid.activity.MainActivity;
import example.sobtid.com.sobtid.adapter.AdapterStatColor;
import example.sobtid.com.sobtid.api.modelrequest.RequestBillingPlanSub;
import example.sobtid.com.sobtid.api.modelrequest.RequestShowsuggestion;
import example.sobtid.com.sobtid.api.modelrequest.RequestStat;
import example.sobtid.com.sobtid.api.request.Billing;
import example.sobtid.com.sobtid.api.request.Stat;
import example.sobtid.com.sobtid.coderequest.RequestCode;
import example.sobtid.com.sobtid.model.ResultBillingPlanSub;
import example.sobtid.com.sobtid.model.ResultRequirekey;
import example.sobtid.com.sobtid.model.ResultShowsuggestion;
import example.sobtid.com.sobtid.model.ResultStat;
import example.sobtid.com.sobtid.utility.GridAutofitLayoutManager;
import example.sobtid.com.sobtid.utility.Utility;
import lecho.lib.hellocharts.animation.ChartAnimationListener;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.LineChartView;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 */
public class StatFragment extends BaseFragment implements TabLayout.OnTabSelectedListener {
sdasdasdasdsad
    private View view = null;
    private TabLayout tabLayout = null;
    private RecyclerView recyclerViewStat = null;

    private LineChartView chart;
    private LineChartData data;
    private int numberOfLines = 1;
    private int maxNumberOfLines = 10;
    private int numberOfPoints = 20;

    float[][] randomNumbersTab = new float[maxNumberOfLines][numberOfPoints];

    private boolean hasAxes = true;
    private boolean hasAxesNames = true;
    private boolean hasLines = true;
    private boolean hasPoints = true;
    private ValueShape shape = ValueShape.CIRCLE;
    private boolean isFilled = false;
    private boolean hasLabels = false;
    private boolean isCubic = true;
    private boolean hasLabelForSelected = false;
    private boolean pointsHaveDifferentColor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_stat, container, false);
        init();
        setRecyclerView();
        getKey(getKey, errorAction, RequestCode.request_code_stat);//call for get Data stat show in stat
        getKey(getKey, errorAction, RequestCode.request_code_showsuggestion);//call for get Data stat show in stat
      //  getActivity().getSupportFragmentManager().beginTransaction().add(R.id.container, new PlaceholderFragment()).commit();
        return view;
    }

    private void init(){
        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setOnTabSelectedListener(this);
        chart = (LineChartView) view.findViewById(R.id.chart);
        //  chart.setOnValueTouchListener(new PlaceholderFragment.ValueTouchListener());
        chart.setZoomEnabled(false);

    }


    private void setRecyclerView() {
        recyclerViewStat = (RecyclerView) view.findViewById(R.id.recycleview_interest);
        recyclerViewStat.setNestedScrollingEnabled(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerViewStat.setLayoutManager(llm);
        recyclerViewStat.setLayoutManager(new GridAutofitLayoutManager(getContext(), 200));
    }

    private Action1<ResultRequirekey> getKey = new Action1<ResultRequirekey>() {
        @Override
        public void call(ResultRequirekey s) {

            if (s.status.equals("completed")) {
                if (getRekey(RequestCode.request_code_stat).equals(s.result.action))
                    getStat(s);
                if (getRekey(RequestCode.request_code_showsuggestion).equals(s.result.action))
                    getShowsuggestion(s);

            } else {
                Utility.ShowMsg(getActivity(), s.message);
            }
        }
    };

    private Action1<Throwable> errorAction = new Action1<Throwable>() {
        @Override
        public void call(Throwable e) {
            e.printStackTrace();

            // On Error
        }
    };


    private void getStat(ResultRequirekey s) {
        retrofit().create(Stat.class).getstat(new RequestStat(MainActivity.token, MainActivity.member_id, s)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(getStat)
                .doOnError(errorAction).subscribe();
    }

    private Action1<ResultStat> getStat = new Action1<ResultStat>() {
        @Override
        public void call(ResultStat s) {
            if (s.status.equals("completed")) {
                setStat(s);
            } else {
                Utility.ShowMsg(getActivity(), s.message);
            }
        }
    };


    private void setStat(ResultStat s) {

        setTap(s.result);
        setAdapterColor(s.result);
        setChart(s);
        chart.setViewportCalculationEnabled(false);

    }

    private void setChart(ResultStat s){
        maxNumberOfLines = s.result.stat.size();
        setData(s);
        generateData(s);
    }

    private void setTap(ResultStat.Result result) {
        tabLayout.addTab(tabLayout.newTab().setText("ทั้งหมด"));
        for (int i = 0; i < result.stat.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(result.stat.get(i).name));
        }


    }

    private void setAdapterColor(ResultStat.Result result) {
        AdapterStatColor adapterStatColor = new AdapterStatColor(getActivity(), result);
        recyclerViewStat.setAdapter(adapterStatColor);

    }

    private void setData(ResultStat stat) {

        for (int i = 0; i < maxNumberOfLines; ++i) {
            numberOfPoints = stat.result.stat.get(i).data.size();
            for (int j = 0; j < numberOfPoints; ++j) {
                    randomNumbersTab[i][j] = Float.parseFloat(stat.result.stat.get(i).data.get(j).percent);
            }
        }

    }


    private void getShowsuggestion(ResultRequirekey s) {
        retrofit().create(Stat.class).getshowsuggestion(new RequestShowsuggestion(MainActivity.token, MainActivity.member_id, s)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(getShowsuggestion)
                .doOnError(errorAction).subscribe();
    }

    private Action1<ResultShowsuggestion> getShowsuggestion = new Action1<ResultShowsuggestion>() {
        @Override
        public void call(ResultShowsuggestion s) {
            if (s.status.equals("completed")) {
                setShowsuggestion(s);
            } else {
                Utility.ShowMsg(getActivity(), s.message);
            }
        }
    };

    private void setShowsuggestion(ResultShowsuggestion resultShowsuggestion){
        for (int i = 0 ; i < resultShowsuggestion.result.stat.size(); i++){
            Log.d("resultShowsuggestion",resultShowsuggestion.result.stat.get(i).tag);
        }
    }


    private String getRekey(String key) {
        return Utility.md5(key + "|sobtideiei");
    }

    @Override
    public void onStart() {
        super.onStart();

    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    //*********************************************************************** Chart ************************************************************************//




    private void generateData(ResultStat s) {

        List<Line> lines = new ArrayList<Line>();

        for (int i = 0; i < maxNumberOfLines; ++i) {
            numberOfPoints = s.result.stat.get(i).data.size();
            List<PointValue> values = new ArrayList<PointValue>();
            for (int j = 0; j < numberOfPoints; ++j) {
                values.add(new PointValue(j, randomNumbersTab[i][j]));
            }

            Line line = new Line(values);
            line.setColor(Color.parseColor(s.result.stat.get(i).color_html));
            //  line.setColor(ChartUtils.COLORS[i]);
            line.setShape(shape);
            line.setCubic(isCubic);
            line.setFilled(isFilled);
            line.setHasLabels(hasLabels);
            line.setHasLabelsOnlyForSelected(hasLabelForSelected);
            line.setHasLines(hasLines);
            line.setHasPoints(hasPoints);
            if (pointsHaveDifferentColor) {
                line.setPointColor(Color.parseColor(s.result.stat.get(i).color_html));
            }
            lines.add(line);
        }

        data = new LineChartData(lines);

        if (hasAxes) {
            Axis axisX = new Axis();
            Axis axisY = new Axis().setHasLines(true);
            if (hasAxesNames) {
                axisX.setName("ครั้งที่");
                axisY.setName("เปอร์เว็นต์");
            }
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);
        } else {
            data.setAxisXBottom(null);
            data.setAxisYLeft(null);
        }

        data.setBaseValue(Float.NEGATIVE_INFINITY);
        chart.setLineChartData(data);

    }


}