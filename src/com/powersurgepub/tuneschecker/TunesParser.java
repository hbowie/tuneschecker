/*
 * Copyright 1999 - 2015 Herb Bowie
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

  import com.powersurgepub.psutils.*;
  import java.io.*;
  import java.util.*;
  import org.xml.sax.*;
  import org.xml.sax.helpers.*;

/**
 * TunesParser reads an iTunes Library XML file and returns album information.

 * @author Herb Bowie
 */
public class TunesParser
    extends DefaultHandler {
  
  public static final        String MUSIC         = "Music";
  public static final        String COMPILATIONS  = "Compilations";
  
  private     String              xmlSourceAsString;
  private     File                xmlSourceAsFile;

  /** Path to the original source file (if any). */
  private	String                  dataParent;

  /** Identifier for this file (to be printed in the log as a source ID). */
  private  String                 fileId;

  /** Data to be sent to the log. */
  private  LogData                logData;

  /** Do we want to log all data, or only data preceding significant events? */
  private  boolean                dataLogging = false;

  /** Debug instance. */
  private  Debug				          debug = new Debug(false);

  /** Sequential number identifying last record read or written. */
  private int                		  recordNumber = 0;

  private     XMLReader           parser;

  /** Data Record currently being built. */
  private     int                 elementLevel = -1;
  
  private     ArrayList           chars;
  
  private     File                mediaMusicFolder = null;
  private     File                artistFolder = null;
  
  /** Log used to record events. */
  private     Logger              log = Logger.getShared();

  
  private     String              key = "";
  private     TunesCollection     tunes = new TunesCollection();
  private     TunesArtist         artist = new TunesArtist();
  private     TunesAlbum          album = new TunesAlbum();
  private     TunesTrack          track = new TunesTrack();
  private     TunesFile           file = new TunesFile();
  
  private     String              musicFolder = "";
  
  private     String              albumArtist = "";
  
  private     boolean             musicTrack = true;
  
  private     int                 libIndex = 0;
  
  private     int                 tracksLoaded = 0;
  
  /**
   Create a new instance of TunesParser.
  */
  public TunesParser() {
    
  }
  /**
   Creates a new instance of TunesParser.

   @param xmlSourceAsString - File to be read.
   */
  public TunesParser(String xmlSourceAsString) {
    this.xmlSourceAsString = xmlSourceAsString;
    commonConstruction();
  }
  
  /**
   Creates a new instance of TunesParser.
   
   @param xmlSourceAsFile - File to be read. 
   */
  public TunesParser(File xmlSourceAsFile) {
    this.xmlSourceAsString = xmlSourceAsFile.toString();
    this.xmlSourceAsFile = xmlSourceAsFile;
    commonConstruction();
  }

  private void commonConstruction() {

  }
  
  /**
     Sets a logger to be used for logging operations.
    
     @param log Logger instance.
   */
  public void setLog (Logger log) {
    this.log = log;
  }

  /**
     Opens the XML file for subsequent input.

     @throws IOException If the input file is not found, or if there
                         is trouble reading it.
   */
  public void openForInput ()
      throws IOException {

  }
  
  /**
   Parse an iTunes Library file stored in XML. 
  
   @param tunes The TunesCollection in which the parsed information will be stored.
   @param libIndex
   @param xmlSourceAsString The path to the file to be scanned. 
  */
  public int parse (TunesCollection tunes,
      int libIndex,
      String xmlSourceAsString) {
    this.tunes = tunes;
    this.libIndex = libIndex;
    this.xmlSourceAsString = xmlSourceAsString;
    musicFolder = "";
    boolean ok = true;
    
    tracksLoaded = 0;

    try {
      parser = XMLReaderFactory.createXMLReader();
    } catch (SAXException e) {
      log.recordEvent (LogEvent.MINOR, 
          "Generic SAX Parser Not Found",
          false);
      try {
        parser = XMLReaderFactory.createXMLReader
            ("org.apache.xerces.parsers.SAXParser");
      } catch (SAXException eex) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Xerces SAX Parser Not Found",
            false);
        ok = false;
      }
    }
    if (ok) {
      parser.setContentHandler (this);
      xmlSourceAsFile = new File (xmlSourceAsString);
      if (! xmlSourceAsFile.exists()) {
        ok = false;
        log.recordEvent (LogEvent.MEDIUM, 
            "XML File or Directory " + xmlSourceAsString + " cannot be found",
            false);
      }
    }
    if (ok) {
      if (! xmlSourceAsFile.canRead()) {
        ok = false;
        log.recordEvent (LogEvent.MEDIUM, 
            "XML File or Directory " + xmlSourceAsString + " cannot be read",
            false);       
      }
    }
    if (ok) {
      if (xmlSourceAsFile.isFile()) {
        parseXMLFile (xmlSourceAsFile);
      }
    } // end if everything still OK
    tunes.getSources().setFromLibrary(libIndex);
    tunes.getLibraries().get(libIndex).setCount
        (TunesLibrary.LIB, TunesLibrary.TRACKS, tracksLoaded);
    
    return tracksLoaded;
  }  
  
  private void parseXMLFile (File xmlFile) {
    // System.out.println ("TunesParser.parseXMLFile " + xmlSourceAsString);
    dataParent = xmlFile.getParent();
    elementLevel = -1;
    chars = new ArrayList();
    try {
      parser.parse (xmlFile.toURI().toString());
    } 
    catch (SAXException saxe) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Encountered SAX error while reading XML file " + xmlFile.toString() 
            + saxe.toString(),
            false);   
    } 
    catch (java.io.IOException ioe) {
        log.recordEvent (LogEvent.MEDIUM, 
            "Encountered I/O error while reading XML file " + xmlFile.toString() 
            + ioe.toString(),
            false);   
    }
  }
  
  /**
   Handle the start of a new XML element. 
  
   @param namespaceURI
   @param localName
   @param qualifiedName
   @param attributes 
  */
  @Override 
  public void startElement (
      String namespaceURI,
      String localName,
      String qualifiedName,
      Attributes attributes) {
    // System.out.println ("startElement " + localName);
    StringBuffer str = new StringBuffer();
    elementLevel++;
    if (elementLevel >= 0) {
      storeField (elementLevel, str);
    }
    if (localName.equalsIgnoreCase("dict")) {
      initTrackVars();
    }
  } // end method
  
  /**
   Handle another array of characters. 
  
   @param ch
   @param start
   @param length 
  */
  @Override
  public void characters (char [] ch, int start, int length) {
    StringBuffer str = (StringBuffer)chars.get (elementLevel);
    str.append (ch, start, length);
  }
  
  /**
   Handle whitespace that can be safely ignored (by ignoring it).
  
   @param ch
   @param start
   @param length 
  */
  @Override
  public void ignorableWhitespace (char [] ch, int start, int length) {
    
  }
  
  /**
   Handle the end of an XML element. 
  
   @param namespaceURI
   @param localName
   @param qualifiedName 
  */
  @Override
  public void endElement (
      String namespaceURI,
      String localName,
      String qualifiedName) {
    // System.out.println ("TunesParser.endElement " + localName);
    if (elementLevel >= 0) {
      StringBuffer str = (StringBuffer)chars.get (elementLevel);
      // System.out.println ("  " + str.toString());
      if (localName.equalsIgnoreCase("key")) {
        key = str.toString();
      }
      else
      if (localName.equalsIgnoreCase("string")) {
        if (key.equalsIgnoreCase("Music Folder")) {
          tunes.getLibrary(libIndex).setMusicFolder(str.toString());
          musicFolder = str.toString();
        }
        else
        if (key.equalsIgnoreCase("Sort Name")) {
          track.setSortName(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Name")) {
          track.setName(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Artist")) {
          artist.setArtist(str.toString());
          if (album.getArtist().length() == 0) {
            album.setArtist(str.toString());
          }
        }
        else
        if (key.equalsIgnoreCase("Composer")) {
          track.setComposer(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Album")) {
          album.setAlbum(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Sort Album")) {
          album.setSortAlbum(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Genre")) {
          track.setGenre(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Sort Artist")) {
          artist.setSortArtist(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Album Artist")) {
          albumArtist = str.toString();
          artist.setArtist(albumArtist);
          album.setArtist(albumArtist);
        }
        else
        if (key.equalsIgnoreCase("Kind")) {
          file.setKind(str.toString());
        }
        else
        if (key.equalsIgnoreCase("Location")) {
          file.setLocation(str.toString());
          String location = str.toString();
          if (musicFolder.length() > 0
              && location.startsWith(musicFolder)
              && (! location.substring(musicFolder.length(), 
                musicFolder.length() + 5).equalsIgnoreCase(MUSIC))) {
            musicTrack = false;
          }
        }
        else
        if (key.startsWith("Playlist")) {
          musicTrack = false;
        }
        else
        if (key.equalsIgnoreCase("Track Type")) {
          if (! str.toString().equalsIgnoreCase("File")) {
            musicTrack = false;
          }
        }
      }
      else
      if (localName.equalsIgnoreCase("integer")) {
        int integer = 0;
        long longInt = 0;
        try {
          longInt = Long.parseLong(str.toString());
          integer = Integer.parseInt(str.toString());
        } catch (NumberFormatException e) {
          // System.out.println("Exception parsing " + key + " value of " + str.toString());
        }
        if (key.equalsIgnoreCase("Size")) {
          file.setSize(longInt);
        }
        else
        if (key.equalsIgnoreCase("Total Time")) {
          track.setTotalTime(integer);
        }
        else
        if (key.equalsIgnoreCase("Disc Number")) {
          album.setDiscNumber(integer);
        }
        else
        if (key.equalsIgnoreCase("Disc Count")) {
          album.setDiscCount(integer);
        }
        else
        if (key.equalsIgnoreCase("Track Number")) {
          track.setTrackNumber(integer);
        }
        else
        if (key.equalsIgnoreCase("Track Count")) {
          album.setTrackCount(integer);
        }
        else
        if (key.equalsIgnoreCase("Year")) {
          track.setYear(integer);
          album.setYear(integer);
        }
        else
        if (key.equalsIgnoreCase("Rating")) {
          track.setRating(integer);
        }
        else
        if (key.equalsIgnoreCase("Bit Rate")) {
          file.setBitRate(integer);
        }
        else
        if (key.equalsIgnoreCase("Sample Rate")) {
          file.setSampleRate(integer);
        }
      }
      else
      if (localName.equalsIgnoreCase("true")) {
        if (key.equalsIgnoreCase("Podcast")) {
          musicTrack = false;
        }
        else
        if (key.equalsIgnoreCase("Audiobooks")) {
          musicTrack = false;
        }
        else
        if (key.equalsIgnoreCase("Movie")) {
          musicTrack = false;
        }
        else
        if (key.equalsIgnoreCase("Has Video")) {
          musicTrack = false;
        }
        else
        if (key.equalsIgnoreCase("Compilation")) {
          album.setCompilation(true);
        }
      }
      else
      if (localName.equalsIgnoreCase("dict")) {
        if (
            // artist.hasKey() &&
            // (album.hasKey() || file.hasKey()) &&
            track.hasKey() 
            && (musicTrack)) {
          processTrack();
        }
      }
    } // end if we are within the desired record type
    elementLevel--;
  } // end method
  
  private void storeField (int level, StringBuffer str) {
    if (chars.size() > level) {
      chars.set (level, str);
    } else {
      chars.add (level, str);
    }
  } // end method
  
  /**
   Scan a music folder for the actual music files contained therein. 
  
   @param tunes
   @param libIndex
   @param inFolder 
  */
  public int scanMediaMusicFolder (TunesCollection tunes,
      int libIndex,
      File inFolder) {
    
    tracksLoaded = 0;
    this.tunes = tunes;
    this.libIndex = libIndex;
    String folderName = inFolder.getName();
    mediaMusicFolder = inFolder;
    if (! folderName.equalsIgnoreCase(MUSIC)) {
      mediaMusicFolder = new File(inFolder, MUSIC);
    }
    
    boolean ok = true;
    initTrackVars();
    
    if (mediaMusicFolder.exists()
        && mediaMusicFolder.canRead()
        && mediaMusicFolder.isDirectory()) {
      ok = true;
    } else {
      ok = false;
      Logger.getShared().recordEvent(LogEvent.MEDIUM, 
          "Invalid Media Music folder", false);
    }
    
    String[] artists;
    if (ok) {
      artists = mediaMusicFolder.list();
      for (int i = 0; i < artists.length; i++) {
        if (artists[i].equals(COMPILATIONS)) {
          artist = tunes.getCompilations();
        }
        else
        if (artists[i].startsWith(".")) {
          artist = null;
        } else {
          artist = new TunesArtist();
          artist.setArtist(artists[i]);
        }
        if (artist != null
            && artist.hasKey()) {
          artist.getSources().setFromFolder(libIndex);
          artist.setArtistFolderName(artists[i]);
          artist = tunes.storeArtist(artist);
          artist.setArtistFolderName(artists[i]);
          scanArtistFolder();
        } // end if we have a good artist
      } // end for each artist
      tunes.getSources().setFromFolder(libIndex);
    } // end if we have a good media music folder
    
    tunes.getLibraries().get(libIndex).setCount
        (TunesLibrary.MEDIA, TunesLibrary.TRACKS, tracksLoaded);
    
    return tracksLoaded;
  }  
  
  private void scanArtistFolder() {
    
    artistFolder = new File(mediaMusicFolder, 
        artist.getArtistFolderName());
    if (! artistFolder.exists()) {
      System.out.println("No Artist Folder");
      System.out.println("Media Music Folder = " + mediaMusicFolder.toString());
      System.out.println("Artist = " + artist.getArtist());
      System.out.println("Sort Artist = " + artist.getSortArtist());
      System.out.println("Artist Folder = " + artist.getArtistFolderName());
    }
    String[] albums = artistFolder.list();
    for (int j = 0; j < albums.length; j++) {
      if (! albums[j].startsWith(".")) {
        album = new TunesAlbum();
        album.setAlbum(albums[j]);
        if (album != null
            && album.hasKey()) {
          album.getSources().setFromFolder(libIndex);
          album.setAlbumFolderName(albums[j]);
          album = artist.storeAlbum(album);
          
          // System.out.println("    " + album.getSortAlbum());
          scanAlbumFolder();
        } // end if we have a good album
      } // end if we have a normal album file
    } // end for each album for this artist
  }
  
  private void scanAlbumFolder() {
    
    File albumFolder = new File(artistFolder, 
        album.getAlbumFolderName());
    if (albumFolder.exists()
        && albumFolder.isDirectory()
        && albumFolder.canRead()) {
      // A-OK
    } else {
      System.out.println("  *** album folder does not exist ***");
    }
    String[] tracks = albumFolder.list();
    for (int k = 0; k < tracks.length; k++) {
      if (! tracks[k].startsWith(".")
          && (! tracks[k].endsWith(".pdf"))) {
        track = new TunesTrack();
        TrackFileName trackFileName = new TrackFileName(tracks[k]);
        
        if (trackFileName.hasTrackNumber()) {
          track.setTrackNumber
              (trackFileName.getTrackNumber());
        }
        
        if (trackFileName.hasSortName()) {
          track.setSortName(trackFileName.getSortName());
        }
        if (trackFileName.hasFileName()) {
          track.setFileName(trackFileName.getFileName());
        }
        
        track.getSources().setFromFolder(libIndex);
        
        track = album.storeTrack(track);
        if ((! track.hasName())
            && trackFileName.hasTrackName()) {
          track.setName(trackFileName.getTrackName());
        } // End if track doesn't yet have a name
        tracksLoaded++;
      } // End if file name doesn't start with a period
    } // end for each directory entry
  }
  
  private void initTrackVars() {
    
    artist = new TunesArtist();
    
    album = new TunesAlbum();
    
    track = new TunesTrack();
    
    file = new TunesFile();
    
    musicTrack = true;
    albumArtist = "";
  }
  
  private void processTrack() {
    
    if (album.isCompilation()) {
      artist = tunes.getCompilations();
    }
    else
    if (albumArtist != null && albumArtist.length() > 0) {
      artist.setArtist(albumArtist);
      album.setArtist(albumArtist);
    }
    else
    if (artist.getArtist().length() == 0) {
      artist.setArtist("Unknown Artist");
    }
    
    if (album.getAlbum().length() == 0) {
      album.setAlbum("Unknown Album");
    }
    
    TrackFileName trackFileName = new TrackFileName(file.getLocation().toString());
    
    if (trackFileName.hasArtistName()) {
      artist.setArtistFolderName(trackFileName.getArtistName());
    }
    artist.getSources().setFromLibrary(libIndex);
    artist = tunes.storeArtist(artist);
    
    if (trackFileName.hasAlbumName()) {
      album.setAlbumFolderName(trackFileName.getAlbumName());
      if (album.getAlbum().length() == 0) {
        album.setAlbum(trackFileName.getAlbumName());
      }
      if (album.getSortAlbum().length() == 0) {
        album.setSortAlbum(trackFileName.getAlbumName());
      }
    }
    album.getSources().setFromLibrary(libIndex);
    album = artist.storeAlbum(album);
    
    if (trackFileName.hasFileName()) {
      track.setFileName(trackFileName.getFileName());
    }
    track.getSources().setFromLibrary(libIndex);
    // int priorNumberOfTracks = album.getNumberOfTracks();
    track = album.storeTrack(track);
    
    file.getSources().setFromLibrary(libIndex);
    file = track.storeFile(file);
    
    // if (album.getNumberOfTracks() > priorNumberOfTracks) {
      tracksLoaded++;
    // }
  }

  /**
     Retrieves the path to the original source file (if any).

     @return Path to the original source file (if any).
   */
  public String getDataParent () {
    if (dataParent == null) {
      return System.getProperty (GlobalConstants.USER_DIR);
    } else {
      return dataParent;
    }
  }

  /**
     Sets maximum number of data levels to expect, and to return.
     Defaults to 3, if not explicitly set via this method.

     @param maxDepth Maximum number of data levels to expec in the XML,
                     and to return in the output.
   */
  public void setMaxDepth (int maxDepth) {
    // this.maxDepth = maxDepth;
  }

  /**
     Sets the debug instance to the passed value.

     @param debug Debug instance.
   */
  public void setDebug (Debug debug) {
    this.debug = debug;
  }

  /**
     Sets the file ID to be passed to the Logger.

     @param fileId Used to identify the source of the data being logged.
   */
  public void setFileId (String fileId) {
    this.fileId = fileId;
    logData.setSourceId (fileId);
  }

  /**
     Sets the option to log all data off or on.

     @param dataLogging True to send all data read or written to the
                        log file.
   */
  public void setDataLogging (boolean dataLogging) {
    this.dataLogging = dataLogging;
  }
  
}
