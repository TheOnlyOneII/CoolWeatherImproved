package com.coolweather.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coolweather.android.gson.Weather;

import java.util.List;

/**
 * auther : leo
 * create date : 2020/6/17
 * describe :
 */
public class RvCityAdapter extends RecyclerView.Adapter<RvCityAdapter.ViewHolder> {
    private Context mContext;
    private List<String> idList;
    private List<String> dataList;
    private final LayoutInflater mInflater;

    public RvCityAdapter(Context context, List<String> dataList,List<String> ids) {
        mContext = context;
        this.dataList = dataList;
        idList = ids;
        mInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(mInflater.inflate(R.layout.item_citys, parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvCity.setText(dataList.get(position));
        holder.tvDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnClickCallBack != null)
                {
                    mOnClickCallBack.onItemDelete(idList.get(position));
                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCity;
        private TextView tvDelete;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tv_city);
            tvDelete = itemView.findViewById(R.id.tv_delete);

        }
    }

    public void setOnClickCallBack(OnClickCallBack onClickCallBack) {
        mOnClickCallBack = onClickCallBack;
    }

    private OnClickCallBack mOnClickCallBack;
    public interface OnClickCallBack{
        void onItemDelete(String id);
    }
}
