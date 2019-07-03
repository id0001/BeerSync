package nl.yspierings.beersync.models;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.untappd.*;

public class Untappd
{
	private static final String UNTAPPD_BASEURL = "https://api.untappd.com/v4/";

	public static final String CLIENT_ID = "<REDACTED>";
	public static final String CLIENT_SECRET = "<REDACTED>";
	public static final String REDIRECT_URL = "http://nl.yspierings.beersync";
	
	public static final String OATH_LOGIN_URL = "https://untappd.com/oauth/authenticate/?client_id=" + CLIENT_ID + "&response_type=code&redirect_url=http://nl.yspierings.beersync";
	
	public static final String GET_AUTH_TOKEN_URL = "https://untappd.com/oauth/authorize/?client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET
			+ "&response_type=code&redirect_url=" + REDIRECT_URL + "&code=";

	public static AsyncUntappdCall<UserResponse> getMe(String authToken)
	{
		String url = UNTAPPD_BASEURL + "user/info/?access_token=" + authToken + "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;
		XLog.debug(url);
		return new AsyncUntappdCall<UserResponse>(url, UserResponse.class);
	}
	
	public static String getDistinctBeersUrl(String authToken, String username, int offset)
	{
		String url = UNTAPPD_BASEURL + "user/beers/" + username + "?access_token=" + authToken + "&client_id=" + CLIENT_ID
				+ "&client_secret=" + CLIENT_SECRET + "&offset=" + offset;
		return url;
	}
	
	public static AsyncUntappdCall<CheckinResponse> getCheckinDetails(String authToken, String checkinId) {
		String url = UNTAPPD_BASEURL + "checkin/view/" + checkinId + "?access_token=" + authToken + "&client_id=" + CLIENT_ID + "&client_secret=" + CLIENT_SECRET;
		return new AsyncUntappdCall<CheckinResponse>(url, CheckinResponse.class);
	}
}
