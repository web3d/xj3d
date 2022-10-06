/*****************************************************************************
 *                        Web3d.org Copyright (c) 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.web3d.vrml.renderer.common.input.dis;

// External Imports
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Local imports
import edu.nps.moves.dis.EntityID;
import edu.nps.moves.dis.EntityStatePdu;
import edu.nps.moves.disutil.PduFactory;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.FromContainsFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.Base64;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import org.web3d.util.PropertyTools;
import org.web3d.vrml.nodes.VRMLDISManagerNodeType;
import org.web3d.vrml.nodes.VRMLDISNodeType;
import org.web3d.vrml.nodes.VRMLNetworkInterfaceNodeType;

/** DIS Protocol Handler for XMPP packets containing ESPDUs
 *
 * @author Yumetech, Inc.
 * @version $Id: DISXMLConnectionHandler.java,v 1.8 2009-09-26 05:54:15 tbnorbra Exp $
 */
public class DISXMLConnectionHandler implements PacketListener {

    /**
     * The default order.
     */
    private static final int DEFAULT_ORDER = 2;

    /**
     * The default convergence interval.
     */
    private static final int DEFAULT_CONVERGENCE_INTERVAL = 200;

    /** Property describing the XMPP login name to use */
    private static final String USERNAME_PROP =
            "org.web3d.vrml.renderer.common.input.dis.username";

    /** The default username value */
    private static final String DEFAULT_USERNAME = "guest";

    /** Property describing the XMPP login password to use */
    private static final String PASSWORD_PROP =
            "org.web3d.vrml.renderer.common.input.dis.password";

    /** The default password value */
    private static final String DEFAULT_PASSWORD = "guest";

    /** The username to use */
    private static String username;

    /** The password to use */
    private static String password;

    private DatagramSocket socket;

    InetAddress address;

    Thread readThread;

    private int port;

    private String group;

    int cnt;

    private LinkedList liveList;

    // Scratch id to avoid gc
    private DISId disId;

    // Scratch translation field
    private float[] translation;

    // Scratch rotation field
    private float[] rotation;

    private float[] dRorientation;

    /** The node to ID mapping */
    private Map<DISId, NodeMapEntry> nodeMap;

    /** The list of managers */
    private List<VRMLDISNodeType> managerList;

    /** The Entities we've placed on the addedEntities */
    private Set<DISId> notifiedSet;

    /** Connection to the XMPP server */
    private XMPPConnection connection;

    /** Multiuser chat room */
    private MultiUserChat load;

    /** Packet Filter */
    private PacketFilter filter;

    /** Is this connection live */
    private boolean live;

    /** The simulation start time for calculating time stamps */
    private static long simStartTime;

    // XMPP connection vars
    private final String xmppUsername;

    private final String xmppPassword;

    private final String[] xmppAuthServer;

    private final String xmppMucServer;

    private final String xmppMucRoom;

    private final String randomId;

    private final String xmppId;

    private EntityStatePdu espdu;

    static {
        // Subtract out 1 day so clients starting at different times will not throw packets
        simStartTime = System.currentTimeMillis() - 24 * 60 * 60 * 1_000;

        username = PropertyTools.fetchSystemProperty(USERNAME_PROP, DEFAULT_USERNAME);
        password = PropertyTools.fetchSystemProperty(PASSWORD_PROP, DEFAULT_PASSWORD);
    }

    /**
     *
     * @param nodeMap
     * @param liveList
     * @param managerList
     * @param notifiedSet
     * @param group
     * @param port
     * @param xUsername
     * @param xPassword
     * @param xAuthServer
     * @param xMucServer
     * @param xMucRoom
     */
    public DISXMLConnectionHandler(Map<DISId, NodeMapEntry> nodeMap, LinkedList liveList, List<VRMLDISNodeType> managerList,
            Set<DISId> notifiedSet, String group, int port, String xUsername, String xPassword,
            String[] xAuthServer, String xMucServer, String xMucRoom) {

        this(group, port, xUsername, xPassword, xAuthServer, xMucServer, xMucRoom, "Xj3D_");

        this.nodeMap = nodeMap;
        this.liveList = liveList;
        this.managerList = managerList;
        this.notifiedSet = notifiedSet;
    }

    /** Default constructor
     * @param group a group name
     * @param port to connect to i.e. 5222
     * @param xUsername XMPP username
     * @param xPassword XMPP password
     * @param xAuthServer XMPP server
     * @param xMucServer multi-user server
     * @param xMucRoom multi-user chat room
     * @param id unique XMPP ID
     */
    public DISXMLConnectionHandler(String group, int port, String xUsername, String xPassword,
            String[] xAuthServer, String xMucServer, String xMucRoom, String id) {

        this.group = group;
        this.port = port;
        live = false;

        this.xmppUsername = (xUsername != null) ? xUsername : username;
        this.xmppPassword = (xPassword != null) ? xPassword : password;
        this.xmppAuthServer = xAuthServer;
        this.xmppMucServer = xMucServer;
        this.xmppMucRoom = xMucRoom;
        this.xmppId = id;

        disId = new DISId(0, 0, 0);
        translation = new float[3];
        rotation = new float[4];
        dRorientation = new float[3];

        randomId = xmppId + Integer.toString((int) (Math.random() * Integer.MAX_VALUE), Character.MAX_RADIX);

        System.out.println("XMPP Params: " + xmppAuthServer[0] + " " + xmppMucRoom);
        login(xmppUsername, xmppPassword, xmppAuthServer, xmppMucServer, xmppMucRoom);
    }

    /**
     * Login into the XMPP chat room.
     *
     * @param username The username to use
     * @param password The password to use
     * @param authServers The authentication servers
     * @param mucServer The multiuser chat server
     * @param mucRoom The multiuser chat room to join
     */
    private void login(String username,
            String password,
            String[] authServers,
            String mucServer,
            String mucRoom) {

        try {
            System.out.println("Attempting login for: " + username);

            if (authServers == null || mucServer == null || mucRoom == null) {
                System.out.println("Invalid XMPP params.  Using defaults for now");
                authServers = new String[] {"savage.nps.edu"};
                mucServer = "conference.savage.nps.edu";
                mucRoom = "auvw";
            }

            int tries = 2;

            while (connection == null && tries > 0) {
                // Authenticate to our local XMPP server
                for (String authServer : authServers) {
                    try {
                        System.out.println(xmppId + " trying authServer: " + authServer);
                        connection = new XMPPConnection(authServer);
                        connection.connect();
                        connection.login(username, password, randomId);
                    } catch (XMPPException e) {
                        // ignore and move on
                    }

                    if (connection != null) {
                        System.out.println(xmppId + " connected to: " + authServer);
                        break;
                    }
                }

                tries--;
            }

            // Establish a connection to the MUC room
            String mucJid = mucRoom + "@" + mucServer;
            System.out.println(xmppId + " connecting to MUC room: " + mucJid);
            DiscussionHistory dh = new DiscussionHistory();
            dh.setMaxStanzas(0);

            //RoomInfo info = MultiUserChat.getRoomInfo(connection, mucJid);
            //System.out.println("Number of occupants:" + info.getOccupantsCount());

            load = new MultiUserChat(connection, mucJid);

            int numRetries = 0;
            boolean connected = false;

            System.out.println(xmppId + " logging into chatroom with username: " + username + randomId);
            while (!connected && numRetries < 5) {
                try {
                    load.join(username + randomId, password, dh, SmackConfiguration.getPacketReplyTimeout());
                    connected = load.isJoined();
                } catch (XMPPException e) {
                    System.err.println(xmppId + " failed to connect, retrying");
                    System.err.println(e);
                }
                numRetries++;
            }

            if (load.isJoined()) {
                System.out.println(xmppId + " successfully joined chat");
                // set up a packet filter to listen for only the things we want
                PacketFilter localFilter = new AndFilter(new PacketTypeFilter(Message.class),
                        new FromContainsFilter(mucJid));

                connection.addPacketListener(this, localFilter);
                live = true;
            } else {
                System.err.println("***Couldn't join chat room");
            }

        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /** Don't use FastEntityStatePdu */
    PduFactory pduFactory = new PduFactory(false);

    /**
     * Process XMPP received packets
     * @param packet the XMPP packet to parse
     */
    @Override
    public void processPacket(Packet packet) {

        // We only want to listen to packets meant for Xj3D
        if (!randomId.contains("Xj3D_")) {return;}

        cnt++;

        if (cnt % 100 == 0) {
//            System.out.println("Packets received: " + cnt);
        }
        EntityID eid;
        NodeMapEntry entry;
        VRMLDISNodeType di;
        long time;
        long timestamp;

        Collection<String> it = packet.getPropertyNames();

        if (it.isEmpty()) {
//            System.out.println("empty properties in packet");
            return;
        }

        // Properties set for DIS packets:
        // messageType:    dis
        // disInformation: the text representation of DIS
        // disFormat:      XML or base64
        // sender:         host that sent this
        // port:           port to send on
        // multicastAddress: multicast group to send on

        // Properties for rollcall messages:
        // messageType: rollcall
        // requestID:   request ID, a unique identifier for this request

        // Properties for rollcall response messages:
        // messageType: rollcallResponse
        // bridgeName: bridgeName (user this bridge is logged in as)
        // request ID: request ID, tied to the ID of the original request

        String messageType = (String) packet.getProperty("messageType");

        if (messageType == null) {
            return;
        }

        // This XMPP type carries a DIS payload.
        if (messageType.equalsIgnoreCase("dis")) {
            String base64 = (String) packet.getProperty("disInformation");
            String disFormat = (String) packet.getProperty("disFormat");

            if (disFormat.equalsIgnoreCase("base64")) {
                try {

//                    System.out.println("DISXML packet length is: " + xml.length());
                    ByteBuffer buff = ByteBuffer.wrap(Base64.decode(base64));

                    // Convert the XMPP XML DIS message to a java object, then marshall it to
                    // IEEE-DIS format and send it out on the local network.
                    espdu = (EntityStatePdu) pduFactory.createPdu(buff);

                    // This seems to happen from time to time, prevent NPE
                    if (espdu == null) {
                        return;
                    }

                    // TODO: Need to handle other types
                    eid = espdu.getEntityID();
                    disId.setValue(eid.getSite(), eid.getApplication(), eid.getEntity());
                    entry = nodeMap.get(disId);

                    if (entry == null) {

                        int len = managerList.size();

                        VRMLDISManagerNodeType manager;

                        for (int i = 0; i < len; i++) {

                            manager = (VRMLDISManagerNodeType) managerList.get(i);

                            if (!notifiedSet.contains(disId)) {
                                manager.entityArrived(espdu);

                                // Clone Id to put on list
                                notifiedSet.add((DISId) disId.clone());
                            }
                        }
                        return;
                    }

                    di = entry.node;

                    if (di.getRole() != VRMLNetworkInterfaceNodeType.ROLE_READER) {
                        System.out.println("Ignoring ESPDU");
                        // Ignore for non readers
                        return;
                    }

                    time = System.currentTimeMillis();
                    timestamp = espdu.getTimestamp();

                    if (di != null) {
                        if (entry.listEntry != null) {
                            // update last time
                            LiveListEntry lle = (LiveListEntry) entry.listEntry;

                            if (timestamp > lle.espduTimestamp) {
                                lle.avgTime = lle.avgTime + (time - lle.lastTime) / 5.0f;
                                lle.lastTime = time;
                                if (lle.currEspdu != null) {
                                    lle.lastEspdu = lle.currEspdu;
                                }
                                lle.currEspdu = espdu;
                                lle.closeEnough = false;
                                lle.newPackets = true;
                            } else {
                                System.out.println("Tossing packet: " + timestamp + " last: " + lle.espduTimestamp);
                            }
                        } else {
                            // create new entry
                            LiveListEntry newlle = new LiveListEntry(di, System.currentTimeMillis());
                            entry.listEntry = newlle;
                            newlle.lastEspdu = espdu;
                            newlle.currEspdu = espdu;
                            newlle.rotationConverger = new OrderNQuat4dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                            newlle.translationConverger = new OrderNVector3dConverger(DEFAULT_ORDER, DEFAULT_CONVERGENCE_INTERVAL, null);
                            newlle.espduTimestamp = timestamp;
                            newlle.closeEnough = false;
                            newlle.avgTime = 0.01f;
                            newlle.newPackets = true;

                            liveList.add(newlle);
                            di.setIsActive(true);
                        }

                    } else {
                        //System.out.println("Unknown entity: " + eid);
                    }
                } catch (Exception e) {
                    System.err.println("Can't reconsitute XML XMPP information " + e);
                    e.printStackTrace(System.err);
                }
            }
        }
    }

    /**
     * Write a PDU's contents to the default XMPP destination.  It is up to the
     * user to clear the ByteBuffer if it is to be reused.
     *
     * @param pdu the DIS PDU data to be written
     */
    public void write(ByteBuffer pdu) {
        if (!live) {
            return;
        }

        try {

            String disString = Base64.encodeBytes(pdu.array());

            // There are two ways to send messages: the "standard" way, via load.sendMessage(String),
            // and by attaching properties to the message. The second technique is pretty similar to
            // using packet extensions. You can add properties and values to packets. But it is also
            // non-standard for most clients, and the messages will not show up. The first method
            // is good for getting traffic to show up in chat rooms in exodous, iChat, etc.

            // Create a message using properties
            Message message = load.createMessage();
            message.setProperty("disInformation", disString);
            message.setProperty("messageType", "dis");
            message.setProperty("disFormat", "base64");

            // The properties method--send the message
            load.sendMessage(message);
        } catch (XMPPException e) {
            System.err.println(e);
        }
    }
}
