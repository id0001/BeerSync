package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class CheckinResponse
{
	@SerializedName("checkin")
	public final Checkin checkin;
	
	public CheckinResponse()
	{
		this.checkin = new Checkin();
	}
}
