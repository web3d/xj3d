package org.web3d.vrml.nodes;

// External imports
// None

// Local imports
// None

public interface VRMLBREPNodeType extends VRMLChildNodeType {

    void render();

    //Object type should be BaseCADKernelRenderer, casted to object to solve compilation dependency issue
    void set_renderer(Object renderer);
}
