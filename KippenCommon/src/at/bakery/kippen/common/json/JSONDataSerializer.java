package at.bakery.kippen.common.json;

import java.io.UnsupportedEncodingException;

import at.bakery.kippen.common.AbstractData;

import com.google.gson.Gson;

public class JSONDataSerializer {

	public static byte[] serialize(AbstractData data) {
		String json = new Gson().toJson(data);
		String result = data.getClass().getCanonicalName() + "\n" + json + "\n";
		
		try {
			return result.getBytes("UTF8");
		} catch (UnsupportedEncodingException e) {
			return result.getBytes();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static AbstractData deserialize(String canonicalClass, String json) throws Exception {
		Class cls = Class.forName(canonicalClass);
		AbstractData data = new Gson().fromJson(json, cls);
		
		return data;
	}
}
