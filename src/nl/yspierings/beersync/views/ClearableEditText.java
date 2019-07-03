package nl.yspierings.beersync.views;

import nl.yspierings.beersync.*;
import android.content.*;
import android.graphics.drawable.*;
import android.text.*;
import android.util.*;
import android.view.*;
import android.widget.*;

public class ClearableEditText extends EditText
{
	private Drawable clearDrawable;

	public ClearableEditText(Context aContext)
	{
		super(aContext);
		this.init();
	}

	public ClearableEditText(Context aContext, AttributeSet aAttrs, int aDefStyle)
	{
		super(aContext, aAttrs, aDefStyle);
		this.init();
	}

	public ClearableEditText(Context aContext, AttributeSet aAttrs)
	{
		super(aContext, aAttrs);
		this.init();
	}

	private void init()
	{
		if (!this.isInEditMode())
		{
			this.clearDrawable = this.getCompoundDrawables()[2];
			if (this.clearDrawable == null)
			{
				this.clearDrawable = this.getResources().getDrawable(R.drawable.btn_clear_textfield_selector);
			}

			this.clearDrawable.setBounds(0, 0, 40, 40);
			this.setClearIconVisible(false);
		}
	}

	private void setClearIconVisible(boolean visible)
	{
		Drawable x = visible ? this.clearDrawable : null;
		this.setCompoundDrawables(this.getCompoundDrawables()[0], this.getCompoundDrawables()[1], x, this.getCompoundDrawables()[3]);
	}

	@Override
	public boolean onTouchEvent(MotionEvent aEvent)
	{
		if (this.getCompoundDrawables()[2] != null)
		{
			boolean tappedX = aEvent.getX() > this.getWidth() - this.getPaddingRight() - this.clearDrawable.getIntrinsicWidth();
			if (tappedX)
			{
				if (aEvent.getAction() == MotionEvent.ACTION_UP)
				{
					this.setText(new String());
				}
				
				return true;
			}
		}

		return super.onTouchEvent(aEvent);
	}

	@Override
	protected void onTextChanged(CharSequence aText, int aStart, int aLengthBefore, int aLengthAfter)
	{
		super.onTextChanged(aText, aStart, aLengthBefore, aLengthAfter);
		this.setClearIconVisible(!TextUtils.isEmpty(aText));
	}

	@Override
	protected void onSizeChanged(int aW, int aH, int aOldw, int aOldh)
	{
		super.onSizeChanged(aW, aH, aOldw, aOldh);
	}

	@Override
	protected void onMeasure(int aWidthMeasureSpec, int aHeightMeasureSpec)
	{
		super.onMeasure(aWidthMeasureSpec, aHeightMeasureSpec);

		if (!this.isInEditMode())
		{
			int h = this.getMeasuredHeight() - this.getPaddingTop() - this.getPaddingBottom();
			this.clearDrawable.setBounds(0, 0, h, h);
		}
	}

}
