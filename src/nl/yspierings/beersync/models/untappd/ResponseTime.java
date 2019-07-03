package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class ResponseTime
{
	@SerializedName("time")
	public final float time;

	@SerializedName("measure")
	public final String measure;

	public ResponseTime()
	{
		this.time = 0;
		this.measure = new String();
	}
}
