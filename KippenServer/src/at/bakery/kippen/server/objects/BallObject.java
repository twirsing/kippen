package at.bakery.kippen.server.objects;

import java.util.HashMap;

import org.encog.util.arrayutil.NormalizationAction;
import org.encog.util.arrayutil.NormalizedField;

import at.bakery.kippen.common.AbstractData;
import at.bakery.kippen.common.data.MoveData;
import at.bakery.kippen.server.EventTypes;

public class BallObject extends AbstractKippenObject {

	private double MOVE_DATA_THRESHHOLD = 0.1;
	private double MAX_LENGTH_VECTOR = 3.0;
	private boolean moveDataWasBelowThreshhold = false;

	public BallObject(String id) {
		super(id);
	}

	@Override
	public void processData(AbstractData data) {
		super.processData(data);

		if (data instanceof MoveData) {
			processMoveData((MoveData) data);
		}
	}

	private void processMoveData(MoveData data) {
		double lengthVector = Math.abs(Math.sqrt(data.getX() * data.getX() + data.getY() * data.getY() + data.getZ()
				* data.getZ()));
		HashMap<String, String> paramMap = new HashMap<String, String>();

		if (lengthVector > MOVE_DATA_THRESHHOLD) {

			NormalizedField normalizer = new NormalizedField(NormalizationAction.Normalize, null, MAX_LENGTH_VECTOR, 0.0, 1.0,
					0.0);
			double normalizedValue = normalizer.normalize(lengthVector);

			paramMap.put("value", String.valueOf(normalizedValue));

			executeCommands(paramMap, EventTypes.MOVE);
			moveDataWasBelowThreshhold = false;
		} else {
			// if we are below threshold set the value to 0
			if (moveDataWasBelowThreshhold == false) {
				paramMap.put("value", String.valueOf(0.0f));
				executeCommands(paramMap, EventTypes.MOVE);
				moveDataWasBelowThreshhold = true;
			}

		}
	}

}
