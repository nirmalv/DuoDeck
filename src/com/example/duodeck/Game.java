package com.example.duodeck;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class Game extends Activity {
//	public String DIAMOND = "d";
//	public String CLUB = "c";
//	public String HEART = "h";
//	public String SPADE = "s";
//	
//	// attributes of this card
//	public int value = 0; // 2-14 (deuce - ace)
//	public String suit = null; // d|c|h|s per above
	
	public Deck deck = new Deck();

	private final String NoGame = "NoGame";
	private final String Solo = "Solo";
	private final String MeInvitingBuddy = "MeInvitingBuddy";
	private final String BuddyInvitingMe = "BuddyInvitingMe";
	private final String BothWorkingout = "BothWorkingout";
	private final String MeWorkingOutAndBuddyWaiting = "MeWorkingOutAndBuddyWaiting";
	private final String MeWaitingAndBuddyWorkingOut = "MeWaitingAndBuddyWorkingOut";
	private final String BothDone = "BothDone";
	// TODO: finish this and use ones from Nirmal's suggestions
	private String gameState = NoGame;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.solo_deck);

		TextView currentCard = (TextView) findViewById(R.id.display_current_card);
		currentCard.setText("this card: " + deck.drawFromDeck() + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );
		
		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
	}
	
	public void doneWithThisCard(View view) 
	{
		if (gameState == Solo) {
			// record the time that the card was finished
			
			if (deck.getCardsRemaining() > 0) 
			{
				// get new card
				// remove the card from the deck of available options
				// display the next card
				System.out.println("done with this card");
	
				TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
				deckInfo.setText(deck.showDeck());
				
				TextView currentCard = (TextView) findViewById(R.id.display_current_card);
				currentCard.setText(deck.drawFromDeck() + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );
			} else {
				// TODO: record finished time
				// TODO: record deck stats in stats
				// TODO: show stats
				// TODO: show finished time
				// TODO: show summary of deck stats (sum of values per suit)
				// TODO: show summary of deck stats (sum of values per EXERCISE)
			}
			
			
		} else {
			if (gameState == MeWaitingAndBuddyWorkingOut) {
				// TODO: record the time that this was finished
				// TODO: queue a new card (pull it from the deck), but don't show it yet
				// TODO: Notify partner that i'm done and send the card
				// TODO: show "wait" screen
				// TODO: put in "wait" state that can receive a push notification to move forward
			} else if (gameState == MeWorkingOutAndBuddyWaiting) {
				// TODO: request the card from buddy
				// TODO: remove this card from my deck
				// TODO: Notify partner that i'm done
				// TODO: start the next card
			}
		}
	}
	
	
	
	
}
