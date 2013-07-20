package net.johnsonlau.sport.activity;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import net.johnsonlau.sport.Config;
import net.johnsonlau.sport.R;
import net.johnsonlau.sport.db.DbAdapter;
import net.johnsonlau.sport.db.DbOpenHelper;
import net.johnsonlau.sport.io.SessionProxy;
import net.johnsonlau.sport.io.SportProxy;
import net.johnsonlau.tool.CmdMessage;
import net.johnsonlau.tool.Utilities;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MainActivity extends Activity implements OnInitListener,
		SensorEventListener, OnTouchListener {
	private static final int MENU_ID_UPLOAD = Menu.FIRST;
	private static final int MENU_ID_CLEAR = Menu.FIRST + 1;
	private static final int MENU_ID_SETTINGS = Menu.FIRST + 2;
	private static final int MENU_ID_ABOUT = Menu.FIRST + 3;

	private MenuItem mUploadMenuItem = null;

	private float mDownXValue;

	private Handler mMainHandler;
	Timer mTimer = null;
	TimerTask mTimerTask;
	int mTimerSeconds = 0;

	private int mCounterCurrentIteration;
	private ArrayList<Integer> mCounterIterationsFrom = new ArrayList<Integer>();
	private ArrayList<Integer> mCounterIterationsTo = new ArrayList<Integer>();

	private String mServiceUrl;
	private String mUser;
	private String mPassword;
	private ArrayList<Integer> mCounterIterationsTarget = new ArrayList<Integer>();
	private String mCounterOrder;
	private boolean mCounterEnableVoice;
	private String mCounterEndText;
	private int mTimerTimeMinutes;
	private int mTimerTimeSeconds;
	private String mTimerEndText;

	private static final int REQ_TTS_STATUS_CHECK = 0;
	private boolean mTtsIsReady = false;
	private TextToSpeech mTts;

	private SensorManager mSensorManager;
	private Sensor mProximity;

	private ViewFlipper mViewFlipper;

	private TextView mMsgTextView;
	private Button mStartButton;

	private TextView mCounterIterationTextView;
	private TextView mCounterCountTextView;
	private TextView mCounterCurrentIterationCountTextView;

	private TextView mTimerTimeTextView;

	private DbAdapter mDbAdapter;

	private String mCurrentView = "COUNTER"; // or "TIMER"
	private String mButtonText = "START"; // or "RESET"

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		checkTts();

		initMembers();
		bindEvents();

		resetCounter();
	}

	// == initialization methods
	// ===============================================================

	private void checkTts() {
		// ensure TTS is ready
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, REQ_TTS_STATUS_CHECK);
	}

	private void initMembers() {
		mDbAdapter = new DbAdapter(this).open();

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		mViewFlipper = (ViewFlipper) findViewById(R.id.main_flipper);
		mMsgTextView = (TextView) findViewById(R.id.main_msg);
		mStartButton = (Button) findViewById(R.id.main_start_btn);

		mCounterIterationTextView = (TextView) findViewById(R.id.main_counter_iteration);
		mCounterCountTextView = (TextView) findViewById(R.id.main_counter_count);
		mCounterCurrentIterationCountTextView = (TextView) findViewById(R.id.main_counter_iteration_count);

		mTimerTimeTextView = (TextView) findViewById(R.id.main_timer_time);

		mMainHandler = new Handler() {
			public void handleMessage(Message msg) {
				CmdMessage message = (CmdMessage) msg.obj;

				if (message.getCmd() == "UpdateTimerText") {
					updateTimerText(true);
				} else if (message.getCmd() == "Speak") {
					String speech = message.getValue();
					speak(speech);
				} else if (message.getCmd() == "Message") {
					String text = message.getValue();
					mMsgTextView.setText(text);
				} else if (message.getCmd() == "Toast") {
					String text = message.getValue();
					Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT)
							.show();
				} else if (message.getCmd() == "UpdateHistoryTimer") {
					mDbAdapter.updateHistoryTimer(null, mTimerTimeMinutes * 60
							+ mTimerTimeSeconds);
				} else if (message.getCmd() == "UpdateHistoryCountOnUploadMenuItem") {
					updateHistoryCountOnUploadMenuItem();
				}
			}
		};
	}

	private void bindEvents() {
		mViewFlipper.setOnTouchListener((OnTouchListener) this);

		mStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {

				if (mCurrentView.equals("COUNTER")) {
					if (mButtonText.equals("START")) {
						resetCounter();
						startSensor();
					} else {
						stopSensor();
						resetCounter();
					}
				} else {
					if (mButtonText.equals("START")) {
						resetTimerText();
						startTimer();
					} else {
						stopTimer();
						resetTimerText();
					}
				}

				mButtonText = mButtonText.equals("START") ? "RESET" : "START";
				mStartButton.setText(mButtonText);
			}
		});
	}

	// == override methods
	// =====================================================================

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		mUploadMenuItem = menu.add(0, MENU_ID_UPLOAD, 0,
				R.string.main_menu_upload);
		menu.add(0, MENU_ID_CLEAR, 1, R.string.main_menu_clear);
		menu.add(0, MENU_ID_SETTINGS, 2, R.string.main_menu_settings);
		menu.add(0, MENU_ID_ABOUT, 3, R.string.main_menu_about);

		updateHistoryCountOnUploadMenuItem();

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		mMsgTextView.setText("");

		switch (item.getItemId()) {

		case MENU_ID_UPLOAD:
			new uploadThread().start();
			return true;

		case MENU_ID_CLEAR:
			clearHistory();
			return true;

		case MENU_ID_SETTINGS:
			goToSettingsActivity();
			return true;

		case MENU_ID_ABOUT:
			goToAboutActivity();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onInit(int status) {
		// TTS Engine is initialized
		if (status == TextToSpeech.SUCCESS) {
			// set voice language
			int result = mTts.setLanguage(Locale.US);

			// if it bad voice data
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				mTtsIsReady = false;
				Log.i(Config.LOG_TAG, "TTS Language is not available.");
			} else {
				mTtsIsReady = true;
			}
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_TTS_STATUS_CHECK) {
			switch (resultCode) {
			// TTS Engine is available
			case TextToSpeech.Engine.CHECK_VOICE_DATA_PASS: {
				mTts = new TextToSpeech(this, this);
			}
				break;

			// miss or bad voice data
			case TextToSpeech.Engine.CHECK_VOICE_DATA_BAD_DATA:
			case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_DATA:
			case TextToSpeech.Engine.CHECK_VOICE_DATA_MISSING_VOLUME: {
				// install voice data
				Intent dataIntent = new Intent();
				dataIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(dataIntent);
			}
				break;

			// fail to check
			case TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL:
			default:
				Log.i(Config.LOG_TAG, "TTS is not available.");
				break;
			}
		}
	}

	protected void onDestroy() {
		super.onDestroy();

		mDbAdapter.close();

		stopTTS();
		stopTimer();
		stopSensor();
		shutdownTTS();
	}

	// == implement SensorEventListener
	// =============================================================================

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.values[0] < event.sensor.getMaximumRange()) {
			int step = mCounterOrder.equals("Ascending") ? 1 : -1;

			mCounterIterationsFrom
					.set(mCounterCurrentIteration,
							mCounterIterationsFrom
									.get(mCounterCurrentIteration) + step);

			boolean spoke = false;
			boolean allFinished = false;

			// at the end of the current iteration
			if (mCounterIterationsFrom.get(mCounterCurrentIteration) == mCounterIterationsTo
					.get(mCounterCurrentIteration)) {

				// at the end of the last iteration
				if (mCounterCurrentIteration == (mCounterIterationsTarget
						.size() - 1)) {
					// all done
					counterAllDone();
					spoke = true;
					allFinished = true;
				} else {
					// get next not zero iteration
					do {
						mCounterCurrentIteration++;
					} while (mCounterIterationsTarget
							.get(mCounterCurrentIteration) == 0
							&& mCounterCurrentIteration != (mCounterIterationsTarget
									.size() - 1)); // while the iteration count
													// is zero and not the last
													// iteration, do

					// the iteration count is zero and is the last iteration
					if (mCounterIterationsTarget.get(mCounterCurrentIteration) == 0
							&& mCounterCurrentIteration == (mCounterIterationsTarget
									.size() - 1)) {
						// all done
						counterAllDone();
						spoke = true;
						allFinished = true;
					}
				}

				// iteration is finished
				if (!spoke) {
					spoke = true;
					counterSpeak("iteration "
							+ String.valueOf(mCounterCurrentIteration)
							+ " is done.");
				}
			}

			if (!allFinished) {
				updateCounterText();

				if (!spoke) {
					counterSpeak(String.valueOf(mCounterIterationsFrom
							.get(mCounterCurrentIteration)));
				}
			}
		}
	}

	// == implement OnTouchListener
	// =============================================================================

	public boolean onTouch(View arg0, MotionEvent arg1) {
		boolean result = false;

		switch (arg1.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			mDownXValue = arg1.getX();
			result = true;
			break;
		}

		case MotionEvent.ACTION_UP: {
			float currentX = arg1.getX();

			if (Math.abs(mDownXValue - currentX) > 70) {
				if (mDownXValue < currentX) {
					mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(
							this, R.anim.push_left_in));
					mViewFlipper.showPrevious();

					afterViewChanged();
				} else {
					mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(
							this, R.anim.push_right_in));
					mViewFlipper.showNext();

					afterViewChanged();
				}

				result = true;
			}
			break;
		}
		}

		return result;
	}

	// == helpers
	// =============================================================================

	private void afterViewChanged() {
		mCurrentView = mCurrentView.equals("COUNTER") ? "TIMER" : "COUNTER";
		if (mCurrentView.equals("COUNTER")) {
			stopTimer();
			resetCounter();
		} else {
			stopSensor();
			resetTimerText();
		}

		mStartButton.setText("START");
		mButtonText = "START";
	}

	private void updateCounterText() {
		mCounterIterationTextView.setText("iteration "
				+ String.valueOf(mCounterCurrentIteration + 1));
		mCounterCountTextView.setText(String.valueOf(mCounterIterationsFrom
				.get(mCounterCurrentIteration)));
		mCounterCurrentIterationCountTextView.setText("("
				+ String.valueOf(mCounterIterationsTarget
						.get(mCounterCurrentIteration)) + ")");
	}

	private void speak(String msg) {
		if (mTtsIsReady) {
			mTts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	private void counterSpeak(String msg) {
		if (mCounterEnableVoice) {
			speak(msg);
		}
	}

	private void goToAboutActivity() {
		Intent intent = new Intent(this, AboutActivity.class);
		startActivity(intent);
	}

	private void goToSettingsActivity() {
		Intent intent = new Intent(this, SettingsActivity.class);
		startActivity(intent);
	}

	private void sendMessage(CmdMessage msg) {
		Message toMain = mMainHandler.obtainMessage();
		toMain.obj = msg;
		mMainHandler.sendMessage(toMain);
	}

	private String getTimeString(boolean speak) {
		int min = mTimerSeconds / 60;
		int sed = mTimerSeconds % 60;

		if (speak) {
			String speech = "";

			// >= 1 minutes
			if (min > 0) {
				// every minutes
				if (sed == 0) {
					speech = String.valueOf(min)
							+ (min > 1 ? " minutes left" : " minute left");
				}
				// 2 minutes 30 seconds or 1 minutes 30 seconds
				else if (sed == 30 && (min <= 2)) {
					speech = String.format("%d minute %s seconds left", min,
							sed);
				}
			}
			// < 1 minutes
			else {
				// < 10 seconds, every second
				if (sed <= 10 && sed != 0) {
					speech = String.valueOf(sed);
				}
				// < 30 seconds, every 5 seconds
				else if ((sed % 5) == 0 && sed < 30 && sed != 0) {
					speech = String.valueOf(sed) + " seconds left";
				}
				// < 1 minutes, every 10 seconds
				else if ((sed % 10) == 0 && sed > 10) {
					speech = String.valueOf(sed) + " seconds left";
				}
			}

			if (!Utilities.isEmptyOrNull(speech)) {
				speak(speech);
			}
		}

		String minString = min < 10 ? "0" + String.valueOf(min) : String
				.valueOf(min);
		String sedString = sed < 10 ? "0" + String.valueOf(sed) : String
				.valueOf(sed);
		return String.format("%s:%s", minString, sedString);
	}

	public void startTimer() {
		stopTimer();
		resetTimerText();

		mTimerTask = new TimerTask() {
			public void run() {
				mTimerSeconds--;
				if (mTimerSeconds == 0) {
					stopTimer();
					sendMessage(new CmdMessage("Speak", mTimerEndText));
					sendMessage(new CmdMessage("UpdateHistoryTimer", null));
					sendMessage(new CmdMessage(
							"UpdateHistoryCountOnUploadMenuItem", null));
				}
				sendMessage(new CmdMessage("UpdateTimerText", null));
			}
		};

		mTimer = new Timer();
		mTimer.schedule(mTimerTask, 1000, 1000);

		sendMessage(new CmdMessage("Speak", "Ready, go!"));
	}

	private void stopTimer() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer.purge();
		}
	}

	private void resetTimerText() {
		getSettings();

		mTimerSeconds = mTimerTimeMinutes * 60 + mTimerTimeSeconds;
		updateTimerText(false);
	}

	private void startSensor() {
		stopSensor();
		mSensorManager.registerListener(MainActivity.this, mProximity,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	private void stopSensor() {
		mSensorManager.unregisterListener(MainActivity.this);
	}

	private void updateTimerText(boolean speak) {
		mTimerTimeTextView.setText(getTimeString(speak));
	}

	private void stopTTS() {
		if (mTts != null) {
			mTts.stop();
		}
	}

	private void shutdownTTS() {
		if (mTts != null) {
			mTts.shutdown();
		}
	}

	@SuppressWarnings("unchecked")
	private void resetCounter() {
		getSettings();

		mCounterCurrentIteration = 0;

		if (mCounterOrder.equals("Ascending")) {
			mCounterIterationsFrom.clear();
			for (int i = 0; i < mCounterIterationsTarget.size(); i++) {
				mCounterIterationsFrom.add(0);
			}

			mCounterIterationsTo = (ArrayList<Integer>) (mCounterIterationsTarget
					.clone());
		} else {
			mCounterIterationsFrom = (ArrayList<Integer>) (mCounterIterationsTarget
					.clone());

			mCounterIterationsTo.clear();
			for (int i = 0; i < mCounterIterationsTarget.size(); i++) {
				mCounterIterationsTo.add(0);
			}
		}

		// current iteration count is zero
		if (mCounterIterationsFrom.get(mCounterCurrentIteration) == mCounterIterationsTo
				.get(mCounterCurrentIteration)) {
			// get next not zero iteration
			do {
				mCounterCurrentIteration++;
			}
			// while the iteration count is zero, do
			while (mCounterIterationsTarget.get(mCounterCurrentIteration) == 0);
		}

		updateCounterText();
	}

	private void counterAllDone() {
		stopSensor();
		counterSpeak(mCounterEndText);

		int totalIterations = 0;
		int totalCount = 0;
		for (int i = 0; i < mCounterIterationsTarget.size(); i++) {
			int count = mCounterIterationsTarget.get(i);
			if (count > 0) {
				totalIterations++;
				totalCount += count;
			}
		}

		mCounterIterationTextView.setText(String.valueOf(totalIterations)
				+ " iterations");
		mCounterCountTextView.setText(String.valueOf(totalCount));
		mCounterCurrentIterationCountTextView.setText("");

		mDbAdapter.updateHistoryCounter(null, totalCount);
		updateHistoryCountOnUploadMenuItem();
	}

	private void getSettings() {
		try {
			Cursor settingsCursor = mDbAdapter.fetchSettings();
			settingsCursor.moveToFirst();
			startManagingCursor(settingsCursor);

			mServiceUrl = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVICE));
			mUser = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_USER_ID));
			mPassword = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_USER_PWD));

			mCounterIterationsTarget.clear();
			mCounterIterationsTarget
					.add(Integer.parseInt(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION1))));
			mCounterIterationsTarget
					.add(Integer.parseInt(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION2))));
			mCounterIterationsTarget
					.add(Integer.parseInt(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION3))));
			mCounterIterationsTarget
					.add(Integer.parseInt(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION4))));
			mCounterIterationsTarget
					.add(Integer.parseInt(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION5))));

			mCounterOrder = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_COUNT_ORDER));
			mCounterEnableVoice = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ENABLE_VOICE)) > 0 ? true
					: false;
			mCounterEndText = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_END_TEXT));
			mTimerTimeMinutes = Integer
					.parseInt(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_MINUTES)));
			mTimerTimeSeconds = Integer
					.parseInt(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_SECONDS)));
			mTimerEndText = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TIMER_END_TEXT));

		} catch (SQLException ex) {
			mMsgTextView.setText("Load settings error!");
		}
	}

	private void clearHistory() {
		new AlertDialog.Builder(MainActivity.this)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.setMessage("Are you sure you want to delete all history data?")
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								mDbAdapter.purgeHistories();
								updateHistoryCountOnUploadMenuItem();

								String text = "History is cleared.";
								Toast.makeText(MainActivity.this, text,
										Toast.LENGTH_SHORT).show();
							}

						}).setNegativeButton("No", null).show();
	}

	private void updateHistoryCountOnUploadMenuItem() {
		int historyCount = mDbAdapter.getHistoryCount();
		String text = this.getResources().getString(R.string.main_menu_upload);
		String title = text + " (" + String.valueOf(historyCount) + ")";

		if (mUploadMenuItem != null) {
			mUploadMenuItem.setTitle(title);
		}
	}

	private void updateHistories() {
		try {
			boolean success = true;

			getSettings();

			// -- create session
			// ----------------------------------------------------------
			sendMessage(new CmdMessage("Message", "Start creating session..."));

			String sessionId = "";
			try {
				sessionId = SessionProxy.createSession(mServiceUrl, mUser,
						mPassword);
			} catch (Exception ex) {
				Log.i(Config.LOG_TAG,
						"createSession exception: " + ex.getMessage());
				sendMessage(new CmdMessage("Message", "Authorization error."));
				success = false;
			}
			if (sessionId == "") {
				sendMessage(new CmdMessage("Message", "Authorization error."));
				success = false;
			}

			Log.i(Config.LOG_TAG, "sessionId: " + sessionId);

			// -- upload history to remote
			// -------------------------------------------------------------
			if (success) {
				sendMessage(new CmdMessage("Message",
						"Start uploading histories to remote..."));
				success = uploadHistoriesToRemote(sessionId);

			}
			if (success) // purge local records
			{
				Log.i(Config.LOG_TAG, "success uploading histories to remote!");
				mDbAdapter.purgeHistories();
			}

			// -- finish
			// ------------------------------------------------------------
			if (success) {
				sendMessage(new CmdMessage("Message", "Upload succeeded."));

				String text = "History upload succeeded.";
				sendMessage(new CmdMessage("Toast", text));
			}

		} catch (Exception ex) {
			Log.i(Config.LOG_TAG, "upload exception: " + ex.getMessage());
			sendMessage(new CmdMessage("Message", "Upload error."));
		}
	}

	private boolean uploadHistoriesToRemote(String sessionId) {
		boolean result = true;

		try {
			result = SportProxy.uploadHistoriesToRemote(mServiceUrl, sessionId,
					mDbAdapter);
		} catch (Exception ex) {
			Log.i(Config.LOG_TAG,
					"uploadHistoriesToRemote exception: " + ex.getMessage());
			sendMessage(new CmdMessage("Message",
					"upload histories to remote error."));
			result = false;
		}

		return result;
	}

	// == new threads
	// ==========================================================================

	private class uploadThread extends Thread {
		public void run() {
			updateHistories();

			sendMessage(new CmdMessage("UpdateHistoryCountOnUploadMenuItem",
					null));
		}
	}
}
