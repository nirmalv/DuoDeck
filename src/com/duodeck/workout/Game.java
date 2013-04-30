package com.duodeck.workout;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.duodeck.R;

public class Game extends Activity {
	
	public Deck deck = new Deck();

	private PersistentStorage ps = new PersistentStorage();
	
	private GameStates gameStates;
	public GameStates currentGameState = GameStates.Solo;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.solo_deck);

		startChronometer(null);
		
		TextView currentCard = (TextView) findViewById(R.id.display_current_card);
		currentCard.setText("this card: " + deck.drawFromDeck() + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );
		
		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
		
	}
	
	public void doneWithThisCard(View view) 
	{
		switch (currentGameState) 
		{
			case Solo:
				// record the time that the card was finished
				
				TextView staticTimeValue = (TextView) findViewById(R.id.staticTimeValueDisplay);
				staticTimeValue.setText(((Chronometer) findViewById(R.id.chronometer1)).getText());
				
				if (deck.getCardsRemaining() >= 0) 
				{
					// get new card
					// remove the card from the deck of available options
					// display the next card
					// TODO: remove the following line
					System.out.println("done with this card");
		
					TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
					deckInfo.setText(deck.showDeck());
					
					TextView currentCard = (TextView) findViewById(R.id.display_current_card);
					currentCard.setText(deck.drawFromDeck() + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );
				} else {
					stopChronometer(null);
					
					// TODO: record finished time
					// TODO: record deck stats in stats
					// TODO: show stats
					// TODO: show finished time
					// TODO: show summary of deck stats (sum of values per suit)
					// TODO: show summary of deck stats (sum of values per EXERCISE)
				}
	
				break;
				
			case MeWaitingBuddyWorkingOut:
				// TODO: record the time that this was finished
				// TODO: queue a new card (pull it from the deck), but don't show it yet
				// TODO: Notify partner that i'm done and send the card
				// TODO: show "wait" screen
				// TODO: put in "wait" state that can receive a push notification to move forward
				break;
				
			case MeWorkingOutBuddyWaiting:
				// TODO: request the card from buddy
				// TODO: remove this card from my deck
				// TODO: Notify partner that i'm done
				// TODO: start the next card
				break;
				
			default:
				break;
		}
	}
	
	public void saveData(View view) 
	{
		ps.saveDataToFile();
	}
	
	public void getData(View view)
	{
		System.out.println(ps.getDataFromInternalStorage());
	}
	
	public void startChronometer(View view) {
	    ((Chronometer) findViewById(R.id.chronometer1)).start();
	}
	
	public void stopChronometer(View view) {
	    ((Chronometer) findViewById(R.id.chronometer1)).stop();
    }
	
	
	
	
}
