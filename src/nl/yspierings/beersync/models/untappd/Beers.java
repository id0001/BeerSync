package nl.yspierings.beersync.models.untappd;

import java.util.*;

import com.google.gson.annotations.*;

public class Beers
{
	@SerializedName("sort")
	public final String sort;

	@SerializedName("sort_english")
	public final String sortEnglish;

	@SerializedName("count")
	public int count;
	
	@SerializedName("items")
	public List<BeersItem> items;

	public Beers()
	{
		this.sort = new String();
		this.sortEnglish = new String();
		this.count = 0;
		this.items = new ArrayList<BeersItem>();
	}
}
