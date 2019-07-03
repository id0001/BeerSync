package nl.yspierings.beersync.models;

import android.content.*;
import android.database.*;
import android.net.*;

public class StubProvider extends ContentProvider
{

	@Override
	public int delete(Uri aUri, String aSelection, String[] aSelectionArgs)
	{
		return 0;
	}

	@Override
	public String getType(Uri aUri)
	{
		return new String();
	}

	@Override
	public Uri insert(Uri aUri, ContentValues aValues)
	{
		return null;
	}

	@Override
	public boolean onCreate()
	{
		return true;
	}

	@Override
	public Cursor query(Uri aUri, String[] aProjection, String aSelection, String[] aSelectionArgs, String aSortOrder)
	{
		return null;
	}

	@Override
	public int update(Uri aUri, ContentValues aValues, String aSelection, String[] aSelectionArgs)
	{
		return 0;
	}

}
