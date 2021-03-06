package com.duodeck.workout;

import android.content.Context;

public class PersistentStorage {
	
//	private String FILENAME = "./hello_file.txt";
//	private String string = "hello world!";

    // file for each deck
	public static final String STORAGE_STATS_DECK1 = "StatsDeck1";
	// TODO: add in decks 2 and 3
	//	  public static final String STORAGE_STATS_DECK2 = "StatsDeck2";	
	//    public static final String STORAGE_STATS_DECK3 = "StatsDeck3";
	public static final String ACCOUNT_SETUP = "DuoDeckAccountStorage";
	
	public String getUserName(Context context) {
		return context.getSharedPreferences(ACCOUNT_SETUP, 0).getString("UserName", null);
	}
	
	public void updateUserName(Context context, String username) {
		context.getSharedPreferences(ACCOUNT_SETUP, 0)
				.edit()
				.putString("UserName", username)
				.commit();
	}
	
	public String getAuthToken(Context context) {
		return context.getSharedPreferences(ACCOUNT_SETUP, 0).getString("AuthToken", null);
	}
	
	public void updateAuthToken(Context context, String authToken) {
		context.getSharedPreferences(ACCOUNT_SETUP, 0)
				.edit()
				.putString("AuthToken", authToken)
				.commit();
	}
    
    // string format (rough idea)
	// dateStarted:yyyy-mm-dd timeOfDayStarted:hh:mm:ss deckName:string duration:hh:mm s:count,duration h:count,duration d:count,duration c:count,duration map:s=exercise,h=exercise,d=exercise,c=exercise"
    public String makeWorkoutStringForStorage(Deck deck) 
    {
    	TrackStatsWhilePlaying stats = deck.inGameStats;
    	
    	String s = ""; String br = "\n";
    	String sp = " "; String cm = ","; String cl = ":";
    	
    	s+= "dateStarted: " + stats.dateStarted.convertDateToStringYMD() + sp + br;
    	s+= "timeOfDayStarted: " + stats.dateStarted.convertDateToStringTimeOfDay() + sp + br;
    	s+= "deckName: " + deck.name + sp + br;
    	s+= "duration: " + stats.duration + sp + br;
    	s+= "pushups: " + deck.pushups.toString() + sp + br;
    	s+= "situps: " + deck.situps.toString() + sp + br;
    	
    	return s;
    }
    
    
    public void saveWorkoutDataToSharedPrefs(Context context, StatKeys key, String data) 
	{ 	// We need an Editor object to make preference changes. All objects are from android.context.Context
    	switch (key) 
		{
			case DecksCompleted:
				context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
				break;
			case FirstDeck:
				context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
				break;
			case DateSinceLastDeck:
				context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
				break;
			case PreviousDeck:
				context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
				break;
			case FastestDeck:
				context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
				break;
			case CumulativePushups:
				context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
				break;
			case CumulativeSitups:
				context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
				break;
			default:
				break;
		}
        context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), data).commit();
	}
	
	public String getWorkoutDataFromSharedPrefs(Context context, StatKeys key)
	{
		String s = "defaultString";
		switch (key) 
		{
			case DecksCompleted:
				s = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.DecksCompleted.toString(), "");
				break;
			case FirstDeck:
				s = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.FirstDeck.toString(), "");
				break;
			case DateSinceLastDeck:
				s = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.DateSinceLastDeck.toString(), "");
				break;
			case PreviousDeck:
				s = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.PreviousDeck.toString(), "");
				break;
			case FastestDeck:
				s = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.FastestDeck.toString(), "");
				break;
			case CumulativePushups:
				s = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.CumulativePushups.toString(), "");
				break;
			case CumulativeSitups:
				s = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.CumulativeSitups.toString(), "");
				break;
			default:
				break;
		}
		return s;
	}




	public void updateStatsWithNewDeck(Context context, Deck deck) {
		for (int i=0; i<StatKeys.values().length; i++)
		{
			StatKeys key = StatKeys.values()[i];
			String storageWorkoutAsString = "";
			String currentWorkoutAsString = makeWorkoutStringForStorage(deck); // from current deck
			int val;
			switch (key) 
			{
				/*
				 * Basic format:
				 * for each stat:
				 * pull the appropriate workoutString for that stat
				 * do necessary comparisons
				 * save the "[fast]-est" time as a string in sharedPrefs
				 */
				case DecksCompleted:
					storageWorkoutAsString = "0" + context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.DecksCompleted.toString(), "");
					val = Integer.parseInt(storageWorkoutAsString) + 1;
					context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), val + "").commit();
					break;
				case FirstDeck:
					storageWorkoutAsString = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.FirstDeck.toString(), "");
					if (storageWorkoutAsString.equalsIgnoreCase("")) 
					{
						context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), currentWorkoutAsString).commit();
					}
					break;
				case DateSinceLastDeck: // TODO: implement this
					storageWorkoutAsString = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.DateSinceLastDeck.toString(), "");
					context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), storageWorkoutAsString).commit();
					break;
				case PreviousDeck:
					currentWorkoutAsString = makeWorkoutStringForStorage(deck);
					context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), currentWorkoutAsString).commit();
					break;
				case FastestDeck:
					storageWorkoutAsString = context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.FastestDeck.toString(), "");
					
					CustomDate storageDurationDate = new CustomDate();
					storageDurationDate.setThisDateToDurationBasedOnString(storageWorkoutAsString);
					
					System.out.println("current: " + storageDurationDate.convertDateToStringTimeOfDay());
					
					if (storageDurationDate.isNullDuration()) 
					{
						context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), currentWorkoutAsString).commit();	
					} else {
						CustomDate currentDurationDate = new CustomDate();
						currentDurationDate.setThisDateToDurationBasedOnString(currentWorkoutAsString);
//						currentDurationDate.setThisDateToDurationBasedOnString(deck.inGameStats.duration);
						
						System.out.println("current: " + deck.inGameStats.duration);
						System.out.println("current: " + currentDurationDate.convertDateToStringTimeOfDay() + " ||| storage: " + storageDurationDate.convertDateToStringTimeOfDay());
						
						if (currentDurationDate.before(storageDurationDate))
						{
							context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), currentWorkoutAsString).commit();
						} else {
							context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), storageWorkoutAsString).commit();
						}
					}
					
					break;
				case CumulativePushups:
					storageWorkoutAsString = "0" + context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.CumulativePushups.toString(), "");
					val = Integer.parseInt(storageWorkoutAsString) + deck.pushups.getCount();
					context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), val + "").commit();
					break;
				case CumulativeSitups:
					storageWorkoutAsString = "0" + context.getSharedPreferences(STORAGE_STATS_DECK1, 0).getString(StatKeys.CumulativeSitups.toString(), "");
					val = Integer.parseInt(storageWorkoutAsString) + deck.situps.getCount();
					context.getSharedPreferences(STORAGE_STATS_DECK1, 0).edit().putString(key.toString(), val + "").commit();
					break;
				default:
					break;
			}
		}
	}
    
}
