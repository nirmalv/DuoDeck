package com.duodeck.workout;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.duodeck.workout.R;


public class StatsActivity extends Activity {

    private ListView listViewStats;
    
    private DuoDeckApplication duoDeckApp;
    private PersistentStorage ps;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    	duoDeckApp = (DuoDeckApplication) getApplication();
    	ps = duoDeckApp.getPersistentStorage();
        
        setContentView(R.layout.activity_stats);
        
        Stats stats_data[] = new Stats[]
        {
            new Stats(R.drawable.victory_icon_r1_c1, "Recent Workout", ps.getWorkoutDataFromSharedPrefs(StatsActivity.this, ps.KEY_PREVIOUS_DECK)),
            new Stats(R.drawable.victory_icon_r1_c3, "Best Time", "some value"),
            new Stats(R.drawable.victory_icon_r1_c5, "Podium (rename)", "some value"),
            new Stats(R.drawable.victory_icon_r1_c7, "ThumbsUp (rename)", "some value"),
            new Stats(R.drawable.victory_icon_r3_c1, "Number1 (rename)", "some value")
        };
        
        StatsAdapter adapter = new StatsAdapter(this, 
                R.layout.activity_stats_row, stats_data);
        
        
        listViewStats = (ListView)findViewById(R.id.statsActivityListView);
         
        View header = (View)getLayoutInflater().inflate(R.layout.activity_stats_header, null);
        listViewStats.addHeaderView(header);
        
        listViewStats.setAdapter(adapter);
    }
}