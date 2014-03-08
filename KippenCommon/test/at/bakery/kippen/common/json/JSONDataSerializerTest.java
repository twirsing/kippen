package at.bakery.kippen.common.json;

import org.junit.Test;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.SensorTripleData;
import at.bakery.kippen.common.data.WifiLevelsData;

import com.google.gson.Gson;

public class JSONDataSerializerTest {

	@Test
	public void testSimple() throws Exception {
		SensorTripleData std = new SensorTripleData(1, 2, 3);
		System.out.println(new String(JSONDataSerializer.serialize(std), "UTF8"));
		
		ArrayDummyData add = new ArrayDummyData();
		System.out.println(JSONDataSerializer.serialize(add));
		
		ArrayDummyData2 add2 = new ArrayDummyData2();
		System.out.println(JSONDataSerializer.serialize(add2));
		
		System.out.println(new Gson().toJson(std));
		System.out.println(new Gson().toJson(add2));
		
		String json = new Gson().toJson(add);
		System.out.println(new Gson().fromJson(json, ArrayDummyData.class));
		
		WifiLevelsData wld = new WifiLevelsData();
		wld.setNetwork("abc", -100);
		wld.setNetwork("hadf", -40);
		json = new Gson().toJson(wld);
		System.out.println(new Gson().fromJson(json, WifiLevelsData.class));
		
		System.out.println(JSONDataSerializer.deserialize(wld.getClass().getCanonicalName(), json));
	}
	
	private class ArrayDummyData extends AbstractData {

		public ArrayDummyData() {
			setDouble("val", 5, 4, 3, 2, 1);
		}
	}
	
	private class ArrayDummyData2 extends AbstractData {

		public ArrayDummyData2() {
			setString("val", "hallo", "huhu", "juhu", "lala");
		}
	}

}
