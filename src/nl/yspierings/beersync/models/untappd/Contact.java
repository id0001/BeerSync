package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class Contact
{
	@SerializedName("twitter")
	public final String twitter;

	@SerializedName("facebook")
	public final String facebook;

	@SerializedName("url")
	public final String url;

	public Contact()
	{
		this.twitter = new String();
		this.facebook = new String();
		this.url = new String();
	}
}
