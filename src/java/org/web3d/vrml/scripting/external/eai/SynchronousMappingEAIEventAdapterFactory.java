package org.web3d.vrml.scripting.external.eai;

/*****************************************************************************
 * Copyright North Dakota State University, 2001
 * Written By Bradley Vender (Bradley.Vender@ndsu.nodak.edu)
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 *****************************************************************************/

import org.web3d.vrml.nodes.VRMLClock;
import org.web3d.vrml.nodes.VRMLNodeType;
import org.web3d.vrml.scripting.external.buffer.*;

import java.util.Hashtable;
import java.util.Map;

/**
  * Implementation of EAIEventAdapterFactory.
  *  <p>
  * This implementation of EAIEventAdapterFactory maintains a mapping
  * from VRMLNodeType to ExternalEventAdapter to
 */

class SynchronousMappingEAIEventAdapterFactory
implements EAIEventAdapterFactory {

    /** Mapping of VRMLNodeType to ExternalEventAdapter */
    Map<VRMLNodeType, SynchronousEAIEventAdapter> adapterTable;

    /** The EAIFieldFactory to give the ExternalEventAdapter instances */
    EAIFieldFactory theFieldFactory;

    /** The clock to use for generating time stamps. */
    VRMLClock theClock;

    /** Basic constructor. */
    SynchronousMappingEAIEventAdapterFactory(VRMLClock clock) {
        adapterTable=new Hashtable<>();
        theClock=clock;
    }

    /** Retrieve or generate the ExternalEventAdapter associated with a node.
      * This is intended for lazy initialization of the event adapters.
      */
    @Override
    public ExternalEventAdapter getAdapter(VRMLNodeType node) {
        SynchronousEAIEventAdapter result=
            adapterTable.get(node);
        if (result==null) {
            result=new SynchronousEAIEventAdapter(theFieldFactory,node,theClock);
            adapterTable.put(node,result);
            node.addNodeListener(result);
        }
        return result;
    }

    /** Set the field factory to pass on. */
    @Override
    public void setFieldFactory(EAIFieldFactory aFactory) {
        theFieldFactory=aFactory;
    }

    /** Shutdown the event adapter system so that no more events are
      * sent out. */
    @Override
    public void shutdown() {
    }

}
