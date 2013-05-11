package com.duodeck.workout;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

public class GameActivity extends Activity {

	private DuoDeckApplication duoDeckApp;
	private PersistentStorage ps;

	public Deck deck;
	public Card currentCard;

	private int buddyCardIndex = 0;

	// chronometer
	long chronometerTimeWhenStopped = 0;
	private boolean chronoRunning = false; 
	private Messenger mService = null;

	final Messenger mMessenger = new Messenger(new HandleMessage());

	class HandleMessage extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case DuoDeckService.MSG_GOT_SHUFFLED_ORDER:
				setDeckOrderAndStartWorkout();
				break;
			case DuoDeckService.MSG_GOT_SHUFFLED_ORDER_RESPONSE:
				startChronoIfNotRunningAndDisplayCurrentCard();
				break;
			case DuoDeckService.MSG_SESSION_CLOSED:
				// TODO: write this (Nirmal)
				informSessionClosed(); // not done!
				break;
			case DuoDeckService.MSG_DONE_WITH_CARD_INDEX:
				// receiving that buddy is done

				setBuddyCardIndex(msg.arg1, msg.arg2);

				switch (getGameState()) 
				{
				case MeWaitingBuddyWorkingOut:
					// i was waiting for buddy to finish this card

					// TODO: compare indices and then show card
					
					// TODO: ensure this works: setGameStateBasedOnIndex(); then remove line below
					setGameState(GameStates.MeWorkingOutBuddyWaiting);

					break;
				case BothWorkingOut:
					// both of us working out

					// TODO: compare indices and then show card
					
					// change the game state
					// TODO: ensure this works: setGameStateBasedOnIndex(); then remove line below
					setGameState(GameStates.MeWorkingOutBuddyWaiting);

					break;
				case MeWorkingOutBuddyWaiting:
					// should not arrive here this way
					// TODO: show this as an error
					break;
				}
				
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	
// TODO: ensure this works and then enable it
//	private void setGameStateBasedOnIndex() {
//		if (buddyCardIndex == deck.getDeckSize())
//		{ 
//			// if mine == buddyIndex then set to both working
//			setGameState(GameStates.BothWorkingOut);
//		} else if (buddyCardIndex > deck.getDeckSize()) 
//		{ 
//			// if mine < buddyIndex then meworking buddy waiting
//			setGameState(GameStates.MeWorkingOutBuddyWaiting);
//		} else if (buddyCardIndex < deck.getDeckSize()) 
//		{
//			// if mine > buddyindex then me waiting buddy working out
//			setGameState(GameStates.MeWaitingBuddyWorkingOut);
//		}
//	}

	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = new Messenger(service);
			sendMsgToService(DuoDeckService.MSG_REGISTER, 1, 1);
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bindService(new Intent(this, DuoDeckService.class), mConnection, Context.BIND_AUTO_CREATE);
		duoDeckApp = (DuoDeckApplication) getApplication();
		ps = duoDeckApp.getPersistentStorage();

		deck = new Deck();

		setContentView(R.layout.solo_deck);

		onResumeGameHandler(); // based on gameState, waits to start game, starts game, and/or sends appropriate messages to buddy
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mService == null)
			bindService(new Intent(this, DuoDeckService.class), mConnection, Context.BIND_AUTO_CREATE);

		onResumeGameHandler(); // based on gameState, waits to start game, starts game, and/or sends appropriate messages to buddy
	}

	@Override 
	protected void onPause(){
		super.onPause();
		if (mService != null)
			unbindService(mConnection);
		// do we want to do anything here?
		// currently not pausing the timer because this is helpful with duodecks
	}

	private void onResumeGameHandler() {
		switch (getGameState()) 
		{
		case Solo:
			// draw card
			// start game
			currentCard = deck.getAndPullNextCardFromDeck(); // draw card from deck
			startChronoIfNotRunningAndDisplayCurrentCard(); // starts timer and displays current card
			break;
		case StartingDuoPlayAsSender:
			// game not yet started
			// send the order to otherside
			sendShuffledOrder();
			// wait for ordered deck response
			// 	async will receive the response
			// 	it will call start deck
			// 	set mode to both working
			pickupGameStartingDuoPlayAsSender();
			break;
		case StartingDuoPlayAsReceiver:
			// game not yet started
			// do nothing; waiting for trigger or async trigger
			break;
		case BothWorkingOut:
			// do nothing; waiting for trigger or async trigger
			break;
		case MeWaitingBuddyWorkingOut:
			// do nothing; waiting for trigger or async trigger
			break;
		case MeWorkingOutBuddyWaiting:
			// do nothing; waiting for trigger or async trigger
			break;
		case BothDone:
			// TODO: show summary card or redirect to stats
			break;
		default:
			break;
		}

	}

	private void pickupGameStartingDuoPlayAsSender() {
		// TODO: get params
		// TODO: take actions
	}

	public void doneWithThisCard(View view) 
	{
		switch (getGameState()) 
		{
		case Solo:
			// record stats about card that was just completed
			switch (currentCard.getExerciseAsInt())
			{
			// TODO: make this dynamic instead of hard coded
			case 0:
				deck.pushups.incrementCount(currentCard.getValue());
				// TODO: add time duration summation
				break;
				// TODO: make this dynamic instead of hard coded
			case 1:
				deck.pushups.incrementCount(currentCard.getValue());
				// TODO: add time duration summation
				break;
				// TODO: make this dynamic instead of hard coded
			case 2:
				deck.situps.incrementCount(currentCard.getValue());
				// TODO: add time duration summation
				break;
				// TODO: make this dynamic instead of hard coded
			case 3:
				deck.situps.incrementCount(currentCard.getValue());
				// TODO: add time duration summation
				break;
			}

			TextView staticTimeValue = (TextView) findViewById(R.id.staticTimeValueDisplay);
			staticTimeValue.setText(((Chronometer) getChronometer()).getText());

			if (deck.getCardsRemaining() > 0) 
			{ // if not done with deck

				// get new card
				// remove the card from the deck of available options
				// display the next card

				//					System.out.println("done with this card");

				// TODO: remove for final, this is for debugging only
				TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
				deckInfo.setText(deck.showDeck());

				// TODO: make the display look nicer
				TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
				currentCard = deck.getAndPullNextCardFromDeck();
				displayOfCurrentCard.setText(currentCard + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );

				deck.inGameStats.duration = ((Chronometer) getChronometer()).getText().toString();					

			} else { // if done with deck
				stopChronometer(null);

				// System.out.println("finished deck");
				deck.inGameStats.duration = ((Chronometer) getChronometer()).getText().toString();

				String statsAsString = ps.makeWorkoutStringForStorage(deck);
				ps.saveWorkoutDataToSharedPrefs(GameActivity.this, StatKeys.PreviousDeck, statsAsString);

				ps.updateStatsWithNewDeck(GameActivity.this, deck);

				// neuter the "next card" button
				View buttonNextCard = findViewById(R.id.solo_done_with_this_card);
				buttonNextCard.setOnClickListener(null);

				// show "finished" text and provide button to stats activity
				View buttonGotoStats = findViewById(R.id.gotoStatsFromGame);
				buttonGotoStats.setVisibility(buttonGotoStats.VISIBLE);
			}

			break;
		case StartingDuoPlayAsSender:
			// should not be an option for "doneWithThisCard()"
			break;
		case StartingDuoPlayAsReceiver:
			// should not be an option for "doneWithThisCard()"
			break;
		case BothWorkingOut:

			showModalMessage("WAITING FOR BUDDY");

			// tell buddy i'm done
			sendDoneWithCard();

			// Get next index from deck
			currentCard = deck.getAndPullNextCardFromDeck();

			// change the status to MeWaitingBuddyWorkingOut
			setGameState(GameStates.MeWaitingBuddyWorkingOut);

			// 	wait
			//   once async responds that buddy has finished, then it will call displayCurrentCard()
			break;
		case MeWaitingBuddyWorkingOut:
			// should not be an option for "doneWithThisCard()"
			break;
		case MeWorkingOutBuddyWaiting:

			// let buddy know that i'm done
			sendDoneWithCard();

			// pull cards
			asyncDrawCardsUntilMatchWithBuddy();
			displayCurrentCard();

			setGameState(GameStates.BothWorkingOut);

			break;
		case BothDone:
			// show summary card or redirect to stats
			break;
		default:
			break;
		}
	}



	private void startChronoIfNotRunningAndDisplayCurrentCard() 
	{
		// make sure that the "done" button isn't visible
		View buttonGotoStats = findViewById(R.id.gotoStatsFromGame);
		findViewById(R.id.gotoStatsFromGame).setVisibility(buttonGotoStats.INVISIBLE);

		// start chrono if not running
		if (!chronoRunning) { 
			startChronometer(null); 
			deck.inGameStats.setStartDate();
		}

		// display current card
		displayCurrentCard();
	}
	private void displayCurrentCard() 
	{		
		// display card
		TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
		// show current card (removed from deck)
		displayOfCurrentCard.setText("this card: " + currentCard + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );

		// ---debugging------------
		// show full deck
		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
	}
	public void showModalMessage(String msg) {
		// TODO: make this a modal 

		// display card
		TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
		// show current card (removed from deck)
		displayOfCurrentCard.setText("this card: " + msg);

		// ---debugging------------
		// show full deck
		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
	}



	public void gotoStatsFromGame(View view) {
		//        System.out.println("goto Stats from Game");
		// TODO: call onDestroy() of game. 
		// TODO: in onDestroy() of game, start stats
		Intent intent = new Intent(this, StatsActivity.class);
		startActivity(intent);
	};




	/* 
	 * CHRONOMETER
	 */
	protected Chronometer getChronometer() 
	{
		return ((Chronometer) findViewById(R.id.chronometer1));
	}
	// start
	public void startChronometer(View view) 
	{
		Chronometer mChronometer = getChronometer();
		mChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeWhenStopped);
		mChronometer.start();
		chronoRunning = true;
	}
	// stop
	public void stopChronometer(View view) 
	{
		getChronometer().stop();
		chronoRunning = false;
	}
	// pause
	public void pauseChronometer(View view) 
	{
		Chronometer mChronometer = getChronometer();
		chronometerTimeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();

		mChronometer.stop();
		chronoRunning = false;
	}
	// resume
	public void resumeChronometer(View view) 
	{
		Chronometer mChronometer = getChronometer();

		mChronometer.setBase(chronometerTimeWhenStopped);
		mChronometer.start();
		chronoRunning = true;

		mChronometer.setBase(SystemClock.elapsedRealtime() + chronometerTimeWhenStopped);
	}






	private void sendMsgToService(int type, int arg1, int arg2) {
		Message msg = Message.obtain(null, type, arg1, arg2);
		msg.replyTo = mMessenger;
		try {
			if (mService != null)
				mService.send(msg);
			else
				System.out.println("No service available");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void setDeckOrderAndStartWorkout() {
		int[] targetOrder = duoDeckApp.getDeckOrder();
		if (targetOrder == null) {
			System.out.println("Some issue in deck order received");
		} else {
			this.deck.setOrderToMatch(targetOrder);
			currentCard = deck.getAndPullNextCardFromDeck();
			this.startChronoIfNotRunningAndDisplayCurrentCard();
		}
	}

	private void informSessionClosed() {
		// TODO: ...
	}



	/*
	 * Card Order
	 */
	private void asyncDrawCardsUntilMatchWithBuddy() 
	{
		if (getGameState() != GameStates.Solo) 
		{
			while (buddyCardIndex < deck.getDeckSize()) 
			{
				currentCard = deck.getAndPullNextCardFromDeck();
			}
		} else {
			currentCard = deck.getAndPullNextCardFromDeck();
		}
	}
	private synchronized void setBuddyCardIndex(int buddyIndex, int myIndexThatBuddyHas) {
		this.buddyCardIndex = buddyIndex;
		//also update required states here.
	}
	private void sendShuffledOrder() {
		duoDeckApp.setDeckOrder(this.deck.getOrder());
		sendMsgToService(DuoDeckService.MSG_SEND_SHUFFLED_ORDER, 1, 1);
	}
	private void sendShuffledOrderResponse() {
		sendMsgToService(DuoDeckService.MSG_SEND_SHUFFLED_ORDER_RESPONSE, 1, 1);
	}
	private void sendDoneWithCard() {
		sendMsgToService(DuoDeckService.MSG_DONE_WITH_CARD_INDEX, deck.getDeckSize(), this.buddyCardIndex);
	}



	/*
	 * Game State setter/getters
	 */
	private void setGameState(GameStates gameState) 
	{
		duoDeckApp.setCurrentGameState(gameState);
	}
	private GameStates getGameState() 
	{
		return duoDeckApp.getCurrentGameState();
	}

}
