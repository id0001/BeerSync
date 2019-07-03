package nl.yspierings.beersync.services;

import java.io.*;
import java.net.*;
import com.google.gson.*;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.*;
import nl.yspierings.beersync.models.data.*;
import nl.yspierings.beersync.models.untappd.*;
import android.accounts.*;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;

public class SyncAdapterService extends Service
{
	private static SyncAdapterImpl syncAdapter = null;
	private static final Object syncAdapterLock = new Object();

	@Override
	public void onCreate()
	{
		super.onCreate();

		synchronized (syncAdapterLock)
		{
			if (syncAdapter == null)
			{
				syncAdapter = new SyncAdapterImpl(this);
			}
		}
	}

	@Override
	public IBinder onBind(Intent aIntent)
	{
		return syncAdapter.getSyncAdapterBinder();
	}

	private final class SyncAdapterImpl extends AbstractThreadedSyncAdapter
	{
		private final ContentResolver contentResolver;

		public SyncAdapterImpl(Context context)
		{
			super(context, true);
			this.contentResolver = context.getContentResolver();
		}

		@Override
		public void onPerformSync(Account aAccount, Bundle aExtras, String aAuthority, ContentProviderClient aProvider, SyncResult aSyncResult)
		{
			XLog.debug("Begin sync");
			this.notifyObserversSyncStarted();
			AccountManager am = AccountManager.get(this.getContext());
			String username = aAccount.name;
			String authToken = am.getPassword(aAccount);
			if (authToken == null || username == null)
			{
				return;
			}

			if (!this.isSyncAllowed())
			{
				return;
			}

			DistinctBeers distinctBeers = this.downloadDistinctBeers(authToken, username);
			this.saveDistinctBeersToDatabase(distinctBeers);
			
			this.notifyObservers();
			XLog.debug("Done syncing");
		}

		private boolean isSyncAllowed()
		{
			if (this.syncViaWifiOnly())
			{
				ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				if (!mWifi.isConnected())
				{
					XLog.debug("App is not running on wifi (Wifi-Only is on)");
					return false;
				}
			}

			return true;
		}

		private boolean syncViaWifiOnly()
		{
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			return prefs.getBoolean("settings.sync.wifionly", false);
		}

		private DistinctBeers downloadDistinctBeers(String authToken, String username)
		{
			DistinctBeers distinctBeers = null;

			int count = 0;
			int offset = 0;
			do
			{
				DistinctBeers beers = this.downloadBatchOfBeers(authToken, username, offset);
				if (beers == null)
				{
					return null;
				}

				count = beers.beers.count;
				offset += count;

				if (count == 0)
				{
					break;
				}

				distinctBeers = this.addBeersToDistinctBeersList(distinctBeers, beers);

			} while (count > 0);

			return distinctBeers;
		}

		private DistinctBeers addBeersToDistinctBeersList(DistinctBeers distinctBeers, DistinctBeers newBeers)
		{
			if (distinctBeers == null)
			{
				return newBeers;
			}
			else
			{
				distinctBeers.beers.items.addAll(newBeers.beers.items);
				distinctBeers.beers.count += newBeers.beers.count;
				XLog.debug("" + newBeers.beers.count + " beers added.");
			}

			return distinctBeers;
		}

		private void saveDistinctBeersToDatabase(DistinctBeers beers)
		{
			if (beers == null)
			{
				return;
			}

			BeerDataContract contract = new BeerDataContract(getContext());
			contract.clear();
			for (BeersItem entry : beers.beers.items)
			{
				contract.insert(entry);
			}

			contract.closeDb();
		}

		private void notifyObservers()
		{
			this.getContext().getContentResolver().notifyChange(Uri.parse("content://nl.yspierings.beersync"), null, false);
		}
		
		private void notifyObserversSyncStarted()
		{
			this.getContext().getContentResolver().notifyChange(Uri.parse("content://nl.yspierings.beersync/syncstarted"), null, false);
		}

		private DistinctBeers downloadBatchOfBeers(String authToken, String username, int offset)
		{
			String url = Untappd.getDistinctBeersUrl(authToken, username, offset);

			try
			{
				String jsonResponse = this.getHttpResponse(url);
				UntappdResponse<DistinctBeers> result = this.serializeObject(jsonResponse);

				if (result == null || result.meta == null)
				{
					throw new Exception("Response malformed.");
				}

				if (result.meta.code != 200)
				{
					throw new Exception(result.meta.errorDetail);
				}

				return this.serializeObject(jsonResponse).response;
			}
			catch (MalformedURLException ex)
			{
				ex.printStackTrace();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			return null;
		}

		private String getHttpResponse(String url) throws MalformedURLException, IOException
		{
			StringBuilder sb = new StringBuilder();

			HttpURLConnection cnn = (HttpURLConnection) new URL(url).openConnection();

			InputStream is;
			if (cnn.getResponseCode() == 200)
			{
				is = cnn.getInputStream();
			}
			else
			{
				is = cnn.getErrorStream();
			}

			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			String line = null;
			while ((line = reader.readLine()) != null)
			{
				sb.append(line);
			}

			reader.close();
			return sb.toString();
		}

		private UntappdResponse<DistinctBeers> serializeObject(String jsonString)
		{
			Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").create();
			JsonParser parser = new JsonParser();
			JsonObject json = parser.parse(jsonString).getAsJsonObject();

			UntappdResponse<DistinctBeers> response = new UntappdResponse<DistinctBeers>();

			if (json.has("meta"))
			{
				response.meta = gson.fromJson(json.getAsJsonObject("meta"), Meta.class);
			}

			if (json.has("response") && json.get("response").isJsonObject())
			{
				response.response = gson.fromJson(json.getAsJsonObject("response"), DistinctBeers.class);
			}

			return response;
		}
	}
}
