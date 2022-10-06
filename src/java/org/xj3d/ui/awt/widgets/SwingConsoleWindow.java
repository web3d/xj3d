/*****************************************************************************
 *                        Web3d.org Copyright (c) 2001 - 2006
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package org.xj3d.ui.awt.widgets;

// External imports
import java.awt.*;
import java.io.*;
import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;

import java.net.UnknownHostException;

// Local imports
import org.web3d.vrml.lang.*;

import org.j3d.util.ErrorReporter;
import org.j3d.util.HashSet;
import org.web3d.util.Version;
import org.web3d.vrml.sav.ErrorHandler;
import org.web3d.vrml.sav.Locator;
import org.web3d.vrml.sav.VRMLParseException;

/**
 * A window that can act as console for error messages from the application.
 * <p>
 *
 * The window will print error messages for all the error levels and only
 * throw an exception for the fatalError.
 *
 * @author Justin Couch
 * @version $Revision: 1.5 $
 */
public class SwingConsoleWindow extends JFrame
    implements ActionListener, ErrorHandler, ErrorReporter {

    /** Tooltip used on the clear button */
    private static final String CLEAR_TOOLTIP =
        "Clear the contents of the console";

    /** Tooltip used on the copy button */
    private static final String COPY_TOOLTIP =
        "Copy the contents of the window to the clipboard";

    /** The set of exceptions to ignore the stack trace for */
    private HashSet<Class<?>> ignoredExceptionTypes;

    /** Scroll Pane instance */
    private JScrollPane scroller;

    /** The text field to put the error information in */
    private JTextArea errorField;

    /** Clear button to remove stuff from window */
    private JButton clearButton;

    /** Copy button that sends the current contents to the clipboard. */
    private JButton copyButton;

    /** Locator used for printing out line/column information */
    private Locator docLocator;

    /** Output buffer */
    private final StringBuilder buf;

    /**
     * Create an instance of the console window.
     */
    public SwingConsoleWindow() {
        super("Xj3D Console");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        // Reuse to conserve resources
        buf = new StringBuilder();

        Container content_pain = getContentPane();

        content_pain.setLayout(new BorderLayout());

        errorField = new JTextArea(20, 60);
        errorField.setEditable(false);

        scroller = new JScrollPane(errorField);
        content_pain.add(scroller, BorderLayout.CENTER);

        JPanel p1 = new JPanel(new FlowLayout());
        content_pain.add(p1, BorderLayout.SOUTH);

        clearButton = new JButton("Clear");
        clearButton.setToolTipText(CLEAR_TOOLTIP);
        clearButton.addActionListener(SwingConsoleWindow.this);

        p1.add(clearButton);

        copyButton = new JButton("Copy");
        copyButton.setToolTipText(COPY_TOOLTIP);
        copyButton.addActionListener(SwingConsoleWindow.this);

        p1.add(copyButton);

        setSize(600, 400);
        setLocation(80, 80);
        setDefaultCloseOperation(HIDE_ON_CLOSE);

        messageReport("Xj3D Version: " + Version.XJ3D_VERSION + "\n");

        ignoredExceptionTypes = new HashSet<>();
        ignoredExceptionTypes.add(InvalidFieldException.class);
        ignoredExceptionTypes.add(InvalidFieldFormatException.class);
        ignoredExceptionTypes.add(InvalidFieldValueException.class);
        ignoredExceptionTypes.add(FileNotFoundException.class);
        ignoredExceptionTypes.add(IOException.class);
        ignoredExceptionTypes.add(UnknownHostException.class);
        ignoredExceptionTypes.add(IllegalArgumentException.class);
        ignoredExceptionTypes.add(InvalidFieldConnectionException.class);
        ignoredExceptionTypes.add(VRMLParseException.class);
        ignoredExceptionTypes.add(UnsupportedComponentException.class);
        ignoredExceptionTypes.add(UnsupportedNodeException.class);
        ignoredExceptionTypes.add(UnsupportedProfileException.class);
        ignoredExceptionTypes.add(ClassNotFoundException.class);
    }

    //----------------------------------------------------------
    // Methods defined by ErrorHandler
    //----------------------------------------------------------

    /**
     * Set the document locator that can be used by the implementing code to
     * find out information about the current line information. This method
     * is called by the parser to your code to give you a locator to work with.
     * If this has not been set by the time <code>startDocument()</code> has
     * been called, you can assume that you will not have one available.
     *
     * @param loc The locator instance to use
     */
    @Override
    public void setDocumentLocator(Locator loc) {
        docLocator = loc;
    }

    /**
     * Notification of a warning in the way the code has been handled. The
     * parser will continue through the file after this. Throw another
     * exception if we want the parser to halt as a result.
     *
     * @param vpe The exception that caused this warning
     * @throws VRMLException Create a further warning condition
     */
    public void warning(VRMLException vpe) throws VRMLException {
        warning(vpe.getMessage(), true);
    }

    /**
     * Notification of a recoverable error in the parsing. The parser will
     * continue to keep parsing after this error. Throw another exception if
     * we really want the parser to stop at this point.
     *
     * @param vpe The exception that caused this warning
     * @throws VRMLException Create a further warning condition
     */
    public void error(VRMLException vpe) throws VRMLException {
        error(vpe.getMessage(), true);
    }

    /**
     * Notification of a non-recoverable error. The parser will not continue
     * after calling this method. Throw another exception if we really want
     * to make note of this, the parser will stop anyway.
     *
     * @param vpe The exception that caused this warning
     * @throws VRMLException Create a further warning condition
     */
    public void fatalError(VRMLException vpe) throws VRMLException {
        buf.append("Fatal Error: ");
        buf.append(" in X3D scene, Line: ");
        buf.append(docLocator.getLineNumber());
        buf.append(" Column: ");
        buf.append(docLocator.getColumnNumber());
        buf.append(" ");
        buf.append(vpe.getMessage());
        buf.append("\n");

        errorField.append(buf.toString());
        advanceScroll();

        // Clear
        buf.setLength(0);

        throw vpe;
    }

    //----------------------------------------------------------
    // Methods defined by ErrorReporter
    //----------------------------------------------------------

    /**
     * Notification of an partial message from the system. When being written
     * out to a display device, a partial message does not have a line
     * termination character appended to it, allowing for further text to
     * appended on that same line.
     *
     * @param msg The text of the message to be displayed
     */
    @Override
    public void partialReport(String msg) {
        errorField.append(msg);
    }

    /**
     * Notification of an informational message from the system. For example,
     * it may issue a message when a URL cannot be resolved.
     *
     * @param msg The text of the message to be displayed
     */
    @Override
    public final void messageReport(String msg) {

        buf.append(msg);
        buf.append('\n');

        errorField.append(buf.toString());
        advanceScroll();

        // Clear
        buf.setLength(0);
    }

    /**
     * Notification of a warning in the way the system is currently operating.
     * This is a non-fatal, non-serious error. For example you will get an
     * warning when a value has been set that is out of range.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    @Override
    public void warningReport(String msg, Throwable e) {
        buf.append("Warning: ");

        if(e instanceof FieldException) {
            FieldException fe = (FieldException)e;

            String name = fe.getFieldName();
            if(name != null) {
                buf.append("Field name: ");
                buf.append(name);
            }
        }

        if(e instanceof VRMLParseException) {
            buf.append(" in X3D scene, Line: ");
            buf.append(((VRMLParseException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((VRMLParseException)e).getColumnNumber());
            buf.append('\n');
        } else if(e instanceof InvalidFieldFormatException) {
            buf.append(" Field name: ");
            buf.append(((FieldException)e).getFieldName());
            buf.append(" in X3D scene, Line: ");
            buf.append(((InvalidFieldFormatException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((InvalidFieldFormatException)e).getColumnNumber());
            buf.append('\n');
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String txt = e.getMessage();
            if(txt == null)
                txt = e.getClass().getName();

            buf.append(txt);
            buf.append('\n');

            if(!ignoredExceptionTypes.contains(e.getClass())) {
                StringWriter sw = new StringWriter();
                PrintWriter  pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                buf.append(sw.toString());
            }
        }

        errorField.append(buf.toString());
        advanceScroll();

        // Clear
        buf.setLength(0);
    }

    /**
     * Notification of a recoverable error. This is a serious, but non-fatal
     * error, for example trying to add a route to a non-existent node or the
     * use of a node that the system cannot find the definition of.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    @Override
    public void errorReport(String msg, Throwable e) {
        buf.append("Error: ");

        if(e instanceof FieldException) {
            FieldException fe = (FieldException)e;

            String name = fe.getFieldName();
            if(name != null) {
                buf.append("Field name: ");
                buf.append(name);
            }
        }

        if(e instanceof VRMLParseException) {
            buf.append(" in X3D scene, Line: ");
            buf.append(((VRMLParseException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((VRMLParseException)e).getColumnNumber());
            buf.append('\n');
        } else if(e instanceof InvalidFieldFormatException) {
            buf.append(" in X3D scene, Line: ");
            buf.append(((InvalidFieldFormatException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((InvalidFieldFormatException)e).getColumnNumber());
            buf.append('\n');
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String txt = e.getMessage();
            if(txt == null)
                txt = e.getClass().getName();

            buf.append(txt);
            buf.append('\n');

            if(!ignoredExceptionTypes.contains(e.getClass())) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                buf.append(sw.toString());
            }
        }

        errorField.append(buf.toString());
        advanceScroll();

        // Clear
        buf.setLength(0);
    }

    /**
     * Notification of a non-recoverable error that halts the entire system.
     * After you receive this report the runtime system will no longer
     * function - for example a non-recoverable parsing error. The best way
     * out is to reload the file or restart the application internals.
     *
     * @param msg The text of the message to be displayed
     * @param e The exception that caused this warning. May be null
     */
    @Override
    public void fatalErrorReport(String msg, Throwable e) {
        buf.append("Fatal Error: ");

        if(e instanceof FieldException) {
            FieldException fe = (FieldException)e;

            String name = fe.getFieldName();
            if(name != null) {
                buf.append("Field name: ");
                buf.append(name);
            }
        }

        if(e instanceof VRMLParseException) {
            buf.append(" in X3D scene, Line: ");
            buf.append(((VRMLParseException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((VRMLParseException)e).getColumnNumber());
            buf.append('\n');
        } else if(e instanceof InvalidFieldFormatException) {
            buf.append(" in X3D scene, Line: ");
            buf.append(((InvalidFieldFormatException)e).getLineNumber());
            buf.append(" Column: ");
            buf.append(((InvalidFieldFormatException)e).getColumnNumber());
            buf.append('\n');
        }

        if(msg != null) {
            buf.append(msg);
            buf.append('\n');
        }

        if(e != null) {
            String txt = e.getMessage();
            if(txt == null)
                txt = e.getClass().getName();

            buf.append(txt);
            buf.append('\n');

            if(!ignoredExceptionTypes.contains(e.getClass())) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                buf.append(sw.toString());
            }
        }

        errorField.append(buf.toString());
        advanceScroll();

        // Clear
        buf.setLength(0);
    }

    //----------------------------------------------------------
    // Methods defined by ActionListener
    //----------------------------------------------------------

    /**
     * Process the action generated from the user interface.
     *
     * @param evt The event that caused this method to be called
     */
    @Override
    public void actionPerformed(ActionEvent evt) {
        Object src = evt.getSource();

        if(src == clearButton)
            errorField.setText("");
        else if(src == copyButton) {
            Toolkit tk = copyButton.getToolkit();
            Clipboard clip = tk.getSystemClipboard();
            StringSelection sel = new StringSelection(errorField.getText());
            clip.setContents(sel, sel);
        }
    }

    //----------------------------------------------------------
    // Local Methods
    //----------------------------------------------------------

    /**
     * Redirect system messages to the console.
     */
    public void redirectSystemMessages() {
        PrintStream out =
                new PrintStream(new SwingTextAreaOutputStream(" ",
                errorField));
        System.setOut(out);

        PrintStream err =
                new PrintStream(new SwingTextAreaOutputStream("System.err: ",
                errorField));
        System.setErr(err);
    }

    /**
     * Send a warning message to the screen with the option of using the
     * docLocator to present line number info.The string will have the prefix
     * "Warning:" prepended to it.
     *
     * @param msg The message to be written.
     * @param useLocator true if line number information should be used
     */
    private void warning(String msg, boolean useLocator) {

        buf.append("Warning: ");

        if(useLocator) {
            buf.append(" in X3D scene, Line: ");
            buf.append(docLocator.getLineNumber());
            buf.append(" Column: ");
            buf.append(docLocator.getColumnNumber());
            buf.append(" ");
        }

        buf.append(msg);
        buf.append("\n");
        errorField.append(buf.toString());
        advanceScroll();

        // Clear
        buf.setLength(0);
    }

    /**
     * Send an error message to the screen with the option of using the
     * docLocator to present line number info.The string will have the prefix
     * "Error:" prepended to it.
     *
     * @param msg The message to be written
     * @param useLocator true if line number information should be used
     */
    private void error(String msg, boolean useLocator) {

        buf.append("Error: ");

        if(useLocator) {
            buf.append(" in X3D scene, Line: ");
            buf.append(docLocator.getLineNumber());
            buf.append(" Column: ");
            buf.append(docLocator.getColumnNumber());
            buf.append(" ");
        }

        buf.append(msg);
        buf.append("\n");
        errorField.append(buf.toString());
        advanceScroll();

        // Clear
        buf.setLength(0);
    }

    /** Attempt to autoscroll to the bottom, but allow the user scroll back */
    private void advanceScroll() {
        final JScrollBar vscroll = scroller.getVerticalScrollBar();

        final int distance_to_bottom = vscroll.getMaximum() - vscroll.getValue();

        Runnable r = () -> {
            vscroll.setValue(distance_to_bottom);
        };
        SwingUtilities.invokeLater(r);
    }
}
