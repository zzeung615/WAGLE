package com.androidlec.wagle.JH;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.androidlec.wagle.R;
import com.androidlec.wagle.activity.wagleSub.AddBJMActivity;
import com.androidlec.wagle.activity.wagleSub.AddDHGActivity;
import com.androidlec.wagle.networkTask.JH_IntNetworkTask;
import com.androidlec.wagle.networkTask.JH_ObjectNetworkTask_Payment;
import com.androidlec.wagle.networkTask.JH_VoidNetworkTask;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MyWagleActivity extends AppCompatActivity {


    final static String TAG = "Log check : ";
    String urlAddr;
    ListView lv_itemlist;
    ArrayList<Payment> lists;
    PaymentAdapter adapter;
    String item;
    int price, paymentcnt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wagle);

        init();
        getData();
    }


    @Override
    protected void onResume() {
        super.onResume();
        // 리스트 가져오기.
        urlDivider("paymentList", 0, null,0);
        getTotal();
    }


    private void init() {
        // 독후감 파트.
        Button btn_bookreportAdd = findViewById(R.id.mywagle_btn_bookreportAdd);
        ProgressBar progressBar = findViewById(R.id.mywagle_pb_progress);
        Button btn_suggestionAdd = findViewById(R.id.mywagle_btn_suggestionAdd);
        ListView listView = findViewById(R.id.mywagle_lv_bookreport);
        btn_bookreportAdd.setOnClickListener(onClickListener);
        btn_suggestionAdd.setOnClickListener(onClickListener);

        // 갤러리 파트.
        Button btn_galleryAdd = findViewById(R.id.mywagle_btn_galleryAdd);
        ImageView iv_gallery1 = findViewById(R.id.mywagle_iv_gallery1);
        ImageView iv_gallery2 = findViewById(R.id.mywagle_iv_gallery2);
        ImageView iv_gallery3 = findViewById(R.id.mywagle_iv_gallery3);
        TextView tv_galleryPlus = findViewById(R.id.mywagle_tv_galleryPlus);
        btn_galleryAdd.setOnClickListener(onClickListener);
        tv_galleryPlus.setOnClickListener(onClickListener);

        // 정산 파트.
        Button btn_paymentAdd = findViewById(R.id.mywagle_btn_paymentAdd);
        LinearLayout layout = findViewById(R.id.payment_ll_paymentActivity);

        switch (paymentCnt()) {
            case 2:
                btn_paymentAdd.setVisibility(View.VISIBLE);
                layout.setVisibility(View.INVISIBLE);
                break;
            case 1:
                btn_paymentAdd.setVisibility(View.INVISIBLE);
                layout.setVisibility(View.VISIBLE);
                break;
            default:
                btn_paymentAdd.setVisibility(View.INVISIBLE);
                layout.setVisibility(View.INVISIBLE);
                break;
        }

        lv_itemlist = findViewById(R.id.payment_lv_itemlist);
        FloatingActionButton addItemBtn = findViewById(R.id.payment_btn_addItem);

        btn_paymentAdd.setOnClickListener(onClickListener);
        addItemBtn.setOnClickListener(onClickListener);
    }


    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = null;

            switch (view.getId()){
                case R.id.mywagle_btn_bookreportAdd:
                    startActivity(new Intent(MyWagleActivity.this, AddDHGActivity.class));
                    break;
                case R.id.mywagle_btn_suggestionAdd:
                    startActivity(new Intent(MyWagleActivity.this, AddBJMActivity.class));
                    break;
                case R.id.mywagle_btn_galleryAdd:
                    break;
                case R.id.mywagle_tv_galleryPlus:
                    break;
                case R.id.mywagle_btn_paymentAdd:
                    break;
                case R.id.payment_btn_addItem:
                    popupAddItem();
                    break;
            }
        }
    };


    private void getData(){
        // 리스트 가져오기.
        urlDivider("paymentList", 0, null,0);
    }


    private void getTotal(){
        int total = 0;
        for (int i = 0 ; i < lists.size() ; i++) {
            total += lists.get(i).getPrice();
        }
        TextView tv_total = findViewById(R.id.payment_tv_total);
        tv_total.setText(total + "원");

        int memNo = 10;
        int ppp = total/memNo;
        TextView tv_PPP = findViewById(R.id.payment_tv_PricePerPerson);
        tv_PPP.setText(ppp + "원");
    }


    private int paymentCnt(){
        int wcSeqno = 1; // 임시 절대값.
        paymentcnt = 3;
        urlAddr = "http://192.168.0.178:8080/wagle/paymentCnt.jsp?";
        urlAddr = urlAddr + "wcSeqno=" + wcSeqno;
        connectDB("paymentCnt");
        return paymentcnt;
    }


    private void urlDivider(String function, int wpSeqno, String wpItem, int wpPrice){
        String wcSeqno = "1"; // 임의로 절대값 넣음.
        switch(function){
            case "wpItemAdd":
                urlAddr = "http://192.168.0.178:8080/wagle/wpItemAdd.jsp?";
                urlAddr = urlAddr + "wcSeqno=" + wcSeqno + "&wpItem=" + item + "&wpPrice=" + price;
                connectDB("wpItemAdd");
                break;
            case "paymentList":
                urlAddr = "http://192.168.0.178:8080/wagle/paymentList.jsp?";
                urlAddr = urlAddr + "wcSeqno=" + wcSeqno;
                connectDB("paymentList");
                break;
            case "deleteItem":
                urlAddr = "http://192.168.0.178:8080/wagle/deleteItem.jsp?";
                urlAddr = urlAddr + "wpSeqno=" + wpSeqno;
                connectDB("deleteItem");
                break;

        }
    }


    private void connectDB(String function){
        try {
            switch (function){
                case "paymentCnt":
                    JH_IntNetworkTask intNetworkTask = new JH_IntNetworkTask(MyWagleActivity.this, urlAddr);
                    paymentcnt = intNetworkTask.execute().get();
                case "wpItemAdd":
                case "deleteItem":
                    JH_VoidNetworkTask voidNetworkTask = new JH_VoidNetworkTask(MyWagleActivity.this, urlAddr);
                    voidNetworkTask.execute().get();
                    break;
                case "paymentList":
                    JH_ObjectNetworkTask_Payment networkTask = new JH_ObjectNetworkTask_Payment(MyWagleActivity.this, urlAddr);
                    Object obj = networkTask.execute().get(); // 오브젝트로 받아야함.
                    lists = (ArrayList<Payment>) obj; // cast.
                    adapter = new PaymentAdapter(MyWagleActivity.this, R.layout.customlayout_payment_listview, lists); // making adapter.
                    lv_itemlist.setAdapter(adapter);
//                    lv_itemlist.setOnItemClickListener(onItemClickListener);
                    lv_itemlist.setOnItemLongClickListener(onItemLongClickListener);
                    if(paymentCnt() == 1) {
                        setListViewHeightBasedOnChildren(lv_itemlist); // 리스트뷰 길이 조절.
                    }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    //-------------- 리스트뷰 길이 조절 -----------------------------------------------------------------
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
    //----------------------------------------------------------------------------------------------


    // ----------------- 꾹~ 롱클릭 이벤트 --------------------------------------------------------------------------------------------------
    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int position, long l) {
//            click = 1;

            // --------------- 대화상자 띄우기 ---------------------------------------------------------
            new AlertDialog.Builder(MyWagleActivity.this)
                    .setTitle("항목을 삭제하시겠습니까?")
                    .setCancelable(false)
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            click = 0;
                        }
                    })
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            urlDivider("deleteItem", lists.get(position).getWpSeqno(), null, 0); // position(번째) 값 입력하면서, 데이터 삭제하기 함수 실행.
//                            click = 1;
                            Toast.makeText(MyWagleActivity.this, "해당 항목이 삭제 되었습니다.", Toast.LENGTH_SHORT).show();
                            MyWagleActivity.this.onResume();
                        }
                    })
                    .show();
            // -------------------------------------------------------------------------------------
            return false;
        }
    };


    private void popupAddItem(){
        // --------------- 대화상자 띄우기 ---------------------------------------------------------
        final LinearLayout linearLayout = (LinearLayout) View.inflate(MyWagleActivity.this, R.layout.customdialog_payment_additem, null);
        new AlertDialog.Builder(MyWagleActivity.this)
                .setView(linearLayout) // 불러줌.
                .setCancelable(true)
                .setPositiveButton("추가하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // 다이어로그 안에 있는 xml.
                        EditText et_item = linearLayout.findViewById(R.id.payment_et_item);
                        EditText et_price = linearLayout.findViewById(R.id.payment_et_price);
                        item = et_item.getText().toString().trim();
                        price = Integer.parseInt(et_price.getText().toString().trim());

                        urlDivider("wpItemAdd",0, item, price); // wpItemAdd()
                        Toast.makeText(MyWagleActivity.this, "추가 되었습니다.", Toast.LENGTH_SHORT).show();

                        MyWagleActivity.this.onResume();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(MyWagleActivity.this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setCancelable(false)
                .show();
        // -------------------------------------------------------------------------------------
    }




}//----