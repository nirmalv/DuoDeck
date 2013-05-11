package com.duodeck.workout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import com.duodeck.workout.TrackStatsWhilePlaying.InGameStatsBySuit;

/*
 * Heavily influenced by the post by crazyjugglerdrummer
 * http://www.dreamincode.net/forums/topic/110380-deck-of-cards-using-various-methods/
 */
public class Deck {
	private ArrayList<Card> cards;
	private int deckSize = 52; // initialize it to expected value, then set dynamically after building the deck
	private int[] order;
	
	private Card finishedCard = new Card(-1, -1);

	public String name = "Deck1";
	
	// track stats
	public TrackStatsWhilePlaying inGameStats = new TrackStatsWhilePlaying();

	// TODO: make this dynamic instead of hard coded
	public InGameStatsBySuit pushups = inGameStats.createStatsForExercise();
	public InGameStatsBySuit situps = inGameStats.createStatsForExercise();
	
	Deck()
	{
		populateDeck();
		createCardOrder();
		shuffleOrder();
		setOrderToMatch(order);
	}
	
	Deck(int[] targetOrder)
	{
		populateDeck();
		createCardOrder();
		setOrderToMatch(targetOrder);
	}

	private void populateDeck() 
	{
		cards = new ArrayList<Card>();

		// TODO: dynamically set the size of the suits based on the card's available suits
		for (int a=0; a<=3; a++)
		{
			//for (int b=0; b<=12; b++)
			// TODO: uncomment the above line for the full deck 
			for (int b=0; b<=3; b++)
			{
			   cards.add( new Card(a,b) );
			}
		}
		deckSize = cards.size();
	}
	
	private void shuffle() // should not be used
	{
		int index_1, index_2;
		Random generator = new Random();
		Card temp;
		
		for (int i=0; i<100; i++)
		{
			index_1 = generator.nextInt( cards.size() - 1 );
			index_2 = generator.nextInt( cards.size() - 1 );

			temp = (Card) cards.get( index_2 );
			cards.set( index_2 , cards.get( index_1 ) );
			cards.set( index_1, temp );
		}
	}
	private void createCardOrder() 
	{
		order = new int[cards.size()];
		for (int i=0; i<cards.size(); i++) 
		{
			order[i] = i;
		}
	}
	public void setOrderToMatch(int[] targetOrder)
	{	
		// requires starting deck in a predictable order
		// create new fresh deck with predictable order (and ALL cards)
		populateDeck();
		createCardOrder();
		
		ArrayList<Card> tempDeckWithPerfectOrder = (ArrayList<Card>) cards.clone();
		
		// note that targetOrder may be smaller than order
		for (int i=0; i<order.length; i++)
		{
//			System.out.println("order.length: " + order.length + "\t i: " + i + "\t order[i]: " + order[i] + "\t cards.get(i)" + cards.get(i));
//			System.out.println("targO.length: " + targetOrder.length + "\t i: " + i + "\t targetOrder[i]: " + order[i] + "\t cards.get(targetOrder[i])" + cards.get(targetOrder[i]));
			// TODO: handle different sized decks!
			if (i < targetOrder.length) 
			{
				cards.set(targetOrder[i], tempDeckWithPerfectOrder.get(i));
			} else {
				// remove unused cards
				cards.remove(i);
			}
		}	

		tempDeckWithPerfectOrder.clear();
		
		// set order to match incoming order
		order = targetOrder;
	}
	public void shuffleOrder() // !
	{
		int index_1, index_2;
		Random generator = new Random();
		int temp;
		
		for (int i=0; i<100; i++)
		{
			index_1 = generator.nextInt( order.length - 1 );
			index_2 = generator.nextInt( order.length - 1 );

			temp = order[ index_2 ];
			order[ index_2 ] = order[ index_1 ];
			order[ index_1 ] =  temp;
		}
	}
	public int[] getOrder() 
	{
		return order;
	}
	
	public String showDeck()
	{
		String s = "Cards left including this: " + getCardsRemaining() + "\n\n";
		for (int i=0; i<cards.size(); i++) 
		{
			Card card = cards.get(i);
			s += i + ": \t" + card.toString() + "\t\t\t\t\t\tvalue" + card.getValue() + "\t\trank: " + card.getRank() + "\n";
		}
		return s;
	}
	
	
	public Card getAndPullNextCardFromDeck()
	{	   
		if (cards.size() > 0) 
		{
			order = Arrays.copyOfRange(order, 0, order.length - 1);
			return cards.remove( cards.size() - 1 );
		} else {
			return finishedCard;
		}
	}

	public int getCardsRemaining()
	{
		return cards.size();
	}
	
	public int getDeckSize()
	{
		return deckSize;
	}

}

