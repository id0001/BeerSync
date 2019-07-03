package nl.yspierings.beersync.models.data;

import nl.yspierings.beersync.models.*;
import nl.yspierings.beersync.models.untappd.*;
import android.content.*;
import android.database.sqlite.*;
import android.provider.*;

public final class UserDataContract
{
	public static abstract class Schema implements BaseColumns
	{
		public static final String TABLE_NAME = "user";
		public static final String UID = "uid";
		public static final String NAME = "name";
	}

	public static String SQL_CREATE_ENTRIES = "CREATE TABLE " + Schema.TABLE_NAME + " (" + Schema._ID + " INTEGER PRIMARY KEY," + Schema.UID + " INTEGER," + Schema.NAME + " TEXT)";

	public static String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + Schema.TABLE_NAME;

	private final DatabaseHelper dbHelper;

	public UserDataContract(Context context)
	{
		this.dbHelper = new DatabaseHelper(context);
	}

	public void closeDb() {
		this.dbHelper.close();
	}

	public void clear()
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		db.execSQL("DELETE FROM " + Schema.TABLE_NAME);
	}

	public void insert(User user)
	{
		this.clear();
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(Schema.UID, Integer.valueOf(user.uid));
		values.put(Schema.NAME, user.username);
		
		
		db.insert(Schema.TABLE_NAME, "null", values);
	}
}
