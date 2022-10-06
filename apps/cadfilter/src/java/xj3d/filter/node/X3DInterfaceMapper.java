/*****************************************************************************
 *                        Copyright Yumetech, Inc (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/gpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.node;

// External imports
import java.io.InputStream;
import java.io.IOException;

import java.security.AccessController;
import java.security.PrivilegedAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

// local imports
import xj3d.filter.node.X3DConstants.TYPE;

/**
 * Utility to provide X3D node inheritance.
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
class X3DInterfaceMapper {

    /** The config file for initializing the inheritance map */
    private static final String NODE_MAP_FILENAME = "config/3.2/nodemap.xml";

    /** Node inheritance map, key = (String)nodeName, value = String[]{ parentNodeNames } */
    private Map<String, String[]> iNameMap;

    /** Node inheritance map, key = (String)nodeName, value = TYPE[]{ ancestorNodeTypes } */
    private HashMap<String, TYPE[]> iTypeMap;

	/**
	 * Constructor
	 */
	X3DInterfaceMapper() {
		initializeIMap();
		int num_node = iNameMap.size();
		iTypeMap = new HashMap<>(num_node);
	}

    /**
     * Return the array of abstract node types for the named argument node
     *
     * @param node_name The node for which to determine the interfaces
     * @return The array of abstract node types. If the named node does not inherit
     * from any known interfaces, an empty array is returned. If the named node is
	 * unknown, null is returned.
     */
    TYPE[] getTypes(String node_name) {

		TYPE[] type = iTypeMap.get(node_name);
		if (type == null) {
			if (iNameMap.containsKey(node_name)) {
				ArrayList<TYPE> type_list = new ArrayList<>();
				getTypes(node_name, type_list);
				int num_type = type_list.size();
				type = new TYPE[num_type];
				if (num_type > 0) {
					type = type_list.toArray(type);
				}
			}
		}
        return(type);
    }

	/**
	 * Accumulate all the types for the specified node name into the argument list
	 *
	 * @param node_name The name of the node (or interface) to search
	 * @param type_list The aggregate list of types
	 */
	private void getTypes(String node_name, ArrayList<TYPE> type_list) {
		String[] type_name_array = iNameMap.get(node_name);
		if (type_name_array != null) {
			int num = type_name_array.length;
			for (int i = 0; i < num; i++) {
				String type_name = type_name_array[i];
				TYPE type = TYPE.valueOf(type_name);
				if (type != null) {
					type_list.add(type);
				}
				getTypes(type_name, type_list);
			}
		}
	}

    /**
     * Retrieve the configuration file containing the node inheritance
     * data and initialize the local map with it.
     */
    private void initializeIMap( ) {

        InputStream is = AccessController.doPrivileged(
            new PrivilegedAction<InputStream>( ) {
                @Override
                public InputStream run( ) {
                    return ClassLoader.getSystemResourceAsStream( NODE_MAP_FILENAME );
                }
            }
            );

        // Fallback mechanism for WebStart
        if( is == null ) {
            ClassLoader cl = X3DInterfaceMapper.class.getClassLoader( );
            is = cl.getResourceAsStream( NODE_MAP_FILENAME );
        }

        Document nodemap_doc = null;
        try {
            DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance( );
            fac.setValidating( false );
            fac.setNamespaceAware( false );
            DocumentBuilder builder = fac.newDocumentBuilder( );
            nodemap_doc = builder.parse( is );
        } catch( FactoryConfigurationError | ParserConfigurationException | SAXException | IOException fce ) {
            //System.out.println( "X3DNodeTypeMapper: FactoryConfigurationError: "+ fce.getMessage( ) );
        }

        if ( nodemap_doc == null ) {
            System.out.println( "X3DInterfaceMapper: abstract type inheritance mapping is unavailable" );
            iNameMap = Collections.unmodifiableMap(new HashMap<String,String[]>(0));
        } else {
            Element root_element = nodemap_doc.getDocumentElement( );

            NodeList node_list = root_element.getElementsByTagName( "node" );
            int num_nodes = node_list.getLength( );
            Map<String,String[]> intf_map = new LinkedHashMap<>( num_nodes );

            for ( int i = 0; i < num_nodes; i++ ) {
                Element node_element = (Element)node_list.item( i );
                String node_name = node_element.getAttribute( "name" );
                NodeList parent_list = node_element.getElementsByTagName( "parent" );
                int num_parents = parent_list.getLength( );
                String[] parent = new String[num_parents];
                for ( int j = 0; j < num_parents; j++ ) {
                    Element parent_element = (Element)parent_list.item( j );
                    parent[j] = parent_element.getAttribute( "name" );
                }
                intf_map.put( node_name, parent );
            }
            iNameMap = Collections.unmodifiableMap(intf_map);
        }
    }
}
