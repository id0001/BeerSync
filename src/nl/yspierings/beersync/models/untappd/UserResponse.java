package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class UserResponse
{
	@SerializedName("user")
	public final User user;
	
	public UserResponse()
	{
		this.user = new User();
	}
}
