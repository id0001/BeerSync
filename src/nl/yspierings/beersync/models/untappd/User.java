package nl.yspierings.beersync.models.untappd;

import com.google.gson.annotations.*;

public class User
{
	@SerializedName("uid")
	public final int uid;

	@SerializedName("id")
	public final int id;

	@SerializedName("user_name")
	public final String username;

	@SerializedName("first_name")
	public final String firstName;

	@SerializedName("last_name")
	public final String lastname;

	@SerializedName("stats")
	public Stats stats;

	public User()
	{
		this.uid = 0;
		this.id = 0;
		this.username = new String();
		this.firstName = new String();
		this.lastname = new String();
		this.stats = new Stats();
	}
}
