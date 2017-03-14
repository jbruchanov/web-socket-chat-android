package com.scurab.android.websocketchat;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jakewharton.rxbinding2.view.RxView;
import com.trello.rxlifecycle2.components.support.RxFragment;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JBruchanov on 08/03/2017.
 */

public class MessagesFragment extends RxFragment {

    RecyclerView mRecyclerView;
    MessagesAdapter mAdapter;
    private List<JSONObject> mData = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRecyclerView = (RecyclerView) inflater.inflate(R.layout.view_recyclerview, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Context context = view.getContext();
        final LinearLayoutManager layout = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layout);
        if (mAdapter == null) {
            mAdapter = new MessagesAdapter(mData);
        }
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(context)
                        .colorResId(R.color.colorPrimary)
                        .build());

        RxView.layoutChangeEvents(mRecyclerView)
                .compose(bindToLifecycle())
                .subscribe((v) -> {
                    if (Math.abs(v.oldBottom() - v.bottom()) > 100) {
                        mRecyclerView.post(mScrollToLast);
                    }
                });

        final Bundle args = getArguments();
        if (args != null && args.containsKey("msg")) {
            JSONObject msg = null;
            try {
                msg = new JSONObject(args.getString("msg"));
                args.remove("msg");
                addMessage(msg);
            } catch (JSONException e) {
                throw new Error(e);
            }
        }
    }

    public void addMessages(@NonNull JSONArray array) {
        mAdapter.addAll(array);
        mScrollToLast.run();
    }

    public void addMessage(@NonNull JSONObject msg) {
        mAdapter.add(msg);
        mSmoothScrollToLast.run();
    }

    List<JSONObject> getData() {
        return mData;
    }

    private Runnable mSmoothScrollToLast = () -> {
        if (mRecyclerView != null && mAdapter != null) {
            mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
        }
    };

    private Runnable mScrollToLast = () -> {
        if (mRecyclerView != null && mAdapter != null) {
            mRecyclerView.scrollToPosition(Math.max(0, mAdapter.getItemCount() - 1));
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView = null;
    }
}
