package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class Brewery
{
	@SerializedName("brewery_id")
	public final int id;

	@SerializedName("brewery_name")
	public final String name;

	@SerializedName("brewery_label")
	public final String labelUrl;

	@SerializedName("country_name")
	public final String country;

	@SerializedName("contact")
	public final Contact contact;

	@SerializedName("location")
	public final Location location;

	@SerializedName("brewery_active")
	public final int breweryActive;

	public Brewery()
	{
		this.id = 0;
		this.name = new String();
		this.labelUrl = new String();
		this.country = new String();
		this.contact = new Contact();
		this.location = new Location();
		this.breweryActive = 0;
	}
	
	public Brewery(String name)
	{
		this.id = 0;
		this.name = name;
		this.labelUrl = new String();
		this.country = new String();
		this.contact = new Contact();
		this.location = new Location();
		this.breweryActive = 0;
	}
}
