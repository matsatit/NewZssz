package com.xytsz.xytsz.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.xytsz.xytsz.MyApplication;
import com.xytsz.xytsz.R;
import com.xytsz.xytsz.adapter.MyReportAdapter;
import com.xytsz.xytsz.bean.ForMyDis;
import com.xytsz.xytsz.bean.ImageUrl;
import com.xytsz.xytsz.global.GlobalContanstant;
import com.xytsz.xytsz.net.NetUrl;
import com.xytsz.xytsz.util.JsonUtil;
import com.xytsz.xytsz.util.SpUtils;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.DuplicateFormatFlagsException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by admin on 2017/5/26.
 * 自己上报的数据
 */
public class MyReporteActivity extends AppCompatActivity {

    private static final int  REPORTE= 100003;
    @Bind(R.id.lv_reprote)
    ListView lvReprote;
    private int personId;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case REPORTE:

                    final List<ForMyDis> details = (List<ForMyDis>) msg.obj;
                    if (details.size() != 0) {
                        MyReportAdapter adapter = new MyReportAdapter(details,imageUrlLists);
                        if (adapter != null) {
                            lvReprote.setAdapter(adapter);
                        }


                        lvReprote.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Intent intent = new Intent(MyReporteActivity.this,MyReporteDetailActivity.class);
                                intent.putExtra("detail",details.get(position));
                                intent.putExtra("imageUrlReport",(Serializable) imageUrlLists.get(position));
                                startActivity(intent);
                            }
                        });
                    }
                    break;
            }
        }
    };
    private List<List<ImageUrl>> imageUrlLists = new ArrayList<>();

    @Override
    protected void 
    onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myreporte);
        ButterKnife.bind(this);

        personId =   SpUtils.getInt(getApplicationContext(), GlobalContanstant.PERSONID);
        initData();



    }

    private void initData() {
        new Thread(){
            @Override
            public void run() {
                String data = getData();
                if (data != null){
                    List<ForMyDis> details = JsonUtil.jsonToBean(data, new TypeToken<List<ForMyDis>>() {
                    }.getType());



                    for (ForMyDis forMyDis:details){
                        String taskNumber = forMyDis.getTaskNumber();

                        String json = null;
                        try {
                            json = MyApplication.getAllImagUrl(taskNumber, GlobalContanstant.GETREVIEW);

                            if (json != null) {
                                //String list = new JSONObject(json).getJSONArray("").toString();
                                List<ImageUrl> imageUrlList = new Gson().fromJson(json, new TypeToken<List<ImageUrl>>() {
                                }.getType());

                                imageUrlLists.add(imageUrlList);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }


                    Message message = Message.obtain();
                    message.obj = details;
                    message.what = REPORTE;
                    handler.sendMessage(message);
                }

            }
        }.start();
    }


    private String getData() {
        SoapObject soapObject = new SoapObject(NetUrl.nameSpace,NetUrl.getALlReportByPersonID);

        soapObject.addProperty("personid",personId);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER12);
        envelope.bodyOut = soapObject;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(soapObject);


        HttpTransportSE httpTransportSE = new HttpTransportSE(NetUrl.SERVERURL);
        try {
            httpTransportSE.call(NetUrl.getALlReportByPersonID_SOAP_ACTION,envelope);
        } catch (Exception e) {
            e.printStackTrace();
        }

        SoapObject object = (SoapObject) envelope.bodyIn;
        String result = object.getProperty(0).toString();
        return result;
    }
}
