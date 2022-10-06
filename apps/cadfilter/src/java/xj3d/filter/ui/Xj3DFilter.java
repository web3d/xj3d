/*****************************************************************************
 *                        Web3d.org Copyright (c) 2011
 *                               Java Source
 *
 * This source is licensed under the GNU LGPL v2.1
 * Please read http://www.gnu.org/copyleft/lgpl.html for more information
 *
 * This software comes with the standard NO WARRANTY disclaimer for any
 * purpose. Use it at your own risk. If there's a problem you get to fix it.
 *
 ****************************************************************************/

package xj3d.filter.ui;

// External imports
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowListener;
import java.awt.event.WindowEvent;

import java.io.File;
import java.io.PrintStream;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.BackingStoreException;

import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import javax.swing.filechooser.FileFilter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// Local imports
import org.xj3d.ui.awt.widgets.SwingTextAreaOutputStream;

import xj3d.filter.CDFFilter;

/**
 * User Interface for CDFFilter
 *
 * @author Rex Melton
 * @version $Revision: 1.0 $
 */
public class Xj3DFilter extends JFrame implements ActionListener, WindowListener {

    /**
     * Name for this
     */
    private static final String LOG_NAME = "Xj3D Filter";

    /**
     * Preferences keys for input / output file paths
     */
    private static final String INPUT_DIR_NAME = "InputDirectory";

    private static final String OUTPUT_DIR_NAME = "OutputDirectory";

    /**
     * Preferences keys for filter import / export paths
     */
    private static final String IMPORT_DIR_NAME = "ImportDirectory";

    private static final String EXPORT_DIR_NAME = "ExportDirectory";

    /**
     * The local preferences node
     */
    private Preferences prefs;

    /**
     * local panel GUI components
     */
    private JFrame frame;

    private JMenuItem importItem;

    private JMenuItem exportItem;

    private FilterPanel filterPanel;

    private FilePanel filePanel;

    private ResultPanel resultPanel;

    private JButton runButton;

    private JFileChooser importFileChooser;

    private JFileChooser exportFileChooser;

    /**
     * Scratch list for gathering arguments to the filter driver
     */
    private List<String> argument_list;

    /**
     * Constructor
     */
    public Xj3DFilter() {
        //
        super(LOG_NAME);
        frame = Xj3DFilter.this;
        prefs = Preferences.userNodeForPackage(Xj3DFilter.class);

        argument_list = new ArrayList<>(50);

        initUI();
    }

    public static void main(String[] arg) {

        final Xj3DFilter xj3df = new Xj3DFilter();

        xj3df.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        xj3df.pack();

        // center the dialog on the screen
        Dimension screenSize = xj3df.getToolkit().getScreenSize();
        Dimension dialogSize = xj3df.getSize();
        xj3df.setLocation(
                ((screenSize.width - dialogSize.width) / 2),
                ((screenSize.height - dialogSize.height) / 2));

        Runnable r = () -> {
            xj3df.setVisible(true);
        };
        SwingUtilities.invokeLater(r);
    }

    private void initUI() {
        //
        Container contentPane = this.getContentPane();
        contentPane.setLayout(new BorderLayout());
        //
        JPanel mainPanel = new JPanel();

        mainPanel.setPreferredSize(new Dimension(600, 600));
        contentPane.add(mainPanel, BorderLayout.CENTER);

        mainPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(2, 2, 2, 2);

        filterPanel = new FilterPanel();
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 0;
        mainPanel.add(filterPanel, c);

        filePanel = new FilePanel();
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 1;
        mainPanel.add(filePanel, c);

        runButton = new JButton("Run");
        runButton.setEnabled(false);
        runButton.addActionListener(this);
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = 0;
        c.gridy = 2;
        mainPanel.add(runButton, c);

        resultPanel = new ResultPanel();
        c.weightx = 1;
        c.weighty = 1;
        c.gridx = 0;
        c.gridy = 3;
        mainPanel.add(resultPanel, c);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        importItem = new JMenuItem("Import");
        importItem.addActionListener(this);
        fileMenu.add(importItem);

        exportItem = new JMenuItem("Export");
        exportItem.addActionListener(this);
        fileMenu.add(exportItem);

        this.setJMenuBar(menuBar);

        FileFilter extFilter = new ExtFilter("xml");

        String importDirName = prefs.get(
                IMPORT_DIR_NAME,
                System.getProperty("user.home"));
        File inDir = new File(importDirName);
        importFileChooser = new JFileChooser(inDir);
        importFileChooser.setFileFilter(extFilter);

        String exportDirName = prefs.get(
                EXPORT_DIR_NAME,
                System.getProperty("user.home"));
        File outDir = new File(exportDirName);
        exportFileChooser = new JFileChooser(outDir);
        exportFileChooser.setFileFilter(extFilter);

        addWindowListener(this);
    }

    //----------------------------------------------------------
    // Window Listener
    //----------------------------------------------------------

    @Override
    public void windowActivated(WindowEvent we) {
    }

    @Override
    public void windowClosed(WindowEvent we) {
    }

    @Override
    public void windowClosing(WindowEvent we) {
        DefaultListModel custom_filters = filterPanel.customListModel;
        int num_custom_filter = custom_filters.getSize();
        for (int i = 0; i < num_custom_filter; i++) {
            FilterParam fp = (FilterParam) custom_filters.get(i);
            prefs.put("CustomFilter" + Integer.toString(i), fp.className);
        }
    }

    @Override
    public void windowDeactivated(WindowEvent we) {
    }

    @Override
    public void windowDeiconified(WindowEvent we) {
    }

    @Override
    public void windowIconified(WindowEvent we) {
    }

    @Override
    public void windowOpened(WindowEvent we) {
    }

    //----------------------------------------------------------
    // Action Listener
    //----------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent ae) {
        Object src = ae.getSource();
        if (src == runButton) {

            boolean filesValid = true;
            String inFileName = filePanel.inFileText.getText();
            File inFile = new File(inFileName);
            if (!inFile.exists()) {
                System.out.println("Error: Input file " + inFileName + " does not exist");
                filesValid = false;
            } else if (inFile.isDirectory()) {
                System.out.println("Error: Input file " + inFileName + " is not a file");
                filesValid = false;
            }
            String outFileName = filePanel.outFileText.getText();
            File outFile = new File(outFileName);
            if (outFile.exists() && outFile.isDirectory()) {
                System.out.println("Error: Output file " + outFileName + " is not a file");
                filesValid = false;
            }
            if (filesValid) {
                argument_list.clear();
                // assemble the filter list
                DefaultListModel filters = filterPanel.activeListModel;
                int num_filter = filters.size();
                if (num_filter > 0) {
                    for (int i = 0; i < num_filter; i++) {
                        FilterParam fp = (FilterParam) filters.getElementAt(i);
                        argument_list.add(fp.name);
                    }
                } else {
		    // if there are no filters selected, default to
                    // the Identity filter
                    argument_list.add("Identity");
                }

                // append the file arguments
                argument_list.add(inFileName);
                argument_list.add(outFileName);

                // assemble the filter options
                if (num_filter > 0) {
                    for (int i = 0; i < num_filter; i++) {
                        FilterParam fp = (FilterParam) filters.getElementAt(i);
                        if (fp.options != null) {
                            String options = fp.options;
                            String[] s = splitOptions(options);
                            argument_list.addAll(Arrays.asList(s));
                        }
                    }
                }

                // setup the argument array
                int num_arg = argument_list.size();
                String[] args = new String[num_arg];
                args = argument_list.toArray(args);

                // print out the command line
                StringBuilder cmd_string = new StringBuilder();
                for (String arg : args) {
                    cmd_string.append(arg).append(" ");
                }
                System.out.println("-----------------------------------------------");
                System.out.println("Executing: " + cmd_string);

                // run the filter driver
                int status = CDFFilter.executeFilters(args, false, null, null);
                System.out.println("Exit Status = " + status);
            }
        } else if (src == importItem) {
            int returnVal = importFileChooser.showDialog(this, "Import Filter");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = importFileChooser.getSelectedFile();
                File inDir = file.getParentFile();
                prefs.put(IMPORT_DIR_NAME, inDir.getPath());

                Document doc = XMLUtils.getDocument(file, false, null);
                if (doc != null) {
                    loadFilterDoc(doc);
                }
            }
        } else if (src == exportItem) {
            int returnVal = exportFileChooser.showDialog(this, "Export Filter");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = exportFileChooser.getSelectedFile();
                File outDir = file.getParentFile();
                prefs.put(EXPORT_DIR_NAME, outDir.getPath());

                Document doc = createFilterDoc();
                XMLUtils.putDocument(doc, file);
            }
        }
    }

    /**
     * Parse the options list into an acceptable array of strings
     *
     * @param options The raw option String
     * @return The processed options in an array
     */
    private String[] splitOptions(String options) {

	// rem: there is probably a more efficient way to handle the
        // quoted option string....
        int quote_idx0 = options.indexOf("\"");
        if (quote_idx0 == -1) {
            // no quoted option sets, simply split
            String[] s = options.split("\\s");
            return (s);

        } else {
            // first indication that there is a quoted option set
            int quote_idx1 = options.indexOf("\"", quote_idx0 + 1);
            if (quote_idx1 != -1) {

                String quote_string = options.substring(quote_idx0, quote_idx1 + 1);

                // trim off the quotation marks
                String replacement = quote_string.substring(1, quote_string.length() - 1);

                // split along the allowed separators
                String[] item_list = replacement.split("\\s|[,]");

                int num_item = item_list.length;
                int last_item = num_item - 1;

                // construct a comma separated list
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < item_list.length; i++) {
                    String item = item_list[i];
                    if (item.length() > 0) {
                        sb.append(item_list[i]);
                        if (i != last_item) {
                            sb.append(",");
                        }
                    }
                }
                String options_modified = options.replace(quote_string, sb.toString());

                // iterate, in case there is another quoted option set
                return (splitOptions(options_modified));

            } else {
		// something is flawed, there is an unterminated quoted section.
                // just split and return
                String[] s = options.split("\\s");
                return (s);
            }
        }

    }

    private void loadFilterDoc(Document doc) {
        Element chain_element = doc.getDocumentElement();
        if (chain_element.getTagName().equals("filter_chain")) {
            DefaultListModel<FilterParam> filters = filterPanel.activeListModel;
            filters.clear();
            NodeList filter_list = chain_element.getElementsByTagName("filter");
            int num_filter = filter_list.getLength();
            for (int i = 0; i < num_filter; i++) {
                Element filter_element = (Element) filter_list.item(i);
                String name = filter_element.getAttribute("name");
                String options = filter_element.getAttribute("options");
                FilterParam fp = new FilterParam(name, name);
                if ((options != null) && options.length() > 0) {
                    fp.options = options;
                }
                filters.addElement(fp);
            }
        } else {
            System.out.println("Unable to import, invalid document");
        }
    }

    private Document createFilterDoc() {

        Document doc = XMLUtils.createNewDocument();
        Element chain_element = doc.createElement("filter_chain");
        doc.appendChild(chain_element);

        DefaultListModel filters = filterPanel.activeListModel;
        int num_filter = filters.size();
        for (int i = 0; i < num_filter; i++) {
            FilterParam fp = (FilterParam) filters.getElementAt(i);
            Element filter_element = doc.createElement("filter");
            filter_element.setAttribute("name", fp.name);
            String options = fp.options;
            if ((options != null) && options.length() > 0) {
                filter_element.setAttribute("options", options);
            }
            chain_element.appendChild(filter_element);
        }
        return (doc);
    }

    private class ExtFilter extends FileFilter {

        /**
         * The file extension string
         */
        private String ext;

        /**
         * Constructor
         *
         * @param ext The file extension string
         */
        public ExtFilter(String ext) {
            this.ext = ext;
        }

        /**
         * Return whether the file path string ends with the specified extension
         *
         * @param file The File to be tested
         */
        @Override
        public boolean accept(File file) {
            boolean isDir = file.isDirectory();
            boolean isExtType = file.getName().endsWith(ext);
            return (isDir | isExtType);
        }

        @Override
        public String getDescription() {
            return ("XML Files");
        }
    }

    private class ResultPanel extends JPanel {

        // local UI
        JTextArea resultsText;

        ResultPanel() {

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            TitledBorder resultBorder = new TitledBorder(new EtchedBorder());
            resultBorder.setTitle("Results");
            mainPanel.setBorder(resultBorder);

            resultsText = new JTextArea(20, 80);
            resultsText.setEditable(false);

            JScrollPane scrollPane = new JScrollPane(resultsText);
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(scrollPane, c);

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            add(mainPanel, c);

            PrintStream out = new PrintStream(
                    new SwingTextAreaOutputStream("", resultsText));
            System.setOut(out);
            PrintStream err = new PrintStream(
                    new SwingTextAreaOutputStream("", resultsText));
            System.setErr(err);
        }
    }

    private class FilePanel extends JPanel implements
            ActionListener {

        // local UI
        JButton inButton;

        JFileChooser inFileChooser;

        JTextField inFileText;

        JButton outButton;

        JFileChooser outFileChooser;

        JTextField outFileText;

        FilePanel() {

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            TitledBorder fileBorder = new TitledBorder(new EtchedBorder());
            fileBorder.setTitle("Files");
            mainPanel.setBorder(fileBorder);

            JPanel inPanel = new JPanel();
            inPanel.setLayout(new BorderLayout());
            TitledBorder inBorder = new TitledBorder(new EtchedBorder());
            inBorder.setTitle("Input");
            inPanel.setBorder(inBorder);

            inButton = new JButton(">");
            inButton.addActionListener(Xj3DFilter.this);
            inPanel.add(inButton, BorderLayout.WEST);

            inFileText = new JTextField();
            inPanel.add(inFileText, BorderLayout.CENTER);

            String inDirName = prefs.get(
                    INPUT_DIR_NAME,
                    System.getProperty("user.home"));
            File inDir = new File(inDirName);
            inFileChooser = new JFileChooser(inDir);

            c.weightx = 1;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(inPanel, c);

            JPanel outPanel = new JPanel();
            outPanel.setLayout(new BorderLayout());
            TitledBorder outBorder = new TitledBorder(new EtchedBorder());
            outBorder.setTitle("Output");
            outPanel.setBorder(outBorder);

            outButton = new JButton("<");
            outButton.addActionListener(Xj3DFilter.this);
            outPanel.add(outButton, BorderLayout.WEST);

            outFileText = new JTextField();
            outPanel.add(outFileText, BorderLayout.CENTER);

            String outDirName = prefs.get(
                    OUTPUT_DIR_NAME,
                    System.getProperty("user.home"));
            File outDir = new File(outDirName);
            outFileChooser = new JFileChooser(outDir);

            c.weightx = 1;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(outPanel, c);

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            add(mainPanel, c);
        }

	//----------------------------------------------------------
        // Action Listener
        //----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent ae) {
            Object src = ae.getSource();
            if (src == inButton) {
                int returnVal = inFileChooser.showDialog(this, "Input File");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = inFileChooser.getSelectedFile();
                    inFileText.setText(file.getPath());
                    File inDir = file.getParentFile();
                    prefs.put(INPUT_DIR_NAME, inDir.getPath());
                    runButton.setEnabled(true);
                }
            } else if (src == outButton) {
                int returnVal = outFileChooser.showDialog(this, "Output File");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = outFileChooser.getSelectedFile();
                    outFileText.setText(file.getPath());
                    File outDir = file.getParentFile();
                    prefs.put(OUTPUT_DIR_NAME, outDir.getPath());
                }
            }
        }
    }

    private class FilterPanel extends JPanel implements
            ActionListener,
            ListSelectionListener,
            ListDataListener,
            MouseListener,
            DocumentListener,
            ChangeListener {

        // local UI
        JLabel activeLabel;

        JList<FilterParam> activeList;

        JLabel availableLabel;

        JTabbedPane tabPane;

        JList<FilterParam> standardList;

        JList<FilterParam> customList;

        FilterParamTransferHandler transfer0;

        FilterParamTransferHandler transfer1;

        JLabel optionsLabel;

        JTextField optionsText;

        JPopupMenu standardPopup;

        JMenuItem addStandardItem;

        JPopupMenu activePopup;

        JMenuItem removeActiveItem;

        JPopupMenu customPopup;

        JMenuItem newCustomItem;

        JMenuItem addCustomItem;

        JMenuItem rmvCustomItem;

        DefaultListModel<FilterParam> activeListModel;

        DefaultListModel<FilterParam> standardListModel;

        DefaultListModel<FilterParam> customListModel;

        // the selected filter
        FilterParam selectedFilterParam;

        FilterPanel() {

            activeListModel = new DefaultListModel<>();
            activeListModel.addListDataListener(FilterPanel.this);
            standardListModel = new DefaultListModel<>();
            customListModel = new DefaultListModel<>();

            FilterParam[] sfp = getStandardFilterParams();
            for (FilterParam sfp1 : sfp) {
                standardListModel.addElement(sfp1);
            }

            FilterParam[] cfp = getCustomFilterParams();
            for (FilterParam cfp1 : cfp) {
                customListModel.addElement(cfp1);
            }

            setLayout(new GridBagLayout());
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.BOTH;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            TitledBorder filterBorder = new TitledBorder(new EtchedBorder());
            filterBorder.setTitle("Filters");
            mainPanel.setBorder(filterBorder);

            activeLabel = new JLabel("Active");

            activeList = new JList<>(activeListModel);
            activeList.setDragEnabled(true);
            activeList.setVisibleRowCount(15);
            activeList.setEnabled(true);
            activeList.setSelectionMode(
                    ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            activeList.addListSelectionListener(FilterPanel.this);
            activeList.addMouseListener(FilterPanel.this);
            JScrollPane activePane = new JScrollPane(activeList);

            JPanel activePanel = new JPanel();
            activePanel.setLayout(new GridBagLayout());
            c.weightx = 1;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            activePanel.add(activeLabel, c);

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 1;
            activePanel.add(activePane, c);

            availableLabel = new JLabel("Available");

            standardList = new JList<>(standardListModel);
            standardList.setFocusable(false);
            standardList.setDragEnabled(true);
            standardList.setVisibleRowCount(15);
            standardList.setEnabled(true);
            standardList.setSelectionMode(
                    ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            standardList.addListSelectionListener(FilterPanel.this);
            standardList.addMouseListener(FilterPanel.this);
            JScrollPane standardPane = new JScrollPane(standardList);

            customList = new JList<>(customListModel);
            customList.setFocusable(false);
            customList.setDragEnabled(true);
            customList.setVisibleRowCount(15);
            customList.setEnabled(true);
            customList.setSelectionMode(
                    ListSelectionModel.SINGLE_INTERVAL_SELECTION);
            customList.addListSelectionListener(FilterPanel.this);
            customList.addMouseListener(FilterPanel.this);
            JScrollPane customPane = new JScrollPane(customList);

            tabPane = new JTabbedPane();
            tabPane.addChangeListener(FilterPanel.this);
            tabPane.add(standardPane, c, 0);
            tabPane.setTitleAt(0, "Standard");

            tabPane.add(customPane, c, 1);
            tabPane.setTitleAt(1, "Custom");

            JPanel availablePanel = new JPanel();
            availablePanel.setLayout(new GridBagLayout());
            c.weightx = 1;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            availablePanel.add(availableLabel, c);

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 1;
            //availablePanel.add(availablePane, c);
            availablePanel.add(tabPane, c);

            transfer0 = new FilterParamTransferHandler(
                    standardList,
                    activeList);
            activeList.setTransferHandler(transfer0);
            standardList.setTransferHandler(transfer0);

            transfer1 = new FilterParamTransferHandler(
                    customList,
                    activeList);
            customList.setTransferHandler(transfer1);

            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT, availablePanel, activePanel);
            splitPane.setOneTouchExpandable(true);
            splitPane.setResizeWeight(0.50);
            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            //c.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(splitPane, c);

            // !!!!! contortions to force the split pane to divy nicely.....
            activePanel.setMinimumSize(new Dimension(0, 150));
            activePanel.setPreferredSize(new Dimension(0, 150));
            availablePanel.setMinimumSize(new Dimension(0, 150));
            availablePanel.setPreferredSize(new Dimension(0, 150));
            splitPane.setDividerLocation(0.50);

            JPanel optionsPanel = new JPanel();
            optionsPanel.setLayout(new GridBagLayout());

            optionsLabel = new JLabel("Options");
            optionsLabel.setEnabled(false);
            c.weightx = 1;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            optionsPanel.add(optionsLabel, c);

            optionsText = new JTextField();
            optionsText.setEnabled(false);
            optionsText.getDocument().addDocumentListener(FilterPanel.this);
            c.weightx = 1;
            c.weighty = 0;
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            optionsPanel.add(optionsText, c);

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 1;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            mainPanel.add(optionsPanel, c);

            c.weightx = 1;
            c.weighty = 1;
            c.gridx = 0;
            c.gridy = 0;
            c.insets = new Insets(2, 2, 2, 2);
            c.gridwidth = GridBagConstraints.REMAINDER;
            add(mainPanel, c);

            standardPopup = new JPopupMenu();
            addStandardItem = new JMenuItem("Add");
            addStandardItem.addActionListener(FilterPanel.this);
            standardPopup.add(addStandardItem);

            activePopup = new JPopupMenu();
            removeActiveItem = new JMenuItem("Remove");
            removeActiveItem.addActionListener(FilterPanel.this);
            activePopup.add(removeActiveItem);

            customPopup = new JPopupMenu();
            newCustomItem = new JMenuItem("New");
            newCustomItem.addActionListener(FilterPanel.this);
            customPopup.add(newCustomItem);
            addCustomItem = new JMenuItem("Add");
            addCustomItem.addActionListener(FilterPanel.this);
            customPopup.add(addCustomItem);
            rmvCustomItem = new JMenuItem("Remove");
            rmvCustomItem.addActionListener(FilterPanel.this);
            customPopup.add(rmvCustomItem);
        }

	//----------------------------------------------------------
        // Change Listener
        //----------------------------------------------------------

        @Override
        public void stateChanged(ChangeEvent ce) {
            Object src = ce.getSource();
            if (src == tabPane) {
                int idx = tabPane.getSelectedIndex();
                switch (idx) {
                    case 0:
                        activeList.setTransferHandler(transfer0);
                        break;
                    case 1:
                        activeList.setTransferHandler(transfer1);
                        break;
                }
            }
        }

	//----------------------------------------------------------
        // Action Listener
        //----------------------------------------------------------

        @Override
        public void actionPerformed(ActionEvent ae) {
            Object src = ae.getSource();
            if (src == addStandardItem) {
                int idx = standardList.getSelectedIndex();
                if (idx != -1) {
                    FilterParam fp = standardListModel.getElementAt(idx);
                    fp = fp.clone();
                    activeListModel.addElement(fp);
                }
            } else if (src == removeActiveItem) {
                int idx = activeList.getSelectedIndex();
                if (idx != -1) {
                    activeListModel.removeElementAt(idx);
                }
            } else if (src == addCustomItem) {
                int idx = standardList.getSelectedIndex();
                if (idx != -1) {
                    FilterParam fp = customListModel.getElementAt(idx);
                    fp = fp.clone();
                    activeListModel.addElement(fp);
                }
            } else if (src == rmvCustomItem) {
                int idx = customList.getSelectedIndex();
                if (idx != -1) {
                    customListModel.removeElementAt(idx);
                }
            } else if (src == newCustomItem) {
                String className = (String) JOptionPane.showInputDialog(
                        frame,
                        "Filter Class Name",
                        "New Custom Filter",
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        null,
                        null);
                if (className != null) {
                    FilterParam fp = new FilterParam(className, className);
                    customListModel.addElement(fp);
                }
            }
        }

	//----------------------------------------------------------
        // Mouse Listener
        //----------------------------------------------------------

        @Override
        public void mouseClicked(MouseEvent me) {
            Component src = me.getComponent();
            if ((src == standardList) && (me.getClickCount() == 2)) {
                int idx = standardList.getSelectedIndex();
                if (idx != -1) {
                    FilterParam fp = standardListModel.getElementAt(idx);
                    fp = fp.clone();
                    activeListModel.addElement(fp);
                }
            } else if ((src == customList) && (me.getClickCount() == 2)) {
                int idx = customList.getSelectedIndex();
                if (idx != -1) {
                    FilterParam fp = customListModel.getElementAt(idx);
                    fp = fp.clone();
                    activeListModel.addElement(fp);
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent me) {
        }

        @Override
        public void mouseExited(MouseEvent me) {
        }

        @Override
        public void mousePressed(MouseEvent me) {
            if (me.isPopupTrigger()) {
                Component src = me.getComponent();
                if (src == activeList) {
                    activePopup.show(me.getComponent(), me.getX(), me.getY());
                    int idx = activeList.locationToIndex(me.getPoint());
                    activeList.setSelectedIndex(idx);

                } else if (src == standardList) {
                    standardPopup.show(me.getComponent(), me.getX(), me.getY());
                    int idx = standardList.locationToIndex(me.getPoint());
                    standardList.setSelectedIndex(idx);

                } else if (src == customList) {
                    customPopup.show(me.getComponent(), me.getX(), me.getY());
                    int idx = customList.locationToIndex(me.getPoint());
                    customList.setSelectedIndex(idx);
                }
            }
        }

        @Override
        public void mouseReleased(MouseEvent me) {
            if (me.isPopupTrigger()) {
                Component src = me.getComponent();
                if (src == activeList) {
                    activePopup.show(me.getComponent(), me.getX(), me.getY());
                    int idx = activeList.locationToIndex(me.getPoint());
                    activeList.setSelectedIndex(idx);

                } else if (src == standardList) {
                    standardPopup.show(me.getComponent(), me.getX(), me.getY());
                    int idx = standardList.locationToIndex(me.getPoint());
                    standardList.setSelectedIndex(idx);

                } else if (src == customList) {
                    customPopup.show(me.getComponent(), me.getX(), me.getY());
                    int idx = customList.locationToIndex(me.getPoint());
                    customList.setSelectedIndex(idx);
                }
            }
        }

	//----------------------------------------------------------
        // List Data Listener
        //----------------------------------------------------------

        @Override
        public void contentsChanged(ListDataEvent lde) {
        }

        @Override
        public void intervalAdded(ListDataEvent lde) {
            activeList.setSelectedIndex(lde.getIndex0());
        }

        @Override
        public void intervalRemoved(ListDataEvent lde) {
            int size = activeListModel.getSize();
            if (size > 0) {
                int last_idx = size - 1;
                int idx = lde.getIndex0();
                if (idx > last_idx) {
                    activeList.setSelectedIndex(last_idx);
                } else {
                    activeList.setSelectedIndex(idx);
                }
            }
        }

	//----------------------------------------------------------
        // List Selection Listener
        //----------------------------------------------------------

        @Override
        public void valueChanged(ListSelectionEvent lse) {

            JList source = (JList) lse.getSource();
            if (source == activeList) {
                if (!lse.getValueIsAdjusting()) {
                    int first = lse.getFirstIndex();
                    int last = lse.getLastIndex();
                    boolean activeIsSelected = false;
                    int activeIndex = 0;
                    for (int i = first; i <= last; i++) {
                        if (activeList.isSelectedIndex(i)) {
                            activeList.setSelectedIndex(i);
                            activeIndex = i;
                            activeIsSelected = true;
                            break;
                        }
                    }
                    if (activeIsSelected) {
                        standardList.clearSelection();
                        customList.clearSelection();
                        selectedFilterParam = activeListModel.getElementAt(activeIndex);
                        optionsLabel.setText("Options [" + selectedFilterParam.name + "]");
                        optionsLabel.setEnabled(true);
                        optionsText.setText(selectedFilterParam.options);
                        optionsText.setEnabled(true);
                    } else {
                        selectedFilterParam = null;
                        optionsLabel.setText("Options");
                        optionsLabel.setEnabled(false);
                        optionsText.setText("");
                        optionsText.setEnabled(false);
                    }
                }
            }
        }

        //----------------------------------------------------------
        // Document Listener
        //----------------------------------------------------------

        @Override
        public void changedUpdate(DocumentEvent de) {
            if (selectedFilterParam != null) {
                selectedFilterParam.options = optionsText.getText();
            }
        }

        @Override
        public void insertUpdate(DocumentEvent de) {
            if (selectedFilterParam != null) {
                selectedFilterParam.options = optionsText.getText();
            }
        }

        @Override
        public void removeUpdate(DocumentEvent de) {
            if (selectedFilterParam != null) {
                selectedFilterParam.options = optionsText.getText();
            }
        }

	    //----------------------------------------------------------
        // Local Methods
        //----------------------------------------------------------
        /**
         * Return the default known set of filters
         *
         * @return The default known set of filters
         */
        private FilterParam[] getStandardFilterParams() {
            Map<String, String> filter_map = CDFFilter.getFilterMap();
            Set<String> key_set = filter_map.keySet();
            int num = key_set.size();
            String[] keys = new String[num];
            keys = key_set.toArray(keys);
            Arrays.sort(keys);
            FilterParam[] fp = new FilterParam[num];
            for (int i = 0; i < num; i++) {
                String name = keys[i];
                String className = filter_map.get(name);
                fp[i] = new FilterParam(name, className);
            }
            return (fp);
        }

        /**
         * Return the set of custom filters
         *
         * @return The set of custom filters
         */
        private FilterParam[] getCustomFilterParams() {
            FilterParam[] fp;
            try {
                String[] keys = prefs.keys();
                int num_key = keys.length;
                List<String> filter_list = new ArrayList<>();
                for (int i = 0; i < num_key; i++) {
                    String key_name = keys[i];
                    if (key_name.startsWith("CustomFilter")) {
                        String className = prefs.get(key_name, null);
                        if (className != null) {
                            filter_list.add(className);
                        }
                        prefs.remove(key_name);
                    }
                }
                int num_filter = filter_list.size();
                fp = new FilterParam[num_filter];
                for (int i = 0; i < num_filter; i++) {
                    String className = filter_list.get(i);
                    fp[i] = new FilterParam(className, className);
                }
            } catch (BackingStoreException e) {
                fp = new FilterParam[0];
            }
            return (fp);
        }
    }
}
