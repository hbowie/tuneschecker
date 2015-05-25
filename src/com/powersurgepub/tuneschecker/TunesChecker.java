/*
 * Copyright 2015 - 2015 Herb Bowie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.powersurgepub.tuneschecker;

  import com.powersurgepub.psdatalib.psdata.*;
  import com.powersurgepub.psdatalib.tabdelim.*;
  import com.powersurgepub.psdatalib.txbio.*;
  import com.powersurgepub.psdatalib.ui.*;
  import com.powersurgepub.psfiles.*;
  import com.powersurgepub.psutils.*;
  import com.powersurgepub.xos2.*;
  import java.awt.*;
  import java.awt.event.*;
  import java.io.*;
  import java.net.*;
  import javax.swing.*;
  import javax.swing.table.*;

/**
 TunesChecker checks two iTunes libraries that are meant to contain the same
 music, saved in different formats. 

 @author Herb Bowie
 */
public class TunesChecker 
    extends 
      javax.swing.JFrame
    implements
      AppToBackup,
      FileSpecOpener,
      XHandler {
  
  public static final String PREFS_LEFT    = "left";
  public static final String PREFS_TOP     = "top";
  public static final String PREFS_WIDTH   = "width";
  public static final String PREFS_HEIGHT  = "height";
  
  public static final String PREFS_ATTRIBUTES = "show-attributes";
  public static final String PREFS_MIN_TRACKS = "min-tracks-for-album";
  // public static final String OPML_EXPORT      = "export-to-opml";
  // public static final String TAB_DELIM_EXPORT = "export-to-tab-delim";
  public static final String EXPORT_FOLDER    = "export-folder";
  public static final String LAST_TRACK_COUNT = "last-track-count";
  public static final String LAST_ANOMALY_COUNT = "last-anomaly-count";
  
  public static final String PROGRAM_NAME    = "TunesChecker";
  public static final String PROGRAM_VERSION = "0.20";

  public static final int    CHILD_WINDOW_X_OFFSET = 60;
  public static final int    CHILD_WINDOW_Y_OFFSET = 60;

  public static final        int    ONE_SECOND    = 1000;
  public static final        int    ONE_MINUTE    = ONE_SECOND * 60;
  public static final        int    ONE_HOUR      = ONE_MINUTE * 60;
  
  public static final        String MUSIC         = "Music";

  private             Appster appster;

  private             String  country = "  ";
  private             String  language = "  ";

  private             Home home;
  private             ProgramVersion      programVersion;
  
  private             XOS                 xos = XOS.getShared();
  private             Trouble             trouble = Trouble.getShared();
  
  private             AboutWindow         aboutWindow;
  
  private             File                musicFolder = null;
  
  private             RecentFiles         recentFiles;
  
  private             StatusBar           statusBar = new StatusBar();
  
  private             UserPrefs           userPrefs;
  private             PrefsWindow         prefsWindow;
  private             FilePrefs           filePrefs;
  
  // Variables used for logging
  private             Logger              logger = Logger.getShared();
  private             LogOutput           logOutput;
  
  private             XFileChooser        fileChooser = new XFileChooser();
  
  private             File                fileToOpen = null;
  
  private             int                 libIndex = 0;
  
  private             TunesCollection     tunes;
  private             TunesParser         tunesParser;
  
  private             int                 currTrackCount = 0;

  /**
   Creates new form TunesChecker
   */
  public TunesChecker() {
    appster = new Appster
        ("powersurgepub", "com",
          PROGRAM_NAME, PROGRAM_VERSION,
          language, country,
          this, this);
    home = Home.getShared ();
    programVersion = ProgramVersion.getShared ();
    
    initComponents();
    // getContentPane().add(statusBar, java.awt.BorderLayout.SOUTH);
    
    clear();
    
    WindowMenuManager.getShared(windowMenu);
    
    // Set About, Quit and other Handlers in platform-specific ways
    xos.setFileMenu (fileMenu);
    home.setHelpMenu(this, helpMenu);
    xos.setHelpMenu (helpMenu);
    xos.setHelpMenuItem(home.getHelpMenuItem());
    xos.setXHandler (this);
    xos.setMainWindow (this);
    xos.enablePreferences();

    // Initialize user preferences
    userPrefs = UserPrefs.getShared();
    prefsWindow = new PrefsWindow (this);
    
    attributesOptionCheckBox.setSelected
        (userPrefs.getPrefAsBoolean(PREFS_ATTRIBUTES, false));
    minTracksSlider.setValue(userPrefs.getPrefAsInt(PREFS_MIN_TRACKS, 2));
    
    filePrefs = new FilePrefs(this);
    filePrefs.loadFromPrefs();
    prefsWindow.setFilePrefs(filePrefs);
    
    setBounds (
        userPrefs.getPrefAsInt (PREFS_LEFT, 100),
        userPrefs.getPrefAsInt (PREFS_TOP,  100),
        userPrefs.getPrefAsInt (PREFS_WIDTH, 620),
        userPrefs.getPrefAsInt (PREFS_HEIGHT, 620));
    
    // Set up Logging
    logOutput = new LogOutputText(logTextArea);
    Logger.getShared().setLog (logOutput);
    Logger.getShared().setLogAllData (false);
    Logger.getShared().setLogThreshold (LogEvent.NORMAL);
    
    aboutWindow = new AboutWindow(
      false,   // loadFromDisk, 
      false,    // jxlUsed,
      false,   // pegdownUsed,
      false,   // xerces used
      false,   // saxon used
      "2015"); // copyRightYearFrom
    
    recentFiles = new RecentFiles();
    
    filePrefs.setRecentFiles(recentFiles);
    // recentFiles.registerMenu(openRecentMenu, this);
    
    recentFiles.loadFromPrefs();
    
    if (filePrefs.purgeRecentFilesAtStartup()) {
      recentFiles.purgeInaccessibleFiles();
    }
    
    File userDir = home.getUserHome();
    musicFolder = new File (userDir, MUSIC);
    if (musicFolder.exists() 
        && musicFolder.isDirectory()
        && musicFolder.canRead()) {
      fileChooser.setCurrentDirectory(musicFolder);
    }
    
    int lastTrackCount = userPrefs.getPrefAsInt(LAST_TRACK_COUNT, -1);
    if (lastTrackCount > 0) {
      lastTrackCountText.setText(String.valueOf(lastTrackCount));
    }
    
    int lastAnomalyCount = userPrefs.getPrefAsInt(LAST_ANOMALY_COUNT, -1);
    if (lastAnomalyCount >= 0) {
      lastAnomalyCountText.setText(String.valueOf(lastAnomalyCount));
    }
  }
  
  public RecentFiles getRecentFiles () {
    return recentFiles;
  }
  
  private void openLibrary1() {
    libIndex = 0;
    if (tunes.getLibraries().size() != libIndex) {
      trouble.report(this, 
          "Libraries must be opened in sequence", 
          "Library Sequence Error", 
          JOptionPane.ERROR_MESSAGE);
    } else {
      openFile();
    }
  }
  
  private void openLibrary2() {
    libIndex = 1;
    if (tunes.getLibraries().size() != libIndex) {
      trouble.report(this, 
          "Libraries must be opened in sequence", 
          "Library Sequence Error", 
          JOptionPane.ERROR_MESSAGE);
    } else {
      openFile();
    }
  }
  
  /**
   Clear any work done so far and prepare to load a new collection.
  */
  private void clear() {
    File userDir = home.getUserHome();
    musicFolder = new File (userDir, MUSIC);
    if (musicFolder.exists() 
        && musicFolder.isDirectory()
        && musicFolder.canRead()) {
      fileChooser.setCurrentDirectory(musicFolder);
    }
    
    tunes = new TunesCollection();
    tunesParser = new TunesParser();
    
    libsTable.setModel(tunes.getLibraries());
    TableColumn column;
    for (int i = 0; i < tunes.getLibraries().getColumnCount(); i++) {
      column = libsTable.getColumnModel().getColumn(i);
      column.setPreferredWidth(tunes.getLibraries().getColumnWidth(i)); 
    }
    
    AnomalyTypeTable anomalyTypes = AnomalyTypeTable.getShared();
    anomalyTypesTable.setModel(anomalyTypes);
    for (int i = 0; i < anomalyTypes.getColumnCount(); i++) {
      column = anomalyTypesTable.getColumnModel().getColumn(i);
      column.setPreferredWidth(anomalyTypes.getColumnWidth(i)); 
    }
    
    anomaliesTree.setModel(tunes.getAnomalies());
  }
  
  private void openFile() {

    fileToOpen = null;
    fileChooser.setDialogTitle ("Select iTunes Library " 
        + String.valueOf(libIndex + 1));
    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

    fileToOpen = fileChooser.showOpenDialog(this);
    if (fileToOpen != null) {
      if (fileToOpen.exists()
          && fileToOpen.canRead()) {
        handleOpenFile(fileToOpen);
      } else {
        logger.recordEvent(LogEvent.MINOR, "No valid file specified", false);
      }
    } // end if user approved a file/folder choice
    
  }
  
  /**
     Standard way to respond to an About Menu Item Selection on a Mac.
   */
  @Override
  public void handleAbout() {
    displayAuxiliaryWindow(aboutWindow);
  }
  
  /**      
    Standard way to respond to a document being passed to this application on a Mac.
   
    @param fileSpec File to be processed by this application, generally
                    as a result of a file or directory being dragged
                    onto the application icon.
   */
  @Override
  public void handleOpenFile (FileSpec fileSpec) {
    handleOpenFile (new File(fileSpec.getPath()));
  }
  
  /**      
    Standard way to respond to a document being passed to this application on a Mac.
   
    @param inFile File to be processed by this application, generally
                  as a result of a file or directory being dragged
                  onto the application icon.
   */
  @Override
  public void handleOpenFile (File inFile) {
    if (inFile.isFile()) {
      openLibraryFile(inFile);
    } else {
      String[] dirEntries = inFile.list();
      String dirEntry = "";
      boolean libraryFound = false;
      boolean musicFolderFound = false;
      int i = 0;
      while (i < dirEntries.length 
          && (! libraryFound)
          && (! musicFolderFound)) {
        dirEntry = dirEntries[i];
        if (dirEntry.endsWith(".xml")) {
          libraryFound = true;
        } // end if we've got an xml file
        else
        if (dirEntry.equals(MUSIC)) {
          musicFolderFound = true;
        }
        else {
          i++;
        }
      } // end while looking for an xml file
      if (libraryFound) {
        File libraryFile = new File(inFile, dirEntry);
        openLibraryFile(libraryFile);
      }
      else
      if (musicFolderFound) {
        // Can't do anything here
        // File mediaMusicFolder = new File(inFile, dirEntry);
        // openMediaMusicFolder(mediaMusicFolder);
      } else {
        // openMediaMusicFolder(inFile);
      }
    } // end if we have a folder instead of a file
      
  } // end method handleOpenFile
  
  /**
   Open an XML Library file. 
  
   @param libraryFile The file to open. 
  */
  private void openLibraryFile(File libraryFile) {
    logger.recordEvent(LogEvent.NORMAL, 
        "Opening library file at " + libraryFile.toString(), false);
    if (libraryFile.exists()
        && libraryFile.canRead()) {
      TunesLibrary library = new TunesLibrary();
      library.setLibraryFile(libraryFile);
      tunes.addLibrary(library);
      if (libIndex < 0) {
        // problem here
      } else {
        int tracksLoaded = tunesParser.parse(
            tunes, 
            libIndex, 
            libraryFile.toString());
        setTrackCount(tracksLoaded);
        tunes.getLibraries().fireTableDataChanged();
        Logger.getShared().recordEvent(LogEvent.NORMAL, 
            "Loaded " + String.valueOf(tracksLoaded) + " tracks", false);
        // tunes.display();
        recentFiles.addRecentFile ("library", libraryFile.toString(), "xml");
        File folder = library.getMusicFolder();
        if (folder == null) {
          Logger.getShared().recordEvent(LogEvent.MINOR, 
              "Music Folder not Identified", false);
        }
        else
        if (! folder.exists()) {
          Logger.getShared().recordEvent(LogEvent.MINOR, 
              "Music Folder cannot be found: " + folder.toString(), false);
        }
        else
        if (! folder.canRead()) {
          Logger.getShared().recordEvent(LogEvent.MINOR, 
              "Music Folder cannot be read: " + folder.toString(), false);
        } else {
          fileChooser.setCurrentDirectory(folder);
          openMediaMusicFolder(libIndex, folder);
        }
      }
      tunes.getLibraries().fireTableDataChanged();
    }
  }
  
  private void setTrackCount(int trackCount) {
    if (trackCount > currTrackCount) {
      currTrackCount = trackCount;
      currTrackCountText.setText(String.valueOf(trackCount));
      userPrefs.setPref(LAST_TRACK_COUNT, trackCount);
    }
  }
  
  private void openMediaMusicFolder(int libIndex, File inFolder) {
    String folderName = inFolder.getName();
    if (! folderName.equalsIgnoreCase(MUSIC)) {
      // No can do
    }
    logger.recordEvent(LogEvent.NORMAL, 
        "Opening Music folder at " + inFolder.toString(), false);
    if (inFolder != null
        && inFolder.exists()
        && inFolder.isDirectory()) {
      int tracksLoaded = tunesParser.scanMediaMusicFolder(tunes, libIndex, inFolder);
      Logger.getShared().recordEvent(LogEvent.NORMAL, 
          "Loaded " + String.valueOf(tracksLoaded) + " tracks", false);
      setTrackCount(tracksLoaded);
    }
  }
  
  private void exportToOPML() {
    File opmlFile = null;
    String exportFolderStr = userPrefs.getPref(EXPORT_FOLDER, "");
    if (exportFolderStr.length() > 0) {
      File exportFolder = new File(exportFolderStr);
      if (exportFolder.exists()
          && exportFolder.isDirectory()
          && exportFolder.canWrite()) {
        fileChooser.setCurrentDirectory(exportFolder);
        File opmlExportFile = new File(exportFolder,
            "tunes-outline-" + FilePrefs.getBackupDate() + ".opml");
        fileChooser.setSelectedFile(opmlExportFile);
      }
    }
    fileChooser.setDialogTitle ("Export to OPML");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    // fileChooser.setSelectedFile("tunes.opml");
    opmlFile = fileChooser.showSaveDialog(this);
    if (opmlFile != null) {
      MarkupWriter writer = new MarkupWriter(opmlFile, MarkupWriter.OPML_FORMAT);
      writer.openForOutput();
      writer.startBody();
      tunes.exportToOPML(writer);
      writer.endBody();
      writer.close();
      userPrefs.setPref(EXPORT_FOLDER, opmlFile.getParent().toString());
      Logger.getShared().recordEvent(LogEvent.NORMAL, 
          "Exported Library info to OPML file at " + opmlFile.toString(), 
          false);
    } // end if user approved a file/folder choice
  }
  
  private void exportToTabDelim() {
    File tabDelimFile = null;
    String exportFolderStr = userPrefs.getPref(EXPORT_FOLDER, "");
    if (exportFolderStr.length() > 0) {
      File exportFolder = new File(exportFolderStr);
      if (exportFolder.exists()
          && exportFolder.isDirectory()
          && exportFolder.canWrite()) {
        fileChooser.setCurrentDirectory(exportFolder);
        File tabDelimExportFile = new File(exportFolder,
            "tunes-tab-delim-" + FilePrefs.getBackupDate() + ".txt");
        fileChooser.setSelectedFile(tabDelimExportFile);
      }
    }
    fileChooser.setDialogTitle("Export to Tab-Delimited");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    tabDelimFile = fileChooser.showSaveDialog(this);
    if (tabDelimFile != null) {
      TabDelimFile tdf = new TabDelimFile(tabDelimFile);
      RecordDefinition recDef = new RecordDefinition();
      TunesCollection.addRecDefColumns(recDef);
      DataRecord rec = new DataRecord();
      try {
        tdf.openForOutput(recDef);
        tunes.exportToTabDelim(tdf, recDef, rec);
        tdf.close();
      } catch (IOException e) {
        trouble.report("I/O Error trying to export to tab-delimited", 
            "I/O Error", JOptionPane.ERROR_MESSAGE);
      }
      userPrefs.setPref(EXPORT_FOLDER, tabDelimFile.getParent().toString());
      Logger.getShared().recordEvent(LogEvent.NORMAL, 
          "Exported Library info to Tab-Delimited file at " + tabDelimFile.toString(), 
          false);
    }
    
  }
  
  /**
   Analyze the collection and identify anomalies. 
  */
  private void analyze() {
    TunesAnalysis analysis = new TunesAnalysis();
    analysis.setAttributesOption(attributesOptionCheckBox.isSelected());
    analysis.setMinTracks(minTracksSlider.getValue());
    
    userPrefs.setPref(PREFS_ATTRIBUTES, attributesOptionCheckBox.isSelected());
    userPrefs.setPref(PREFS_MIN_TRACKS, minTracksSlider.getValue());
    
    tunes.analyze(analysis);
    
    currAnomalyCountText.setText(String.valueOf(tunes.getAnomalyCount()));
    userPrefs.setPref(LAST_ANOMALY_COUNT, tunes.getAnomalyCount());
    
    // tabs.setSelectedComponent(treePanel);
  }
  
  /**
   Open the passed URI. 
   
   @param inURI The URI to open. 
  */
  public void handleOpenURI(URI inURI) {
    // Not supported
  }
  
  /**
   Prompt the user for a backup location. 
  
   @return True if backup was successful.
  */
  public boolean promptForBackup() {
    return false;
  }
  
  /**
   Backup without prompting the user. 
  
   @return True if backup was successful. 
  */
  public boolean backupWithoutPrompt() {
    return false;
  }
  
  public boolean preferencesAvailable() {
    return true;
  }
  
  /**
     Standard way to respond to a Preferences Item Selection on a Mac.
   */
  public void handlePreferences() {
    // displayPrefs ();
  }
  
  /**
   Standard way to respond to a print request.
   */
  public void handlePrintFile (File printFile) {
    // not supported
  }
  
  /**
     We're out of here!
   */
  public void handleQuit() {

    // closeFile();

    // savePrefs();

    System.exit(0);
  }
  
  public void displayAuxiliaryWindow(WindowToManage window) {
    window.setLocation(
        this.getX() + 60,
        this.getY() + 60);
    WindowMenuManager.getShared().makeVisible(window);
    window.toFront();
  }

  /**
   This method is called from within the constructor to initialize the form.
   WARNING: Do NOT modify this code. The content of this method is always
   regenerated by the Form Editor.
   */
  @SuppressWarnings("unchecked")
  // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
  private void initComponents() {
    java.awt.GridBagConstraints gridBagConstraints;

    tabs = new javax.swing.JTabbedPane();
    libsPanel = new javax.swing.JPanel();
    openLib1Button = new javax.swing.JButton();
    openLib2Button = new javax.swing.JButton();
    analyzeButton = new javax.swing.JButton();
    clearButton = new javax.swing.JButton();
    libsScrollPane = new javax.swing.JScrollPane();
    libsTable = new javax.swing.JTable();
    currTrackCountLabel = new javax.swing.JLabel();
    currTrackCountText = new javax.swing.JLabel();
    lastTrackCountLabel = new javax.swing.JLabel();
    lastTrackCountText = new javax.swing.JLabel();
    currAnomalyCountLabel = new javax.swing.JLabel();
    currAnomalyCountText = new javax.swing.JLabel();
    lastAnomalyCountLabel = new javax.swing.JLabel();
    lastAnomalyCountText = new javax.swing.JLabel();
    optionsPanel = new javax.swing.JPanel();
    attributesOptionLabel = new javax.swing.JLabel();
    attributesOptionCheckBox = new javax.swing.JCheckBox();
    minTracksLabel = new javax.swing.JLabel();
    minTracksSlider = new javax.swing.JSlider();
    anomalyTypesScrollPane = new javax.swing.JScrollPane();
    anomalyTypesTable = new javax.swing.JTable();
    treePanel = new javax.swing.JPanel();
    treeScrollPane = new javax.swing.JScrollPane();
    anomaliesTree = new javax.swing.JTree();
    logPanel = new javax.swing.JPanel();
    logScrollPane = new javax.swing.JScrollPane();
    logTextArea = new javax.swing.JTextArea();
    mainMenuBar = new javax.swing.JMenuBar();
    fileMenu = new javax.swing.JMenu();
    openLib1MenuItem = new javax.swing.JMenuItem();
    openLib2MenuItem = new javax.swing.JMenuItem();
    analyzeMenuItem = new javax.swing.JMenuItem();
    clearMenuItem = new javax.swing.JMenuItem();
    exportMenu = new javax.swing.JMenu();
    exportToOPMLMenuItem = new javax.swing.JMenuItem();
    exportToTabDelim = new javax.swing.JMenuItem();
    editMenu = new javax.swing.JMenu();
    windowMenu = new javax.swing.JMenu();
    helpMenu = new javax.swing.JMenu();

    setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

    libsPanel.setLayout(new java.awt.GridBagLayout());

    openLib1Button.setText("Open Library 1");
    openLib1Button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openLib1ButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    libsPanel.add(openLib1Button, gridBagConstraints);

    openLib2Button.setText("Open Library 2");
    openLib2Button.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openLib2ButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    libsPanel.add(openLib2Button, gridBagConstraints);

    analyzeButton.setText("Analyze");
    analyzeButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        analyzeButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    libsPanel.add(analyzeButton, gridBagConstraints);

    clearButton.setText("Clear");
    clearButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearButtonActionPerformed(evt);
      }
    });
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
    gridBagConstraints.weightx = 0.5;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    libsPanel.add(clearButton, gridBagConstraints);

    libsTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null}
      },
      new String [] {
        "Title 1", "Title 2", "Title 3", "Title 4"
      }
    ));
    libsScrollPane.setViewportView(libsTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 4;
    gridBagConstraints.gridwidth = 4;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    libsPanel.add(libsScrollPane, gridBagConstraints);

    currTrackCountLabel.setText("Current Track Count:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 4, 4);
    libsPanel.add(currTrackCountLabel, gridBagConstraints);

    currTrackCountText.setText("   ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 12);
    libsPanel.add(currTrackCountText, gridBagConstraints);

    lastTrackCountLabel.setText("Last Track Count:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 12, 4);
    libsPanel.add(lastTrackCountLabel, gridBagConstraints);

    lastTrackCountText.setText("   ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 12, 12);
    libsPanel.add(lastTrackCountText, gridBagConstraints);

    currAnomalyCountLabel.setText("Current Anomaly Count:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 4, 4);
    libsPanel.add(currAnomalyCountLabel, gridBagConstraints);

    currAnomalyCountText.setText("   ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 12);
    libsPanel.add(currAnomalyCountText, gridBagConstraints);

    lastAnomalyCountLabel.setText("Last Anomaly Count:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 2;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 12, 12, 4);
    libsPanel.add(lastAnomalyCountLabel, gridBagConstraints);

    lastAnomalyCountText.setText("   ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 3;
    gridBagConstraints.gridy = 3;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 12, 12);
    libsPanel.add(lastAnomalyCountText, gridBagConstraints);

    tabs.addTab("Libraries", libsPanel);

    optionsPanel.setLayout(new java.awt.GridBagLayout());

    attributesOptionLabel.setText("Include attributes with anomaly nodes?");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    optionsPanel.add(attributesOptionLabel, gridBagConstraints);

    attributesOptionCheckBox.setText(" ");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 0;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    optionsPanel.add(attributesOptionCheckBox, gridBagConstraints);

    minTracksLabel.setText("Minimum # of tracks for missing analysis:");
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    optionsPanel.add(minTracksLabel, gridBagConstraints);

    minTracksSlider.setMajorTickSpacing(5);
    minTracksSlider.setMaximum(20);
    minTracksSlider.setMinorTickSpacing(1);
    minTracksSlider.setPaintLabels(true);
    minTracksSlider.setPaintTicks(true);
    minTracksSlider.setSnapToTicks(true);
    minTracksSlider.setToolTipText("Minimum number of tracks in an album before it's worth looking for any missing tracks");
    minTracksSlider.setValue(2);
    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 1;
    gridBagConstraints.gridy = 1;
    gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
    gridBagConstraints.insets = new java.awt.Insets(4, 4, 4, 4);
    optionsPanel.add(minTracksSlider, gridBagConstraints);

    anomalyTypesTable.setModel(new javax.swing.table.DefaultTableModel(
      new Object [][] {
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null},
        {null, null, null, null}
      },
      new String [] {
        "Title 1", "Title 2", "Title 3", "Title 4"
      }
    ));
    anomalyTypesScrollPane.setViewportView(anomalyTypesTable);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.gridx = 0;
    gridBagConstraints.gridy = 2;
    gridBagConstraints.gridwidth = 2;
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    optionsPanel.add(anomalyTypesScrollPane, gridBagConstraints);

    tabs.addTab("Options", optionsPanel);

    treePanel.setLayout(new java.awt.GridBagLayout());

    treeScrollPane.setViewportView(anomaliesTree);

    gridBagConstraints = new java.awt.GridBagConstraints();
    gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
    gridBagConstraints.weightx = 1.0;
    gridBagConstraints.weighty = 1.0;
    treePanel.add(treeScrollPane, gridBagConstraints);

    tabs.addTab("Anomalies", treePanel);

    logPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
      public void componentShown(java.awt.event.ComponentEvent evt) {
        logPanelformComponentShown(evt);
      }
    });
    logPanel.setLayout(new java.awt.BorderLayout());

    logTextArea.setLineWrap(true);
    logTextArea.setWrapStyleWord(true);
    logScrollPane.setViewportView(logTextArea);

    logPanel.add(logScrollPane, java.awt.BorderLayout.CENTER);

    tabs.addTab("Log", logPanel);

    getContentPane().add(tabs, java.awt.BorderLayout.PAGE_START);
    tabs.getAccessibleContext().setAccessibleName("Anomalies");

    fileMenu.setText("File");

    openLib1MenuItem.setAccelerator(KeyStroke.getKeyStroke (KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
    openLib1MenuItem.setText("Open Library 1...");
    openLib1MenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        openLib1MenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(openLib1MenuItem);

    openLib2MenuItem.setText("Open Library 2...");
    fileMenu.add(openLib2MenuItem);

    analyzeMenuItem.setText("Analyze...");
    analyzeMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        analyzeMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(analyzeMenuItem);

    clearMenuItem.setText("Clear");
    clearMenuItem.setToolTipText("Clear all of the iTunes info collected so far");
    clearMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        clearMenuItemActionPerformed(evt);
      }
    });
    fileMenu.add(clearMenuItem);

    exportMenu.setText("Export");

    exportToOPMLMenuItem.setText("To OPML...");
    exportToOPMLMenuItem.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportToOPMLMenuItemActionPerformed(evt);
      }
    });
    exportMenu.add(exportToOPMLMenuItem);

    exportToTabDelim.setText("To Tab-Delimited...");
    exportToTabDelim.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        exportToTabDelimActionPerformed(evt);
      }
    });
    exportMenu.add(exportToTabDelim);

    fileMenu.add(exportMenu);

    mainMenuBar.add(fileMenu);

    editMenu.setText("Edit");
    mainMenuBar.add(editMenu);

    windowMenu.setText("Window");
    mainMenuBar.add(windowMenu);

    helpMenu.setText("Help");
    mainMenuBar.add(helpMenu);

    setJMenuBar(mainMenuBar);

    pack();
  }// </editor-fold>//GEN-END:initComponents

  private void openLib1MenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openLib1MenuItemActionPerformed
    openLibrary1();
  }//GEN-LAST:event_openLib1MenuItemActionPerformed

  private void logPanelformComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_logPanelformComponentShown
    logScrollPane.requestFocus();
  }//GEN-LAST:event_logPanelformComponentShown

  private void clearMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearMenuItemActionPerformed
    clear();
  }//GEN-LAST:event_clearMenuItemActionPerformed

  private void analyzeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzeMenuItemActionPerformed
    analyze();
  }//GEN-LAST:event_analyzeMenuItemActionPerformed

  private void openLib1ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openLib1ButtonActionPerformed
    openLibrary1();
  }//GEN-LAST:event_openLib1ButtonActionPerformed

  private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
    clear();
  }//GEN-LAST:event_clearButtonActionPerformed

  private void analyzeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_analyzeButtonActionPerformed
    analyze();
  }//GEN-LAST:event_analyzeButtonActionPerformed

  private void openLib2ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openLib2ButtonActionPerformed
    openLibrary2();
  }//GEN-LAST:event_openLib2ButtonActionPerformed

  private void exportToOPMLMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToOPMLMenuItemActionPerformed
    exportToOPML();
  }//GEN-LAST:event_exportToOPMLMenuItemActionPerformed

  private void exportToTabDelimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportToTabDelimActionPerformed
    exportToTabDelim();
  }//GEN-LAST:event_exportToTabDelimActionPerformed

  /**
   @param args the command line arguments
   */
  public static void main(String args[]) {
    /* Set the Nimbus look and feel */
    //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
     * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
     */
    /*
    try {
      for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          javax.swing.UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException ex) {
      java.util.logging.Logger.getLogger(TunesChecker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (InstantiationException ex) {
      java.util.logging.Logger.getLogger(TunesChecker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IllegalAccessException ex) {
      java.util.logging.Logger.getLogger(TunesChecker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      java.util.logging.Logger.getLogger(TunesChecker.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
    */
    //</editor-fold>

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
      public void run() {
        new TunesChecker().setVisible(true);
      }
    });
  }
  // Variables declaration - do not modify//GEN-BEGIN:variables
  private javax.swing.JButton analyzeButton;
  private javax.swing.JMenuItem analyzeMenuItem;
  private javax.swing.JTree anomaliesTree;
  private javax.swing.JScrollPane anomalyTypesScrollPane;
  private javax.swing.JTable anomalyTypesTable;
  private javax.swing.JCheckBox attributesOptionCheckBox;
  private javax.swing.JLabel attributesOptionLabel;
  private javax.swing.JButton clearButton;
  private javax.swing.JMenuItem clearMenuItem;
  private javax.swing.JLabel currAnomalyCountLabel;
  private javax.swing.JLabel currAnomalyCountText;
  private javax.swing.JLabel currTrackCountLabel;
  private javax.swing.JLabel currTrackCountText;
  private javax.swing.JMenu editMenu;
  private javax.swing.JMenu exportMenu;
  private javax.swing.JMenuItem exportToOPMLMenuItem;
  private javax.swing.JMenuItem exportToTabDelim;
  private javax.swing.JMenu fileMenu;
  private javax.swing.JMenu helpMenu;
  private javax.swing.JLabel lastAnomalyCountLabel;
  private javax.swing.JLabel lastAnomalyCountText;
  private javax.swing.JLabel lastTrackCountLabel;
  private javax.swing.JLabel lastTrackCountText;
  private javax.swing.JPanel libsPanel;
  private javax.swing.JScrollPane libsScrollPane;
  private javax.swing.JTable libsTable;
  private javax.swing.JPanel logPanel;
  private javax.swing.JScrollPane logScrollPane;
  private javax.swing.JTextArea logTextArea;
  private javax.swing.JMenuBar mainMenuBar;
  private javax.swing.JLabel minTracksLabel;
  private javax.swing.JSlider minTracksSlider;
  private javax.swing.JButton openLib1Button;
  private javax.swing.JMenuItem openLib1MenuItem;
  private javax.swing.JButton openLib2Button;
  private javax.swing.JMenuItem openLib2MenuItem;
  private javax.swing.JPanel optionsPanel;
  private javax.swing.JTabbedPane tabs;
  private javax.swing.JPanel treePanel;
  private javax.swing.JScrollPane treeScrollPane;
  private javax.swing.JMenu windowMenu;
  // End of variables declaration//GEN-END:variables
}
