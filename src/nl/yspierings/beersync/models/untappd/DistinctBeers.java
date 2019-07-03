package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class DistinctBeers
{
	@SerializedName("is_search")
	public final boolean isSearch;

	@SerializedName("beers")
	public final Beers beers;

	public DistinctBeers()
	{
		this.isSearch = false;
		this.beers = new Beers();
	}
}
