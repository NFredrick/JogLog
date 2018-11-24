package com.nfredrick.android.joglog.log;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.nfredrick.android.joglog.R;
import com.nfredrick.android.joglog.db.Jog;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class LogRecyclerViewAdapter extends RecyclerView.Adapter<LogRecyclerViewAdapter.LogViewHolder> {

    private List<Jog> mJogs;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private static final String TAG = "com.nfredrick.android.joglog.log.LogRecyclerViewAdapter";

    @Override
    public LogViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");
        View v = mInflater.inflate(R.layout.recycler_view_row, parent, false);
        return new LogViewHolder(v);
    }

    @Override
    public void onBindViewHolder(LogViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        Jog jog = mJogs.get(position);
        holder.mDistanceTextView.setText(Double.toString(jog.distance));
        holder.mTimeTextView.setText(Long.toString(jog.time));
        holder.mDateTextView.setText(jog.date.toString());
    }

    @Override
    public int getItemCount() {
        return mJogs.size();
    }

    public LogRecyclerViewAdapter(Context context, List<Jog> data) {
        mInflater = LayoutInflater.from(context);
        mJogs = data;
    }

    public class LogViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        public TextView mDistanceTextView;
        public TextView mTimeTextView;
        public TextView mDateTextView;

        LogViewHolder(View view) {
            super(view);
            mDistanceTextView = view.findViewById(R.id.jog_distance);
            mTimeTextView = view.findViewById(R.id.jog_time);
            mDateTextView = view.findViewById(R.id.jog_date);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
