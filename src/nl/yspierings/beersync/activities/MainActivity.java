package nl.yspierings.beersync.activities;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.adapters.*;
import nl.yspierings.beersync.models.data.*;
import nl.yspierings.beersync.models.untappd.*;
import android.accounts.*;
import android.app.*;
import android.content.*;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.*;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.*;
import android.net.*;
import android.os.*;
import android.preference.*;
import android.support.v4.view.*;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import android.widget.ExpandableListView.OnChildClickListener;

public class MainActivity extends ActionBarActivity
{
	private static final int SETTINGS_REQUEST_CODE = 234;

	private ViewPagerAdapter pagerAdapter;
	private ContentObserverImpl contentObserver;
	private EditText beerFilterEdit;
	private TextView filterCountText;

	private DistinctBeersListAdapter allBeersListAdapter;
	private BreweryGroupedListAdapter breweryListAdapter;
	private StyleGroupedListAdapter styleListAdapter;
	private RatingGroupedListAdapter ratingListAdapter;

	private MenuItem sortMenu;

	@Override
	protected void onCreate(Bundle aSavedInstanceState)
	{
		super.onCreate(aSavedInstanceState);
		this.setContentView(R.layout.activity_main);
		this.contentObserver = new ContentObserverImpl(new Handler());
		this.getContentResolver().registerContentObserver(App.CONTENT_URI, false, this.contentObserver);
		this.getContentResolver().registerContentObserver(Uri.withAppendedPath(App.CONTENT_URI, "syncstarted"), false, this.contentObserver);
		this.beerFilterEdit = (EditText) this.findViewById(R.id.beer_filter_edit);

		XLog.setSpecificTag("untappd_debug");
		XLog.setDebuggable((getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);

		this.initializeViewPager();

		this.checkIfAppIsUpdated();

		if (!this.hasAccount())
		{
			this.clearAdapters();
			this.showCreateAccountDialog();
		}
		else
		{
			this.updateView(this.isSyncInProgress());
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		this.getContentResolver().unregisterContentObserver(this.contentObserver);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (requestCode == SETTINGS_REQUEST_CODE && resultCode == RESULT_OK)
		{
			if (!this.hasAccount())
			{
				clearAdapters();
				this.hideSyncProgress();
				this.showEmptyListTexts();
			}
			else
			{
				this.updateView(this.isSyncInProgress());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu aMenu)
	{
		this.getMenuInflater().inflate(R.menu.menu_main, aMenu);
		this.sortMenu = aMenu.findItem(R.id.menu_sort);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem aItem)
	{
		switch (aItem.getItemId())
		{
			case R.id.menu_settings:
				Intent i = new Intent(this, SettingsActivity.class);
				this.startActivityForResult(i, SETTINGS_REQUEST_CODE);
				break;
		}

		if (aItem.getGroupId() == R.id.menu_group_sort)
		{
			this.allBeersListAdapter.sortBy(aItem.getItemId());
		}

		return true;
	}

	private void checkIfAppIsUpdated()
	{
		XLog.debug("Check for version mismatch");
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

		PackageInfo packageInfo;
		try
		{
			packageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			int versionCode = packageInfo.versionCode;
			int prefsVersion = prefs.getInt("versionCode", 0);
			XLog.debug("VersionCode: " + versionCode + ", PrefsVersion: " + prefsVersion);
			if (versionCode > prefsVersion)
			{
				this.enableSyncAfterUpdate();
				SharedPreferences.Editor edit = prefs.edit();
				edit.putInt("versionCode", versionCode);
				edit.commit();
			}
		}
		catch (NameNotFoundException ex)
		{
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	private void enableSyncAfterUpdate()
	{
		if (this.hasAccount())
		{
			XLog.debug("Enableing sync");
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			long interval = Long.parseLong(prefs.getString("settings.sync.interval", "" + SettingsActivity.DEFAULT_SYNC_INTERVAL));

			ContentResolver.setSyncAutomatically(getAccount(), App.AUTHORITY, true);
			ContentResolver.addPeriodicSync(getAccount(), App.AUTHORITY, new Bundle(), interval);
			this.requestImmediateSync();
		}
	}

	private void initializeViewPager()
	{
		ViewPager pager = (ViewPager) this.findViewById(R.id.pager);
		PagerTabStrip tabStrip = (PagerTabStrip) this.findViewById(R.id.pager_tab_strip);
		pager.setOnPageChangeListener(new ViewPagerPageChangedListener());

		this.initializeAdapters();

		ListView allBeersList = (ListView) this.pagerAdapter.pages[ViewPagerAdapter.ALL_PAGE_POSITION].findViewById(R.id.list);
		ExpandableListView breweryList = (ExpandableListView) this.pagerAdapter.pages[ViewPagerAdapter.BREWERY_PAGE_POSITION].findViewById(R.id.list);
		ExpandableListView styleList = (ExpandableListView) this.pagerAdapter.pages[ViewPagerAdapter.STYLE_PAGE_POSITION].findViewById(R.id.list);
		ExpandableListView ratingList = (ExpandableListView) this.pagerAdapter.pages[ViewPagerAdapter.RATING_PAGE_POSITION].findViewById(R.id.list);

		pager.setAdapter(this.pagerAdapter);
		allBeersList.setAdapter(this.allBeersListAdapter);
		breweryList.setAdapter(this.breweryListAdapter);
		styleList.setAdapter(this.styleListAdapter);
		ratingList.setAdapter(this.ratingListAdapter);

		ListItemClickListener listener = new ListItemClickListener();
		allBeersList.setOnItemClickListener(listener);
		breweryList.setOnChildClickListener(new ExpandableListItemClickListener(this.breweryListAdapter));
		styleList.setOnChildClickListener(new ExpandableListItemClickListener(this.styleListAdapter));
		ratingList.setOnChildClickListener(new ExpandableListItemClickListener(this.ratingListAdapter));

		allBeersList.setFastScrollEnabled(true);
		breweryList.setFastScrollEnabled(true);
		styleList.setFastScrollEnabled(true);
		ratingList.setFastScrollEnabled(true);
	}

	private void initializeAdapters()
	{
		this.pagerAdapter = new ViewPagerAdapter(this);

		this.allBeersListAdapter = new DistinctBeersListAdapter(this);
		this.beerFilterEdit.addTextChangedListener(new SearchFilter(this.allBeersListAdapter));

		this.breweryListAdapter = new BreweryGroupedListAdapter(this);
		this.beerFilterEdit.addTextChangedListener(new SearchFilter(this.breweryListAdapter));

		this.styleListAdapter = new StyleGroupedListAdapter(this);
		this.beerFilterEdit.addTextChangedListener(new SearchFilter(this.styleListAdapter));

		this.ratingListAdapter = new RatingGroupedListAdapter(this);
		this.beerFilterEdit.addTextChangedListener(new SearchFilter(this.ratingListAdapter));
	}

	private void updateView(boolean syncInProgress)
	{
		DistinctBeers beers = this.getDistinctBeersFromDatabase();

		if (beers.beers.count == 0)
		{
			if (syncInProgress)
			{
				this.showSyncProgress();
			}
			else
			{
				this.showEmptyListTexts();
			}
		}
		else
		{
			this.hideEmptyListTexts();
			this.hideSyncProgress();
			this.fillAdapters(beers);
		}
	}

	private DistinctBeers getDistinctBeersFromDatabase()
	{
		BeerDataContract contract = new BeerDataContract(this);
		DistinctBeers allBeers = contract.getAllBeers();
		contract.closeDb();
		return allBeers;
	}

	private void fillAdapters(DistinctBeers allBeers)
	{
		this.clearAdapters();

		this.allBeersListAdapter.addAll(allBeers.beers.items);
		this.setFilter(this.allBeersListAdapter);

		this.breweryListAdapter.addAll(allBeers.beers.items);
		this.setFilter(this.breweryListAdapter);

		this.styleListAdapter.addAll(allBeers.beers.items);
		this.setFilter(this.styleListAdapter);

		this.ratingListAdapter.addAll(allBeers.beers.items);
		this.setFilter(this.ratingListAdapter);
	}

	private void setFilter(Filterable filterable)
	{
		String filter = this.beerFilterEdit.getText().toString();
		filterable.getFilter().filter(filter);
	}

	private void clearAdapters()
	{
		this.allBeersListAdapter.clear();
		this.breweryListAdapter.clear();
		this.styleListAdapter.clear();
		this.ratingListAdapter.clear();
	}

	private boolean isSyncInProgress()
	{
		Account account = this.getAccount();
		return ContentResolver.isSyncActive(account, App.AUTHORITY);
	}

	private void showEmptyListTexts()
	{
		View empty0 = this.pagerAdapter.pages[ViewPagerAdapter.ALL_PAGE_POSITION].findViewById(R.id.empty_text);
		View empty1 = this.pagerAdapter.pages[ViewPagerAdapter.BREWERY_PAGE_POSITION].findViewById(R.id.empty_text);
		View empty2 = this.pagerAdapter.pages[ViewPagerAdapter.STYLE_PAGE_POSITION].findViewById(R.id.empty_text);
		View empty3 = this.pagerAdapter.pages[ViewPagerAdapter.RATING_PAGE_POSITION].findViewById(R.id.empty_text);

		this.hideSyncProgress();
		empty0.setVisibility(View.VISIBLE);
		empty1.setVisibility(View.VISIBLE);
		empty2.setVisibility(View.VISIBLE);
		empty3.setVisibility(View.VISIBLE);
	}

	private void hideEmptyListTexts()
	{
		View empty0 = this.pagerAdapter.pages[ViewPagerAdapter.ALL_PAGE_POSITION].findViewById(R.id.empty_text);
		View empty1 = this.pagerAdapter.pages[ViewPagerAdapter.BREWERY_PAGE_POSITION].findViewById(R.id.empty_text);
		View empty2 = this.pagerAdapter.pages[ViewPagerAdapter.STYLE_PAGE_POSITION].findViewById(R.id.empty_text);
		View empty3 = this.pagerAdapter.pages[ViewPagerAdapter.RATING_PAGE_POSITION].findViewById(R.id.empty_text);

		empty0.setVisibility(View.INVISIBLE);
		empty1.setVisibility(View.INVISIBLE);
		empty2.setVisibility(View.INVISIBLE);
		empty3.setVisibility(View.INVISIBLE);
	}

	private void showSyncProgress()
	{
		View v = this.findViewById(R.id.sync_in_progress);
		View pager = this.findViewById(R.id.pager);

		this.hideEmptyListTexts();
		pager.setVisibility(View.INVISIBLE);
		v.setVisibility(View.VISIBLE);
	}

	private void hideSyncProgress()
	{
		View v = this.findViewById(R.id.sync_in_progress);
		View pager = this.findViewById(R.id.pager);

		v.setVisibility(View.INVISIBLE);
		pager.setVisibility(View.VISIBLE);
	}

	private void requestImmediateSync()
	{
		Account account = this.getAccount();
		if (account != null)
		{
			Bundle b = new Bundle();
			b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
			b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
			ContentResolver.requestSync(account, App.AUTHORITY, b);
		}
	}

	private Account getAccount()
	{
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(App.ACCOUNT_TYPE);
		if (accounts.length == 0)
		{
			return null;
		}

		return accounts[0];
	}

	private boolean hasAccount()
	{
		return this.getAccount() != null;
	}

	private void updateFilterCount()
	{
		this.filterCountText.setText("" + this.allBeersListAdapter.getCount());
	}

	private void showCreateAccountDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.create_account_dialog_title);
		builder.setMessage(R.string.create_account_dialog_message);
		builder.setPositiveButton(android.R.string.ok, new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface aDialog, int aWhich)
			{
				AccountManager am = AccountManager.get(MainActivity.this);
				am.addAccount(App.ACCOUNT_TYPE, null, null, null, MainActivity.this, new AccountManagerCallbackImpl(), null);
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new OnClickListener()
		{

			@Override
			public void onClick(DialogInterface aDialog, int aWhich)
			{
				aDialog.dismiss();
			}
		});

		builder.create().show();
	}

	private class SearchFilter implements TextWatcher
	{
		private final Filterable adapter;

		public SearchFilter(Filterable adapter)
		{
			this.adapter = adapter;
		}

		@Override
		public void afterTextChanged(Editable aS)
		{
			if (this.adapter != null)
			{
				this.adapter.getFilter().filter(aS);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence aS, int aStart, int aCount, int aAfter)
		{
		}

		@Override
		public void onTextChanged(CharSequence aS, int aStart, int aBefore, int aCount)
		{
		}

	}

	private final class AccountManagerCallbackImpl implements AccountManagerCallback<Bundle>
	{

		@Override
		public void run(AccountManagerFuture<Bundle> aArg0)
		{
			if (hasAccount())
			{
				updateView(true);
			}
			else
			{
				showEmptyListTexts();
			}
		}
	}

	private final class ContentObserverImpl extends ContentObserver
	{

		public ContentObserverImpl(Handler aHandler)
		{
			super(aHandler);
		}

		@Override
		public void onChange(boolean aSelfChange, Uri aUri)
		{
			if (aUri.getLastPathSegment() != null && aUri.getLastPathSegment().equals("syncstarted"))
			{
				updateView(true);
			}
			else
			{
				updateView(false);
			}
		}
	}

	private final class ListItemClickListener implements AdapterView.OnItemClickListener
	{

		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
		{
			BeersItem item = (BeersItem) adapterView.getItemAtPosition(position);
			Intent i = new Intent(MainActivity.this, CheckinDetailsActivity.class);
			i.putExtra("checkinId", item.recentCheckinId);
			startActivity(i);
		}
	}

	private final class ExpandableListItemClickListener implements OnChildClickListener
	{
		private final BaseExpandableListAdapter adapter;

		public ExpandableListItemClickListener(BaseExpandableListAdapter adapter)
		{
			this.adapter = adapter;
		}

		@Override
		public boolean onChildClick(ExpandableListView aParent, View aV, int aGroupPosition, int aChildPosition, long aId)
		{
			BeersItem item = (BeersItem) this.adapter.getChild(aGroupPosition, aChildPosition);
			Intent i = new Intent(MainActivity.this, CheckinDetailsActivity.class);
			i.putExtra("checkinId", item.recentCheckinId);
			startActivity(i);
			return true;
		}
	}

	private final class ViewPagerPageChangedListener implements OnPageChangeListener
	{

		@Override
		public void onPageSelected(int state)
		{
			if (state == 0)
			{
				MainActivity.this.sortMenu.setEnabled(true);
				MainActivity.this.sortMenu.setVisible(true);
			}
			else
			{
				MainActivity.this.sortMenu.setEnabled(false);
				MainActivity.this.sortMenu.setVisible(false);
			}
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
		{
		}

		@Override
		public void onPageScrollStateChanged(int position)
		{
		}

	}
}
