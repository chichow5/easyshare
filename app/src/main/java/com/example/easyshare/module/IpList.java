package com.example.easyshare.module;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;

import com.example.easyshare.network.Detector;
import com.example.easyshare.network.Server;

import java.util.ArrayList;

public class IpList {
    private final Adapter adapter;
    private final DiagEditor diagEditor;

    protected static class Adapter extends BaseAdapter implements Server.Callback {
        protected final ArrayList<Server> items;
        private final Context context;
        public Adapter(Detector d, Context context1){
            this.items = d.getServers();
            this.context = context1;
        }

        @Override
        public int getCount(){ return items.size(); }

        @Override
        public Object getItem(int position) { return items.get(position).toString(); }

        @Override
        public long getItemId(int position){
            return position;
        }

        @Override
        public View getView(int psn, View cView, ViewGroup parent){
            if (cView == null) cView = new TextView(context);
            ((TextView) cView).setText(items.get(psn).toString());
            ((TextView) cView).setTextSize(25);
            return cView;
        }

        @Override
        public void callback(Server s) {
            if (s != null) items.add(s);
            this.notifyDataSetChanged();
        }
    }

    public IpList(Detector d, Context context, ListView listview) {
        adapter = new Adapter(d, context);
        listview.setAdapter(adapter);
        diagEditor = new DiagEditor(context);
        listview.setOnItemClickListener(((parent, view, position, id) -> {
            diagEditor.Edit(adapter.items.get(position), adapter);
        }));
        listview.setOnItemLongClickListener((parent, view, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("删除确认");
            builder.setPositiveButton("确认", (dialog, which) -> {
                adapter.items.remove(position);
                adapter.notifyDataSetChanged();
            });
            builder.setNegativeButton("取消", (dialog, which) -> {});// do nothing
            builder.show();
            return true;
        });
    }

    public Adapter getAdapter() {
        return adapter;
    }
}
