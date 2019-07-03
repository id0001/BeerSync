package nl.yspierings.beersync.activities;

import java.io.*;

import nl.yspierings.beersync.*;
import android.accounts.*;
import android.accounts.OperationCanceledException;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.*;
import android.os.*;
import android.preference.*;
import android.preference.Preference.*;
import android.widget.*;

public class SettingsActivity extends PreferenceActivity
{
	public static final int DEFAULT_SYNC_INTERVAL = 43200;
	
	private SharedPreferences settings;
	private Intent resultIntent;

	@Override
	protected void onCreate(Bundle aSavedInstanceState)
	{
		super.onCreate(aSavedInstanceState);
		this.resultIntent = new Intent();
		this.settings = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());

		this.addPreferencesFromResource(R.xml.sync_preferences);

		Preference deleteAccountPreference = this.findPreference("settings.account.delete");
		deleteAccountPreference.setOnPreferenceClickListener(new OnDeleteAccountClick());

		Preference syncIntervalPreference = this.findPreference("settings.sync.interval");
		syncIntervalPreference.setOnPreferenceChangeListener(new OnSyncIntervalChanged());
		this.setSyncIntervalSummary(syncIntervalPreference);
		setResult(RESULT_OK);
	}

	private void setSyncIntervalSummary(Preference syncIntervalPreference)
	{
		String value = this.settings.getString("settings.sync.interval", "" + DEFAULT_SYNC_INTERVAL);
		this.setSyncIntervalSummary(syncIntervalPreference, value);
	}

	private void setSyncIntervalSummary(Preference syncIntervalPreference, String value)
	{
		String[] intervalText = this.getResources().getStringArray(R.array.interval_list);
		String[] intervalValues = this.getResources().getStringArray(R.array.interval_list_values);

		for (int i = 0; i < intervalValues.length; i++)
		{
			if (intervalValues[i].equals(value))
			{
				syncIntervalPreference.setSummary(intervalText[i]);
				break;
			}
		}
	}

	private void setNewSyncInterval(long interval)
	{
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(App.ACCOUNT_TYPE);
		if (accounts.length > 0)
		{
			Account a = accounts[0];
			ContentResolver.addPeriodicSync(a, App.AUTHORITY, new Bundle(), interval);
		}
	}

	private class OnDeleteAccountClick implements OnPreferenceClickListener
	{

		@Override
		public boolean onPreferenceClick(Preference aPreference)
		{
			final AccountManager am = AccountManager.get(SettingsActivity.this);
			final Account[] accounts = am.getAccountsByType(App.ACCOUNT_TYPE);

			if (accounts.length == 0)
			{
				Toast.makeText(SettingsActivity.this, R.string.no_account_toast, Toast.LENGTH_LONG).show();
				return true;
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
			builder.setTitle(R.string.remove_account);
			builder.setMessage(R.string.remove_account_confirmation);
			builder.setPositiveButton(android.R.string.yes, new OnClickListener()
			{

				@Override
				public void onClick(DialogInterface aDialog, int aWhich)
				{
					aDialog.dismiss();

					if (accounts.length == 1)
					{
						am.removeAccount(accounts[0], new RemoveAccountCallback(), null);
					}
				}
			});

			builder.setNegativeButton(android.R.string.no, new OnClickListener()
			{

				@Override
				public void onClick(DialogInterface aDialog, int aWhich)
				{
					aDialog.dismiss();
				}
			});

			builder.create().show();

			return true;
		}
	}

	private class RemoveAccountCallback implements AccountManagerCallback<Boolean>
	{

		@Override
		public void run(AccountManagerFuture<Boolean> aArg0)
		{
			try
			{
				boolean result = aArg0.getResult().booleanValue();
				if (result)
				{
					Toast.makeText(SettingsActivity.this, R.string.remove_account_success, Toast.LENGTH_LONG).show();
				}
				else
				{
					Toast.makeText(SettingsActivity.this, R.string.remove_account_failed, Toast.LENGTH_LONG).show();
				}
			}
			catch (OperationCanceledException ex)
			{
				ex.printStackTrace();
			}
			catch (AuthenticatorException ex)
			{
				ex.printStackTrace();
				Toast.makeText(SettingsActivity.this, R.string.remove_account_failed, Toast.LENGTH_LONG).show();
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
				Toast.makeText(SettingsActivity.this, R.string.remove_account_failed, Toast.LENGTH_LONG).show();
			}
		}
	}

	private class OnSyncIntervalChanged implements OnPreferenceChangeListener
	{

		@Override
		public boolean onPreferenceChange(Preference aPreference, Object aNewValue)
		{
			setSyncIntervalSummary(aPreference, (String) aNewValue);
			setNewSyncInterval(Long.parseLong((String)aNewValue));
			return true;
		}
	}
}
