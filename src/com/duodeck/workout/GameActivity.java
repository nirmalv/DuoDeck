package com.duodeck.workout;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class GameActivity extends Activity {

	private Context context = (Context) this;
	
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
				break;
			case DuoDeckService.MSG_GOT_SHUFFLED_ORDER_RESPONSE:
				if (getGameState() == GameStates.StartingDuoPlayAsSender) {
					System.out.println("update StartingDuoPlayAsSender in msg");
					startChronoIfNotRunningAndDisplayCurrentCard();
					setGameState(GameStates.BothWorkingOut);
				}
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
		if (currentCard.equals(Deck.finishedCard) && buddyCardIndex == 0) {
			dismissModal();
			this.goToStatePage();
		} else if (buddyCardIndex == deck.getCardsRemaining()) {
			dismissModal();
			setGameState(GameStates.BothWorkingOut);
		} else if (buddyCardIndex < deck.getCardsRemaining()) {
			dismissModal();
			setGameState(GameStates.MeWorkingOutBuddyWaiting);
		} else if (buddyCardIndex > deck.getCardsRemaining()) {
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
		duoDeckApp = (DuoDeckApplication) getApplication();
		ps = duoDeckApp.getPersistentStorage();
		deck = new Deck();
		setContentView(R.layout.game);
	}

	@Override
	protected void onResume() {
		super.onResume();
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
				TextView gameTypeDisplay = (TextView) findViewById(R.id.solo_deck_title);
				gameTypeDisplay.setText("Solo Game");
				startChronoIfNotRunningAndDisplayCurrentCard(); // starts timer and displays current card
			}
			break;
		case StartingDuoPlayAsSender:
			((TextView) findViewById(R.id.solo_deck_title)).setText("Duo Game");
			
			sendShuffledOrder();
			pauseChronoAndShowModal();
			runOnUiThread(new Runnable() {
				//ugly hack
				@Override
				public void run() {
					for (int i = 0; i < 10; i++)
						if (getGameState() != GameStates.StartingDuoPlayAsSender)
							break;
						else if (getGameState() == GameStates.StartingDuoPlayAsSender
							&& duoDeckApp.delayedService == 1) {
							System.out.println("update StartingDuoPlayAsSender in runnable");
							startChronoIfNotRunningAndDisplayCurrentCard();
							setGameState(GameStates.BothWorkingOut);
							break;
						} else {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
				}
			});
			break;
		case StartingDuoPlayAsReceiver:
			((TextView) findViewById(R.id.solo_deck_title)).setText("Duo Game");
			pauseChronoAndShowModal();
			runOnUiThread(new Runnable() {
				//ugly hack
				@Override
				public void run() {
					for (int i = 0; i < 10; i++)
						if (getGameState() != GameStates.StartingDuoPlayAsReceiver) 
							break;
						else if (getGameState() == GameStates.StartingDuoPlayAsReceiver
							&& duoDeckApp.getDeckOrder() != null) {
							setDeckOrderAndStartWorkout();
							break;
						} else {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
				}
			});
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
			
			if (deck.getCardsRemaining() > 0) 
			{ // if not done with deck

				// get new card
				// remove the card from the deck of available options
				// display the next card
				System.out.println("displayCurrentCard in SOLO");
				displayCurrentCard();
				
				deck.inGameStats.duration = ((Chronometer) getChronometer()).getText().toString();					

			} else { // if done with deck
				stopChronometer(null);

				// System.out.println("finished deck");
				deck.inGameStats.duration = ((Chronometer) getChronometer()).getText().toString();

				String statsAsString = ps.makeWorkoutStringForStorage(deck);
				ps.saveWorkoutDataToSharedPrefs(GameActivity.this, StatKeys.PreviousDeck, statsAsString);

				ps.updateStatsWithNewDeck(GameActivity.this, deck);

				// neuter the "next card" button
				View buttonNextCard = findViewById(R.id.done_with_this_card);
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
			System.out.println("displayCurrentCard in moveForward");
			displayCurrentCard();
		} else if(getGameState() == GameStates.BothDone) {
			this.goToStatePage();
		} 
	}

	private synchronized void startChronoIfNotRunningAndDisplayCurrentCard() 
	{
		System.out.println("Calling start chrono, " + getGameState());
		dismissModal();
		duoDeckApp.delayedService = 0;
		
		// start chrono if not running
		if (!chronoRunning) { 
			startChronometer(null); 
			deck.inGameStats.setStartDate();
		}
		// display current card
		System.out.println("displayCurrentCard in start");
		displayCurrentCard();
	}

	private void pauseChronoAndShowModal() {
		pauseChronometer(null);
		showModalMessage("Performing sync-up", false);
	}

	private void displayCurrentCard() 
	{		
		setFontSizeToMax();
		
		// get new card
		currentCard = deck.getAndPullNextCardFromDeck();
		System.out.println("current card: " + currentCard.toString() + " : " + deck.getCardsRemaining() + "/" + deck.getDeckSize());
		
		// display card
		Button doneWithCard = (Button) findViewById(R.id.done_with_this_card);
		doneWithCard.setText(currentCard.toString());

		TextView progressDisplay = (TextView) findViewById(R.id.progress_tracking);
		progressDisplay.setText(deck.getCardsRemaining() + "/" + deck.getDeckSize() );
		
		// ---debugging------------
		// show full deck
//		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
//		deckInfo.setText(deck.showDeck());
		if (currentCard.equals(Deck.finishedCard) && buddyCardIndex == 0) {
			this.goToStatePage();
		}
	}
	
	private void goToStatePage() {
		
		this.dismissModal();
		
		stopChronometer(null);
		setGameState(GameStates.Solo);
		System.out.println("finished deck");
		deck.inGameStats.duration = ((Chronometer) getChronometer()).getText().toString();

		String statsAsString = ps.makeWorkoutStringForStorage(deck);
		ps.saveWorkoutDataToSharedPrefs(GameActivity.this, StatKeys.PreviousDeck, statsAsString);

		ps.updateStatsWithNewDeck(GameActivity.this, deck);

		// neuter the "next card" button
		View buttonNextCard = findViewById(R.id.done_with_this_card);
		buttonNextCard.setOnClickListener(null);

//		setGameState(GameStates.Solo);

		// show "finished" text and provide button to stats activity
//		View buttonGotoStats = findViewById(R.id.gotoStatsFromGame);
//		buttonGotoStats.setVisibility(View.VISIBLE);

		gotoStatsFromGame();
	}
	
	public void showModalMessage(String msg, boolean showOk) {

		dismissModal();

		AlertDialog.Builder builder = new AlertDialog.Builder(GameActivity.this);
		builder.setTitle(msg);
		if (showOk) {
			builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
		}
		popupModal = builder.create();
		popupModal.show();

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
		if (getGameState() == GameStates.StartingDuoPlayAsReceiver) {
			buddyCardIndex = deck.getCardsRemaining() + 1;
	
			int[] targetOrder = duoDeckApp.getDeckOrder();
			System.out.println("Target order: " + Arrays.toString(targetOrder));
			if (targetOrder == null) {
				System.out.println("Some issue in deck order received");
				// TODO: handle this error message
			} else {
				this.deck.setOrderToMatch(targetOrder);
				this.startChronoIfNotRunningAndDisplayCurrentCard();
				this.sendShuffledOrderResponse();
	
				setGameState(GameStates.BothWorkingOut);
			}
		}
	}

	private void informSessionClosed() {
		this.showModalMessage("Workout session Lost", true);
	}

	private synchronized void setBuddyCardIndex(int buddyIndex) {
		this.buddyCardIndex = buddyIndex;
		System.out.println("Got buddy index: " + buddyIndex + ", my index: " + (deck.getCardsRemaining()) + ", status: " + getGameState());
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
		Button doneWithCard = (Button) findViewById(R.id.done_with_this_card);
		
		String before = doneWithCard.getText().toString(); 
		
		// Determine Scaled Density for later calculations
		Display dd = ((Activity) context).getWindowManager().getDefaultDisplay();
		DisplayMetrics dm = new DisplayMetrics();
		dd.getMetrics(dm);
		float m_ScaledDensity = dm.scaledDensity;

		Rect bounds = new Rect();
		Paint p = new Paint();
		
		p.setTypeface(doneWithCard.getTypeface());

		String sample = "22 pushups"; //doneWithCard.getText().toString();
		int maxFont;
		for (maxFont = 30; 
//					-bounds.top <= doneWithCard.getHeight() && 
					bounds.right <= doneWithCard.getWidth(); 
				maxFont++) {
		  p.setTextSize(maxFont);
		  p.getTextBounds(sample, 0, sample.length(), bounds);
		}
		maxFont = (int) ((maxFont - 1) / m_ScaledDensity);
		doneWithCard.setTextSize(maxFont);

		String after = doneWithCard.getText().toString();
		
		doneWithCard.setText(doneWithCard.getText());
		
		System.out.println(
				"before: " + before + "\t\t after: " + after + 
				"\t\t maxFont: " + maxFont +
				"\t\t bounds: " + bounds.toString() +
				"\t\t paint: " + p
				);
	}

}
