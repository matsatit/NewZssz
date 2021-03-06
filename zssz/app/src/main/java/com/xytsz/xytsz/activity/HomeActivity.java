package com.xytsz.xytsz.activity;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.baidu.mapapi.SDKInitializer;
import com.google.gson.reflect.TypeToken;
import com.xytsz.xytsz.bean.Deal;
import com.xytsz.xytsz.bean.DealType;
import com.xytsz.xytsz.bean.DiseaseType;
import com.xytsz.xytsz.bean.FacilityName;
import com.xytsz.xytsz.bean.FacilitySpecifications;
import com.xytsz.xytsz.bean.FacilityType;
import com.xytsz.xytsz.bean.Road;
import com.xytsz.xytsz.bean.UpdateStatus;
import com.xytsz.xytsz.bean.VersionInfo;
import com.xytsz.xytsz.fragment.MainFragment;
import com.xytsz.xytsz.fragment.MainFragments;
import com.xytsz.xytsz.fragment.SuperviseFragment;
import com.xytsz.xytsz.global.GlobalContanstant;
import com.xytsz.xytsz.base.BaseFragment;
import com.xytsz.xytsz.fragment.HomeFragment;
import com.xytsz.xytsz.adapter.MainAdapter;
import com.xytsz.xytsz.fragment.MeFragment;

import com.xytsz.xytsz.net.NetUrl;
import com.xytsz.xytsz.ui.NoScrollViewpager;
import com.xytsz.xytsz.R;

import com.xytsz.xytsz.util.IntentUtil;
import com.xytsz.xytsz.util.JsonUtil;
import com.xytsz.xytsz.util.PermissionUtils;
import com.xytsz.xytsz.util.PermissionUtils.PermissionGrant;
import com.xytsz.xytsz.util.SpUtils;
import com.xytsz.xytsz.util.ToastUtil;
import com.xytsz.xytsz.util.UpdateVersionUtil;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.microedition.khronos.opengles.GL;

/**
 * Created by admin on 2017/1/3.
 * <p/>
 * 主页
 */
public class HomeActivity extends AppCompatActivity {


    private RadioGroup mRadiogroup;
    private NoScrollViewpager mViewpager;
    private ArrayList<Fragment> fragments;
    private Boolean isFive;
    private RelativeLayout rl_notonlie;
    private Button mbtrefresh;
    private ProgressBar mprogressbar;
    private int role;
    private boolean isOnCreat;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * 没有登陆的时候，先登陆
         */

        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.activity_home);
        String loginId = SpUtils.getString(getApplicationContext(), GlobalContanstant.LOGINID);

        role = SpUtils.getInt(getApplicationContext(), GlobalContanstant.ROLE);

        if (loginId == null || TextUtils.isEmpty(loginId)) {
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            HomeActivity.this.finish();
        }

        /**
         *
         * 最后去掉注释
         */

        initView();

        mbtrefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isNetworkAvailable(getApplicationContext())) {
                    getData();
                    //修改
                    mViewpager.setVisibility(View.VISIBLE);
                    rl_notonlie.setVisibility(View.GONE);
                    mprogressbar.setVisibility(View.GONE);
                } else {
                    ToastUtil.shortToast(getApplicationContext(), "请检查网络");
                }
            }
        });

        initData();
    }

    private void getData() {
        new Thread() {
            @Override
            public void run() {
                try {
                    int allUserCount = getAllUserCount(NetUrl.getAllUserCountMethodName, NetUrl.getAllUserCount_SOAP_ACITION);
                    SpUtils.saveInt(getApplicationContext(), GlobalContanstant.ALLUSERCOUNT, allUserCount);

                } catch (Exception e) {

                }

            }
        }.start();

    }

    public int getAllUserCount(String method, String soap_action) throws Exception {
        SoapObject soapObject = new SoapObject(NetUrl.nameSpace, method);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = soapObject;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(soapObject);

        HttpTransportSE httpTransportSE = new HttpTransportSE(NetUrl.SERVERURL);
        httpTransportSE.call(soap_action, envelope);

        SoapObject object = (SoapObject) envelope.bodyIn;

        return Integer.valueOf(object.getProperty(0).toString());

    }


    private void initView() {
        mRadiogroup = (RadioGroup) findViewById(R.id.homeactivity_rg_radiogroup);
        mViewpager = (NoScrollViewpager) findViewById(R.id.homeactivity_vp);

        rl_notonlie = (RelativeLayout) findViewById(R.id.rl_notonline);
        mprogressbar = (ProgressBar) findViewById(R.id.home_progressbar);
        mbtrefresh = (Button) findViewById(R.id.btn_refresh);
        //默认显示home界面
        mRadiogroup.check(R.id.homeactivity_rbtn_home);
    }

    private void initData() {

        fragments = new ArrayList<>();
        fragments.clear();
        fragments.add(new HomeFragment());
        fragments.add(new MainFragments());
        fragments.add(new SuperviseFragment());
        fragments.add(new MeFragment());
        //把fragment填充到viewpager

        MainAdapter adapter = new MainAdapter(getSupportFragmentManager(), fragments);
        mViewpager.setAdapter(adapter);
        mViewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            //当界面切换完成的时候
            @Override
            public void onPageSelected(int position) {
                BaseFragment fragment = (BaseFragment) fragments.get(position);
                //加载的时候可能会出错
                try {
                    fragment.initData();
                } catch (Exception e) {

                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        if (role == 0) {
            mViewpager.setCurrentItem(1, false);
            mRadiogroup.check(R.id.homeactivity_rbtn_working);
        }


        mRadiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {

                    //加载首页的时候加载Main
                    case R.id.homeactivity_rbtn_home:
                        mViewpager.setCurrentItem(0, false);
                        break;

                    //加载会员
                    case R.id.homeactivity_rbtn_working:
                        mViewpager.setCurrentItem(1, false);
                        break;

                    //加载更多的时候  加载 监管页面
                    case R.id.homeactivity_rbtn_more:
                        mViewpager.setCurrentItem(2, false);
                        break;


                    //我的界面
                    case R.id.homeactivity_rbtn_me:
                        mViewpager.setCurrentItem(3, false);
                        break;


                }
            }
        });


        if (getIntent() != null) {
            String backHome = getIntent().getStringExtra("backHome");
            if (backHome != null && backHome.equals(GlobalContanstant.BACKHOME)) {
                mViewpager.setCurrentItem(0, false);
            }
        }


        //先判断有没有现版本
        new Thread() {
            @Override
            public void run() {
                try {
                    String versionInfo = UpdateVersionUtil.getVersionInfo();
                    Message message = Message.obtain();
                    message.obj = versionInfo;
                    message.what = VERSIONINFO;
                    handler.sendMessage(message);
                } catch (Exception e) {

                }

            }
        }.start();


    }


    /**
     * @param context： 上下文
     * @return 网络是否可用
     */

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }


    private static final int DATA_SUCCESS = 1166666;
    private static final int VERSIONINFO = 144211;
    private static final int DATA_REPORT = 155552;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case DATA_SUCCESS:
                    mViewpager.setVisibility(View.VISIBLE);
                    rl_notonlie.setVisibility(View.GONE);
                    mprogressbar.setVisibility(View.GONE);
                    break;
                case VERSIONINFO:
                    String info = (String) msg.obj;
                    if (info != null) {
                        //检查更新
                        UpdateVersionUtil.localCheckedVersion(getApplicationContext(), new UpdateVersionUtil.UpdateListener() {

                            @Override
                            public void onUpdateReturned(int updateStatus, final VersionInfo versionInfo) {
                                //判断回调过来的版本检测状态
                                switch (updateStatus) {
                                    case UpdateStatus.YES:
                                        //弹出更新提示
                                        UpdateVersionUtil.showDialog(HomeActivity.this, versionInfo);
                                        break;
                                    case UpdateStatus.NO:
                                        //没有新版本
                                        //ToastUtil.shortToast(getApplicationContext(), "已经是最新版本了!");
                                        break;
                                    case UpdateStatus.NOWIFI:
                                        //当前是非wifi网络
                                        //UpdateVersionUtil.showDialog(getContext(),versionInfo);

                                        new AlertDialog.Builder(HomeActivity.this).setTitle("温馨提示").setMessage("当前非wifi网络,下载会消耗手机流量!").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                UpdateVersionUtil.showDialog(HomeActivity.this, versionInfo);
                                            }
                                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).create().show();


                                        break;
                                    case UpdateStatus.ERROR:
                                        //检测失败
                                        ToastUtil.shortToast(getApplicationContext(), "检测失败，请稍后重试！");
                                        break;
                                    case UpdateStatus.TIMEOUT:
                                        //链接超时
                                        ToastUtil.shortToast(getApplicationContext(), "链接超时，请检查网络设置!");
                                        break;
                                }
                            }
                        }, info);
                    }
                    break;

                case DATA_REPORT:
                    mprogressbar.setVisibility(View.GONE);
                    rl_notonlie.setVisibility(View.VISIBLE);
                    ToastUtil.shortToast(getApplicationContext(), "网络异常,未获取数据,请刷新");
                    break;
            }
        }
    };

    /**
     * 防止误触退出
     */
    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                ToastUtil.shortToast(HomeActivity.this, "再按一次退出程序");
                mExitTime = System.currentTimeMillis();

            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onResume() {
        super.onResume();

        //判断是否有网络
        //做判断 如果是只走 onResume 那就请求  ,如果走onCreate 就不请求
        //isOncreat  =false

        if (isNetworkAvailable(getApplicationContext())) {
            getData();
            mViewpager.setVisibility(View.VISIBLE);
            rl_notonlie.setVisibility(View.GONE);
            mprogressbar.setVisibility(View.GONE);
        } else {
            ToastUtil.shortToast(getApplicationContext(), "未连接网络");
            rl_notonlie.setVisibility(View.VISIBLE);
            mViewpager.setVisibility(View.GONE);

        }





    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }


}
