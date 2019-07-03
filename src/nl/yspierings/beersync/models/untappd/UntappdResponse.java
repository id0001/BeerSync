package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class UntappdResponse<T>
{
	@SerializedName("meta")
	public Meta meta;

	@SerializedName("response")
	public T response;

	public UntappdResponse()
	{
		this.meta = new Meta();
		this.response = null;
	}
}
