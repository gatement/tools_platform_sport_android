package net.johnsonlau.sport.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.johnsonlau.sport.Config;
import net.johnsonlau.sport.db.DbAdapter;
import net.johnsonlau.sport.db.DbOpenHelper;
import net.johnsonlau.tool.HttpRequest;
import net.johnsonlau.tool.Utilities;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.util.Log;

public class SportProxy {

	public static boolean uploadHistoriesToRemote(String serviceUrl,
			String sessionId, DbAdapter dbAdapter) throws IOException,
			JSONException {

		boolean result = false;

		// -- get histories ----------------------------------------------
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
		String data = "";
		Cursor cursor = dbAdapter.fetchHistories();
		while (cursor.moveToNext()) {
			String date = cursor.getString(cursor
					.getColumnIndexOrThrow(DbOpenHelper.TABLE_HISTORIES_DATE));
			int counter = cursor
					.getInt(cursor
							.getColumnIndexOrThrow(DbOpenHelper.TABLE_HISTORIES_COUNTER));
			int timer = cursor.getInt(cursor
					.getColumnIndexOrThrow(DbOpenHelper.TABLE_HISTORIES_TIMER));

			if (Utilities.isEmptyOrNull(data)) {
				data = date + "," + String.valueOf(counter) + ","
						+ String.valueOf(timer);
			} else {
				data += ";" + date + "," + String.valueOf(counter) + ","
						+ String.valueOf(timer);
			}
		}
		cursor.close();
		nameValuePairs.add(new BasicNameValuePair("data", data));

		Log.i(Config.LOG_TAG, "uploading: " + data);

		// -- start uploading --------------------------------------------
		if (!Utilities.isEmptyOrNull(data)) // in case no history to upload
		{
			String url = serviceUrl + Config.URL_SPORT_UPLOAD;
			String cookie = Config.SESSION_COOKIE_NAME + "=" + sessionId;
			String response = HttpRequest.doPost(url, nameValuePairs, cookie);

			Log.i(Config.LOG_TAG, "uploadHistoriesToRemote return: " + response);

			JSONObject obj = new JSONObject(response);
			result = obj.getBoolean("success");
		} else {
			result = true;
		}

		return result;
	}
}
