package eu.supersede.mdm.storage.util;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class JSONUtil {

	public static boolean isObject(Object json) {
		return json != null && json.getClass().getName().equals(JSONObject.class.getName());
	}

	public static boolean isArray(Object json) {
		return json != null && json.getClass().getName().equals(JSONArray.class.getName());
	}

	public static boolean isAttribute(Object json) {
		if (json == null)
			return true;

		String name = json.getClass().getName();
		return !(name.equals(JSONArray.class.getName()) || name.equals(JSONObject.class.getName()));
	}
}
