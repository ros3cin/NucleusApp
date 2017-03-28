package io.github.inesescin.nucleus.asyncTasks;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.github.inesescin.nucleus.callback.EcopointsCallback;
import io.github.inesescin.nucleus.connection.FiwareConnection;
import io.github.inesescin.nucleus.models.Nucleus;
import io.github.inesescin.nucleus.util.Constants;

/**
 * Created by danielmaida on 03/03/16.
 */
public class MapMarkingAsyncTask extends AsyncTask<Void, Void,  Map<String, Nucleus>> {

    private EcopointsCallback callback;
    private Activity activity;
    private WebView webView;

    public MapMarkingAsyncTask(EcopointsCallback callback,Activity activity,WebView webView) {
        this.callback = callback;
        this.activity = activity;
        this.webView = webView;
        if(!isJSCallbackInstalled) {
            this.webView.addJavascriptInterface(this, "Android");
            isJSCallbackInstalled=true;
        }
    }
    @JavascriptInterface
    public static void receiveJSData(String[] js_Coordinates, double[] js_Value, String[] js_Status,String[] js_Id){
        MapMarkingAsyncTask.js_Coordinates=js_Coordinates;
        MapMarkingAsyncTask.js_Value=js_Value;
        MapMarkingAsyncTask.js_Status=js_Status;
        MapMarkingAsyncTask.js_Id = js_Id;
        MapMarkingAsyncTask.waitForJs=false;
    }

    public class JavaScriptInterfacey {
        private MapMarkingAsyncTask task;
        JavaScriptInterfacey(MapMarkingAsyncTask task) {
            this.task=task;
        }

    }
    @Override
    protected Map<String, Nucleus>  doInBackground(Void... params) {

        FiwareConnection fiwareConnection = new FiwareConnection();
        Map<String, Nucleus> ecopoints = new HashMap<>();
        try{
            String stringResponse = fiwareConnection.getEntityByType(Constants.FIWARE_ADDRESS, "Nucleus");
            ecopoints = parseJsonToNucleusArray(ecopoints, stringResponse);//java
            //ecopoints = ndkParseJsonToNucleusArray(ecopoints,stringResponse);//ndk

            //ecopoints = jsParseJsonToNucleusArray(ecopoints,stringResponse);//js
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return ecopoints;
    }

    private  Map<String, Nucleus>  parseJsonToNucleusArray( Map<String, Nucleus> ecopoints, String stringResponse) throws JSONException
    {
        JSONObject response = new JSONObject(stringResponse);
        JSONArray contextResponse = response.getJSONArray("contextResponses");
        for (int i = 0; i < contextResponse.length(); i++) {

            Nucleus nucleus = new Nucleus();
            JSONObject currentEntityResponse = contextResponse.getJSONObject(i);
            JSONObject contextElement = currentEntityResponse.getJSONObject("contextElement");
            nucleus.setId(contextElement.getString("id"));
            JSONArray attributes = contextElement.getJSONArray("attributes");
            nucleus.setCoordinates(attributes.getJSONObject(0).getString("value"));
            nucleus.setValue(Double.parseDouble(attributes.getJSONObject(1).getString("value")));
            nucleus.setStatus(attributes.getJSONObject(2).getString("value")); //status
            ecopoints.put(nucleus.getId(), nucleus);
        }
        return ecopoints;
    }
    static {
        System.loadLibrary("native-lib");
    }
    public native Map<String, Nucleus> ndkParseJsonToNucleusArray(Map<String, Nucleus> ecopoints, String stringResponse);

    public static boolean isJSCallbackInstalled=false;
    public static String[] js_Coordinates;
    public static double[] js_Value;
    public static String[] js_Status;
    public static String[] js_Id;
    public static boolean waitForJs;
    private  Map<String, Nucleus>  jsParseJsonToNucleusArray( Map<String, Nucleus> ecopoints, final String stringResponse) throws JSONException
    {
        waitForJs=true;

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*webView.setWebViewClient(new WebViewClient() {
                    public void onPageFinished(WebView view, String url) {
                        webView.loadUrl("javascript:parseJson('"+stringResponse+"');");
                    }
                });*/
                webView.loadUrl("javascript:parseJson('"+stringResponse+"');");
            }
        });


        while(waitForJs){
            try {
                Thread.sleep(500,0);
            } catch (InterruptedException e) {
            }
        }

        for(int i = 0; i < js_Coordinates.length;i++){
            Nucleus nucleus = new Nucleus();
            nucleus.setId(js_Id[i]);
            nucleus.setCoordinates(js_Coordinates[i]);
            nucleus.setValue(js_Value[i]);
            nucleus.setStatus(js_Status[i]);
            ecopoints.put(nucleus.getId(), nucleus);
        }



        return ecopoints;
    }



    @Override
    protected void onPostExecute(Map<String, Nucleus> ecopoints) {
        super.onPostExecute(ecopoints);
        System.out.println("Post-execute executado!");
        if(callback!=null) {
            callback.onEcopointsReceived(ecopoints);
        }
    }
}