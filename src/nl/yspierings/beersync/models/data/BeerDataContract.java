package nl.yspierings.beersync.models.data;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.*;
import nl.yspierings.beersync.models.untappd.*;
import android.content.*;
import android.database.*;
import android.database.sqlite.*;
import android.provider.*;

public final class BeerDataContract
{
	public static abstract class TableEntry implements BaseColumns
	{
		public static final String TABLE_NAME = "distinctbeers";
		public static final String NAME = "name";
		public static final String STYLE = "style";
		public static final String BREWERY = "brewery";
		public static final String ABV = "abv";
		public static final String IBU = "ibu";
		public static final String RATING = "rating";
		public static final String LASTDRANK = "lastdrank";
		public static final String CHECKIN_ID = "checkin_id";
		public static final String BEER_LABEL = "beer_label";
		public static final String OVERAL_RATING = "overal_rating";
		public static final String DESCRIPTION = "description";
	}

	public static final String SQL_CREATE_ENTRIES = "CREATE TABLE " + TableEntry.TABLE_NAME + " (" + TableEntry._ID + " INTEGER PRIMARY KEY," + TableEntry.NAME + " TEXT,"
			+ TableEntry.STYLE + " TEXT," + TableEntry.BREWERY + " TEXT," + TableEntry.ABV + " FLOAT," + TableEntry.IBU + " INTEGER," + TableEntry.RATING + " FLOAT,"
			+ TableEntry.LASTDRANK + " LONG," + TableEntry.CHECKIN_ID + " TEXT,"
			+ TableEntry.BEER_LABEL + " TEXT,"
			+ TableEntry.OVERAL_RATING + " FLOAT,"
			+ TableEntry.DESCRIPTION + " TEXT)";

	public static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + TableEntry.TABLE_NAME;

	private final DatabaseHelper dbHelper;

	public BeerDataContract(Context context)
	{
		this.dbHelper = new DatabaseHelper(context);
	}

	public void closeDb()
	{
		this.dbHelper.close();
	}

	public void insert(BeersItem item)
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(TableEntry.NAME, item.beer.name);
		values.put(TableEntry.BREWERY, item.brewery.name);
		values.put(TableEntry.STYLE, item.beer.style);
		values.put(TableEntry.ABV, Float.valueOf(item.beer.abv));
		values.put(TableEntry.IBU, Integer.valueOf(item.beer.ibu));
		values.put(TableEntry.RATING, Float.valueOf(item.ratingScore));
		values.put(TableEntry.LASTDRANK, Long.valueOf(item.recentCreatedAt.getTime()));
		values.put(TableEntry.CHECKIN_ID, item.recentCheckinId);
		values.put(TableEntry.BEER_LABEL, item.beer.labelUrl);
		values.put(TableEntry.OVERAL_RATING, Float.valueOf(item.beer.ratingScore));
		values.put(TableEntry.DESCRIPTION, item.beer.description);

		long newRowId = db.insert(TableEntry.TABLE_NAME, "null", values);
		if (newRowId == -1)
		{
			XLog.debug("Error while inserting row in database: " + item.toString());
		}
	}

	public DistinctBeers getAllBeers()
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		String[] projection = { TableEntry._ID, TableEntry.NAME, TableEntry.BREWERY, TableEntry.STYLE, TableEntry.ABV, TableEntry.IBU, TableEntry.RATING, TableEntry.LASTDRANK,
				TableEntry.CHECKIN_ID };
		String sortOrder = TableEntry.NAME + " ASC";

		DistinctBeers beers = new DistinctBeers();

		Cursor c = db.query(TableEntry.TABLE_NAME, projection, null, null, null, null, sortOrder);
		if (!c.moveToFirst())
		{
			beers.beers.count = 0;
			return beers;
		}

		do
		{
			beers.beers.items.add(new BeersItem(c.getString(1), c.getString(2), c.getString(3), c.getFloat(4), c.getInt(5), c.getFloat(6), c.getLong(7), c.getString(8)));
		} while (c.moveToNext());

		beers.beers.count = beers.beers.items.size();
		return beers;
	}

	public BeersItem getBeerByCheckinId(String id)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();

		String[] projection = { TableEntry._ID, TableEntry.NAME, TableEntry.BREWERY, TableEntry.STYLE, TableEntry.ABV, TableEntry.IBU, TableEntry.RATING, TableEntry.LASTDRANK,
				TableEntry.CHECKIN_ID, TableEntry.BEER_LABEL, TableEntry.OVERAL_RATING, TableEntry.DESCRIPTION };

		Cursor c = db.query(TableEntry.TABLE_NAME, projection, TableEntry.CHECKIN_ID + "=?", new String[] { id }, null, null, null, "1");
		if (!c.moveToFirst())
		{
			return null;
		}

		return new BeersItem(c.getString(1), c.getString(2), c.getString(3), c.getFloat(4), c.getInt(5), c.getFloat(6), c.getLong(7), c.getString(8), c.getString(9), c.getFloat(10), c.getString(11));
	}

	public void clear()
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		db.execSQL("DELETE FROM " + TableEntry.TABLE_NAME);
	}
}
