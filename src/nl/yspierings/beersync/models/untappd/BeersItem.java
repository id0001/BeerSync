package nl.yspierings.beersync.models.untappd;

import java.util.*;

import com.google.gson.annotations.*;

public class BeersItem
{
	@SerializedName("recent_checkin_id")
	public final String recentCheckinId;

	@SerializedName("recent_created_at")
	public final Date recentCreatedAt;

	@SerializedName("rating_score")
	public final float ratingScore;

	@SerializedName("first_had")
	public final Date firstHad;

	@SerializedName("count")
	public final int count;

	@SerializedName("beer")
	public final Beer beer;

	@SerializedName("brewery")
	public final Brewery brewery;

	public BeersItem()
	{
		this.recentCheckinId = new String();
		this.recentCreatedAt = new Date();
		this.ratingScore = 0;
		this.firstHad = new Date();
		this.count = 0;
		this.beer = new Beer();
		this.brewery = new Brewery();
	}

	public BeersItem(String name, String brewery, String style, float abv, int ibu, float rating, long lastdrank, String recentCheckinId)
	{
		this.recentCheckinId = recentCheckinId;
		this.recentCreatedAt = new Date(lastdrank);
		this.ratingScore = rating;
		this.firstHad = new Date();
		this.count = 0;
		this.beer = new Beer(name, style, abv, ibu);
		this.brewery = new Brewery(brewery);
	}

	public BeersItem(String name, String brewery, String style, float abv, int ibu, float rating, long lastdrank, String recentCheckinId, String beerLabel, float overalRating,
			String description)
	{
		this.recentCheckinId = recentCheckinId;
		this.recentCreatedAt = new Date(lastdrank);
		this.ratingScore = rating;
		this.firstHad = new Date();
		this.count = 0;
		this.beer = new Beer(name, style, abv, ibu, beerLabel, overalRating, description);
		this.brewery = new Brewery(brewery);
	}

	@Override
	public String toString()
	{
		return this.beer.name;
	}
}
