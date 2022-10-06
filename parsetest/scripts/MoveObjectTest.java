/**
 * A test to show eventOut routing
 */
import vrml.*;
import vrml.field.*;
import vrml.node.*;

public class MoveObjectTest extends Script {
	SFVec3f location;
	SFRotation rotation;
	SFVec3f homeLocation;
	SFVec3f newLocation;
	SFRotation homeRotation;
	SFRotation newRotation;

	boolean flip=true;

    public MoveObjectTest() {
		homeLocation = new SFVec3f(0,0,0);	
		homeRotation = new SFRotation(0,0,0,0);
		newLocation = new SFVec3f(0,2,0);	
		newRotation = new SFRotation(0,1,0,0.5f);
    }

    @Override
    public void initialize() {
		location = (SFVec3f) getEventOut("location");
		rotation = (SFRotation) getEventOut("orientation");
    }

    @Override
    public void processEvent(Event evt) {
		if (flip) {
			flip=false;
			location.setValue(newLocation);
			rotation.setValue(newRotation);
		}
		else {
			flip=true;
			location.setValue(homeLocation);
			rotation.setValue(homeRotation);
		}

    }

    @Override
    public void shutdown() {
        System.out.println("Shutdown called");
    }
}
