package nl.yspierings.beersync.models;

import nl.yspierings.beersync.models.data.*;
import android.content.*;
import android.database.sqlite.*;

public class DatabaseHelper extends SQLiteOpenHelper
{
	public static final int DATABASE_VERSION = 3;
	public static final String DATABASE_NAME = "beersync.db";

	public DatabaseHelper(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase aDb)
	{
		aDb.execSQL(BeerDataContract.SQL_CREATE_ENTRIES);
		aDb.execSQL(UserDataContract.SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase aDb, int aOldVersion, int aNewVersion)
	{
		aDb.execSQL(BeerDataContract.SQL_DELETE_ENTRIES);
		aDb.execSQL(UserDataContract.SQL_DELETE_ENTRIES);
		onCreate(aDb);
	}

	@Override
	public void onDowngrade(SQLiteDatabase aDb, int aOldVersion, int aNewVersion)
	{
		onUpgrade(aDb, aOldVersion, aNewVersion);
	}
}
