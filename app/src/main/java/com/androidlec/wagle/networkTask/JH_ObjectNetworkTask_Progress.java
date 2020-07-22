package com.androidlec.wagle.networkTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.androidlec.wagle.CS.Model.User;
import com.androidlec.wagle.JH.Progress;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class JH_ObjectNetworkTask_Progress extends AsyncTask<Integer, String, Object> { // 어레이리스트 불러와야 하니깐 오브젝트로 쓴다. 그래야 오브젝트로 바뀜.

    // Field
    //final static String TAG = "Log check : ";
    Context context;
    String mAddr;
    ProgressDialog progressDialog;
    ArrayList<Progress> progressdata;

    // Constructor
    public JH_ObjectNetworkTask_Progress(Context context, String mAddr) {
        this.context = context;
        this.mAddr = mAddr;
        this.progressdata = new ArrayList<Progress>();
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(progressDialog.STYLE_SPINNER);
        progressDialog.setTitle("대화상자");
        progressDialog.setMessage("로딩중입니다.");
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        progressDialog.dismiss();
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    // 중요한 건, doInBackground 죠.
    @Override
    protected Object doInBackground(Integer... integers) {
        //Log.v(TAG, "doInBackground()");

        StringBuffer stringBuffer = new StringBuffer();
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;

         try{
             URL url = new URL(mAddr);
             HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
             httpURLConnection.setConnectTimeout(10000); // 10 seconds.

             //Log.v(TAG, "Accept : " + httpURLConnection.getResponseCode());

             if(httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK){

                 inputStream = httpURLConnection.getInputStream(); // 이때 데이터 가져오는 겁니다.
                 inputStreamReader = new InputStreamReader(inputStream); // 가져온거를 리더에 넣어야겠죠.
                 bufferedReader = new BufferedReader(inputStreamReader); // 버퍼드리더에 넣어야합니다.

                 while (true){
                     String strline = bufferedReader.readLine();
                     if(strline == null) break; // 브레이크 만나면 와일문 빠져나간다.
                     stringBuffer.append(strline + "\n");
                 } // 와일문 끝나면 다 가져왔다~.

                 //Log.v(TAG, stringBuffer.toString());
                 // 파싱.
                 parser(stringBuffer.toString()); // 아직 안만들었어요~~~ But, 파씽 하겠다~.

             }
         }catch (Exception e){
             e.printStackTrace();
         }finally {
             try {
                 if(bufferedReader != null) bufferedReader.close();
                 if(inputStreamReader != null) inputStreamReader.close();
                 if(inputStream != null) inputStream.close();
             }catch (Exception e){
                 //Log.v(TAG, "Network Error");
                 e.printStackTrace();
             }
         }

        return progressdata; // 다 해놓고 리턴 안하면 꽝이죠~. 이제 parser에 대해서 정리만 해주면 됨.
    }


    private void parser(String s){ // 스트링 하나만 가져오죠.
        //Log.v(TAG, "parse()");

        try {
            JSONObject jsonObject = new JSONObject(s);
            JSONArray jsonArray = new JSONArray(jsonObject.getString("WagleProgress")); // students_info 에 있는걸 가져와라.
            progressdata.clear(); // 깨끗하게.

            for(int i=0; i<jsonArray.length(); i++){
                JSONObject jsonObject1 = (JSONObject) jsonArray.get(i);
                String uImageName = jsonObject1.getString("uImageName");
                int wpReadPage = jsonObject1.getInt("wpReadPage");
                int uSeqno = jsonObject1.getInt("uSeqno");
                String uLoginType = jsonObject1.getString("uLoginType");

//                Log.v(TAG, uImageName);
//                Log.v(TAG, String.valueOf(wpReadPage));
//                Log.v(TAG, String.valueOf(uSeqno));
//                Log.v(TAG, uLoginType);

                Progress data = new Progress(uImageName, wpReadPage, uSeqno, uLoginType);
                progressdata.add(data);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}//----
