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
		System.out.println("done with this card");

		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
		
		TextView currentCard = (TextView) findViewById(R.id.display_current_card);
		currentCard.setText(deck.drawFromDeck() + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );

	}
	
	
	
}
