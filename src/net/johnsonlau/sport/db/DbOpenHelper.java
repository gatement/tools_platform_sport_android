package net.johnsonlau.sport.db;

import net.johnsonlau.sport.Config;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpenHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "data";
	private static final int DATABASE_VERSION = 1;

	// == table settings =============================================
	public static final String TABLE_SETTINGS = "settings";
	public static final String TABLE_SETTINGS_ROWID = "_id";
	public static final String TABLE_SETTINGS_SERVICE = "service";
	public static final String TABLE_SETTINGS_USER_ID = "user_id";
	public static final String TABLE_SETTINGS_USER_PWD = "user_pwd";
	public static final String TABLE_SETTINGS_COUNTER_ITERATION1 = "counter_iteration1";
	public static final String TABLE_SETTINGS_COUNTER_ITERATION2 = "counter_iteration2";
	public static final String TABLE_SETTINGS_COUNTER_ITERATION3 = "counter_iteration3";
	public static final String TABLE_SETTINGS_COUNTER_ITERATION4 = "counter_iteration4";
	public static final String TABLE_SETTINGS_COUNTER_ITERATION5 = "counter_iteration5";
	public static final String TABLE_SETTINGS_COUNTER_COUNT_ORDER = "counter_count_order";
	public static final String TABLE_SETTINGS_COUNTER_ENABLE_VOICE = "counter_enable_voice";
	public static final String TABLE_SETTINGS_COUNTER_END_TEXT = "counter_end_text";
	public static final String TABLE_SETTINGS_TIMER_TIME_MINUTES = "timer_time_minutes";
	public static final String TABLE_SETTINGS_TIMER_TIME_SECONDS = "timer_time_seconds";
	public static final String TABLE_SETTINGS_TIMER_END_TEXT = "timer_end_text";
	
	private static final String TABLE_SETTINGS_CREATE = "CREATE TABLE "
			+ TABLE_SETTINGS 
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT"
			+ ", service TEXT NOT NULL" 
			+ ", user_id TEXT NOT NULL"
			+ ", user_pwd TEXT NOT NULL"
			+ ", counter_iteration1 INTEGER NOT NULL"
			+ ", counter_iteration2 INTEGER NOT NULL"
			+ ", counter_iteration3 INTEGER NOT NULL"
			+ ", counter_iteration4 INTEGER NOT NULL"
			+ ", counter_iteration5 INTEGER NOT NULL"
			+ ", counter_count_order TEXT NOT NULL" // 'Ascending' or 'Descending'
			+ ", counter_enable_voice INTEGER NOT NULL" // enabled: 1, disabled: 0
			+ ", counter_end_text TEXT NOT NULL"
			+ ", timer_time_minutes INTEGER NOT NULL"
			+ ", timer_time_seconds INTEGER NOT NULL"
			+ ", timer_end_text TEXT NOT NULL);";
	private static final String TABLE_SETTINGS_INITIALIZE = "INSERT INTO settings "
			+ "(service, user_id, user_pwd, counter_iteration1, counter_iteration2, counter_iteration3, counter_iteration4, counter_iteration5, counter_count_order, counter_enable_voice, counter_end_text, timer_time_minutes, timer_time_seconds, timer_end_text)"
			+ " VALUES("
			+ "'https://tools.johnson.uicp.net'" //service
			+ ", 'id'" //user_id
			+ ", 'pwd'"//user_pwd
			+ ", 20"//counter_iteration1
			+ ", 17"//counter_iteration2
			+ ", 13"//counter_iteration3
			+ ", 0"//counter_iteration4
			+ ", 0"//counter_iteration5
			+ ", 'Ascending'"//counter_count_order
			+ ", 1"//counter_enable_voice
			+ ", 'well done, strong man!'"//counter_end_text
			+ ", 8"//timer_time_minute
			+ ", 0"//timer_time_second
			+ ", 'you made it. Have a good day!'"//timer_end_text
			+ ");";	
	

	// == table histories =============================================
	public static final String TABLE_HISTORIES = "histories";
	public static final String TABLE_HISTORIES_ROWID = "_id";
	public static final String TABLE_HISTORIES_DATE = "date";
	public static final String TABLE_HISTORIES_COUNTER = "counter";
	public static final String TABLE_HISTORIES_TIMER = "timer";
	
	private static final String TABLE_HISTORIES_CREATE = "CREATE TABLE "
			+ TABLE_HISTORIES 
			+ "(_id INTEGER PRIMARY KEY AUTOINCREMENT"
			+ ", date TEXT NOT NULL" 
			+ ", counter INTEGER NOT NULL"
			+ ", timer INTEGER NOT NULL);";

	// == ovreride methods =========================================

	DbOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(TABLE_SETTINGS_CREATE);
		db.execSQL(TABLE_SETTINGS_INITIALIZE);
		db.execSQL(TABLE_HISTORIES_CREATE);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion == 2) {
			upgradeToVersion2();
		}
		if (oldVersion == 2 && newVersion == 3) {
			upgradeToVersion3();
		}

		Log.i(Config.LOG_TAG, "Upgraded database " + DATABASE_NAME + " from version "
				+ oldVersion + " to " + newVersion);
	}

	private void upgradeToVersion2() {
		// do upgrading job
	}

	private void upgradeToVersion3() {
		// do upgrading job
	}
}
