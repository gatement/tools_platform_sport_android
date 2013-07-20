package net.johnsonlau.sport.activity;

import net.johnsonlau.sport.R;
import net.johnsonlau.sport.db.DbAdapter;
import net.johnsonlau.sport.db.DbOpenHelper;
import net.johnsonlau.tool.Utilities;
import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {
	private DbAdapter mDbAdapter;

	private Button mSaveButton;
	private TextView mMsgTextView;

	private EditText mServiceUrlEditText;
	private EditText mUserEditText;
	private EditText mPasswordEditText;

	private EditText mCounterIteration1EditText;
	private EditText mCounterIteration2EditText;
	private EditText mCounterIteration3EditText;
	private EditText mCounterIteration4EditText;
	private EditText mCounterIteration5EditText;
	private Spinner mCounterOrderSpinner;
	private CheckBox mCounterEnableVoiceCheckBox;
	private EditText mCounterEndTextEditText;

	private EditText mTimerTimeMinutesEditText;
	private EditText mTimerTimeSecondsEditText;
	private EditText mTimerEndTextEditText;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		initMembers();
		bindEvents();
		populateData();
	}

	// == initialization methods
	// ===============================================================

	private void initMembers() {
		mDbAdapter = new DbAdapter(this).open();

		mSaveButton = (Button) findViewById(R.id.settings_save);
		mMsgTextView = (TextView) findViewById(R.id.settings_msg);

		mServiceUrlEditText = (EditText) findViewById(R.id.settings_service_url);
		mUserEditText = (EditText) findViewById(R.id.settings_user);
		mPasswordEditText = (EditText) findViewById(R.id.settings_password);

		mCounterIteration1EditText = (EditText) findViewById(R.id.settings_counter_iteration1);
		mCounterIteration2EditText = (EditText) findViewById(R.id.settings_counter_iteration2);
		mCounterIteration3EditText = (EditText) findViewById(R.id.settings_counter_iteration3);
		mCounterIteration4EditText = (EditText) findViewById(R.id.settings_counter_iteration4);
		mCounterIteration5EditText = (EditText) findViewById(R.id.settings_counter_iteration5);
		mCounterOrderSpinner = (Spinner) findViewById(R.id.settings_counter_order);
		mCounterEnableVoiceCheckBox = (CheckBox) findViewById(R.id.settings_counter_enable_voice);
		mCounterEndTextEditText = (EditText) findViewById(R.id.settings_counter_end_text);

		mTimerTimeMinutesEditText = (EditText) findViewById(R.id.settings_timer_time_minutes);
		mTimerTimeSecondsEditText = (EditText) findViewById(R.id.settings_timer_time_seconds);
		mTimerEndTextEditText = (EditText) findViewById(R.id.settings_timer_end_text);

		String[] orderings = { "Ascending", "Descending" };
		ArrayAdapter<String> counterSpinnerAdapter = new ArrayAdapter<String>(
				this, android.R.layout.simple_spinner_item, orderings);
		mCounterOrderSpinner.setAdapter(counterSpinnerAdapter);
	}

	private void bindEvents() {
		this.mSaveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// get setting values
				String service = mServiceUrlEditText.getText().toString()
						.trim();
				String user = mUserEditText.getText().toString().trim();
				String password = mPasswordEditText.getText().toString().trim();

				String counterIteration1 = mCounterIteration1EditText.getText()
						.toString().trim();
				String counterIteration2 = mCounterIteration2EditText.getText()
						.toString().trim();
				String counterIteration3 = mCounterIteration3EditText.getText()
						.toString().trim();
				String counterIteration4 = mCounterIteration4EditText.getText()
						.toString().trim();
				String counterIteration5 = mCounterIteration5EditText.getText()
						.toString().trim();
				String counterOrder = mCounterOrderSpinner.getSelectedItem()
						.toString();
				int counterEnableVoice = mCounterEnableVoiceCheckBox
						.isChecked() ? 1 : 0;
				String counterEndText = mCounterEndTextEditText.getText()
						.toString().trim();

				String timerTimeMinutes = mTimerTimeMinutesEditText.getText()
						.toString().trim();
				String timerTimeSeconds = mTimerTimeSecondsEditText.getText()
						.toString().trim();
				String timerEndText = mTimerEndTextEditText.getText()
						.toString().trim();

				if (Utilities.isEmptyOrNull(counterIteration1)) {
					counterIteration1 = "0";
				}
				if (Utilities.isEmptyOrNull(counterIteration2)) {
					counterIteration2 = "0";
				}
				if (Utilities.isEmptyOrNull(counterIteration3)) {
					counterIteration3 = "0";
				}
				if (Utilities.isEmptyOrNull(counterIteration4)) {
					counterIteration4 = "0";
				}
				if (Utilities.isEmptyOrNull(counterIteration5)) {
					counterIteration5 = "0";
				}
				if (Utilities.isEmptyOrNull(timerTimeMinutes)) {
					timerTimeMinutes = "0";
				}
				if (Utilities.isEmptyOrNull(timerTimeSeconds)) {
					timerTimeSeconds = "0";
				}

				if (Utilities.isEmptyOrNull(service)
						|| Utilities.isEmptyOrNull(user)
						|| Utilities.isEmptyOrNull(password)
						|| Utilities.isEmptyOrNull(counterEndText)
						|| Utilities.isEmptyOrNull(timerEndText)) {
					mMsgTextView.setText("Please fill out all requred fields.");
					return;
				} else if (counterIteration1.equals("0")
						&& counterIteration2.equals("0")
						&& counterIteration3.equals("0")
						&& counterIteration4.equals("0")
						&& counterIteration5.equals("0")) {
					mMsgTextView
							.setText("Please fill out at lease one iteration count.");
					return;
				} else if (timerTimeMinutes.equals("0")
						&& timerTimeSeconds.equals("0")) {
					mMsgTextView
							.setText("Please fill out at lease one timer time.");
					return;
				}

				// save settings
				mDbAdapter.updateSettings(service, user, password,
						Integer.parseInt(counterIteration1),
						Integer.parseInt(counterIteration2),
						Integer.parseInt(counterIteration3),
						Integer.parseInt(counterIteration4),
						Integer.parseInt(counterIteration5), counterOrder,
						counterEnableVoice, counterEndText,
						Integer.parseInt(timerTimeMinutes),
						Integer.parseInt(timerTimeSeconds), timerEndText);

				goToMainActivity();
			}
		});
	}

	private void populateData() {
		try {
			Cursor settingsCursor = mDbAdapter.fetchSettings();
			settingsCursor.moveToFirst();
			startManagingCursor(settingsCursor);

			mServiceUrlEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_SERVICE)));
			mUserEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_USER_ID)));
			mPasswordEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_USER_PWD)));

			mCounterIteration1EditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION1)));
			mCounterIteration2EditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION2)));
			mCounterIteration3EditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION3)));
			mCounterIteration4EditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION4)));
			mCounterIteration5EditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION5)));

			String ordering = settingsCursor
					.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_COUNT_ORDER));
			mCounterOrderSpinner.setSelection(ordering.equals("Ascending") ? 0
					: 1);

			int enableVoice = settingsCursor
					.getInt(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_ENABLE_VOICE));
			mCounterEnableVoiceCheckBox.setChecked(enableVoice == 1);

			mCounterEndTextEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_COUNTER_END_TEXT)));

			mTimerTimeMinutesEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_MINUTES)));
			mTimerTimeSecondsEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_SECONDS)));
			mTimerEndTextEditText
					.setText(settingsCursor.getString(settingsCursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_SETTINGS_TIMER_END_TEXT)));

		} catch (SQLException ex) {
			mMsgTextView.setText("Load settings error!");
		}
	}

	// == override methods
	// =====================================================================
	protected void onDestroy() {
		super.onDestroy();
		mDbAdapter.close();
	}

	// == helpers
	// ============================================================================

	private void goToMainActivity() {
		setResult(RESULT_OK);
		finish();
	}
}