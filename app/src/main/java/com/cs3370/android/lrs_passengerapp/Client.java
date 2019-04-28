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
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class Client {
    private static Client instance = null;

    private Dictionary mClientInfo;

    private List<DisplayListItem> mClientRidesList;

    public static Client getInstance() {
        if (instance == null) {
            instance = new Client();
        }
        return instance;
    }
    private Client() {
        mClientInfo = new Hashtable();
        mClientRidesList = new ArrayList<>();
    }

    public String get(String key) {
        return mClientInfo.get(key).toString();
    }

    public void set(String key, Boolean value) {
        mClientInfo.put(key, value);
    }

    public void set(String key, String value) {
        mClientInfo.put(key, value);
    }

    public List<DisplayListItem> getClientRides() {
        return mClientRidesList;
    }

    public void updateList(Context context) {
        mClientRidesList = new ArrayList<>();
        RequestQueue mRequestQueue = Volley.newRequestQueue(context);
        String url = context.getResources().getString(R.string.server_addr) + "/api/client-requests?id=" + Client.getInstance().get("id");
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

            @Override
            public void onResponse(JSONArray response) {
                try {
                    for (int i = 0; i < response.length(); i++){
                        JSONObject item = response.getJSONObject(i);

                        String status = item.getString("status");
                        if (status.equals("1")) {
                            //get driver info
                            //JSONObject driver = item.getJSONObject("driver");
                            //BUG: if driver is null and we try to create a driver JSONObject, this loop breaks and we fail to retrieve the ride
                                //we should only have driver == null when status is "0", but that is currently not the case
                        }
                        //String id = item.getString("id");
                        //String client_id = item.getString("client_id");
                        //String driver_id = item.getString("driver_id");
                        String dropOff = item.getString("destination_address");
                        String pickUp = item.getString("pick_up_address");
                        String estimatedLength = item.getString("estimated_length");
                        String pickUpTime = item.getString("time");
                        String pickUpDate = item.getString("date");
                        //String created_at = item.getString("created_at");
                        //String updated_at = item.getString("updated_at");
                        DisplayListItem listitem = new DisplayListItem(Client.getInstance().get("name"), pickUp, dropOff, pickUpTime, pickUpDate, estimatedLength, status);
                        mClientRidesList.add(listitem);
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
    //   mClientRidesList.add(newRide);
    //}
}
