package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class Stats
{
	@SerializedName("total_beers")
	public final int totalBeers;

	public Stats()
	{
		this.totalBeers = 0;
	}
}
