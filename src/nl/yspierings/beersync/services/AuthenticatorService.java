package nl.yspierings.beersync.services;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.activities.*;
import android.accounts.*;
import android.app.*;
import android.content.*;
import android.os.*;

public class AuthenticatorService extends Service
{
	private AccountAuthenticatorImpl authenticator;

	@Override
	public void onCreate()
	{
		super.onCreate();
		XLog.debug("Authenticator service started");
		this.authenticator = new AccountAuthenticatorImpl(this);
	}

	@Override
	public IBinder onBind(Intent aIntent)
	{
		if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(aIntent.getAction()))
		{
			return this.authenticator.getIBinder();
		}
		
		return null;
	}

	private final class AccountAuthenticatorImpl extends AbstractAccountAuthenticator
	{
		private final Context context;

		public AccountAuthenticatorImpl(Context context)
		{
			super(context);
			this.context = context;
		}

		@Override
		public Bundle addAccount(AccountAuthenticatorResponse aResponse, String aAccountType, String aAuthTokenType, String[] aRequiredFeatures, Bundle aOptions)
				throws NetworkErrorException
		{
			Bundle b = new Bundle();
			Intent i = new Intent(this.context, OAuthAuthenticationActivity.class);
			i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, aResponse);
			b.putParcelable(AccountManager.KEY_INTENT, i);
			return b;
		}

		@Override
		public Bundle confirmCredentials(AccountAuthenticatorResponse aResponse, Account aAccount, Bundle aOptions) throws NetworkErrorException
		{
			return null;
		}

		@Override
		public Bundle editProperties(AccountAuthenticatorResponse aResponse, String aAccountType)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bundle getAuthToken(AccountAuthenticatorResponse aResponse, Account aAccount, String aAuthTokenType, Bundle aOptions) throws NetworkErrorException
		{
			return null;
		}

		@Override
		public String getAuthTokenLabel(String aAuthTokenType)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bundle hasFeatures(AccountAuthenticatorResponse aResponse, Account aAccount, String[] aFeatures) throws NetworkErrorException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public Bundle updateCredentials(AccountAuthenticatorResponse aResponse, Account aAccount, String aAuthTokenType, Bundle aOptions) throws NetworkErrorException
		{
			throw new UnsupportedOperationException();
		}

	}
}
