package at.bakery.kippen.server.outlets;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;


/** JDK 6 or before. */
public class JframeKippOutlet extends AbstractKippOutlet{

	protected HashMap<String, JLabel> JLabels;

	/**
	 * @param filename
	 *            is an existing file which can be written to.
	 * @throws FileNotFoundException
	 *             if the file does not exist.
	 * @throws IOException
	 *             if problem encountered during write.
	 *             
	 */
	public JframeKippOutlet(HashMap<String, JLabel> Jlabels) {
		
		this.JLabels = JLabels;
		

	}

	public void output(Map<String, String> InfoData)  {
		
		for (Map.Entry<String, String> entry :  InfoData.entrySet()) {
			JLabels.get(entry.getKey()).setText(entry.getValue());
		}

	}

	@Override
	public void output() {
		// TODO Auto-generated method stub
		
	}

}