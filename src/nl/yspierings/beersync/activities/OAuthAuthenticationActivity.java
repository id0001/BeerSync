package nl.yspierings.beersync.activities;

import java.io.*;
import java.net.*;

import com.google.gson.*;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.*;
import nl.yspierings.beersync.models.data.*;
import nl.yspierings.beersync.models.untappd.*;
import android.accounts.*;
import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.text.*;
import android.view.*;
import android.webkit.*;
import android.webkit.CookieManager;
import android.widget.*;

public class OAuthAuthenticationActivity extends AccountAuthenticatorActivity
{
	private WebView webView;
	private ProgressBar progress;
	private ProgressDialog progressDialog;
	private GetAuthTokenAsync getAuthTokenRequest;

	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle aSavedInstanceState)
	{
		super.onCreate(aSavedInstanceState);
		this.setContentView(R.layout.activity_oauth_authenticate);

		this.assertSingleAccount();

		this.progress = (ProgressBar) this.findViewById(R.id.load_progress);
		this.webView = (WebView) this.findViewById(R.id.webview);
		this.webView.getSettings().setSaveFormData(false);
		this.webView.getSettings().setSavePassword(false);
		this.webView.getSettings().setJavaScriptEnabled(true);
		this.webView.setWebViewClient(new WebViewClientImpl());
		this.webView.setWebChromeClient(new WebChromeClientImpl());

		this.resetProgressBar();
		this.reloadLoginPage();
	}

	@Override
	protected void onDestroy()
	{
		if (this.progressDialog != null && this.progressDialog.isShowing())
		{
			this.progressDialog.dismiss();
		}

		super.onDestroy();
	}

	@Override
	public void onBackPressed()
	{
		// if (this.getAuthTokenRequest != null &&
		// this.getAuthTokenRequest.getStatus() != Status.FINISHED)
		// {
		// this.getAuthTokenRequest.cancel(true);
		// }

		if (this.webView.canGoBack())
		{
			this.webView.goBack();
			return;
		}

		super.onBackPressed();
	}

	private void reloadLoginPage()
	{
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeAllCookie();
		this.webView.loadUrl(Untappd.OATH_LOGIN_URL);
	}

	private void assertSingleAccount()
	{
		AccountManager am = AccountManager.get(this);

		Account[] accounts = am.getAccountsByType(App.ACCOUNT_TYPE);
		if (accounts.length > 0)
		{
			Toast.makeText(this, R.string.single_account_message, Toast.LENGTH_LONG).show();
			this.finish();
			return;
		}
	}

	private void resetProgressBar()
	{
		this.progress.setVisibility(View.VISIBLE);
		this.progress.setProgress(0);
	}

	private class WebViewClientImpl extends WebViewClient
	{

		@Override
		public boolean shouldOverrideUrlLoading(WebView aView, String aUrl)
		{
			if (aUrl.startsWith("http://nl.yspierings.beersync"))
			{
				String code = extractCodeFromUri(Uri.parse(aUrl));
				getAuthToken(code);
				return true;
			}

			OAuthAuthenticationActivity.this.resetProgressBar();
			aView.loadUrl(aUrl);
			return true;
		}
	}

	private void getAuthToken(String code)
	{
		this.progressDialog = ProgressDialog.show(this, null, getString(R.string.retrieving_authtoken));
		this.getAuthTokenRequest = new GetAuthTokenAsync();
		this.getAuthTokenRequest.execute(code);
	}

	private void saveAccessToken(AuthTokenResult tokenResult)
	{
		this.getAuthTokenRequest = null;
		if (tokenResult.ex != null)
		{
			this.showExceptionDialog(tokenResult.ex);
			this.progressDialog.dismiss();
			this.reloadLoginPage();
			return;
		}

		this.downloadUserInformation(tokenResult.token);
	}

	private String extractCodeFromUri(Uri uri)
	{
		return uri.getQueryParameter("code");
	}

	private void downloadUserInformation(final String authToken)
	{
		this.progressDialog.setMessage(getString(R.string.retrieving_user_information));

		AsyncUntappdCall<UserResponse> call = Untappd.getMe(authToken);
		call.setOnResultHandler(new IResultHandler<AsyncUntappdCall<UserResponse>>()
		{

			@Override
			public void onComplete(AsyncUntappdCall<UserResponse> aResult)
			{
				try
				{
					UserResponse response = aResult.getResult();

					String username = response.user.username;
					createAccount(username, authToken);
				}
				catch (Exception ex)
				{
					showExceptionDialog(ex);
					OAuthAuthenticationActivity.this.progressDialog.dismiss();
					reloadLoginPage();
				}
			}
		});

		call.execute();
	}

	private void createAccount(String username, String accessToken)
	{
		AccountManager am = AccountManager.get(this);
		Account account = new Account(username, App.ACCOUNT_TYPE);
		boolean accountCreated = am.addAccountExplicitly(account, accessToken, null);
		if (accountCreated)
		{
			Toast.makeText(this, R.string.create_account_success, Toast.LENGTH_LONG).show();
			this.clearDatabase();
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			long interval = Long.parseLong(prefs.getString("settings.sync.interval", "" + SettingsActivity.DEFAULT_SYNC_INTERVAL));

			ContentResolver.setSyncAutomatically(account, App.AUTHORITY, true);
			ContentResolver.addPeriodicSync(account, App.AUTHORITY, new Bundle(), interval);

			Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, username);
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, App.ACCOUNT_TYPE);
			this.setAccountAuthenticatorResult(result);
		}
		else
		{
			Toast.makeText(this, R.string.create_account_failed, Toast.LENGTH_LONG).show();
		}

		this.finish();
	}

	private void clearDatabase()
	{
		BeerDataContract beerContract = new BeerDataContract(this);
		beerContract.clear();
		beerContract.closeDb();

		UserDataContract userContract = new UserDataContract(this);
		userContract.clear();
		userContract.closeDb();
	}

	private void showExceptionDialog(Exception ex)
	{
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setMessage(ex.getMessage());
		b.setTitle(android.R.string.dialog_alert_title);
		b.setIcon(android.R.drawable.ic_dialog_alert);
		b.setPositiveButton(android.R.string.ok, null);
		b.create().show();
	}

	private class WebChromeClientImpl extends WebChromeClient
	{

		@Override
		public void onProgressChanged(WebView aView, int aNewProgress)
		{
			if (aNewProgress == 100)
			{
				OAuthAuthenticationActivity.this.progress.setVisibility(View.GONE);
			}

			OAuthAuthenticationActivity.this.progress.setProgress(aNewProgress);
		}
	}

	private class GetAuthTokenAsync extends AsyncTask<String, Void, AuthTokenResult>
	{

		@Override
		protected AuthTokenResult doInBackground(String... aParams)
		{
			AuthTokenResult result = new AuthTokenResult();
			String getAuthTokenUrl = Untappd.GET_AUTH_TOKEN_URL + aParams[0];
			String json = this.getHttpResponse(result, getAuthTokenUrl);
			if (!TextUtils.isEmpty(json))
			{
				this.serializeObject(result, json);
			}

			return result;
		}

		private String getHttpResponse(AuthTokenResult result, String getAuthTokenUrl)
		{
			HttpURLConnection cnn = null;
			BufferedReader reader = null;
			StringBuilder sb = new StringBuilder();
			try
			{
				URL url = new URL(getAuthTokenUrl);
				cnn = (HttpURLConnection) url.openConnection();

				InputStream is;
				if (cnn.getResponseCode() == 200)
				{
					is = cnn.getInputStream();
				}
				else
				{
					is = cnn.getErrorStream();
				}

				reader = new BufferedReader(new InputStreamReader(is));
				String line;

				while ((line = reader.readLine()) != null)
				{
					sb.append(line);
				}

				reader.close();
			}
			catch (MalformedURLException ex)
			{
				ex.printStackTrace();
				result.ex = ex;
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				result.ex = ex;
			}

			return sb.toString();
		}

		private void serializeObject(AuthTokenResult result, String json)
		{
			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(json);

			int httpStatusCode = element.getAsJsonObject().getAsJsonObject("meta").getAsJsonPrimitive("http_code").getAsInt();

			if (httpStatusCode == 200)
			{
				String authToken = element.getAsJsonObject().getAsJsonObject("response").getAsJsonPrimitive("access_token").getAsString();
				result.token = authToken;
			}
			else
			{
				String errorDetail = element.getAsJsonObject().getAsJsonObject("meta").getAsJsonPrimitive("error_detail").getAsString();
				result.ex = new Exception(errorDetail);
			}
		}

		@Override
		protected void onPostExecute(AuthTokenResult aResult)
		{
			saveAccessToken(aResult);
		}
	}
}
