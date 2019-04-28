package com.cs3370.android.lrs_passengerapp;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.List;

//all lists are displayed in this activity

public class RecyclerViewActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    private List<DisplayListItem> mRidesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Client.getInstance().updateList(this);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRidesList = Client.getInstance().getClientRides();
                mAdapter = new MyAdapter(mRidesList);
                mRecyclerView.setAdapter(mAdapter);
                RecyclerSectionItemDecoration sectionItemDecoration =
                        new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),
                                true, getSectionCallback(mRidesList));
                mRecyclerView.addItemDecoration(sectionItemDecoration);
            }
        }, 1500);
    }

    private void RecyclerSectionItemDecorationHelper() {
        mRecyclerView.removeItemDecorationAt(0);
        RecyclerSectionItemDecoration sectionItemDecoration =
                new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.recycler_section_header_height),
                        true, getSectionCallback(mRidesList));
        mRecyclerView.addItemDecoration(sectionItemDecoration);
    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback(final List<DisplayListItem> thelist) {
        return new RecyclerSectionItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                String dateOne = thelist.get(Math.min(position, thelist.size() - 1)).getPickUpDate();
                String dateTwo = thelist.get(Math.min(position + 1, thelist.size() - 1)).getPickUpDate();
                return (position == 0 || (dateOne != dateTwo) || position == thelist.size() - 1);
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return thelist.get(Math.min(position, thelist.size() - 1)).getPickUpDate().subSequence(0, 5);
            }
        };
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
            Client.getInstance().updateList(this);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mRidesList = Client.getInstance().getClientRides();
                    mAdapter = new MyAdapter(mRidesList);
                    mRecyclerView.setAdapter(mAdapter);
                    RecyclerSectionItemDecorationHelper();
                }
            }, 1500);
        }else if (item.getItemId() == R.id.newRideRequest) {
            Intent intent = new Intent(RecyclerViewActivity.this, RideRequestFormActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
