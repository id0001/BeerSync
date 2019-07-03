package nl.yspierings.beersync.models;

import java.io.*;
import java.net.*;
import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.untappd.*;
import android.os.*;

import com.google.gson.*;

public class AsyncUntappdCall<T>
{
	private UntappdResponse<T> result;
	private Exception exception;
	private IResultHandler<AsyncUntappdCall<T>> resultHandler;
	public final AsyncWebRequest request;
	private final String url;
	private final Class<T> responseType;

	public AsyncUntappdCall(String url, Class<T> responseType)
	{
		this.url = url;
		this.responseType = responseType;
		this.request = new AsyncWebRequest();
	}

	public void execute()
	{
		XLog.debug("Executing webcall: " + this.url.substring(0, this.url.indexOf("?")));
		this.request.execute();
	}

	public void setOnResultHandler(IResultHandler<AsyncUntappdCall<T>> handler)
	{
		this.resultHandler = handler;
	}

	public T getResult() throws Exception
	{
		if (this.exception != null)
		{
			throw this.exception;
		}

		if (this.result == null || this.result.meta == null)
		{
			throw new Exception("Response malformed.");
		}

		if (this.result.meta.code != 200)
		{
			throw new Exception(this.result.meta.errorDetail);
		}

		return this.result.response;
	}

	private void finishRequest()
	{
		if (this.resultHandler != null)
		{
			this.resultHandler.onComplete(this);
		}
	}

	class AsyncWebRequest extends AsyncTask<Void, Void, UntappdResponse<T>>
	{

		@Override
		protected UntappdResponse<T> doInBackground(Void... aParams)
		{
			try
			{
				XLog.debug("Running webcall...");
				StringBuilder sb = new StringBuilder();

				HttpURLConnection cnn = (HttpURLConnection) (new URL(AsyncUntappdCall.this.url).openConnection());

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

				XLog.debug("Data received. Serializing...");

				Gson gson = new GsonBuilder().setDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").create();
				JsonParser parser = new JsonParser();

				JsonObject json = parser.parse(sb.toString()).getAsJsonObject();

				UntappdResponse<T> res = new UntappdResponse<T>();

				if (json.has("meta"))
				{
					res.meta = gson.fromJson(json.getAsJsonObject("meta"), Meta.class);
				}

				if (json.has("response") && json.get("response").isJsonObject())
				{
					res.response = gson.fromJson(json.getAsJsonObject("response"), AsyncUntappdCall.this.responseType);
				}

				XLog.debug("Finished successfully.");
				return res;
			}
			catch (Exception ex)
			{
				XLog.debug("Finished with error.");
				AsyncUntappdCall.this.exception = ex;
			}

			return null;
		}

		@Override
		protected void onPostExecute(UntappdResponse<T> aResult)
		{
			AsyncUntappdCall.this.result = aResult;
			finishRequest();
		}

	}
}
