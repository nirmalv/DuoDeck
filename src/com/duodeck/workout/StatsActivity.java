package com.duodeck.workout;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.example.duodeck.R;

public class StatsActivity extends Activity {

    private ListView listView1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        
        Stats stats_data[] = new Stats[]
        {
            new Stats(R.drawable.victory_icon_r1_c1, "Recent Workout", "some value"),
            new Stats(R.drawable.victory_icon_r1_c3, "Best Time", "some value"),
            new Stats(R.drawable.victory_icon_r1_c5, "Podium (rename)", "some value"),
            new Stats(R.drawable.victory_icon_r1_c7, "ThumbsUp (rename)", "some value"),
            new Stats(R.drawable.victory_icon_r3_c1, "Number1 (rename)", "some value")
        };
        
        StatsAdapter adapter = new StatsAdapter(this, 
                R.layout.activity_stats_row, stats_data);
        
        
        listView1 = (ListView)findViewById(R.id.statsActivityListView);
         
        View header = (View)getLayoutInflater().inflate(R.layout.activity_stats_header, null);
        listView1.addHeaderView(header);
        
        listView1.setAdapter(adapter);
    }
}