package com.android.system.service;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ViewHolder> {

    public interface OnAppClickListener {
        void onAppClick(PackageInfo pkg);
    }

    private Context context;
    private List<PackageInfo> appList;
    private OnAppClickListener listener;

    public AppListAdapter(Context context, List<PackageInfo> appList, OnAppClickListener listener) {
        this.context = context;
        this.appList = appList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PackageInfo pkg = appList.get(position);
        holder.appName.setText(pkg.applicationInfo.loadLabel(context.getPackageManager()));
        holder.appIcon.setImageDrawable(pkg.applicationInfo.loadIcon(context.getPackageManager()));
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAppClick(pkg);
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView appIcon;
        TextView appName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            appIcon = itemView.findViewById(R.id.img_icon);
            appName = itemView.findViewById(R.id.txt_name);
        }
    }
}
