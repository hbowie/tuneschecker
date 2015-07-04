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
  import com.powersurgepub.psdatalib.txbmodel.*;
  import java.io.*;
  import java.util.*;
  import javax.swing.tree.*;

/**
 A single track. 

 @author Herb Bowie
 */
public class TunesTrack 
  implements Comparable<TunesTrack>,
             TunesObject {
  
  public static final String      TRACK_NUMBER      = "Track Number";
  public static final String      TRACK_NAME        = "Track Name";
  public static final String      TRACK_SORT_NAME   = "Track Sort Name";
  public static final String      TRACK_FILE_NAME   = "Track File Name";
  public static final String      TRACK_COMMON_NAME = "Track Common Name";
  public static final String      TRACK_ARTIST      = "Track Artist";
  public static final String      COMPOSER          = "Composer";
  public static final String      GENRE             = "Genre";
  public static final String      YEAR              = "Year";
  public static final String      RATING            = "Rating";
  public static final String      PLAYING_TIME      = "Time";
  
  private     TunesAlbum          tunesAlbum = null;
  
  private     TunesSources        sources = new TunesSources();
  
  private     int                 trackNumber = 0;
  private     String              name = "";
  private     String              sortName = "";
  private     String              fileName = "";
  private     TunesCommonName     commonName = new TunesCommonName();
  
  private     String              sortArtist = "";
  private     String              artist = "";
  private     String              composer = "";
  private     String              genre = "";
  private     int                 year = 0;
  private     int                 rating = 0;
  private     int                 totalTime = 0;
  
  private ArrayList<TunesFile>    files = new ArrayList<TunesFile>();
  private TunesFile               file = new TunesFile();
  private int                     index = -1;
  
  private DefaultMutableTreeNode  anomalyNode = null;
  
  public TunesTrack() {
    
  }
  
  public void setTunesAlbum(TunesAlbum tunesAlbum) {
    this.tunesAlbum = tunesAlbum;
  }
  
  private TunesAlbum getTunesAlbum() {
    return tunesAlbum;
  }
  
  public TunesSources getSources() {
    return sources;
  }
  
  public boolean hasKey() {
    return (commonName != null && commonName.length() > 0);
  }
  
  /**
   Does this track have the same sort key as another?
  
   @param track2 The second track.
  
   @return True if equal, false otherwise. 
  */
  public boolean equals(TunesTrack track2) {
    return (compareTo(track2) == 0);
  }
  
  /**
   Compare this track to another instance of TunesTrack to determine
   their sort order. 
  
   @param track2 The second track, to which this one will be compared. 
  
   @return a negative integer if this track is less than the second,
           a positive integer if this track is greater than the second, or
           zero if the two TunesTrack instances represent the same track. 
  */
  @Override
  public int compareTo(TunesTrack track2) {
    return commonName.compareTo(track2.getCommonName());
  }
  
  public void merge(TunesTrack track2) {
    if (track2.getName().length() > name.length()) {
      setName(track2.getName());
    }
    
    if (track2.getSortName().length() > 0
        && (sortName.length() == 0
          || sortName.length() > track2.getSortName().length())) {
      setSortName(track2.getSortName());
    }
    
    if (track2.getFileName().length() > 0
        && (fileName.length() == 0
          || fileName.length() > track2.getFileName().length())) {
      setFileName(track2.getFileName());
    }
    
    if (track2.getTrackNumber() > 0) {
      setTrackNumber(track2.getTrackNumber());
    }
    
    sources.merge(track2.getSources());
  }
  
  public void setName(String name) {
    this.name = name;
    if (sortName.length() == 0) {
      sortName = TunesCollection.makeSortName(name);
    }
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(name, TunesCommonName.TRACK_NAME);
    }
  }
  
  public boolean hasName() {
    return (name.length() > 0);
  }
  
  public String getName() {
    return name;
  }
  
  public void setSortName(String sortName) {
    this.sortName = sortName;
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(sortName, TunesCommonName.TRACK_NAME);
    }
  }
  
  public String getSortName() {
    return sortName;
  }
  
  public void setFileName(String fileName) {
    this.fileName = fileName;
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(fileName, TunesCommonName.TRACK_NAME);
    }
  }
  
  public boolean hasFileName() {
    return (fileName != null && fileName.length() > 0);
  }
  
  public String getFileName() {
    return fileName;
  }
  
  public TunesCommonName getCommonName() {
    return commonName;
  }
  
  public void setYear(int year) {
    this.year = year;
  }
  
  public int getYear() {
    return year;
  }
  
  public void setTrackNumber(String trackNumber) {
    try {
      setTrackNumber(Integer.parseInt(trackNumber));
    } catch (NumberFormatException e) {
      System.out.println("Trouble converting track number to an integer");
    }
  }
  
  public void setTrackNumber(int trackNumber) {
    this.trackNumber = trackNumber;
  }
  
  public int getTrackNumber() {
    return trackNumber;
  }
  
  public void setArtist(String artist) {
    this.artist = artist;
    if (sortArtist.length() == 0) {
      sortArtist = artist;
    }
  }
  
  public String getArtist() {
    return artist;
  }
  
  public void setSortArtist(String sortArtist) {
    this.sortArtist = sortArtist;
  }
  
  public String getSortArtist() {
    return sortArtist;
  }
  
  public void setComposer(String composer) {
    this.composer = composer;
  }
  
  public String getComposer() {
    return composer;
  }
  
  public void setGenre(String genre) {
    this.genre = genre;
  }
  
  public String getGenre() {
    return genre;
  }
  
  public void setRating(int rating) {
    this.rating = rating;
  }
  
  public int getRating() {
    return rating;
  }
  
  public void setTotalTime(int totalTime) {
    this.totalTime = totalTime;
  }
  
  public int getTotalTime() {
    return totalTime;
  }
  
  /**
   Store the passed file info, either in a new list entry, 
   or by merging with an existing entry for the same file. 
  
   @param fileToStore The file to be stored. 
  
   @return The resulting file entry. 
  */
  public TunesFile storeFile(TunesFile fileToStore) {
    
    if (files.isEmpty()) {
      files.add(fileToStore);
      index = 0;
      file = fileToStore;
    }
    else
    if (index >= 0 && index < files.size() && file != null
        && file.equals(fileToStore)) {
      file.merge(fileToStore);
    }
    else {
      index = 0;
      int comparison = 1;
      while (index < files.size() && comparison > 0) {
        comparison = fileToStore.compareTo(files.get(index));
        if (comparison > 0) {
          index++;
        } // end if still looking for the right insertion point
      } // end while looking for the right insertion point
      if (index == files.size() || comparison < 0) {
        files.add(index, fileToStore);
        file = files.get(index);
      } 
      else {
        files.get(index).merge(fileToStore);
        file = files.get(index);
      }
    } // end if we had to go looking for an insertion point
    
    file.setTunesTrack(this);
    
    return file;
  }
  
  public void display() {
    System.out.print("      " + String.valueOf(trackNumber) + ". ");
    System.out.print(sortName);
    if (! sortName.equals(name)) {
      System.out.print(" (" + name + ")");
    }
    System.out.println(" ");
    for (int i = 0; i < files.size(); i++) {
      files.get(i).display();
    }
  }
  
  /**
   Analyze this object and identify any anomalies. 
  */
  public void analyze(TunesCollection collection, TunesAnalysis analysis) {
    
    // Analyze this track
    for (int libIndex = 0; libIndex < collection.getNumberOfLibraries(); libIndex++) {
      if (sources.isFromBoth(libIndex)) {
        // Everything is cool
      }
      else
      if (sources.isFromFolder(libIndex)) {
        collection.addAnomaly(libIndex, this,  
            analysis.getAnomalyType(AnomalyTypeTable.ON_DISK_NOT_IN_LIB), 
            analysis);
      } 
      else
      if (sources.isFromLibrary(libIndex)) {
        collection.addAnomaly(libIndex, this,  
            analysis.getAnomalyType(AnomalyTypeTable.IN_LIB_NOT_ON_DISK), 
            analysis);
      } else {
        collection.addAnomaly(libIndex, this, 
            analysis.getAnomalyType(AnomalyTypeTable.MISSING_FROM_LIBRARY), 
            analysis);
      }
    } // end for each library
    
    // Now perform analysis for each track
    for (TunesFile nextFile: files) {
      // nextFile.analyze(collection);
    }
  }
  
  /**
   Export this object to an OPML file. 
  
   @param writer The OPML writer. 
  */
  public void exportToOPML(MarkupWriter writer) {
    writer.startOutlineOpen();
    writer.writeOutlineAttribute(TextType.TEXT, name);
    writer.writeOutlineAttribute("Number", 
        String.format("%02d", getTrackNumber()));
    writer.writeOutlineAttribute("Genre", genre);
    writer.writeOutlineAttribute("Rating", String.valueOf(rating / 20.0));
    writer.startOutlineClose();
    /* for (TunesTrack nextTrack: tracks) {
      nextTrack.exportToOPML(writer);
    } */
    writer.endOutline();
  }
  
  public static void addRecDefColumns(RecordDefinition recDef) {

    recDef.addColumn(TRACK_NUMBER);
    recDef.addColumn(TRACK_NAME);
    recDef.addColumn(TRACK_SORT_NAME);
    recDef.addColumn(TRACK_FILE_NAME);
    recDef.addColumn(TRACK_COMMON_NAME);
    recDef.addColumn(TRACK_ARTIST);
    recDef.addColumn(COMPOSER);
    recDef.addColumn(GENRE);
    recDef.addColumn(YEAR);
    recDef.addColumn(RATING);
    recDef.addColumn(PLAYING_TIME);
    
    // TunesTrack.addRecDefColumns(recDef);
  }
  
  public void exportToTabDelim(
      TabDelimFile tdf, 
      RecordDefinition recDef, 
      DataRecord rec) 
        throws IOException {
    
    rec.storeField(recDef, TRACK_NUMBER, 
        String.format("%02d", getTrackNumber()));
    rec.storeField(recDef, TRACK_NAME, name);
    rec.storeField(recDef, TRACK_SORT_NAME, sortName);
    rec.storeField(recDef, TRACK_FILE_NAME, fileName);
    rec.storeField(recDef, TRACK_COMMON_NAME, commonName.toString());
    rec.storeField(recDef, TRACK_ARTIST, artist);
    rec.storeField(recDef, COMPOSER, composer);
    rec.storeField(recDef, GENRE, genre);
    rec.storeField(recDef, YEAR, String.valueOf(year));
    rec.storeField(recDef, RATING, String.valueOf(rating / 20.0));
    rec.storeField(recDef, PLAYING_TIME, String.valueOf(totalTime));
    
    tdf.nextRecordOut(rec);
    
  }
  
  /**
   Return a string to be used to identify the node in the anomaly tree. 
  
   @return A String representation of the object. 
  */
  @Override
  public String toString() {
    return "Track: " + name;
  }
  
  /**
   Add this object as a node in the anomaly tree.
  */
  @Override
  public void addToAnomalyTree(TunesAnalysis analysis) {
    if (anomalyNode == null) {
      DefaultMutableTreeNode thisAnomaly = new DefaultMutableTreeNode(this);
      if (tunesAlbum.getAnomalyNode() == null) {
        tunesAlbum.addToAnomalyTree(analysis);
      }
      tunesAlbum.getAnomalyNode().add(thisAnomaly);
      anomalyNode = thisAnomaly;
      if (analysis.getAttributesOption()) {
        TunesCollection.addToTree(thisAnomaly, "Sort Name:   " + sortName);
        TunesCollection.addToTree(thisAnomaly, "File Name:   " + fileName);
        TunesCollection.addToTree(thisAnomaly, 
            "Matching Key: " + commonName.toString());
      }
    }
  }
  
  /**
   Get the anomaly node that represents this object in the anomaly tree. 
  
   @return The anomaly node representing this object in the tree. 
  */
  @Override
  public DefaultMutableTreeNode getAnomalyNode() {
    return anomalyNode;
  }

}
