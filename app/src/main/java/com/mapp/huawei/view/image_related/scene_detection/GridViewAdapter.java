package com.mapp.huawei.view.image_related.scene_detection;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.mapp.huawei.R;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter extends BaseAdapter {

    private  List<String> mItems = new ArrayList<String>();
    private  ArrayList<String> mResult = new ArrayList<String>();
    private final LayoutInflater mInflater;
    private Context context;

    public GridViewAdapter(Context context , List mItems , ArrayList mResult) {
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.mItems = mItems;
        this.mResult = mResult;
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Object getItem(int i) {
        return mItems.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0; //mItems.get(i).drawableId.getAbsolutePath();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View v = view;
        ImageView picture;
        TextView name;

        if (v == null) {
            v = mInflater.inflate(R.layout.grid_item, viewGroup, false);
            v.setTag(R.id.picture, v.findViewById(R.id.picture));
            v.setTag(R.id.text, v.findViewById(R.id.text));
        }

        picture = (ImageView) v.getTag(R.id.picture);
        name = (TextView) v.getTag(R.id.text);
        name.setText(mResult.get(i));
        Glide
                .with(context)
                .load(mItems.get(i))
                        .into(picture);
        //picture.setImageBitmap(BitmapFactory.decodeFile(mItems.get(i))) ;



        return v;
    }

}
