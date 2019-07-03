package nl.yspierings.beersync.models.untappd;

import java.util.*;

import com.google.gson.annotations.*;

public class Checkin
{
	@SerializedName("checkin_id")
	public final String checkinId;

	@SerializedName("created_at")
	public final Date createdAt;

	@SerializedName("rating_score")
	public final float ratingScore;

	@SerializedName("checkin_comment")
	public final String checkinComment;

	public Checkin()
	{
		this.checkinId = new String();
		this.createdAt = new Date();
		this.ratingScore = 0f;
		this.checkinComment = new String();
	}

	@Override
	public String toString()
	{
		return String.format("[%s], [%s], [%s], [%s]", this.checkinId, this.createdAt.toString(), "" + this.ratingScore, this.checkinComment);
	}
}
