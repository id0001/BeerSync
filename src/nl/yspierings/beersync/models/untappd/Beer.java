package nl.yspierings.beersync.models.untappd;

import java.util.*;

import com.google.gson.annotations.*;

public class Beer
{
	@SerializedName("bid")
	public final int id;

	@SerializedName("beer_name")
	public final String name;

	@SerializedName("beer_label")
	public final String labelUrl;

	@SerializedName("beer_abv")
	public final float abv;

	@SerializedName("beer_ibu")
	public final int ibu;

	@SerializedName("beer_style")
	public final String style;

	@SerializedName("beer_description")
	public final String description;

	@SerializedName("created_at")
	public final Date createdAt;

	@SerializedName("auth_rating")
	public final float authRating;

	@SerializedName("wish_list")
	public final boolean wishList;

	@SerializedName("rating_score")
	public final float ratingScore;

	public Beer()
	{
		this.id = 0;
		this.name = new String();
		this.labelUrl = new String();
		this.abv = 0;
		this.ibu = 0;
		this.style = new String();
		this.description = new String();
		this.createdAt = new Date();
		this.authRating = 0;
		this.wishList = false;
		this.ratingScore = 0;
	}

	public Beer(String name, String style, float abv, int ibu)
	{
		this.id = 0;
		this.name = name;
		this.labelUrl = new String();
		this.abv = abv;
		this.ibu = ibu;
		this.style = style;
		this.description = new String();
		this.createdAt = new Date();
		this.authRating = 0;
		this.wishList = false;
		this.ratingScore = 0;
	}
	
	public Beer(String name, String style, float abv, int ibu, String label, float rating, String description)
	{
		this.id = 0;
		this.name = name;
		this.labelUrl = label;
		this.abv = abv;
		this.ibu = ibu;
		this.style = style;
		this.description = description;
		this.createdAt = new Date();
		this.authRating = 0;
		this.wishList = false;
		this.ratingScore = rating;
	}
}
