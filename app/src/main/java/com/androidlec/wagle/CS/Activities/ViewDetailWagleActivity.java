package com.androidlec.wagle.CS.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidlec.wagle.CS.Model.WagleList;
import com.androidlec.wagle.R;
import com.androidlec.wagle.UserInfo;
import com.androidlec.wagle.dto.BookInfo;
import com.androidlec.wagle.networkTask.JH_VoidNetworkTask;
import com.androidlec.wagle.network_sh.NetworkTask_BookInfo;


public class ViewDetailWagleActivity extends AppCompatActivity {

    private TextView et_title, et_startDate, et_endDate, et_dueDate, et_location, et_fee, et_wagleDetail, et_wagleAgreeRefund, tv_joinIn;

    private TextView tv_bookInfo, tv_num6Name;

    private CheckBox cb_agreement;
    String title;
    Intent intent;

    private Boolean cbClick = false;

    //BookInfo
    private TextView bk_title, bk_writer, bk_maxpage, bk_intro, bk_data, bk_checkOk;
    private BookInfo bookinfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_detail_wagle);

        init();
        getData();

    }

    private void init() {
        et_title = findViewById(R.id.vdw_cs_et_title);
        et_startDate = findViewById(R.id.vdw_cs_tv_startDate);
        et_endDate = findViewById(R.id.vdw_cs_tv_endDate);
        et_dueDate = findViewById(R.id.vdw_cs_tv_dueDate);
        et_location = findViewById(R.id.vdw_cs_et_location);
        et_fee = findViewById(R.id.vdw_cs_et_fee);
        et_wagleDetail = findViewById(R.id.vdw_cs_tv_wagleDetail);
        et_wagleAgreeRefund = findViewById(R.id.vdw_cs_et_wagleAgreeRefund);
        tv_joinIn = findViewById(R.id.vdw_cs_tv_joinIn);
        cb_agreement = findViewById(R.id.vdw_cs_cb_agreement);
        tv_bookInfo = findViewById(R.id.vdw_cs_tv_bookInfo);
        tv_num6Name = findViewById(R.id.vdw_cs_tv_num6name);

        tv_joinIn.setOnClickListener(onClickListener);
        tv_bookInfo.setOnClickListener(onClickListener);

        intent = getIntent();

        intent = getIntent();

    }

    private void getData() {
        WagleList data = intent.getParcelableExtra("data");

        setData(data);

        String centIP = "192.168.0.138";
        String url = "http://" + centIP + ":8080/test/wagle_bookinfoGet.jsp?wcSeqno=" + intent.getStringExtra("wcSeqno");
        Log.e("ViewDetailWagle", url);
        bookinfo = getBookinfo(url);
    }

    private void setData(WagleList data) {
        title = data.getWcName();
        et_title.setText(data.getWcName());
        et_startDate.setText(data.getWcStartDate());
        et_endDate.setText(data.getWcEndDate());
        et_dueDate.setText(data.getWcDueDate());
        et_location.setText(data.getWcLocate());
        et_fee.setText(data.getWcEntryFee());
        et_wagleDetail.setText(data.getWcWagleDetail());
        et_wagleAgreeRefund.setText(data.getWcWagleAgreeRefund());

        if (data.getWcType().equals("투데이")) {
            et_wagleAgreeRefund.setVisibility(View.GONE);
            tv_bookInfo.setVisibility(View.GONE);
            tv_num6Name.setVisibility(View.GONE);

        }
    }

    View.OnClickListener onClickListener = v -> {
        switch (v.getId()) {
            case R.id.vdw_cs_tv_joinIn:
                if (!cbClick) {
                    cb_agreement.setVisibility(View.VISIBLE);
                    cbClick = true;
                } else {
                    if (cb_agreement.isChecked()) {
                        Toast.makeText(this, title + " 와글에 가입되었습니다.", Toast.LENGTH_SHORT).show();
                        joinInWagle();
                    } else {
                        Toast.makeText(this, "동의 사항 및 환불규정을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }

                }
                break;
            case R.id.vdw_cs_tv_bookInfo:
                setContentView(R.layout.custom_bookinfo_sh);

                bk_title = findViewById(R.id.bookinfo_tv_bookname);
                bk_writer = findViewById(R.id.bookinfo_tv_bookwriter);
                bk_maxpage = findViewById(R.id.bookinfo_tv_bookmaxpage);
                bk_intro = findViewById(R.id.bookinfo_tv_bookinfo);
                bk_data = findViewById(R.id.bookinfo_tv_bookdata);

                bk_checkOk = findViewById(R.id.bookinfo_tv_checkBtn);

                bk_title.setText(bookinfo.getTitle());
                bk_writer.setText(bookinfo.getWriter());
                bk_maxpage.setText(Integer.toString(bookinfo.getMaxpage()));
                bk_intro.setText(bookinfo.getIntro());
                bk_data.setText(bookinfo.getData());

                bk_checkOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });


        }
    };


    private void joinInWagle() {
        String wcSeqno = intent.getStringExtra("wcSeqno");
        String uSeqno = String.valueOf(UserInfo.USEQNO);
        String urlAddr = "http://192.168.0.178:8080/wagle/joinInWagle.jsp?";
        urlAddr = urlAddr + "Moim_mSeqno=" + UserInfo.MOIMSEQNO + "&wcSeqno=" + wcSeqno + "&uSeqno=" + uSeqno;
        try {
            JH_VoidNetworkTask networkTask = new JH_VoidNetworkTask(ViewDetailWagleActivity.this, urlAddr);
            networkTask.execute().get();
        }catch (Exception e){
            e.printStackTrace();
        }
        finish();
    }

    private BookInfo getBookinfo(String urlAddr) {
        BookInfo result = null;
        try {
            NetworkTask_BookInfo networkTask = new NetworkTask_BookInfo(ViewDetailWagleActivity.this, urlAddr);
            Object obj = networkTask.execute().get();

            result = (BookInfo) obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}//---