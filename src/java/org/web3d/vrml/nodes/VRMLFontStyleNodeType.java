/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/
package org.web3d.vrml.nodes;

// Standard imports
import java.awt.Font;

// Application specific imports
// none

/**
 * Describes a font in VRML.
 *
 * @author Alan Hudson, Justin Couch
 * @version $Revision: 1.9 $
 */
public interface VRMLFontStyleNodeType extends VRMLNodeType {

    /** The font style is PLAIN */
    int PLAIN_STYLE = Font.PLAIN;

    /** The font style is ITALIC */
    int ITALIC_STYLE = Font.ITALIC;

    /** The font style is BOLD */
    int BOLD_STYLE = Font.BOLD;

    /** The font style is BOLDITALIC */
    int BOLDITALIC_STYLE = Font.BOLD | Font.ITALIC;

    /** Justify to the beginning */
    int BEGIN_JUSTIFY = 1;

    /** Justify to the end of the string */
    int END_JUSTIFY = 2;

    /** Justify about the middle */
    int MIDDLE_JUSTIFY = 3;

    /**
     * Justify about the first character of the line. FIRST and BEGIN are
     * equivalent values as per VRML spec.
     */
    int FIRST_JUSTIFY = 4;

    /**
     * Fetch the AWT font description that matches the internal field settings.
     *
     * @return The font that is based on the fields
     */
    Font getFont();

    /**
     * Get the horizontal justification flag. Uses the constants defined in
     * this interface. This is the real justification after it has been
     * modified according to the effects of the horizontal field
     *
     * @return The justification value
     */
    int getHorizontalJustification();

    /**
     * Get the vertical justification flag. Uses the constants defined in
     * this interface. This is the real justification after it has been
     * modified according to the effects of the horizontal field
     *
     * @return The justification value
     */
    int getVerticalJustification();

    /**
     * Get the spacing definition for the lines of text.
     *
     * @return The font spacing information
     */
    float getSpacing();

    /**
     * Get the size information for a single line of text. Font size is
     * already incorporated into the AWT Font information, but may also be
     * needed for the inter-line spacing.
     *
     * @return The font size information
     */
    float getSize();

    /**
     * Get the value of the topToBottom flag. Returns true if the text strings
     * should be rendered from the top first, false for bottom first.
     *
     * @return true if rendering top to bottom
     */
    boolean isTopToBottom();

    /**
     * Get the value of the leftToRight flag. Returns true if the text strings
     * should be rendered from the left side to the right or in reverse -
     * regardless of the original character encoding.
     *
     * @return true if rendering left to right
     */
    boolean isLeftToRight();

    /**
     * Get the value of the horizontal flag. Returns true if the text strings
     * should be rendered from vertically rather than horizontally for each
     * string.
     *
     * @return true if rendering with a horizontal orientation
     */
    boolean isHorizontal();
}
