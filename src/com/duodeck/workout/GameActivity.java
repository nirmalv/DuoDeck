package com.duodeck.workout;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
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

	private int buddyCardIndex;

	// chronometer
	long chronometerTimeWhenStopped = 0;
	private boolean chronoRunning = false; 
	private Messenger mService = null;
	private AlertDialog popupModal;

	final Messenger mMessenger = new Messenger(new HandleMessage());

	class HandleMessage extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case DuoDeckService.MSG_GOT_SHUFFLED_ORDER:
				setDeckOrderAndStartWorkout();
				buddyCardIndex = deck.getCardsRemaining() + 1;
				break;
			case DuoDeckService.MSG_GOT_SHUFFLED_ORDER_RESPONSE:
				startChronoIfNotRunningAndDisplayCurrentCard();
				setGameState(GameStates.BothWorkingOut);
				break;
			case DuoDeckService.MSG_SESSION_CLOSED:
				informSessionClosed(); 
				break;
			case DuoDeckService.MSG_DONE_WITH_CARD_INDEX:
				// receiving that buddy is done
				setBuddyCardIndex(msg.arg1);
				break;
			case DuoDeckService.MSG_REPEAT_DONE_WITH_CARD:
				sendDoneWithCard();
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}


	private void dismissModal() {
		if (popupModal != null) {
			popupModal.dismiss();
			popupModal = null;
		}
	}
	
	private void setGameStateBasedOnIndex() {
		if (deck.getCardsRemaining() == 0 && buddyCardIndex == 0) {
			dismissModal();
			setGameState(GameStates.BothDone);
		}
		if (buddyCardIndex == deck.getCardsRemaining())
		{ 
			dismissModal();
			setGameState(GameStates.BothWorkingOut);
		} else if (buddyCardIndex < deck.getCardsRemaining()) 
		{ 
			dismissModal();
			setGameState(GameStates.MeWorkingOutBuddyWaiting);
		} else if (buddyCardIndex > deck.getCardsRemaining()) 
		{
			showModalMessage("WAITING FOR BUDDY", false);
			setGameState(GameStates.MeWaitingBuddyWorkingOut);
		}
	}

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
		sendMsgToService(DuoDeckService.MSG_UNREGISTER, 1, 1);
		if (mService != null)
			unbindService(mConnection);
		// do we want to do anything here?
		// currently not pausing the timer because this is helpful with duodecks
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dismissModal();
	}

	private void onResumeGameHandler() {
		switch (getGameState()) 
		{
		case Solo: 
			if (!chronoRunning) 
			{ // if resuming instead of starting a new game
				TextView gameTypeDisplay = (TextView) findViewById(R.id.display_current_card);
				// show current card (removed from deck)
				gameTypeDisplay.setText("Solo Game");

				// create deck
//				deck.shuffleOrder();
//				deck.setOrderToMatch(deck.getOrder());
				// draw card
				currentCard = deck.getAndPullNextCardFromDeck(); // draw card from deck
				// start game
				startChronoIfNotRunningAndDisplayCurrentCard(); // starts timer and displays current card
			}
			break;
		case StartingDuoPlayAsSender:
			((TextView) findViewById(R.id.display_current_card)).setText("Duo Game");
			
			sendShuffledOrder();
			pauseChronoAndShowModal();
			showModalMessage("Performing sync-up", false);
			// wait for ordered deck response
			// 	async will receive the response
			// 	it will call start deck
			// 	set mode to both working
			break;
		case StartingDuoPlayAsReceiver:
			((TextView) findViewById(R.id.display_current_card)).setText("Duo Game");
			
			showModalMessage("Performing sync-up", false);
			pauseChronoAndShowModal();
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

	public void doneWithThisCard(View view) 
	{
		System.out.println("done with card. game state: " + getGameState());
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
				// TODO: remove for final, this is for debugging only
				TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
				deckInfo.setText(deck.showDeck());

				// TODO: make the display look nicer
				TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
				currentCard = deck.getAndPullNextCardFromDeck();
				displayOfCurrentCard.setText(currentCard + "");

				TextView progressDisplay = (TextView) findViewById(R.id.textView1);
				progressDisplay.setText(deck.getCardsRemaining() + "/" + deck.getDeckSize() );
				
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
//				View buttonGotoStats = findViewById(R.id.gotoStatsFromGame);
//				buttonGotoStats.setVisibility(View.VISIBLE);
				gotoStatsFromGame();
			}

			break;
		case StartingDuoPlayAsSender:
			// should not be an option for "doneWithThisCard()"
			break;
		case StartingDuoPlayAsReceiver:
			// should not be an option for "doneWithThisCard()"
			break;
		case BothWorkingOut:
		case MeWorkingOutBuddyWaiting:
			sendDoneWithCard();
			this.moveGameForward();
			break;
		case MeWaitingBuddyWorkingOut:
			// should not be an option for "doneWithThisCard()"
			if (buddyCardIndex < deck.getCardsRemaining()) {
				sendDoneWithCard();
				this.moveGameForward();
			}
			break;
		case BothDone:
			// should not be an option
			break;
		default:
			break;
		}
	}

	private void moveGameForward() {
		setGameStateBasedOnIndex();
		if (getGameState() == GameStates.MeWorkingOutBuddyWaiting || 
				getGameState() == GameStates.BothWorkingOut) { 
			currentCard = deck.getAndPullNextCardFromDeck();
			displayCurrentCard();
		} else if(getGameState() == GameStates.BothDone) {
			stopChronometer(null);

			System.out.println("finished deck");
			deck.inGameStats.duration = ((Chronometer) getChronometer()).getText().toString();

			String statsAsString = ps.makeWorkoutStringForStorage(deck);
			ps.saveWorkoutDataToSharedPrefs(GameActivity.this, StatKeys.PreviousDeck, statsAsString);

			ps.updateStatsWithNewDeck(GameActivity.this, deck);

			// neuter the "next card" button
			View buttonNextCard = findViewById(R.id.solo_done_with_this_card);
			buttonNextCard.setOnClickListener(null);

//			setGameState(GameStates.Solo);

			// show "finished" text and provide button to stats activity
//			View buttonGotoStats = findViewById(R.id.gotoStatsFromGame);
//			buttonGotoStats.setVisibility(View.VISIBLE);

			gotoStatsFromGame();
			
		} 
	}

	private void startChronoIfNotRunningAndDisplayCurrentCard() 
	{
		dismissModal();
		
		// make sure that the "done" button isn't visible
		View buttonGotoStats = findViewById(R.id.gotoStatsFromGame);
//		findViewById(R.id.gotoStatsFromGame).setVisibility(View.INVISIBLE);

		// start chrono if not running
		if (!chronoRunning) { 
			startChronometer(null); 
			deck.inGameStats.setStartDate();
		}

		if (currentCard == null) 
		{
			currentCard = deck.getAndPullNextCardFromDeck();
		}
		
		// display current card
		displayCurrentCard();
	}

	private void pauseChronoAndShowModal() {
		pauseChronometer(null);
		showModalMessage("Game waiting...", true);
	}
	private void resumeChronoAndDismissModal() {
		resumeChronometer(null);
		popupModal.dismiss();
	}

	private void displayCurrentCard() 
	{		
		// display card

		TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
		currentCard = deck.getAndPullNextCardFromDeck();
		displayOfCurrentCard.setText(currentCard + "");

		TextView progressDisplay = (TextView) findViewById(R.id.textView1);
		progressDisplay.setText(deck.getCardsRemaining() + "/" + deck.getDeckSize() );
		
		// ---debugging------------
		// show full deck
		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
	}
	public void showModalMessage(String msg, boolean showOk) {
		// TODO: make this a modal

		dismissModal();

		AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
		builder.setTitle(msg);
		if (showOk) {
//			builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
//				@Override
//				public void onClick(DialogInterface dialog, int which) {}
//			});
		}
		popupModal = builder.create();
		popupModal.show();

		// display card
		TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
		// show current card (removed from deck)
		displayOfCurrentCard.setText("this card: " + msg);

		// ---debugging------------
		// show full deck
		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
	}



	public void gotoStatsFromGame() {
		Intent intent = new Intent(this, StatsActivity.class);
		startActivity(intent);
		finish(); // kills game activity
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
			else {
				duoDeckApp.mService.send(msg);
				System.out.println("No service available, but sent with app service");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private synchronized void setDeckOrderAndStartWorkout() {
		int[] targetOrder = duoDeckApp.getDeckOrder();
		System.out.println("Target order: " + Arrays.toString(targetOrder));
		if (targetOrder == null) {
			System.out.println("Some issue in deck order received");
			// TODO: handle this error message
		} else {
			this.deck.setOrderToMatch(targetOrder);
			currentCard = deck.getAndPullNextCardFromDeck();
			this.startChronoIfNotRunningAndDisplayCurrentCard();
			this.sendShuffledOrderResponse();

			setGameState(GameStates.BothWorkingOut);
		}
	}

	private void informSessionClosed() {
		this.showModalMessage("Workout session Lost", true);
	}

	/*
	 * Card Order
	 */
	private void asyncDrawCardsUntilMatchWithBuddy() 
	{
		if (getGameState() != GameStates.Solo) 
		{
			while (buddyCardIndex < deck.getCardsRemaining()) 
			{
				currentCard = deck.getAndPullNextCardFromDeck();
			}
		} else {
			currentCard = deck.getAndPullNextCardFromDeck();
		}
	}
	private synchronized void setBuddyCardIndex(int buddyIndex) {
		this.buddyCardIndex = buddyIndex;
		if (getGameState() == GameStates.MeWaitingBuddyWorkingOut) {
			this.moveGameForward();
		} 
	}
	private void sendShuffledOrder() {
		duoDeckApp.setDeckOrder(this.deck.getOrder());
		sendMsgToService(DuoDeckService.MSG_SEND_SHUFFLED_ORDER, 1, 1);
		buddyCardIndex = deck.getCardsRemaining() + 1;
	}
	private void sendShuffledOrderResponse() {
		sendMsgToService(DuoDeckService.MSG_SEND_SHUFFLED_ORDER_RESPONSE, 1, 1);
	}
	private void sendDoneWithCard() {
		sendMsgToService(DuoDeckService.MSG_DONE_WITH_CARD_INDEX, deck.getCardsRemaining(), 0);
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
	
	/*
	 * Aesthetic adjustments
	 */
	public void setFontSizeToMax() 
	{
		// Determine Scaled Density for later calculations
//		Display dd = ((Activity) Context).getWindowManager().getDefaultDisplay();
//		DisplayMetrics dm = new DisplayMetrics();
//		dd.getMetrics(dm);
//		float m_ScaledDensity = dm.scaledDensity;
//
//		Rect bounds = new Rect();
//		Paint p = new Paint();
//		p.setTypeface(btn.getTypeface());
//
//		// W is a very wide character
//		String sample = "NeedsToFIt"
//		int maxFont;
//		for (maxFont= 1
//		    ; -bounds.top <= btn.getHeight() && bounds.right <= btn.getWidth()
//		    ; maxFont++) {
//		  p.setTextSize(maxFont);
//		  p.getTextBounds(sample, 0, sample.length(), bounds);
//		}
//		maxFont = (int) ((maxFont - 1) / m_ScaledDensity);
//		btn.setTextSize(maxFont);
	}

}
