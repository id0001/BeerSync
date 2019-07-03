package nl.yspierings.beersync.models.adapters;

import java.text.*;
import java.util.*;
import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.untappd.*;
import android.content.*;
import android.view.*;
import android.widget.*;

public class DistinctBeersListAdapter extends BaseAdapter implements Filterable
{
	private static final int RESOURCE = R.layout.beer_list_item;

	private List<BeersItem> filteredItems;
	private final List<BeersItem> allItems;
	private final Context context;

	private BeersItemFilter filter;
	private final DateFormat format;
	private String currentFilter = new String();
	private Comparator<BeersItem> comparator;

	public DistinctBeersListAdapter(Context context)
	{
		this.context = context;
		this.filteredItems = new ArrayList<BeersItem>();
		this.allItems = new ArrayList<BeersItem>();
		this.format = android.text.format.DateFormat.getMediumDateFormat(this.context);
		this.comparator = new BeersItemNameComparator();
	}

	public void add(BeersItem item)
	{
		this.allItems.add(item);
	}

	public void addAll(Collection<? extends BeersItem> items)
	{
		this.allItems.addAll(items);
	}

	public void clear()
	{
		this.allItems.clear();
		this.filteredItems.clear();
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount()
	{
		return this.filteredItems.size();
	}

	@Override
	public BeersItem getItem(int aPosition)
	{
		return this.filteredItems.get(aPosition);
	}

	@Override
	public long getItemId(int aPosition)
	{
		return aPosition;
	}

	@Override
	public View getView(int aPosition, View aConvertView, ViewGroup aParent)
	{
		View v = aConvertView;
		if (v == null)
		{
			v = LayoutInflater.from(this.context).inflate(RESOURCE, aParent, false);

			ViewHolder holder = new ViewHolder();
			holder.text1 = (TextView) v.findViewById(R.id.text1);
			holder.abv = (TextView) v.findViewById(R.id.text_abv);
			holder.ibu = (TextView) v.findViewById(R.id.text_ibu);
			holder.style = (TextView) v.findViewById(R.id.text_style);
			holder.by = (TextView) v.findViewById(R.id.text_by);
			holder.lastHad = (TextView) v.findViewById(R.id.text_last_had);
			holder.rating = (RatingBar) v.findViewById(R.id.beer_rating);
			v.setTag(holder);
		}

		BeersItem item = this.getItem(aPosition);

		ViewHolder holder = (ViewHolder) v.getTag();

		v.setBackgroundResource(aPosition % 2 == 0 ? R.drawable.list_background_selector_odd : R.drawable.list_background_selector_even);

		holder.text1.setText(item.beer.name);
		holder.abv.setText("" + item.beer.abv + "%");
		holder.ibu.setText("" + item.beer.ibu);
		holder.style.setText(item.beer.style);
		holder.by.setText(this.context.getString(R.string.by) + " " + item.brewery.name);
		holder.rating.setRating(item.ratingScore);
		holder.lastHad.setText(this.format.format(item.recentCreatedAt));
		return v;
	}

	private static class ViewHolder
	{
		public TextView text1;
		public TextView abv;
		public TextView ibu;
		public TextView style;
		public TextView by;
		public TextView lastHad;
		public RatingBar rating;
	}

	@Override
	public Filter getFilter()
	{
		if (this.filter == null)
		{
			this.filter = new BeersItemFilter();
		}

		return this.filter;
	}

	public void sortBy(int id)
	{
		switch (id)
		{
			case R.id.menu_sortby_name:
				this.comparator = new BeersItemNameComparator();
				if (this.filter != null)
				{
					this.filter.filter(this.currentFilter);
				}
				
				break;
			case R.id.menu_sortby_last_had:
				this.comparator = new BeersItemDateComparator();
				if (this.filter != null)
				{
					this.filter.filter(this.currentFilter);
				}
				
				break;
			case R.id.menu_sortby_rating:
				this.comparator = new BeersItemRatingComparator();
				if (this.filter != null)
				{
					this.filter.filter(this.currentFilter);
				}
				
				break;
		}
	}

	private class BeersItemDateComparator implements Comparator<BeersItem>
	{

		@Override
		public int compare(BeersItem aLhs, BeersItem aRhs)
		{
			return aRhs.recentCreatedAt.compareTo(aLhs.recentCreatedAt);
		}

	}

	private class BeersItemNameComparator implements Comparator<BeersItem>
	{

		@Override
		public int compare(BeersItem aLhs, BeersItem aRhs)
		{
			return aLhs.beer.name.compareToIgnoreCase(aRhs.beer.name);
		}
	}

	private class BeersItemRatingComparator implements Comparator<BeersItem>
	{

		@Override
		public int compare(BeersItem aLhs, BeersItem aRhs)
		{
			if (aRhs.ratingScore > aLhs.ratingScore)
			{
				return 1;
			}
			else if (aRhs.ratingScore < aLhs.ratingScore)
			{
				return -1;
			}

			return 0;
		}
	}

	public class BeersItemFilter extends Filter
	{

		@Override
		protected FilterResults performFiltering(CharSequence aConstraint)
		{
			String constraint = aConstraint != null ? (String) aConstraint : null;
			DistinctBeersListAdapter.this.currentFilter = constraint;

			FilterResults results = new FilterResults();
			List<BeersItem> filteredList = new ArrayList<BeersItem>();

			BeersItem[] items = DistinctBeersListAdapter.this.allItems.toArray(new BeersItem[0]);
			for (BeersItem item : items)
			{
				if (this.constraintInAny(constraint, item.beer.name, item.brewery.name, item.beer.style))
				{
					filteredList.add(item);
				}
			}

			Collections.sort(filteredList, DistinctBeersListAdapter.this.comparator);

			results.values = filteredList;
			results.count = filteredList.size();

			return results;
		}

		private boolean constraintInAny(String constraint, String... checkList)
		{
			if (constraint == null || constraint.length() == 0)
			{
				return true;
			}

			for (String s : checkList)
			{
				if (s == null)
				{
					continue;
				}

				if (s.toLowerCase(Locale.US).contains(constraint))
				{
					return true;
				}
			}

			return false;
		}

		@Override
		protected void publishResults(CharSequence aConstraint, FilterResults aResults)
		{
			DistinctBeersListAdapter.this.filteredItems = (List<BeersItem>) aResults.values;
			notifyDataSetChanged();
		}
	}
}
