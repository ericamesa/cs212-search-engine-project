import java.util.HashMap;
import java.util.Map;

// TODO Homework: Fill in or fix all of the methods with TODO comments.

/**
 * Parses command-line arguments into flag/value pairs, and stores those pairs
 * in a map for easy access.
 */
public class ArgumentMap {

	private final Map<String, String> map;

	/**
	 * Initializes the argument map.
	 */
	public ArgumentMap() {
		map = new HashMap<>();
	}

	/**
	 * Initializes the argument map and parses the specified arguments into
	 * key/value pairs.
	 *
	 * @param args
	 *            command line arguments
	 *
	 * @see #parse(String[])
	 */
	public ArgumentMap(String[] args) {
		this();
		parse(args);
	}

	/**
	 * Parses the specified arguments into key/value pairs and adds them to the
	 * argument map.
	 *
	 * @param args
	 *            command line arguments
	 */
	public void parse(String[] args) {
		String key = null, value = null, prev = null, curr; 

		// TODO An alternative
//		for (int i = 0; i < args.length; i++) {
//			
//			if (isFlag(args[i])) {
//				if (i + 1 < args.length && isValue(args[i + 1])) {
//					map.put(args[i], args[i + 1]);
//					i++;
//				}
//				else {
//					map.put(args[i], null);
//				}
//			}
//			
//		}
		
		
		for (int i = 0; i < args.length; i++) {
			curr = args[i];
			if (prev == null){
				if (isFlag(curr)) {
					key = curr;
				}
			}
			else {
				if (isFlag(curr)) {
					if (isFlag(prev)) {
						map.put(key, value);
						key = null;
						value = null;
					}
					key = curr;
				}
				else {
					if (isFlag(prev)) {
						value = curr;
						map.put(key, value);
						key = null;
						value = null;
					}
				}
			}
			prev = curr;
		}
		if (key != null) map.put(key, value);
	}

	/**
	 *
	 * @param arg
	 * @return
	 */
	public static boolean isFlag(String arg) {
		// TODO An alternate approach
		// return arg != null && arg.trim().length() > 1 && arg.startsWith("-");
		
		if (arg != null && arg.length() > 1) {
			if (arg.charAt(0) == '-' && arg.charAt(1) != ' ') return true;
		}
		return false;
	}

	/**
	 *
	 * @param arg
	 * @return
	 */
	public static boolean isValue(String arg) {
		if (arg != null && arg.length() >= 1) {
			if (arg.charAt(0) != '-' && (Character.isDigit(arg.charAt(0)) ||  Character.isLetter(arg.charAt(0)))) return true;
		}
		return false;
	}

	/**
	 * Returns the number of unique flags stored in the argument map.
	 *
	 * @return number of flags
	 */
	public int numFlags() {
		return map.size();
	}

	/**
	 * Determines whether the specified flag is stored in the argument map.
	 *
	 * @param flag
	 *            flag to test
	 *
	 * @return true if the flag is in the argument map
	 */
	public boolean hasFlag(String flag) {
		return (map.containsKey(flag)) ? true : false;
	}

	/**
	 * Determines whether the specified flag is stored in the argument map and
	 * has a non-null value stored with it.
	 *
	 * @param flag
	 *            flag to test
	 *
	 * @return true if the flag is in the argument map and has a non-null value
	 */
	public boolean hasValue(String flag) {
		return (map.containsKey(flag) && map.get(flag) != null) ? true : false;
	}

	/**
	 * Returns the value for the specified flag as a String object.
	 *
	 * @param flag
	 *            flag to get value for
	 *
	 * @return value as a String or null if flag or value was not found
	 */
	public String getString(String flag) {
		return map.get(flag); 
	}

	/**
	 * Returns the value for the specified flag as a String object. If the flag
	 * is missing or the flag does not have a value, returns the specified
	 * default value instead.
	 *
	 * @param flag
	 *            flag to get value for
	 * @param defaultValue
	 *            value to return if flag or value is missing
	 * @return value of flag as a String, or the default value if the flag or
	 *         value is missing
	 */
	public String getString(String flag, String defaultValue, String defaultNullValue) {
		// TODO Avoid the one-line if/else style, always include braces
		// TODO You can also configure Eclipse to always add the braces for you
		if (map.containsKey(flag)){
			if (map.get(flag) == null)
				return defaultNullValue;
			else
				return map.get(flag);
		}
		else
			return defaultValue; 
	}

	/**
	 * Returns the value for the specified flag as an int value. If the flag is
	 * missing or the flag does not have a value, returns the specified default
	 * value instead.
	 *
	 * @param flag
	 *            flag to get value for
	 * @param defaultValue
	 *            value to return if the flag or value is missing
	 * @return value of flag as an int, or the default value if the flag or
	 *         value is missing
	 */
	public int getInteger(String flag, int defaultValue) {
		Integer integer; 
		if (map.containsKey(flag)){
			try {
				integer = Integer.parseInt(map.get(flag));
			} 
			catch (NumberFormatException e) {
				return defaultValue;
			}
			return integer;
		}
			
		else
			return defaultValue; 
	}

	@Override
	public String toString() {
		return map.toString();
	}
}
