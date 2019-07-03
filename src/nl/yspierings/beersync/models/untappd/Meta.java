package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class Meta
{
	@SerializedName("code")
	public final int code;

	@SerializedName("response_time")
	public final ResponseTime responseTime;

	@SerializedName("error_detail")
	public final String errorDetail;

	public Meta()
	{
		this.code = 0;
		this.responseTime = new ResponseTime();
		this.errorDetail = new String();
	}
}
