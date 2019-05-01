package com.cs3370.android.lrs_passengerapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class History {
    private static History instance = null;

    private List<DisplayListItem> mDriverHistory;

    public static History getInstance() {
        if (instance == null) {
            instance = new History();
        }
        return instance;
    }
    private History() {
        mDriverHistory = new ArrayList<>();
    }

    public List<DisplayListItem> getDriverHistory() {
        return mDriverHistory;
    }

    public void updateList(Context context) {
        mDriverHistory = new ArrayList<>();
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String url = context.getResources().getString(R.string.server_addr) + "/api/client_history?id=" + Client.getInstance().get("id");
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++){
                        JSONObject item = response.getJSONObject(i);

                        JSONObject client = item.getJSONObject("client");

                        String clientName = client.getString("name");

                        String id = item.getString("id");
                        //String client_id = item.getString("client_id");
                        //String driver_id = item.getString("driver_id");
                        String status = item.getString("status");
                        String dropOff = item.getString("destination_address");
                        String pickUp = item.getString("pick_up_address");
                        String estimatedLength = item.getString("estimated_length");
                        String pickUpTime = item.getString("time");
                        String pickUpDate = item.getString("date");
                        //String created_at = item.getString("created_at");
                        //String updated_at = item.getString("updated_at");
                        DisplayListItem listitem = new DisplayListItem(clientName, pickUp, dropOff, pickUpTime, pickUpDate, estimatedLength, status);
                        mDriverHistory.add(listitem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });
        mRequestQueue.add(request);
    }

    //public void addRide(DisplayListItem newRide) {
    //   mDriverRidesList.add(newRide);
    //}
}
