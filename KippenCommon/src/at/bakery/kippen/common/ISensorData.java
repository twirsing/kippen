package at.bakery.kippen.common;

public interface ISensorData {

	// get the data with its associated timestamp
	public DataWithTimestamp getData();
	
	public class DataWithTimestamp {
		
		public Object data;
		public long timestamp;
		
		public DataWithTimestamp(Object data, long timestamp) {
			this.data = data;
			this.timestamp = timestamp;
		}
		
		public Object getData() {
			return data;
		}
		
		public long getTimestamp() {
			return timestamp;
		}
	}
}
