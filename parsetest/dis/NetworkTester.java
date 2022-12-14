
import edu.nps.moves.disenum.CountryType;
import edu.nps.moves.disenum.EntityDomain;
import edu.nps.moves.disenum.EntityKind;
import edu.nps.moves.disenum.ForceID;
import edu.nps.moves.dis.*;
import edu.nps.moves.net.BehaviorProducerUDP;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.StringTokenizer;

public class NetworkTester {

    public static int numEntities = 50;
    public static int pauseTime = 100;
    public static final String DATA_LAYOUT =
      "# EntityXLocation EntityYLocation EntityZLocation velocityX velocityY velocityZ Psi Theta Phi AngVelX AngVelY AngVelZ\n"
    + " 4   14  1.5 3  0   0   0   -.1 0   0   0   0 \n"
    + " 7   14  1.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "10   14  2   3   0   .25 0   -.1 0   0   0   0 \n"
    + "13   14  2.25    3   0   .25 0   -.1 0   0   0   0 \n"
    + "16   14  2.5 3   0   .25 0   -.1 0   0   0   0 \n"
    + "19   14  2.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "22   14  3   3   0   .25 0   -.1 0   0   0   0 \n"
    + "25   14  3.25    3   0   .25 0   -.1 0   0   0   0 \n"
    + "28   14  3.5 3   0   .25 0   -.1 0   0   0   0 \n"
    + "31   14  3.75    3   0   .25 0   -.1 0   0   0   0 \n"
    + "34   14  4   1   1   .25 0.785   -.1 0   0   0   0 \n"
    + "35   15  4.25    0   1   .25 1.5708  -.1 0   0   0   0 \n"
    + "35   16  4.5 0   1   .25 1.5708  -.1 0   0   0   0 \n"
    + "35   17  4.75    -1  1   .25 2.355   -.1 0   0   0   0 \n"
    + "34   18  5   -3  0   0   3.1416  .1  0   0   0   0 \n"
    + "31   18  4.75    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "28   18  4.5 -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "25   18  4.25    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "22   18  4   -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "19   18  3.75    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "16   18  3.5 -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "13   18  3.25    -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "10   18  3   -3  0   -.25    3.1416  .1  0   0   0   0 \n"
    + "7    18  2.75    -3  0   -.25    3.9221  .1  0   0   0   0 \n"
    + "4    18  2.5 -1  -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    17  2.25    0   -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    16  2   0   -1  -.25    4.7124  .1  0   0   0   0 \n"
    + "3    15  1.75    1   -1  -.25    5.497   .1  0   0   0   0 \n";

    private static final int PORT = 62040;
    private static final String GROUP = "224.2.181.145";

    public static void main(String args[]) {
        if (args.length > 1) {
            numEntities = Integer.parseInt(args[0]);
            pauseTime = 1000 / Integer.parseInt(args[1]);
        }

        System.out.println("Number entities: " + numEntities + " pauseTime: " + pauseTime);

        NetworkTester tester = new NetworkTester();
        tester.test();
    }

    public void test() {

        DatagramSocket socket = null;
        InetAddress address;
        int pause = 2500 / numEntities;
        float offset = -10;

        try {
            try {
                socket = new MulticastSocket(PORT);
                address = InetAddress.getByName(GROUP);
                ((MulticastSocket) socket).joinGroup(address);
            } catch (Exception e) {
                System.err.println("Unicast fallback");

                if (socket != null)
                    socket.close();

                socket = new DatagramSocket();
                address = InetAddress.getByName("localhost");
            }

            for (int i = 0; i < numEntities; i++) {
                if (i % 10 == 0) {
                    offset += 2.5;
                }
                launchEntity(socket, address, PORT, DATA_LAYOUT, 0, 1, i, 0, 0, offset);

                try {
                    Thread.sleep(pause);
                } catch (Exception e) {
                }
            }
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace(System.err);
        }
    }

    private void launchEntity(DatagramSocket socket, InetAddress address, int port, String path,
            int siteID, int appID, int entityID,
            float xoff, float yoff, float zoff) {

        Thread thread;

        BehaviorProducerUDP writer1 = new BehaviorProducerUDP(socket);
        writer1.setUseCopies(false);
        writer1.setDefaultDestination(address, port);

        Runner e1 = new Runner(writer1, path, siteID, appID, entityID, pauseTime);
        e1.setOffset(xoff, yoff, zoff);
        thread = new Thread(e1);
        thread.start();
    }
}

class Runner implements Runnable {

    EntityID id;
    BehaviorProducerUDP writer;
    String data;
    EntityStatePdu espdu;
    float xoff;
    float yoff;
    float zoff;
    int entityID;
    int pauseTime;
    ByteBuffer buffer;
    boolean sendingPDUs = true;
    StringTokenizer lineTokenizer;

    public Runner(BehaviorProducerUDP writer, String data, int siteID, int appID, int entityID, int pauseTime) {
        this.writer = writer;
        this.data = data;
        this.entityID = entityID;
        this.pauseTime = pauseTime;

        /***** Begin DIS Enumerations *****/
        id = new EntityID();
        id.setSite(siteID);
        id.setApplication(appID);
        id.setEntity(entityID);
        espdu = new EntityStatePdu();
        espdu.setEntityID(id);

        // Use ASCII for the character set
        espdu.getMarking().setCharacterSet((short) 1);
        Charset cs = Charset.forName("US-ASCII");
        byte[] charArray = cs.encode("DIS TESTER").array();
        espdu.getMarking().setCharacters(charArray);

        espdu.setForceId((short) ForceID.valueOf("FRIENDLY").getValue());

        EntityType entTyp = espdu.getEntityType();
        entTyp.setEntityKind((short) EntityKind.valueOf("PLATFORM").getValue());
        entTyp.setDomain((short) EntityDomain.valueOf("SUBSURFACE").getValue());
        entTyp.setCountry((short) CountryType.valueOf("UNITED_STATES").getValue());

        espdu.getArticulationParameters().clear();

        if (entityID == 0) {
            ArticulationParameter p = new ArticulationParameter();
            p.setParameterValue(10.0);
            espdu.getArticulationParameters().add(p);
        }
        /***** End DIS Enumerations *****/

        buffer = ByteBuffer.allocate(espdu.getLength());
    }

    public void setOffset(float x, float y, float z) {
        xoff = x;
        yoff = y;
        zoff = z;
    }

    @Override
    public void run() {
        int totalPacketsSent = 0;

        while (sendingPDUs) {
            String lineString;                   // one line from the string
            StringTokenizer itemTokenizer;

            // Check to see if we're beyond the time-out limit. If so, generate a fake event
            // that presses the "stop" button. This saves us having to write a bunch of
            // duplicate code.
            lineTokenizer = new StringTokenizer(data, "\r\n");

            // while we have more lines....
            while (lineTokenizer.hasMoreTokens() && sendingPDUs) {
                float pduValues[] = new float[12];  //holds x,y,z; dx,dy,dz; psi,theta,phi; angX,angY,angZ
                int valueCount;

                // get one line of input, then decode each token in that string
                lineString = lineTokenizer.nextToken();
                itemTokenizer = new StringTokenizer(lineString);
                valueCount = 0;

                while (itemTokenizer.hasMoreTokens()) {
                    float value;
                    String token;

                    token = itemTokenizer.nextToken();

                    // got a hash mark somewhere in the token; ignore all the rest
                    if (token.indexOf('#') != -1) {
                        break;
                    }

                    // Read the value into a float
                    value = Float.valueOf(token);
                    valueCount++;

                    // prevents array out of bounds if extra values present
                    if (valueCount > 12) {
                        break;
                    }

                    pduValues[valueCount - 1] = value;
                }

                if (valueCount == 0) // got a blank line; skip it, and don't send out a zero PDU
                {
                    continue;
                }

                // location
                Vector3Double v3d = new Vector3Double();
                v3d.setX(pduValues[0] + xoff);
                v3d.setY(pduValues[1] + yoff);
                v3d.setZ(pduValues[2] + zoff);
                espdu.setEntityLocation(v3d);

                // velocity
                Vector3Float v3f = new Vector3Float();
                v3f.setX(pduValues[3]);
                v3f.setY(pduValues[4]);
                v3f.setZ(pduValues[5]);
                espdu.setEntityLinearVelocity(v3f);

                // orientation
                Orientation orien = new Orientation();
                orien.setPsi(pduValues[6]);
                orien.setTheta(pduValues[7]);
                orien.setPhi(pduValues[8]);
                espdu.setEntityOrientation(orien);

                // angular velocity
                v3f.setX(pduValues[9]);
                v3f.setY(pduValues[10]);
                v3f.setZ(pduValues[11]);
                espdu.getDeadReckoningParameters().setEntityAngularVelocity(v3f);

                buffer.clear();
                espdu.marshalWithNpsTimestamp(buffer);
                writer.write(buffer, espdu.getMarshalledSize());
                try {
                    Thread.sleep(pauseTime);
                } catch (Exception e) {
                }
            }
        }
    }
}
