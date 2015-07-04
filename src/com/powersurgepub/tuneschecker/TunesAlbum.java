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
 
 @author Herb Bowie
 */
public class TunesAlbum 
  implements Comparable<TunesAlbum>,
             TunesObject {
  
  public static final String      ALBUM_ARTIST = "Album Artist";
  public static final String      ALBUM = "Album";
  public static final String      SORT_ALBUM = "Sort Album";
  public static final String      ALBUM_FOLDER_NAME = "Album Folder Name";
  public static final String      ALBUM_COMMON_NAME = "Album Common Name";
  
  private     TunesArtist         tunesArtist = null;
  
  private     TunesSources        sources = new TunesSources();
  
  private     String              album = "";
  private     String              sortAlbum = "";
  private     String              albumFolderName = "";
  private     TunesCommonName     commonName = new TunesCommonName();

  private     String              artist = "";
  private     int                 trackCount = 0;
  private     int                 year = 0;
  private     int                 discNumber = 0;
  private     int                 discCount = 0;
  private     boolean             compilation = false;
  
  private ArrayList<TunesTrack>   tracks = new ArrayList<TunesTrack>();
  private ArrayList<TunesTrack>   tracksByNumber = new ArrayList<TunesTrack>();
  private TunesTrack              track = new TunesTrack();
  private int                     index = -1;
  
  private DefaultMutableTreeNode  anomalyNode = null;
  
  public TunesAlbum() {
    
  }
  
  public void setTunesArtist(TunesArtist tunesArtist) {
    this.tunesArtist = tunesArtist;
  }
  
  public TunesArtist getTunesArtist() {
    return tunesArtist;
  }
  
  public TunesSources getSources() {
    return sources;
  }
  
  public boolean hasKey() {
    return (commonName != null && commonName.length() > 0);
  }
  
  /**
   Does this album have the same sort key as another?
  
   @param album2 The second album.
  
   @return True if equal, false otherwise. 
  */
  public boolean equals(TunesAlbum album2) {
    return (compareTo(album2) == 0);
  }
  
  /**
   Compare this album to another instance of TunesAlbum to determine
   their sort order. 
  
   @param album2 The second album, to which this one will be compared. 
  
   @return a negative integer if this album is less than the second,
           a positive integer if this album is greater than the second, or
           zero if the two TunesAlbum instances represent the same album. 
  */
  @Override
  public int compareTo(TunesAlbum album2) {
    return commonName.compareTo(album2.getCommonName());
  }
  
  /**
   Merge the contents of two instances for the same album. 
  
   @param album2 The second instance for the same album. 
  */
  public void merge(TunesAlbum album2) {
    if (album2.getAlbum().length() > album.length()) {
      setAlbum(album2.getAlbum());
    }
    if (album2.getArtist().length() > 0) {
      setArtist(album2.getArtist());
    }
    if (album2.getSortAlbum().length() > 0
        && (sortAlbum.length() == 0
          || album2.getSortAlbum().length()
            < sortAlbum.length())) {
        setSortAlbum(album2.getSortAlbum());
    }
    if (album2.getTrackCount() > trackCount) {
      setTrackCount(album2.getTrackCount());
    }
    if (year == 0 || (album2.getYear() > 0 && album2.getYear() < year)) {
      setYear(album2.getYear());
    }
    if (album2.getDiscCount() > discCount) {
      setDiscCount(album2.getDiscCount());
    }
    if (album2.getDiscNumber() > 0) {
      setDiscNumber(album2.getDiscNumber());
    }
    if (album2.getAlbumFolderName().length() > 0) {
      setAlbumFolderName(album2.getAlbumFolderName());
    }
    sources.merge(album2.getSources());
  }
  
  public void setAlbum(String album) {
    this.album = album;
    if (sortAlbum.length() == 0) {
      sortAlbum = TunesCollection.makeSortName(album);
    }
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(album);
    }
  }
  
  public String getAlbum() {
    return album;
  }
  
  public void setSortAlbum(String sortAlbum) {
    this.sortAlbum = sortAlbum;
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(sortAlbum);
    }
  }
  
  public String getSortAlbum() {
    return sortAlbum;
  }
  
  public void setAlbumFolderName(String albumFolderName) {
    this.albumFolderName = albumFolderName;
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(albumFolderName);
    }
  }
  
  public String getAlbumFolderName() {
    return albumFolderName;
  }
  
  public TunesCommonName getCommonName() {
    return commonName;
  }
  
  public void setArtist(String artist) {
    this.artist = artist;
  }
  
  public String getArtist() {
    return artist;
  }
  
  public void setTrackCount(int trackCount) {
    this.trackCount = trackCount;
  }
  
  public int getTrackCount() {
    return trackCount;
  }
  
  public void setYear(int year) {
    if (this.year == 0
        || this.year > year) {
      this.year = year;
    }
  }
  
  public int getYear() {
    return year;
  }
  
  public void setDiscNumber(int discNumber) {
    this.discNumber = discNumber;
  }
  
  public int getDiscNumber() {
    return discNumber;
  }
  
  public void setDiscCount(int discCount) {
    this.discCount = discCount;
  }
  
  public int getDiscCount() {
    return discCount;
  }
  
  public void setCompilation(boolean compilation) {
    this.compilation = compilation;
  }
  
  public boolean getCompilation() {
    return compilation;
  }
  
  public boolean isCompilation() {
    return compilation;
  }
  
  /**
   Store the passed trackToDisplay info, either in a new list entry, 
   or by merging with an existing entry for the same trackToDisplay. 
  
   @param trackToStore The trackToDisplay to be stored. 
  
   @return The resulting trackToDisplay entry. 
  */
  public TunesTrack storeTrack(TunesTrack trackToStore) {
    
    // Store in trackToDisplay name sequence
    if (tracks.isEmpty()) {
      tracks.add(trackToStore);
      index = 0;
      track = trackToStore;
    }
    else
    if (index >= 0 && index < tracks.size() && track != null
        && track.equals(trackToStore)) {
      track.merge(trackToStore);
    }
    else {
      index = 0;
      int comparison = 1;
      while (index < tracks.size() && comparison > 0) {
        comparison = trackToStore.compareTo(tracks.get(index));
        if (comparison > 0) {
          index++;
        } // end if still looking for the right insertion point
      } // end while looking for the right insertion point
      if (index == tracks.size() || comparison < 0) {
        tracks.add(index, trackToStore);
        track = tracks.get(index);
      } 
      else {
        tracks.get(index).merge(trackToStore);
        track = tracks.get(index);
      }
    } // end if we had to go looking for an insertion point
    
    // Store in trackToDisplay number sequence
    while (tracksByNumber.size() < track.getTrackNumber()) {
      tracksByNumber.add(null);
    }
    if (tracksByNumber.size() == track.getTrackNumber()) {
      tracksByNumber.add(track);
    } else {
      tracksByNumber.set(track.getTrackNumber(), track);
    }
    
    track.setTunesAlbum(this);
    
    return track;
  }
  
  /**
   Return the actual number of track objects stored in this album object. 
  
   @return The number of track object stored in this album object. 
  */
  public int getNumberOfTracks() {
    return tracks.size();
  }
  
  public void display() {
    System.out.print("    " + sortAlbum);
    if (! album.equals(sortAlbum)) {
      System.out.print(" (" + album + ")");
    }
    if (artist.length() > 0) {
      System.out.print(", by " + artist);
    }
    if (year > 0) {
      System.out.print(", " + String.valueOf(year));
    }
    if (discNumber > 0 || discCount > 0) {
      System.out.print(", disc # " + String.valueOf(discNumber)
          + " of " + String.valueOf(discCount));
    }
    if (trackCount > 0) {
      System.out.print(", " + String.valueOf(trackCount) + " tracks");
    }
    System.out.println(" ");
    
    TunesTrack trackToDisplay = null;
    for (int i = 0; i < tracks.size(); i++) {
      trackToDisplay = tracks.get(i);
      if (trackToDisplay == null) {
        if (i > 0) {
          System.out.println("      " + String.valueOf(i) + ". ");
        }
      } else {
        trackToDisplay.display();
      }
    }
  }
  
  /**
   Analyze this object and identify any anomalies. 
  */
  public void analyze(TunesCollection collection, TunesAnalysis analysis) {
    
    // Analyze this album
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
    for (TunesTrack nextTrack: tracks) {
      nextTrack.analyze(collection, analysis);
    }
    
    if (tracks.size() > analysis.getMinTracks()
        && analysis.getMinTracks() > 0) {
      for (int trackNumber = 1; 
          trackNumber <= trackCount || trackNumber < tracksByNumber.size(); 
          trackNumber++) {
        TunesTrack nextTrack = null;
        if (trackNumber < tracksByNumber.size()) {
          nextTrack = tracksByNumber.get(trackNumber);
        }
        if (nextTrack == null) {
          collection.addAnomaly(-1, this, 
              analysis.getAnomalyType(AnomalyTypeTable.MISSING_TRACK_NUMBER), 
              trackNumber, analysis);
        }
      }
    }

  }
  
  /**
   Export this object to an OPML file. 
  
   @param writer The OPML writer. 
  */
  public void exportToOPML(MarkupWriter writer) {
    
    String yyyy = "    ";
    if (year > 0) {
      yyyy = String.valueOf(year);
    }
    
    writer.startOutlineOpen();
    writer.writeOutlineAttribute(TextType.TEXT, album);
    writer.writeOutlineAttribute("Artist", artist);
    writer.writeOutlineAttribute("Year", yyyy);
    writer.startOutlineClose();
    for (TunesTrack nextTrack: tracksByNumber) {
      if (nextTrack != null) {
        nextTrack.exportToOPML(writer);
      }
    }
    writer.endOutline();
  }
  
  
  public static void addRecDefColumns(RecordDefinition recDef) {

    recDef.addColumn(ALBUM_ARTIST);
    recDef.addColumn(ALBUM);
    recDef.addColumn(SORT_ALBUM);
    recDef.addColumn(ALBUM_FOLDER_NAME);
    recDef.addColumn(ALBUM_COMMON_NAME);
    
    TunesTrack.addRecDefColumns(recDef);
  }
  
  public void exportToTabDelim(
      TabDelimFile tdf, 
      RecordDefinition recDef, 
      DataRecord rec) 
        throws IOException {
    
    rec.storeField(recDef, ALBUM_ARTIST, artist);
    rec.storeField(recDef, ALBUM, album);
    rec.storeField(recDef, SORT_ALBUM, sortAlbum);
    rec.storeField(recDef, ALBUM_FOLDER_NAME, albumFolderName);
    rec.storeField(recDef, ALBUM_COMMON_NAME, commonName.toString());
    
    for (TunesTrack nextTrack: tracksByNumber) {
      if (nextTrack != null) {
        nextTrack.exportToTabDelim(tdf, recDef, rec);
      }
    }
    
  }
  
  /**
   Return a string to be used to identify the node in the anomaly tree. 
  
   @return A String representation of the object. 
  */
  @Override
  public String toString() {
    return "Album: " + album;
  }
  
  /**
   Add this object as a node in the anomaly tree.
  */
  @Override
  public void addToAnomalyTree(TunesAnalysis analysis) {
    if (anomalyNode == null) {
      DefaultMutableTreeNode thisAnomaly = new DefaultMutableTreeNode(this);
      if (tunesArtist.getAnomalyNode() == null) {
        tunesArtist.addToAnomalyTree(analysis);
      }
      tunesArtist.getAnomalyNode().add(thisAnomaly);
      anomalyNode = thisAnomaly;
      if (analysis.getAttributesOption()) {
        TunesCollection.addToTree(thisAnomaly, "Sort Album:   " + sortAlbum);
        TunesCollection.addToTree(thisAnomaly, "Album Folder: " + albumFolderName);
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
