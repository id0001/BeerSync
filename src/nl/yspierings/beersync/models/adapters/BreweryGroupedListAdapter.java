package nl.yspierings.beersync.models.adapters;

import java.text.*;
import java.util.*;
import java.util.Map.Entry;

import nl.yspierings.beersync.*;
import nl.yspierings.beersync.models.untappd.*;
import android.content.*;
import android.view.*;
import android.widget.*;

public class BreweryGroupedListAdapter extends BaseExpandableListAdapter implements Filterable
{
	private static final int RESOURCE = R.layout.beer_list_item_compact;
	private static final int HEADER_RESOURCE = R.layout.default_list_header;

	private List<Entry<Brewery, List<BeersItem>>> filteredItems;

	private final List<BeersItem> allItems;
	private final Context context;

	private BeersItemFilter filter;
	private final DateFormat format;
	private String currentFilter = new String();

	public BreweryGroupedListAdapter(Context context)
	{
		this.context = context;
		this.filteredItems = new ArrayList<Entry<Brewery, List<BeersItem>>>();
		this.allItems = new ArrayList<BeersItem>();
		this.format = android.text.format.DateFormat.getMediumDateFormat(this.context);
	}

	public void add(BeersItem item)
	{
		this.allItems.add(item);
	}

	public void addAll(Collection<? extends BeersItem> items)
	{
		this.allItems.addAll(items);
	}

	@Override
	public BeersItem getChild(int aGroupPosition, int aChildPosition)
	{
		return this.filteredItems.get(aGroupPosition).getValue().get(aChildPosition);
	}

	@Override
	public long getChildId(int aGroupPosition, int aChildPosition)
	{
		return aChildPosition;
	}

	@Override
	public View getChildView(int aGroupPosition, int aChildPosition, boolean aIsLastChild, View aConvertView, ViewGroup aParent)
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

		BeersItem item = this.getChild(aGroupPosition, aChildPosition);

		ViewHolder holder = (ViewHolder) v.getTag();

		v.setBackgroundResource(aChildPosition % 2 == 0 ? R.drawable.list_background_selector_odd : R.drawable.list_background_selector_even);

		holder.text1.setText(item.beer.name);
		holder.abv.setText("" + item.beer.abv + "%");
		holder.ibu.setText("" + item.beer.ibu);
		holder.style.setText(item.beer.style);
		holder.by.setText(this.context.getString(R.string.by) + " " + item.brewery.name);
		holder.rating.setRating(item.ratingScore);
		holder.lastHad.setText(this.format.format(item.recentCreatedAt));
		return v;
	}

	@Override
	public int getChildrenCount(int aGroupPosition)
	{
		return this.filteredItems.get(aGroupPosition).getValue().size();
	}

	@Override
	public Brewery getGroup(int aGroupPosition)
	{
		return this.filteredItems.get(aGroupPosition).getKey();
	}

	@Override
	public int getGroupCount()
	{
		return this.filteredItems.size();
	}

	@Override
	public long getGroupId(int aGroupPosition)
	{
		return aGroupPosition;
	}

	@Override
	public View getGroupView(int aGroupPosition, boolean aIsExpanded, View aConvertView, ViewGroup aParent)
	{
		View v = aConvertView;
		if (v == null)
		{
			v = LayoutInflater.from(this.context).inflate(HEADER_RESOURCE, aParent, false);
		}

		Brewery b = this.getGroup(aGroupPosition);
		TextView tv = (TextView) v.findViewById(R.id.text1);
		tv.setText(b.name);

		return v;
	}

	@Override
	public boolean hasStableIds()
	{
		return false;
	}

	@Override
	public boolean isChildSelectable(int aGroupPosition, int aChildPosition)
	{
		return true;
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

	private class BreweryNameComparer implements Comparator<Entry<Brewery, List<BeersItem>>>
	{

		@Override
		public int compare(Entry<Brewery, List<BeersItem>> aLhs, Entry<Brewery, List<BeersItem>> aRhs)
		{
			return aLhs.getKey().name.compareTo(aRhs.getKey().name);
		}
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

	private final class BeerEntry implements Entry<Brewery, List<BeersItem>>
	{
		private final Brewery key;
		private List<BeersItem> value;

		public BeerEntry(Brewery key, List<BeersItem> value)
		{
			this.key = key;
			this.value = value;
		}

		@Override
		public Brewery getKey()
		{
			return this.key;
		}

		@Override
		public List<BeersItem> getValue()
		{
			return this.value;
		}

		@Override
		public List<BeersItem> setValue(List<BeersItem> aObject)
		{
			this.value = aObject;
			return this.value;
		}

	}

	public class BeersItemFilter extends Filter
	{

		@Override
		protected FilterResults performFiltering(CharSequence aConstraint)
		{
			String constraint = aConstraint != null ? (String)aConstraint : null;
			BreweryGroupedListAdapter.this.currentFilter = constraint;
			
			FilterResults results = new FilterResults();
			List<Entry<Brewery, List<BeersItem>>> filteredList = new ArrayList<Entry<Brewery, List<BeersItem>>>();

			BeersItem[] items = BreweryGroupedListAdapter.this.allItems.toArray(new BeersItem[0]);
			for (BeersItem item : items)
			{
				if (this.constraintInAny(constraint, item.beer.name, item.brewery.name, item.beer.style))
				{
					boolean keyExists = false;
					for (Entry<Brewery, List<BeersItem>> entry : filteredList)
					{
						if (entry.getKey().name.equals(item.brewery.name))
						{
							keyExists = true;
							entry.getValue().add(item);
							break;
						}
					}

					if (!keyExists)
					{
						Entry<Brewery, List<BeersItem>> entry = new BeerEntry(item.brewery, new ArrayList<BeersItem>());
						entry.getValue().add(item);
						filteredList.add(entry);
					}
				}
			}
			
			Collections.sort(filteredList, new BreweryNameComparer());
			
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
			BreweryGroupedListAdapter.this.filteredItems = (List<Entry<Brewery, List<BeersItem>>>) aResults.values;
			notifyDataSetChanged();
		}
	}

	public void clear()
	{
		this.allItems.clear();
		this.filteredItems.clear();
		this.notifyDataSetChanged();
	}
}
