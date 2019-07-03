package nl.yspierings.beersync;

import java.io.*;
import java.text.*;
import android.os.*;
import android.util.*;

public class XLog
{
	private static final byte LOG_TYPE_DEBUG = 1;
	private static final byte LOG_TYPE_ERROR = 2;

	private static boolean isDebuggable = true;
	private static String logTag = null;
	private static RandomAccessFile outLog;
	private static DateFormat dateFormat;

	/**
	 * Write a debug message to the log.
	 * 
	 * @param msg
	 */
	public static void debug(String msg)
	{
		internalLog(msg, null, LOG_TYPE_DEBUG);
	}

	/**
	 * Write a debug message to the log.
	 * 
	 * @param msg
	 * @param tr
	 */
	public static void debug(String msg, Throwable tr)
	{
		internalLog(msg, tr, LOG_TYPE_DEBUG);
	}

	/**
	 * Write a error message to the log.
	 * 
	 * @param msg
	 */
	public static void error(String msg)
	{
		internalLog(msg, null, LOG_TYPE_ERROR);
	}

	/**
	 * Write a error message to the log.
	 * 
	 * @param msg
	 * @param tr
	 */
	public static void error(String msg, Throwable tr)
	{
		internalLog(msg, tr, LOG_TYPE_ERROR);
	}

	/**
	 * Set the log tag. If the tag is null The classname will be used as tag.
	 * 
	 * @param tag
	 */
	public static void setSpecificTag(String tag)
	{
		logTag = tag;
	}

	/**
	 * If debuggable is true, the app is able to send debug messages. Otherwise it
	 * doesn't.
	 * 
	 * @param debuggable
	 */
	public static void setDebuggable(boolean debuggable)
	{
		isDebuggable = debuggable;
	}

	public static String getLogFileContents()
	{
		//openLogFile();

		if (outLog == null)
		{
			return "Output log can't be read.";
		}

		try
		{
			byte[] buffer = new byte[(int) outLog.length()];
			outLog.seek(0);
			outLog.readFully(buffer);
			return new String(buffer);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}

		return "Output log can't be read.";
	}

	public static void clearLog()
	{
		if (outLog != null)
		{
			String storageState = Environment.getExternalStorageState();
			if (storageState.equals(Environment.MEDIA_MOUNTED))
			{
				File outFile = new File(Environment.getExternalStorageDirectory(), "beersynclog.txt");
				try
				{
					outLog.close();
					outFile.delete();
					outLog = null;
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	private static void openLogFile()
	{
		if (outLog == null)
		{
			String storageState = Environment.getExternalStorageState();
			if (storageState.equals(Environment.MEDIA_MOUNTED))
			{
				File outFile = new File(Environment.getExternalStorageDirectory(), "beersynclog.txt");
				try
				{
					outLog = new RandomAccessFile(outFile, "rw");
				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}
		}
	}

	private static void internalLog(String rawMessage, Throwable thr, byte logType)
	{
		//openLogFile();

//		if (dateFormat == null)
//		{
//			dateFormat = new SimpleDateFormat("HH:mm:ss:SSS");
//		}
//		String time = dateFormat.format(new Date(System.currentTimeMillis()));
//
//		String message = String.format("[%s] %s\n", time, rawMessage);
//
//		if (outLog != null)
//		{
//			try
//			{
//				outLog.seek(outLog.length());
//				outLog.write(message.getBytes());
//
//				if (thr != null)
//				{
//					outLog.seek(outLog.length());
//					outLog.write((thr.getMessage() + "\n").getBytes());
//					for (StackTraceElement ste : thr.getStackTrace())
//					{
//						outLog.seek(outLog.length());
//						outLog.write(("\t" + ste.toString() + "\n").getBytes());
//					}
//				}
//			}
//			catch (IOException ex)
//			{
//				// TODO Auto-generated catch block
//				ex.printStackTrace();
//			}
//		}

		if (isDebuggable)
		{
			StackTraceElement callerParentMethod = Thread.currentThread().getStackTrace()[5];
			StackTraceElement callerMethod = Thread.currentThread().getStackTrace()[4];

			String className = callerMethod.getClassName().substring(callerMethod.getClassName().lastIndexOf('.') + 1);
			String tag = logTag;
			String msg;
			if (tag == null)
			{
				logTag = className;
				msg = String.format("[%s][%s] - %s", callerParentMethod.getMethodName(), callerMethod.getMethodName(), rawMessage);
			}
			else
			{
				msg = String.format("<%s> [%s][%s] - %s", className, callerParentMethod.getMethodName(), callerMethod.getMethodName(), rawMessage);
			}

			if (logType == LOG_TYPE_DEBUG)
			{
				Log.d(tag, msg, thr);
			}
			else if (logType == LOG_TYPE_ERROR)
			{
				Log.e(tag, msg, thr);
			}
		}
	}
}
