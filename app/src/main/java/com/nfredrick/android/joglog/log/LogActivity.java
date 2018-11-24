package com.nfredrick.android.joglog.log;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.nfredrick.android.joglog.R;
import com.nfredrick.android.joglog.db.Jog;
import com.nfredrick.android.joglog.db.JogData;
import com.nfredrick.android.joglog.jog.Repository;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class LogActivity extends AppCompatActivity implements LogRecyclerViewAdapter.ItemClickListener{

    private LogRecyclerViewAdapter mAdapter;
    private Repository sRepository;
    private List<Jog> mJogs;

    private static final String TAG = "com.nfredrick.android.joglog.log.LogActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
        setContentView(R.layout.activity_log);

        sRepository = Repository.getInstance();
        mJogs = sRepository.getJogs();

        Log.d(TAG, "number of jogs = " + Integer.toString(mJogs.size()));

        RecyclerView recyclerView = findViewById(R.id.log_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new LogRecyclerViewAdapter(this, mJogs);
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Log.d(TAG, "onItemClick()");
        int jogId = mJogs.get(position).jogId;
        Log.d(TAG, "Jog ID = " + jogId);
        ArrayList<JogData> data = (ArrayList<JogData>) sRepository.getSingleJogData(jogId);
        Log.d(TAG, "data.size() = " + Integer.toString(data.size()));
        Intent intent = JogMapActivity.newIntent(LogActivity.this, data);
        startActivity(intent);
    }

}
