package com.duodeck.workout;

/*
 * Heavily influenced by the post by crazyjugglerdrummer
 * http://www.dreamincode.net/forums/topic/110380-deck-of-cards-using-various-methods/
 */
public class Card
{
	private int rank, suit;

	private static String[] suits = { "hearts", "spades", "diamonds", "clubs" };
	private static String[] ranks  = { "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King" };


	Card(int suit, int rank)
	{
		this.rank=rank;
		this.suit=suit;
	}

	public @Override String toString()
	{
		if (rank == -1 || suit == -1) 
		{
			return "The FINISHED CARD";
		} else {
			return ranks[rank] + " of " + suits[suit];
		}
	}

	public int getRank() {
		 return rank;
	}
    
	public int getValue()
    {
    	switch(rank)
    	{
    		case -1: return -1;
    		case 0: return 14;
    		case 1: return 2;
    		case 2: return 3;
    		case 3: return 4;
    		case 4: return 5;
    		case 5: return 6;
    		case 6: return 7;
    		case 7: return 8;
    		case 8: return 9;
    		case 9: return 10;
    		case 10: return 11;
    		case 11: return 12;
    		case 12: return 13;
    		default: return 0;
    	}
    }
	
	public int getSuit() {
		return suit;
	}

}

