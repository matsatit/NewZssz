package com.xytsz.xytsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.xytsz.xytsz.R;
import com.xytsz.xytsz.adapter.SendAdapter;
import com.xytsz.xytsz.bean.Review;
import com.xytsz.xytsz.global.GlobalContanstant;
import com.xytsz.xytsz.net.NetUrl;
import com.xytsz.xytsz.util.JsonUtil;
import com.xytsz.xytsz.util.SpUtils;
import com.xytsz.xytsz.util.ToastUtil;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.List;

/**
 * Created by admin on 2017/2/17.
 * 派发界面
 */
public class SendActivity extends AppCompatActivity {

    private static final int ISSEND = 2003;
    private ListView mLv;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case ISSEND:
                    ToastUtil.shortToast(getApplicationContext(), "没有已审核的数据，请稍后重试");
                    break;
            }
        }
    };
    private int personId;
    private List<Review.ReviewRoad> list;
    private SendAdapter adapter;
    private int size;
    private ProgressBar mProgressBar;
    private TextView mtvfail;
    private String nodata;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send);
        personId = SpUtils.getInt(getApplicationContext(), GlobalContanstant.PERSONID);
        nodata = getString(R.string.send_nodata);
        initView();
        initData();

    }

    private void initView() {
        mLv = (ListView) findViewById(R.id.lv_send);
        mProgressBar = (ProgressBar) findViewById(R.id.review_progressbar);
        mtvfail = (TextView) findViewById(R.id.tv_send_fail);
    }

    private void initData() {
        mProgressBar.setVisibility(View.VISIBLE);
        //ToastUtil.shortToast(getApplicationContext(), "正在加载数据...");
        new Thread() {
            @Override
            public void run() {

                try {
                    //获取json
                    String reviewData = getReviewData(GlobalContanstant.GETSEND);

                    if (reviewData != null) {

                        Review review = JsonUtil.jsonToBean(reviewData, Review.class);
                        list = review.getReviewRoadList();

                        int sendSum = 0;
                        for (Review.ReviewRoad reviewRoad : list){
                            sendSum += reviewRoad.getList().size();
                        }
                        SpUtils.saveInt(getApplicationContext(),GlobalContanstant.SENDSUM,sendSum);


                        adapter = new SendAdapter(list);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (adapter != null) {

                                    mLv.setAdapter(adapter);
                                    if (list.size() == 0){
                                        mtvfail.setText(nodata);
                                        mtvfail.setVisibility(View.VISIBLE);
                                        ToastUtil.shortToast(getApplicationContext(),nodata);
                                    }
                                    mProgressBar.setVisibility(View.GONE);
                                }
                            }
                        });


                    }else {

                        Message message = Message.obtain();
                        message.what = ISSEND;
                        handler.sendMessage(message);


                    }
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }.start();


        mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(SendActivity.this, SendRoadActivity.class);
                intent.putExtra("position", position);
                startActivityForResult(intent,600);
            }
        });
    }

    public static String getReviewData(int phaseIndication) throws Exception {
        SoapObject soapObject = new SoapObject(NetUrl.nameSpace, NetUrl.getTaskList);
        soapObject.addProperty("PhaseIndication", phaseIndication);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER12);
        envelope.bodyOut = soapObject;//由于是发送请求，所以是设置bodyOut
        envelope.dotNet = true;
        envelope.setOutputSoapObject(soapObject);

        HttpTransportSE httpTransportSE = new HttpTransportSE(NetUrl.SERVERURL);
        httpTransportSE.call(NetUrl.getTasklist_SOAP_ACTION, envelope);

        SoapObject object = (SoapObject) envelope.bodyIn;
        String json = object.getProperty(0).toString();

        Log.i("json", json);
        return json;
    }

    private int position;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (resultCode){
            case 505:
                position = data.getIntExtra("position", -1);
                int passposition = data.getIntExtra("passposition", -1);
                //list.get(position).getList().remove(passposition);

                int size = list.get(position).getList().size();
                if (size == 0 ){
                    list.remove(position);
                }else {
                    list.get(position).getList().remove(passposition);
                }


                adapter.notifyDataSetChanged();

                break;

            case 605:
                position = data.getIntExtra("position", -1);
                int failposition = data.getIntExtra("failposition", -1);
                //list.get(position).getList().remove(failposition);

                size = list.get(position).getList().size();
                if (size == 0 ){
                    list.remove(position);
                }else {
                    list.get(position).getList().remove(failposition);
                }
                adapter.notifyDataSetChanged();
                break;
        }



        super.onActivityResult(requestCode, resultCode, data);
    }
}
