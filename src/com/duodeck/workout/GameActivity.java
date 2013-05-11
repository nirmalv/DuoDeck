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
	
	private GameStates gameStates;
	// TODO: get game state
	public GameStates currentGameState = GameStates.Solo;
	
	public Deck deck;
	public Card currentCard;
	
	private int myCardIndex = 0;
	private int buddyCardIndex = 0;
	
	// chronometer
	long chronometerTimeWhenStopped = 0;
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
				startTheWorkout();
				break;
			case DuoDeckService.MSG_SESSION_CLOSED:
				informSessionClosed();
				break;
			case DuoDeckService.MSG_DONE_WITH_CARD_INDEX:
				setBuddyCardIndex(msg.arg1, msg.arg2);
				break;
			default:
				super.handleMessage(msg);
			}
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

		startChronometer(null);
		
		TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
		deck.inGameStats.setStartDate();
		currentCard = deck.drawFromDeck();
		displayOfCurrentCard.setText("this card: " + currentCard + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );
		
		TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
		deckInfo.setText(deck.showDeck());
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (mService == null)
			bindService(new Intent(this, DuoDeckService.class), mConnection, Context.BIND_AUTO_CREATE);
		
		switch (currentGameState) 
		{
			case Solo:
				break;
			case StartingDuoPlayAsSender:
				pickupGameStartingDuoPlayAsSender();
				break;
			case StartingDuoPlayAsReceiver:
				break;
			case BothWorkingOut:
				break;
			case MeWaitingBuddyWorkingOut:
				break;
			case MeWorkingOutBuddyWaiting:
				break;
			case BothDone:
				break;
		}
		
	}
	
	private void pickupGameStartingDuoPlayAsSender() {
		// TODO: get params
		// TODO: take actions
	}

	@Override 
	protected void onPause(){
        super.onPause();
		if (mService != null)
			unbindService(mConnection);
        // do we want to do anything here?
        // currently not pausing the timer because this is helpful with duodecks
    }
	
	
	public void doneWithThisCard(View view) 
	{
		switch (currentGameState) 
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
				
				// record the time that the card was finished
				
				
				TextView staticTimeValue = (TextView) findViewById(R.id.staticTimeValueDisplay);
				staticTimeValue.setText(((Chronometer) getChronometer()).getText());
				
				if (deck.getCardsRemaining() > 0) 
				{
					// get new card
					// remove the card from the deck of available options
					// display the next card
					
//					System.out.println("done with this card");
		
					// TODO: remove for final, this is for debugging only
					TextView deckInfo = (TextView) findViewById(R.id.display_deck_info);
					deckInfo.setText(deck.showDeck());

					// TODO: make the display look nicer
					TextView displayOfCurrentCard = (TextView) findViewById(R.id.display_current_card);
					currentCard = deck.drawFromDeck();
					displayOfCurrentCard.setText(currentCard + "\t\t" + deck.getCardsRemaining() + "/" + deck.getDeckSize() );
					
					deck.inGameStats.duration = ((Chronometer) getChronometer()).getText().toString();					
				} else {
					stopChronometer(null);

//					System.out.println("finished deck");
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
					
					
					
					// TODO: show stats
					// TODO: show finished time
					// TODO: show summary of deck stats (sum of values per suit)
					// TODO: show summary of deck stats (sum of values per EXERCISE)
				}
	
				break;
				
				
			// TODO: person receiving the card needs to remove the used card

			case StartingDuoPlayAsSender:
				pickupGameStartingDuoPlayAsSender();
				break;
			case StartingDuoPlayAsReceiver:
				break;
			case BothWorkingOut:
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
			case BothDone:
			default:
				break;
		}
	}
	
	

	public void gotoStatsFromGame(View view) {
//        System.out.println("goto Stats from Game");
    	// TODO: call onDestroy() of game. 
		// TODO: in onDestroy() of game, start stats
		Intent intent = new Intent(this, StatsActivity.class);
    	startActivity(intent);
	};
	
	
//	public void saveData(View view) 
//	{
//		EditText inputText = (EditText) findViewById(R.id.editText1);
//		ps.saveWorkoutDataToSharedPrefs(GameActivity.this, StatKeys.PreviousDeck, inputText.getText().toString());
//	}
//	
//	public void getData(View view)
//	{
//		EditText inputText = (EditText) findViewById(R.id.editText1);
//		TextView responseText = (TextView) findViewById(R.id.textView1);
//		String data = "";
//		System.out.println(ps.getWorkoutDataFromSharedPrefs(GameActivity.this, StatKeys.PreviousDeck));
//	}
	
	
	
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
	}
	// stop
	public void stopChronometer(View view) 
	{
	    getChronometer().stop();
    }
	// pause
	public void pauseChronometer(View view) 
	{
		Chronometer mChronometer = getChronometer();

		chronometerTimeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
		mChronometer.stop();
	}
	// resume
	public void resumeChronometer(View view) 
	{
		Chronometer mChronometer = getChronometer();
		
		mChronometer.setBase(chronometerTimeWhenStopped);
		mChronometer.start();

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
			this.startTheWorkout();
		}
	}
	
	private void startTheWorkout() {
		
	}
	
	private void informSessionClosed() {
		
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
		sendMsgToService(DuoDeckService.MSG_DONE_WITH_CARD_INDEX, this.myCardIndex, this.buddyCardIndex);
	}
	
}
