package com.duodeck.workout;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.duodeck.workout.R;

public class StatsAdapter extends ArrayAdapter<Stats>{

    Context context; 
    int layoutResourceId;    
    Stats data[] = null;
    
    public StatsAdapter(Context context, int layoutResourceId, Stats[] data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        StatsHolder holder = null;
        
        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            
            holder = new StatsHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imgIcon);
            holder.txtValue = (TextView)row.findViewById(R.id.txtValue);
            holder.txtTitle = (TextView)row.findViewById(R.id.txtTitle);
            
            row.setTag(holder);
        }
        else
        {
            holder = (StatsHolder)row.getTag();
        }
        
        Stats stats = data[position];
        holder.txtValue.setText(stats.value);
        holder.txtTitle.setText(stats.title);
        holder.imgIcon.setImageResource(stats.icon);
        
        return row;
    }
    
    static class StatsHolder
    {
        ImageView imgIcon;
        TextView txtTitle;
        TextView txtValue;
    }
}