package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class Location
{
	@SerializedName("brewery_city")
	public final String city;

	@SerializedName("brewery_state")
	public final String state;

	@SerializedName("lat")
	public final float latitude;

	@SerializedName("lng")
	public final float longitude;

	public Location()
	{
		this.city = new String();
		this.state = new String();
		this.latitude = 0;
		this.longitude = 0;
	}
}
