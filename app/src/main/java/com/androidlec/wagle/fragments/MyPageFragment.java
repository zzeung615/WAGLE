package com.androidlec.wagle.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.androidlec.wagle.CS.Model.WagleList;
import com.androidlec.wagle.JH.MyWagleActivity;
import com.androidlec.wagle.JH.Rank;
import com.androidlec.wagle.JH.RankAdapter;
import com.androidlec.wagle.R;
import com.androidlec.wagle.UserInfo;
import com.androidlec.wagle.activity.wagleSub.AddDHGActivity;
import com.androidlec.wagle.jhj.Jhj_HomeAndMyPage_Plus_List;
import com.androidlec.wagle.CS.Activities.ViewDetailWagleActivity;
import com.androidlec.wagle.jhj.Jhj_MyPage_DTO;
import com.androidlec.wagle.jhj.Jhj_MySql_Select_NetworkTask;
import com.androidlec.wagle.jhj.Jhj_Suggestion_DTO;
import com.androidlec.wagle.networkTask.JH_IntNetworkTask;
import com.androidlec.wagle.networkTask.JH_ObjectNetworkTask_Rank;
import com.bumptech.glide.Glide;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyPageFragment extends Fragment {

    private final static String TAG = "MyPAgeFragment";

    private static Button btn_sendMessage;

    // Layout (findViewById 를 사용하기위해) 선언
    private static ViewGroup rootView;
    private static String IP = "192.168.0.82";

    private static Jhj_MyPage_DTO data;

    //유저 랭킹
    private  ImageView profileIcon, Rank_Grade;

    // 랭킹 탑5 구하기.
    private static ArrayList<Rank> ranks;
    private static RankAdapter rankAdapter;


    public MyPageFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_my_page, container, false);

        // 카카오톡 메시지 초대 버튼
        btn_sendMessage = rootView.findViewById(R.id.myPage_btn_sendMessage);
        btn_sendMessage.setOnClickListener(onClickListener);


        // 참가안한 와글, 독후감 더보기 버튼
        rootView.findViewById(R.id.fragment_my_page_Wagle_Plus).setOnClickListener(Plus_MyPage_OnClickListener);
        rootView.findViewById(R.id.fragment_my_page_BookReport_Plus).setOnClickListener(Plus_MyPage_OnClickListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();


        btn_sendMessage = rootView.findViewById(R.id.myPage_btn_sendMessage);
        btn_sendMessage.setOnClickListener(onClickListener);

        profileIcon = rootView.findViewById(R.id.fragment_my_page_ProfileIcon);
        Rank_Grade = rootView.findViewById(R.id.fragment_my_page_iv_Rank_Grade);

        // 페이지 정보 가져오기
        String urlAddr = "http://" + IP + ":8080/wagle/MyPage_Select.jsp?moimSeqno=" + UserInfo.MOIMSEQNO + "&userSeqno=" + UserInfo.USEQNO;
        String MyPage_JsonString = MyPage_Select_All(urlAddr);
        data = MyPage_parser(MyPage_JsonString);

        // 유저 정보 세팅
        MyPageSetting();

        // 참가 안한 와글 세팅
        WagleSetting();

        // 안쓴 독후감 세팅
        SuggestionSetting();
    }

    // 내 정보 세팅하기
    protected void MyPageSetting() {
        // 프로필 사진 가져오기
        UserInfo.ULOGINTYPE = "wagle";
        ImageView profileIcon = rootView.findViewById(R.id.fragment_my_page_ProfileIcon);

        if(UserInfo.ULOGINTYPE.equals("wagle")){
            Glide.with(this)
                    .load("http://192.168.0.82:8080/wagle/userImgs/" + UserInfo.UIMAGENAME)
                    .placeholder(R.drawable.ic_outline_emptyimage)
                    .into(profileIcon);
        } else {
            Glide.with(this)
                    .load(UserInfo.UIMAGENAME)
                    .placeholder(R.drawable.ic_outline_emptyimage)
                    .into(profileIcon);
        }

        // 유저 이름 넣기
        TextView myName = rootView.findViewById(R.id.fragment_my_page_Text_Name);
        myName.setText(UserInfo.UNAME);

        // 참여한 총 와글 00개 / 00개 ( 00 % )
        TextView wagleNum = rootView.findViewById(R.id.fragment_my_page_Text_WagleNum);
        double result = Double.parseDouble(data.getWagleNum()) / Double.parseDouble(data.getTotalWagle());
        int percentage = (int) (result * 100);
        wagleNum.setText("참여한 총 와글 : " + data.getWagleNum() + " 개 / " + data.getTotalWagle() + "개 (" + percentage + "%)");

        // 참여한 총 독후감 00개 / 00개 ( 00 % )
        result = Double.parseDouble(data.getWagleBookReportNum()) / Double.parseDouble(data.getTotalBookReport());
        percentage = (int) (result * 100);
        TextView bookReportNum = rootView.findViewById(R.id.fragment_my_page_Text_BookReportNum);
        bookReportNum.setText("참여한 총 독후감 : " + data.getWagleBookReportNum() + " 개 / " + data.getTotalBookReport() + "개 (" + percentage + "%)");

        // 총점 계산
        TextView totalScore = rootView.findViewById(R.id.fragment_my_page_Text_TotalScore);
        int myscore = (Integer.parseInt(data.getWagleNum())*10) + (Integer.parseInt(data.getWagleBookReportNum())*5);
        totalScore.setText("총 " + myscore + " 점");

        // 랭크 이미지
        if(getRankGrade() <= 10){
            Rank_Grade.setImageResource(R.drawable.diamond);
        }else if(getRankGrade() <= 30){
            Rank_Grade.setImageResource(R.drawable.gold);
        }else if(getRankGrade() <= 60) {
            Rank_Grade.setImageResource(R.drawable.silver);
        }else {
            Rank_Grade.setImageResource(R.drawable.bronze);
        }

        // 랭킹 탑5
        getTop5();

        // 와글 버튼 텍스트, 이벤트 설정
        Button[] wagleBtn = new Button[4];
        Integer[] wagle_Frag_Btn_Id = {
                R.id.fragment_my_page_Wagle1, R.id.fragment_my_page_Wagle2, R.id.fragment_my_page_Wagle3, R.id.fragment_my_page_Wagle4
        };

        for (int i = 0 ; i < data.getWagle().size() ; i++) {
            wagleBtn[i] = rootView.findViewById(wagle_Frag_Btn_Id[i]);
            wagleBtn[i].setOnClickListener(wagle_MyPage_OnClickListener);
            wagleBtn[i].setText(data.getWagle().get(i).getWcName());
        }

        // 독후감 버튼 텍스트, 이벤트 설정
        Button[] suggestionBtn = new Button[4];
        Integer[] suggestion_Frag_Btn_Id = {
                R.id.fragment_my_page_BookReport1, R.id.fragment_my_page_BookReport2, R.id.fragment_my_page_BookReport3, R.id.fragment_my_page_BookReport4
        };

        for (int i = 0 ; i < data.getSuggestion().size() ; i++) {
            suggestionBtn[i] = rootView.findViewById(suggestion_Frag_Btn_Id[i]);
            suggestionBtn[i].setOnClickListener(suggestion_MyPage_OnClickListener);
            suggestionBtn[i].setText(data.getSuggestion().get(i).getsContent());
        }
    }

    private void getTop5(){
        ListView lv_ranktop5 = rootView.findViewById(R.id.fragment_my_page_ListView_Rank);
        String Moim_mSeqno = UserInfo.MOIMSEQNO;
        String JH_IP = "192.168.0.178";
        String urlAddr = "http://" + JH_IP + ":8080/wagle/getTop5.jsp?";
        urlAddr = urlAddr + "Moim_mSeqno=" + Moim_mSeqno;
        try {
            JH_ObjectNetworkTask_Rank objectNetworkTask_rank = new JH_ObjectNetworkTask_Rank(getActivity(), urlAddr);
            Object obj = objectNetworkTask_rank.execute().get();
            ranks = (ArrayList<Rank>) obj;
            rankAdapter = new RankAdapter(getContext(), R.layout.customlayout_ranktop5_listview, ranks); // making adapter.
            lv_ranktop5.setAdapter(rankAdapter);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int getRankGrade(){
        int rankgrade = 0;
//        String muSeqno = UserInfo.MOIMUSERSEQNO;
        String muSeqno = "33"; // 일단 절대값 넣어둠!!!
        String JH_IP = "192.168.0.178";
        String urlAddr = "http://" + JH_IP + ":8080/wagle/getRankGrade.jsp?";
        urlAddr = urlAddr + "muSeqno=" + muSeqno;
        try {
            JH_IntNetworkTask networkTask4 = new JH_IntNetworkTask(getActivity(), urlAddr);
            rankgrade = networkTask4.execute().get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return rankgrade;
    }





    // JsonData 가져오기
    protected String MyPage_Select_All(String urlAddr) {
        String data = null;

        try {
            Jhj_MySql_Select_NetworkTask networkTask = new Jhj_MySql_Select_NetworkTask(getActivity(), urlAddr);
            // execute() java 파일안의 메소드 한번에 동작시키기, 메소드를 사용하면 HttpURLConnection 이 제대로 작동하지않는다.
            Object obj = networkTask.execute().get();
            data = (String) obj;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    // Json 파싱
    protected Jhj_MyPage_DTO MyPage_parser(String jsonString) {
        Jhj_MyPage_DTO dtos = null;

        try {
            JSONObject jsonObject = new JSONObject(jsonString);

            String wagleNum = jsonObject.getString("wagleNum");
            String wagleBookReportNum = jsonObject.getString("wagleBookReportNum");
            String wagleScore = jsonObject.getString("wagleScore");
            String totalBookReport = jsonObject.getString("totalBookReport");
            String totalWagle = jsonObject.getString("totalWagle");

            JSONArray jsonArray = new JSONArray(jsonObject.getString("noBookReport"));
            ArrayList<Jhj_Suggestion_DTO> book_Dtos = new ArrayList<Jhj_Suggestion_DTO>();
            for (int i = 0 ; i < jsonArray.length() ; i++) {
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);

                String wcSeqno = jsonObject1.getString("sSeqno");
                String wcName = jsonObject1.getString("sContent");
                String wcDueDate = jsonObject1.getString("wcSeqno");

                book_Dtos.add(new Jhj_Suggestion_DTO(wcSeqno, wcName, wcDueDate));
            }

            JSONArray jsonArray2 = new JSONArray(jsonObject.getString("noWagle"));
            ArrayList<WagleList> wagle_Dtos = new ArrayList<WagleList>();
            for (int i = 0 ; i < jsonArray2.length() ; i++) {
                JSONObject jsonObject1 = (JSONObject) jsonArray2.get(i);

                String wcSeqno = jsonObject1.getString("wcSeqno");
                String Moim_wmSeqno = jsonObject1.getString("Moim_wmSeqno");
                String MoimUser_muSeqno = jsonObject1.getString("MoimUser_muSeqno");
                String WagleBook_wbSeqno = jsonObject1.getString("WagleBook_wbSeqno");
                String wcName = jsonObject1.getString("wcName");
                String wcType = jsonObject1.getString("wcType");
                String wcStartDate = jsonObject1.getString("wcStartDate");
                String wcEndDate = jsonObject1.getString("wcEndDate");
                String wcDueDate = jsonObject1.getString("wcDueDate");
                String wcLocate = jsonObject1.getString("wcLocate");
                String wcEntryFee = jsonObject1.getString("wcEntryFee");
                String wcWagleDetail = jsonObject1.getString("wcWagleDetail");
                String wcWagleAgreeRefund = jsonObject1.getString("wcWagleAgreeRefund");

                wagle_Dtos.add(new WagleList(wcSeqno, Moim_wmSeqno, MoimUser_muSeqno, WagleBook_wbSeqno, wcName, wcType,
                        wcStartDate, wcEndDate, wcDueDate, wcLocate, wcEntryFee, wcWagleDetail, wcWagleAgreeRefund));
            }

            dtos = new Jhj_MyPage_DTO(wagleNum, wagleBookReportNum, wagleScore, totalWagle, totalBookReport, wagle_Dtos, book_Dtos);


        } catch (Exception e) {
            e.printStackTrace();
        }

        return dtos;
    }

    // -------------------------------------------------------------------------------------
    // 와글 시작
    // -------------------------------------------------------------------------------------

    protected void WagleSetting() {
        // 와글 버튼 텍스트, 이벤트 설정
        Button[] wagleBtn = new Button[4];
        Integer[] wagle_Frag_Btn_Id = {
                R.id.fragment_my_page_Wagle1, R.id.fragment_my_page_Wagle2, R.id.fragment_my_page_Wagle3, R.id.fragment_my_page_Wagle4
        };

        for (int i = 0 ; i < data.getWagle().size() ; i++) {
            wagleBtn[i] = rootView.findViewById(wagle_Frag_Btn_Id[i]);
            wagleBtn[i].setOnClickListener(wagle_MyPage_OnClickListener);
            wagleBtn[i].setText(data.getWagle().get(i).getWcName());
        }
    }

    // 와글 신청 이벤트
    Button.OnClickListener wagle_MyPage_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fragment_my_page_Wagle1 :
                    chkWagleCheck(0);
                    break;
                case R.id.fragment_my_page_Wagle2 :
                    chkWagleCheck(1);
                    break;
                case R.id.fragment_my_page_Wagle3 :
                    chkWagleCheck(2);
                    break;
                case R.id.fragment_my_page_Wagle4 :
                    chkWagleCheck(3);
                    break;
            }
        }
    };

    protected void chkWagleCheck(int position) {
        Intent intent;
        switch (chkJoinIn(data.getWagle().get(position).getWcSeqno())){
            case 1: // 와글 신청이 되었을 때.
                intent = new Intent(getActivity(), MyWagleActivity.class);
                UserInfo.WAGLESEQNO = data.getWagle().get(position).getWcSeqno();
                UserInfo.WAGLENAME = data.getWagle().get(position).getWcName();
                UserInfo.WAGLETYPE = data.getWagle().get(position).getWcType();
                startActivity(intent);
                break;
            case 2: // 와글 신청이 안되었을 때.
                intent = new Intent(getActivity(), ViewDetailWagleActivity.class);
                intent.putExtra("data", data.getWagle().get(position));
                intent.putExtra("wcSeqno", data.getWagle().get(position).getWcSeqno());
                startActivity(intent);
                break;
            case 0: // 데이터베이스 연결이 안되었을 때.
                Toast.makeText(getActivity(), "인터넷 환경을 확인해주세요.", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    protected int chkJoinIn(String wcSeqno){
        int chk = 3;
        String uSeqno = String.valueOf(UserInfo.USEQNO);
        //uSeqno = "1"; // 임시 절대값. 위에꺼 쓰면 됨.
        String urlAddr = "http://192.168.0.178:8080/wagle/joininChk.jsp?";
        urlAddr = urlAddr + "wcSeqno=" + wcSeqno + "&User_uSeqno=" + uSeqno;
        try {
            JH_IntNetworkTask networkTask = new JH_IntNetworkTask(getContext(), urlAddr);
            chk = networkTask.execute().get();
        }catch (Exception e){
            e.printStackTrace();
        }
        return chk;
    }

    // -------------------------------------------------------------------------------------
    // 와글 끝
    // -------------------------------------------------------------------------------------


    // -------------------------------------------------------------------------------------
    // 발제문 시작
    // -------------------------------------------------------------------------------------

    protected void SuggestionSetting() {
        // 독후감 버튼 텍스트, 이벤트 설정
        Button[] suggestionBtn = new Button[4];
        Integer[] suggestion_Frag_Btn_Id = {
                R.id.fragment_my_page_BookReport1, R.id.fragment_my_page_BookReport2, R.id.fragment_my_page_BookReport3, R.id.fragment_my_page_BookReport4
        };

        for (int i = 0 ; i < data.getSuggestion().size() ; i++) {
            suggestionBtn[i] = rootView.findViewById(suggestion_Frag_Btn_Id[i]);
            suggestionBtn[i].setOnClickListener(suggestion_MyPage_OnClickListener);
            suggestionBtn[i].setText(data.getSuggestion().get(i).getsContent());
        }
    }

    Button.OnClickListener suggestion_MyPage_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.fragment_my_page_BookReport1 :
                    BookReportMove(0);
                    break;
                case R.id.fragment_my_page_BookReport2 :
                    BookReportMove(1);
                    break;
                case R.id.fragment_my_page_BookReport3 :
                    BookReportMove(2);
                    break;
                case R.id.fragment_my_page_BookReport4 :
                    BookReportMove(3);
                    break;
            }
        }
    };

    protected void BookReportMove(int position) {
        UserInfo.WAGLESEQNO = data.getSuggestion().get(position).getWcSeqno();

        Intent intent = new Intent(getActivity(), AddDHGActivity.class);
        startActivity(intent);
    }

    // -------------------------------------------------------------------------------------
    // 발제문 끝
    // -------------------------------------------------------------------------------------

    // 카카오톡 이벤트
    View.OnClickListener onClickListener = v -> {
        kakaoLink();
    };

    private void kakaoLink() {
        // 템플릿 ID
        String templateId = "32851";

        // 템플릿에 입력된 Argument에 채워질 값
        Map<String, String> templateArgs = new HashMap<>();
        templateArgs.put("USER_NAME", UserInfo.UNAME);
        templateArgs.put("MOIM_NAME", UserInfo.MOIM_NAME);

        // 링크 콜백에서 함께 수신할 커스텀 파라미터를 설정합니다.
        Map<String, String> serverCallbackArgs = new HashMap<>();
        serverCallbackArgs.put("user_id", "${current_user_id}");
        serverCallbackArgs.put("product_id", "${shared_product_id}");

        // 커스텀 템플릿으로 카카오링크 보내기
        KakaoLinkService.getInstance()
                .sendCustom(getActivity(), templateId, templateArgs, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
                    @Override
                    public void onFailure(ErrorResult errorResult) {
                    }

                    @Override
                    public void onSuccess(KakaoLinkResponse result) {

                    }
                });
    }
    // 카카오톡 이벤트 끝

    // 참가안한 와글, 독후감 더보기 버튼
    Button.OnClickListener Plus_MyPage_OnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = null;
            switch (v.getId()) {
                case R.id.fragment_my_page_Wagle_Plus :
                    intent = new Intent(getActivity(), Jhj_HomeAndMyPage_Plus_List.class);
                    intent.putExtra("Type", "Wagle");
                    break;
                case R.id.fragment_my_page_BookReport_Plus :
                    intent = new Intent(getActivity(), Jhj_HomeAndMyPage_Plus_List.class);
                    intent.putExtra("Type","NoBookReport");
                    break;
            }
            startActivity(intent);
        }
    };

}