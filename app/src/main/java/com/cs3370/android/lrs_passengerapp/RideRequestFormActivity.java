package com.cs3370.android.lrs_passengerapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RideRequestFormActivity extends AppCompatActivity {

    private String mClient;
    private EditText mPickUp;
    private EditText mDropOff;
    private Button mNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_request_form);

        mClient = Client.getInstance().get("name");
        mPickUp = (EditText) findViewById(R.id.pickUpLocation);
        mDropOff = (EditText) findViewById(R.id.dropOffLocation);
        mNext = (Button) findViewById(R.id.submitRequest);

        mNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RideRequestFormActivity.this, MapsActivity.class);
                intent.putExtra("pickUp", mPickUp.getText().toString());
                intent.putExtra("dropOff", mDropOff.getText().toString());
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.drop_down_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clientRides) {
            Intent intent = new Intent(RideRequestFormActivity.this, RecyclerViewActivity.class);
            startActivity(intent);
        }else if (item.getItemId() == R.id.newRideRequest) {

        }
        return true;
    }
}
