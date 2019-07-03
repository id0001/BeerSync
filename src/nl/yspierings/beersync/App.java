package nl.yspierings.beersync;

import com.testflightapp.lib.*;

import android.app.*;
import android.net.*;

public class App extends Application
{
	public static final String AUTHORITY = "nl.yspierings.beersync.provider";
	public static final String ACCOUNT_TYPE = "nl.yspierings.beersync.account";
	public static final Uri CONTENT_URI = Uri.parse("content://nl.yspierings.beersync");

	private static final boolean TESTFLIGHT_ENABLED = false;

	@Override
	public void onCreate()
	{
		super.onCreate();

		if (TESTFLIGHT_ENABLED)
		{
			TestFlight.takeOff(this, "2cb9ddc0-49a7-457f-b69c-aef9b627bd4c");
			XLog.debug("Testflight is enabled");
		}
	}
}
