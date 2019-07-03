package nl.yspierings.beersync.models.untappd;

public interface IResultHandler<T>
{
	void onComplete(T result);
}
