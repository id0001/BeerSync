package nl.yspierings.beersync.models.adapters;

import nl.yspierings.beersync.*;
import android.content.*;
import android.support.v4.view.*;
import android.view.*;

public class ViewPagerAdapter extends PagerAdapter
{
	private static final int PAGE_COUNT = 4;

	public static final int ALL_PAGE_POSITION = 0;
	public static final int RATING_PAGE_POSITION = 1;
	public static final int BREWERY_PAGE_POSITION = 2;
	public static final int STYLE_PAGE_POSITION = 3;
	

	public final ViewGroup[] pages = new ViewGroup[PAGE_COUNT];
	private final String[] titles = new String[PAGE_COUNT];

	private final Context context;

	public ViewPagerAdapter(Context context)
	{
		super();
		this.context = context;
		this.pages[ALL_PAGE_POSITION] = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.default_list_layout, null, false);
		this.pages[BREWERY_PAGE_POSITION] = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.grouped_list_layout, null, false);
		this.pages[STYLE_PAGE_POSITION] = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.grouped_list_layout, null, false);
		this.pages[RATING_PAGE_POSITION] = (ViewGroup) LayoutInflater.from(this.context).inflate(R.layout.grouped_list_layout, null, false);

		this.titles[ALL_PAGE_POSITION] = context.getString(R.string.all_tab_title);
		this.titles[BREWERY_PAGE_POSITION] = context.getString(R.string.brewery_tab_title);
		this.titles[STYLE_PAGE_POSITION] = context.getString(R.string.style_tab_title);
		this.titles[RATING_PAGE_POSITION] = context.getString(R.string.rating_tab_title);
	}

	@Override
	public int getCount()
	{
		return PAGE_COUNT;
	}

	@Override
	public boolean isViewFromObject(View aArg0, Object aArg1)
	{
		return aArg0 == ((View) aArg1);
	}

	@Override
	public CharSequence getPageTitle(int aPosition)
	{
		return this.titles[aPosition];
	}

	@Override
	public void destroyItem(View aContainer, int aPosition, Object aObject)
	{
		((ViewPager) aContainer).removeView((View) aObject);
	}

	@Override
	public Object instantiateItem(ViewGroup aContainer, int aPosition)
	{
		((ViewPager) aContainer).addView(this.pages[aPosition]);
		return this.pages[aPosition];
	}

}
