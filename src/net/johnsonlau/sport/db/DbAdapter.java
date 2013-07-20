package net.johnsonlau.sport.db;

import net.johnsonlau.tool.DateTime;
import net.johnsonlau.tool.Utilities;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DbAdapter {

	private final Context mContext;
	private DbOpenHelper mDbOpenHelper;
	private SQLiteDatabase mDb;

	public DbAdapter(Context content) {
		this.mContext = content;
	}

	public DbAdapter open() throws SQLException {
		mDbOpenHelper = new DbOpenHelper(mContext);
		mDb = mDbOpenHelper.getWritableDatabase();
		return this;
	}

	public void close() {
		mDbOpenHelper.close();
	}

	// == settings
	// ===============================================================

	public Cursor fetchSettings() throws SQLException {
		Cursor cursor = mDb.query(true, DbOpenHelper.TABLE_SETTINGS,
				new String[] { DbOpenHelper.TABLE_SETTINGS_SERVICE,
						DbOpenHelper.TABLE_SETTINGS_USER_ID,
						DbOpenHelper.TABLE_SETTINGS_USER_PWD,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION1,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION2,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION3,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION4,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION5,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_COUNT_ORDER,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_ENABLE_VOICE,
						DbOpenHelper.TABLE_SETTINGS_COUNTER_END_TEXT,
						DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_MINUTES,
						DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_SECONDS,
						DbOpenHelper.TABLE_SETTINGS_TIMER_END_TEXT }, null,
				null, null, null, null, null);

		return cursor;
	}

	public boolean updateSettings(String service, String userId,
			String userPwd, int counterIteration1, int counterIteration2,
			int counterIteration3, int counterIteration4,
			int counterIteration5, String counterCountOrder,
			int counterEnableVoice, String counterEndText, int timerTimeMinute,
			int timerTimeSecond, String timerEndText) {
		ContentValues args = new ContentValues();
		args.put(DbOpenHelper.TABLE_SETTINGS_SERVICE, service);
		args.put(DbOpenHelper.TABLE_SETTINGS_USER_ID, userId);
		args.put(DbOpenHelper.TABLE_SETTINGS_USER_PWD, userPwd);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION1,
				counterIteration1);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION2,
				counterIteration2);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION3,
				counterIteration3);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION4,
				counterIteration4);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_ITERATION5,
				counterIteration5);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_COUNT_ORDER,
				counterCountOrder);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_ENABLE_VOICE,
				counterEnableVoice);
		args.put(DbOpenHelper.TABLE_SETTINGS_COUNTER_END_TEXT, counterEndText);
		args.put(DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_MINUTES,
				timerTimeMinute);
		args.put(DbOpenHelper.TABLE_SETTINGS_TIMER_TIME_SECONDS,
				timerTimeSecond);
		args.put(DbOpenHelper.TABLE_SETTINGS_TIMER_END_TEXT, timerEndText);

		return mDb.update(DbOpenHelper.TABLE_SETTINGS, args, null, null) > 0;
	}

	// == histories ==========================================================

	public Cursor fetchHistories() throws SQLException {
		return mDb.query(true, DbOpenHelper.TABLE_HISTORIES, new String[] {
				DbOpenHelper.TABLE_HISTORIES_ROWID,
				DbOpenHelper.TABLE_HISTORIES_DATE,
				DbOpenHelper.TABLE_HISTORIES_COUNTER,
				DbOpenHelper.TABLE_HISTORIES_TIMER }, null, null, null, null,
				null, null);
	}

	public void purgeHistories() {
		mDb.delete(DbOpenHelper.TABLE_HISTORIES, null, null);
	}

	public void updateHistoryCounter(String date, int counter) {
		if (Utilities.isEmptyOrNull(date)) {
			date = DateTime.getUtcDateString();
		}

		Cursor cursor = mDb.query(true, DbOpenHelper.TABLE_HISTORIES,
				new String[] { DbOpenHelper.TABLE_HISTORIES_ROWID,
						DbOpenHelper.TABLE_HISTORIES_DATE,
						DbOpenHelper.TABLE_HISTORIES_COUNTER,
						DbOpenHelper.TABLE_HISTORIES_TIMER },
				DbOpenHelper.TABLE_HISTORIES_DATE + " = ?",
				new String[] { date }, null, null, null, null);

		if (cursor.getCount() > 0) {
			// get existing counter
			cursor.moveToFirst();
			long rowId = cursor.getLong(cursor
					.getColumnIndexOrThrow(DbOpenHelper.TABLE_HISTORIES_ROWID));
			int existingCounter = cursor
					.getInt(cursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_HISTORIES_COUNTER));
			cursor.close();

			counter = counter + existingCounter;

			// update it
			ContentValues args = new ContentValues();
			args.put(DbOpenHelper.TABLE_HISTORIES_COUNTER, counter);
			mDb.update(DbOpenHelper.TABLE_HISTORIES, args,
					DbOpenHelper.TABLE_HISTORIES_ROWID + " = ?",
					new String[] { String.valueOf(rowId) });
		} else {
			ContentValues values = new ContentValues();
			values.put(DbOpenHelper.TABLE_HISTORIES_DATE, date);
			values.put(DbOpenHelper.TABLE_HISTORIES_COUNTER, counter);
			values.put(DbOpenHelper.TABLE_HISTORIES_TIMER, 0);

			mDb.insert(DbOpenHelper.TABLE_HISTORIES, null, values);
		}
	}

	public void updateHistoryTimer(String date, int timer) {
		if (Utilities.isEmptyOrNull(date)) {
			date = DateTime.getUtcDateString();
		}

		Cursor cursor = mDb.query(true, DbOpenHelper.TABLE_HISTORIES,
				new String[] { DbOpenHelper.TABLE_HISTORIES_ROWID,
						DbOpenHelper.TABLE_HISTORIES_DATE,
						DbOpenHelper.TABLE_HISTORIES_COUNTER,
						DbOpenHelper.TABLE_HISTORIES_TIMER },
				DbOpenHelper.TABLE_HISTORIES_DATE + " = ?",
				new String[] { date }, null, null, null, null);

		if (cursor.getCount() > 0) {
			// get existing counter
			cursor.moveToFirst();
			long rowId = cursor.getLong(cursor
					.getColumnIndexOrThrow(DbOpenHelper.TABLE_HISTORIES_ROWID));
			int existingTimer = cursor.getInt(cursor
					.getColumnIndexOrThrow(DbOpenHelper.TABLE_HISTORIES_TIMER));
			cursor.close();

			timer = timer + existingTimer;

			// update it
			ContentValues args = new ContentValues();
			args.put(DbOpenHelper.TABLE_HISTORIES_TIMER, timer);
			mDb.update(DbOpenHelper.TABLE_HISTORIES, args,
					DbOpenHelper.TABLE_HISTORIES_ROWID + " = ?",
					new String[] { String.valueOf(rowId) });
		} else {
			ContentValues values = new ContentValues();
			values.put(DbOpenHelper.TABLE_HISTORIES_DATE, date);
			values.put(DbOpenHelper.TABLE_HISTORIES_COUNTER, 0);
			values.put(DbOpenHelper.TABLE_HISTORIES_TIMER, timer);

			mDb.insert(DbOpenHelper.TABLE_HISTORIES, null, values);
		}
	}

	public int getHistoryCount() {
		Cursor cursor = mDb.query(true, DbOpenHelper.TABLE_HISTORIES,
				new String[] { DbOpenHelper.TABLE_HISTORIES_ROWID }, null,
				null, null, null, null, null);

		return cursor.getCount();
	}
}
