package io.cloudwalk.pos.demo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import io.cloudwalk.pos.demo.R;
import io.cloudwalk.pos.loglibrary.Log;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {
    private static final String TAG = MainAdapter.class.getSimpleName();

    private static final Semaphore sSemaphore = new Semaphore(1, true);

    private ArrayList<String> sTraceList = new ArrayList<>(0);

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mTrace;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            Log.d(TAG, "ViewHolder");

            mTrace = itemView.findViewById(R.id.tv_trace);
        }

        public void bind(@NonNull String trace) {
            android.util.Log.d(TAG, "ViewHolder::bind");

            mTrace.setText(trace);
        }
    }

    @NonNull
    @Override
    public MainAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder");

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.content_main_item, parent, false);

        return new MainAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");

        sSemaphore.acquireUninterruptibly();

        String trace = sTraceList.get(position);

        sSemaphore.release();

        holder.bind(trace);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount");

        int itemCount = 0;

        sSemaphore.acquireUninterruptibly();

        itemCount = (sTraceList != null) ? sTraceList.size() : 0;

        sSemaphore.release();

        return itemCount;
    }

    public void clear(int index, int count) { // TODO: turn it into 'truncate'?
        Log.d(TAG, "pop");

        sSemaphore.acquireUninterruptibly();

        sTraceList.subList(index, count).clear();

        notifyItemRangeRemoved(index, count);

        sSemaphore.release();
    }

    public void push(String trace) {
        Log.d(TAG, "push");

        sSemaphore.acquireUninterruptibly();

        if (!trace.isEmpty() && !trace.equals("\r")) {
            sTraceList.add(trace); // TODO: drop if too much was pushed?

            notifyItemInserted(sTraceList.size());
        }

        sSemaphore.release();
    }
}
