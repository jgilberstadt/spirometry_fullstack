package shared;

import android.util.Log;

public final class STTrace {
	private STTrace() {}
	private static String _tag = "";
	
	public static void setTag(String tag) {
		STTrace._tag = tag;
	}
	
	public static void line(String message) {
		STTrace.output(message);
	}
	
	public static void method(String methodName) {
		String className = STTrace.removePackageFromClassName(Thread.currentThread().getStackTrace()[3].getClassName());
		STTrace.output(className + "." + methodName);
	}
	
	public static void method(String methodName, String message) {
		String className = STTrace.removePackageFromClassName(Thread.currentThread().getStackTrace()[3].getClassName());
		STTrace.output(className + "." + methodName + " " + message);
	}
	
	public static void error(String message) {
		Log.e(STTrace._tag, "! " + message);
	}
	
	public static void exception(Exception ex) {
		STTrace.error("exception: " + ex.getMessage() + "\n" + Log.getStackTraceString(ex));
	}
	
	public static String byteArrayToHexDump(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		int count = 0;
		for (byte b: bytes) {
			builder.append(String.format("%02x", b&0xff));
			if (++count % 16 == 0) {
				builder.append("\n");
			}
		}
		return builder.toString();
	}

	public static String byteArrayToString(byte[] bytes) {
		StringBuilder builder = new StringBuilder();
		for (byte b: bytes) {
			if (b >= 0x20 && b <= 0x7F) {
				builder.append((char) b);
			}
			else {
				builder.append('.');
			}
		}
		return builder.toString();
	}

	private static void output(String message) {
		Log.d(STTrace._tag, message);
	}
	
	private static String removePackageFromClassName(String className) {
		int lastDotIndex = className.lastIndexOf('.');
		if (lastDotIndex == -1)
			return className;
		return className.substring(lastDotIndex + 1);
	}
}
