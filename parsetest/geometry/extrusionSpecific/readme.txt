Notes:

Three identified problems with extrusion:

1. In "VarietyOfShapes.x3d" the bottom object displays nicely in Flux but does not appear very nicely in our browser.  Closer examination of the faces of this object indicates that the object is indeed being defined correctly, but somehow the resulting indexedFaceSet is not smoothing the faces properly.

2. In "ExtrusionExampleConcave.x3d" the left-most triangular object has a flat triangle for the beginCap, instead of a boomerang shape.  Boomerang endCap looks correct, however.

3. When looking at "../extrusion-textured.x3dv" the texture does not appear correctly.  Are the texture coordinates all set to zero?



@author Eric Fickenscher
@version Wed, Sep. 24, 2008
