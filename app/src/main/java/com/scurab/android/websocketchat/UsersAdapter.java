package com.scurab.android.websocketchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by JBruchanov on 14/03/2017.
 */

public class UsersAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnUserClickListener {
        void onClick(String user, int position);
    }

    private List<String> mUsers = new ArrayList<>();

    final private View.OnClickListener mInnerClickListener = (view) -> {
        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) view.getLayoutParams();
        final int viewAdapterPosition = layoutParams.getViewAdapterPosition();
        dispatchUserClicked(viewAdapterPosition);
    };

    private OnUserClickListener mOuterClickListener;

    private void dispatchUserClicked(int viewAdapterPosition) {
        if (mOuterClickListener != null) {
            mOuterClickListener.onClick(mUsers.get(viewAdapterPosition), viewAdapterPosition);
        }
    }

    private RecyclerView mRecyclerView;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        view.setOnClickListener(mInnerClickListener);
        return new RecyclerView.ViewHolder(view) {
        };
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = null;
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        TextView tv = (TextView) holder.itemView;
        final String name = mUsers.get(position);
        tv.setText(name);
    }

    @Override
    public int getItemCount() {
        return mUsers != null ? mUsers.size() : 0;
    }

    public void setUsers(JSONArray users) {
        mUsers.clear();
        try {
            for (int i = 0, n = users.length(); i < n; i++) {
                mUsers.add(users.getString(i));
            }
            Collections.sort(mUsers, String::compareToIgnoreCase);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new Error(e);
        }
        notifyDataSetChanged();
    }

    public void setOuterClickListener(OnUserClickListener outerClickListener) {
        mOuterClickListener = outerClickListener;
    }
}
