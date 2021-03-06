package com.xytsz.xytsz.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.xytsz.xytsz.R;
import com.xytsz.xytsz.adapter.ReviewAdapter;
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
 * Created by admin on 2017/1/11.
 * 审核页面
 */
public class ReviewActivity extends AppCompatActivity{

    private static final int FAIL = 500;
    private ListView mLv;

    private String json;
    private int personId;
    private List<Review.ReviewRoad> list;
    private ReviewAdapter reviewAdapter;
    private int position;
    private int size;
    private ProgressBar mProgressBar;
    private TextView tvFail;
    private String nodata;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case FAIL:
                    mProgressBar.setVisibility(View.GONE);
                    ToastUtil.shortToast(getApplicationContext(),"未获取数据,请稍后");
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        initAcitionbar();
        personId = SpUtils.getInt(getApplicationContext(), GlobalContanstant.PERSONID);
        nodata = getString(R.string.review_nodata);
        initView();
        //获取的是所有人员的上报信息 这里显示的就是所有的道路
        initData();

    }

    private void initView() {
        mLv = (ListView) findViewById(R.id.review_lv);
        mProgressBar = (ProgressBar) findViewById(R.id.review_progressbar);
        tvFail = (TextView)findViewById(R.id.tv_fail);
    }

    private void initData() {

        mProgressBar.setVisibility(View.VISIBLE);
        GetTaskAsyn getTaskAsyn = new GetTaskAsyn();
        getTaskAsyn.execute(GlobalContanstant.GETREVIEW);

    }

    public static String getServiceData(int phaseIndication)throws Exception {
        SoapObject soapObject = new SoapObject(NetUrl.nameSpace,NetUrl.getTaskList);
        soapObject.addProperty("PhaseIndication",phaseIndication);
       // soapObject.addProperty("personid",personid);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER12);
        envelope.bodyOut = soapObject;//由于是发送请求，所以是设置bodyOut
        envelope.dotNet = true;
        envelope.setOutputSoapObject(soapObject);

        HttpTransportSE httpTransportSE = new HttpTransportSE(NetUrl.SERVERURL);
        httpTransportSE.call(NetUrl.getTasklist_SOAP_ACTION,envelope);

        SoapObject object = (SoapObject) envelope.bodyIn;
        String json = object.getProperty(0).toString();

        return json;
    }


    class GetTaskAsyn extends AsyncTask<Integer,Integer,String>{


        @Override
        protected String doInBackground(Integer... params) {
            try {
                 json = getServiceData(params[0]);

                //Log.i("result",json);
            } catch (Exception e) {
                Message obtain = Message.obtain();
                obtain.what = FAIL;
                handler.sendMessage(obtain);
            }
            return json;
        }

        @Override
        protected void onPostExecute(String json) {
            //解析json
            if (json != null) {
                //转化成bean
                Review review = JsonUtil.jsonToBean(json, Review.class);


                list = review.getReviewRoadList();


                if (list == null || list.size() == 0){
                    tvFail.setVisibility(View.VISIBLE);
                    tvFail.setText(nodata);
                    mProgressBar.setVisibility(View.GONE);
                }

                reviewAdapter = new ReviewAdapter(list);


                mProgressBar.setVisibility(View.GONE);
                mLv.setAdapter(reviewAdapter);


                mLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(ReviewActivity.this, RoadActivity.class);
                        intent.putExtra("position", position);
                        startActivityForResult(intent,500);
                    }
                });
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode){
            case 500:
                position = data.getIntExtra("position", -1);
                int lastposition = data.getIntExtra("lastposition", -1);
                list.get(position).getList().remove(lastposition);
                size = list.get(position).getList().size();

                if (size == 0 ){
                    list.remove(position);
                }
                reviewAdapter.notifyDataSetChanged();
                break;
            case 600:
                position = data.getIntExtra("position", -1);
                int failposition = data.getIntExtra("failposition", -1);
                list.get(position).getList().remove(failposition);
                size = list.get(position).getList().size();
                if (size == 0 ){
                    list.remove(position);
                }
                reviewAdapter.notifyDataSetChanged();
                break;
        }
    }



    private void initAcitionbar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(R.string.review);
        }
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
