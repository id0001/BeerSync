package nl.yspierings.beersync.activities;

import java.io.*;
import java.net.*;
import java.text.*;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.*;
import nl.yspierings.beersync.models.data.*;
import nl.yspierings.beersync.models.untappd.*;
import android.accounts.*;
import android.app.*;
import android.graphics.*;
import android.os.*;
import android.support.v7.app.*;
import android.text.*;
import android.widget.*;

public class CheckinDetailsActivity extends ActionBarActivity
{
	private ViewSwitcher viewSwitcher;

	@Override
	protected void onCreate(Bundle aSavedInstanceState)
	{
		super.onCreate(aSavedInstanceState);

		this.setContentView(R.layout.activity_checkin_details);
		this.viewSwitcher = (ViewSwitcher) this.findViewById(R.id.view_switcher);

		String checkinId = this.getIntent().getExtras().getString("checkinId");
		if (checkinId == null)
		{
			finish();
		}

		this.downloadCheckin(checkinId);
	}

	private void downloadCheckin(String checkinId)
	{
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccountsByType(App.ACCOUNT_TYPE);
		if (accounts.length == 0)
		{
			return;
		}

		Account account = accounts[0];
		String authToken = am.getPassword(account);
		if (authToken == null)
		{
			return;
		}

		AsyncUntappdCall<CheckinResponse> call = Untappd.getCheckinDetails(authToken, checkinId);
		call.setOnResultHandler(new IResultHandler<AsyncUntappdCall<CheckinResponse>>()
		{

			@Override
			public void onComplete(AsyncUntappdCall<CheckinResponse> aResult)
			{
				try
				{
					CheckinResponse response = aResult.getResult();
					setCheckinData(response.checkin);
				}
				catch (Exception ex)
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(CheckinDetailsActivity.this);
					builder.setTitle(R.string.error_title);
					builder.setMessage(ex.getMessage());
					builder.create().show();
				}

			}
		});

		call.execute();
	}

	private void setCheckinData(Checkin checkin)
	{
		DateFormat format = android.text.format.DateFormat.getMediumDateFormat(this);
		TextView beerName = (TextView) this.findViewById(R.id.text1);
		TextView breweryName = (TextView) this.findViewById(R.id.text_by);
		TextView style = (TextView) this.findViewById(R.id.text_style);
		TextView abv = (TextView) this.findViewById(R.id.text_abv);
		TextView ibu = (TextView) this.findViewById(R.id.text_ibu);
		TextView lastHad = (TextView) this.findViewById(R.id.text_last_had);
		ImageView icon = (ImageView) this.findViewById(R.id.icon);
		TextView description = (TextView) this.findViewById(R.id.text_description);
		TextView checkinComment = (TextView) this.findViewById(R.id.text_checkin_comment);
		RatingBar yourRating = (RatingBar) this.findViewById(R.id.your_rating_bar);
		RatingBar overallRating = (RatingBar) this.findViewById(R.id.overall_rating_bar);

		yourRating.setRating(checkin.ratingScore);

		if (!TextUtils.isEmpty(checkin.checkinComment))
		{
			checkinComment.setText(checkin.checkinComment);
		}
		else
		{
			checkinComment.setText(R.string.no_checkin_comment);
		}

		BeerDataContract db = new BeerDataContract(this);
		BeersItem item = db.getBeerByCheckinId(checkin.checkinId);
		db.closeDb();

		if (item != null)
		{
			new DownloadImageTask(icon).execute(item.beer.labelUrl);
			beerName.setText(item.beer.name);
			breweryName.setText(this.getString(R.string.by) + " " + item.brewery.name);
			style.setText(item.beer.style);
			abv.setText("" + item.beer.abv + "%");
			ibu.setText("" + item.beer.ibu);
			lastHad.setText(format.format(item.recentCreatedAt));
			XLog.debug("" + item.beer.ratingScore);
			overallRating.setRating(item.beer.ratingScore);

			if (!TextUtils.isEmpty(item.beer.description))
			{
				description.setText(item.beer.description);
			}
			else
			{
				description.setText(R.string.no_description);
			}
		}

		this.viewSwitcher.showNext();
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
	{
		private final ImageView iv;

		public DownloadImageTask(ImageView iv)
		{
			this.iv = iv;
		}

		@Override
		protected Bitmap doInBackground(String... aParams)
		{
			String url = aParams[0];
			Bitmap bm = null;

			try
			{
				InputStream is = new URL(url).openStream();
				bm = BitmapFactory.decodeStream(is);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}

			return bm;
		}

		@Override
		protected void onPostExecute(Bitmap aResult)
		{
			if (this.iv != null)
			{
				this.iv.setImageBitmap(aResult);
			}
		}

	}
}
