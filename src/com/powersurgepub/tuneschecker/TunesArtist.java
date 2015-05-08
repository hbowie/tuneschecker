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

  import com.powersurgepub.psdatalib.txbio.*;
  import java.util.*;
  import javax.swing.tree.*;

/**
 An artist and his albums. 

 @author Herb Bowie
 */
public class TunesArtist 
  implements Comparable<TunesArtist>,
             TunesObject {
  
  private     TunesSources        sources = new TunesSources();
  
  private     TunesCollection     tunes = null;
  
  private     String              artist = "";
  private     String              sortArtist = "";
  private     String              artistFolderName = "";
  private     TunesCommonName     commonName = new TunesCommonName();
  
  private ArrayList<TunesAlbum>   albums = new ArrayList<TunesAlbum>();
  private ArrayList<TunesAlbum>   albumsByYear = new ArrayList<TunesAlbum>();
  private TunesAlbum              album = new TunesAlbum();
  private int                     index = -1;
  
  private DefaultMutableTreeNode  anomalyNode = null;
  
  public TunesArtist() {
    
  }
  
  public void setCollection(TunesCollection tunes) {
    this.tunes = tunes;
  }
  
  public TunesSources getSources() {
    return sources;
  }
  
  private TunesCollection getCollection() {
    return tunes;
  }
  
  public boolean hasKey() {
    return (commonName != null && commonName.length() > 0);
  }
  
  /**
   Does this artist have the same sort key as another?
  
   @param artist2 The second artist.
  
   @return True if equal, false otherwise. 
  */
  public boolean equals(TunesArtist artist2) {
    return (compareTo(artist2) == 0);
  }
  
  /**
   Compare this artist to another instance of TunesArtist to determine
   their sort order. 
  
   @param artist2 The second artist, to which this one will be compared. 
  
   @return a negative integer if this artist is less than the second,
           a positive integer if this artist is greater than the second, or
           zero if the two TuneArtist instances represent the same artist. 
  */
  @Override
  public int compareTo(TunesArtist artist2) {
    return commonName.compareTo(artist2.getCommonName());
  }
  
  /**
   Merge the contents of two instances for the same artist. 
  
   @param artist2 The second instance for the same artist. 
  */
  public void merge(TunesArtist artist2) {
    if (artist2.getArtist().length() > artist.length()) {
      setArtist(artist2.getArtist());
    }
    if (artist2.getSortArtist().length() > 0 
        && (artist2.getSortArtist().length() < sortArtist.length()
          || sortArtist.length() == 0)) {
      setSortArtist(artist2.getSortArtist());
    }
    if (artist2.getArtistFolderName().length() > artistFolderName.length()) {
      setArtistFolderName(artist2.getArtistFolderName());
    }
    sources.merge(artist2.getSources());
  }
  
  public void setArtist(String artist) {
    this.artist = artist;
    if (sortArtist.length() == 0) {
      setSortArtist(TunesCollection.makeSortName(artist));
    }
    if (artistFolderName == null
        || artistFolderName.length() == 0) {
      artistFolderName = artist;
    }
    commonName.setName(artist, TunesCommonName.ARTIST_NAME);
  }
  
  public String getArtist() {
    return artist;
  }
  
  public void setSortArtist(String sortArtist) {
    this.sortArtist = sortArtist;
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(sortArtist, TunesCommonName.ARTIST_NAME);
    }
  }
  
  public String getSortArtist() {
    return sortArtist;
  }
  
  public void setArtistFolderName(String artistFolderName) {
    this.artistFolderName = artistFolderName;
    if (commonName == null || commonName.length() == 0) {
      commonName.setName(artistFolderName, TunesCommonName.ARTIST_NAME);
    }
  }
  
  public String getArtistFolderName() {
    return artistFolderName;
  }
  
  public TunesCommonName getCommonName() {
    return commonName;
  }
  
  /**
   Store the passed album info, either in a new list entry, 
   or by merging with an existing entry for the same album. 
  
   @param albumToStore The album to be stored. 
  
   @return The resulting album entry. 
  */
  public TunesAlbum storeAlbum(TunesAlbum albumToStore) {
    
    if (albums.size() == 0) {
      albums.add(albumToStore);
      albumsByYear.add(albumToStore);
      index = 0;
      album = albumToStore;
    }
    else
    if (index >= 0 && index < albums.size() && album != null
        && album.equals(albumToStore)) {
      album.merge(albumToStore);
      sortAlbumsByYear();
    }
    else {
      index = 0;
      int comparison = 1;
      while (index < albums.size() && comparison > 0) {
        comparison = albumToStore.compareTo(albums.get(index));
        if (comparison > 0) {
          index++;
        } // end if still looking for the right insertion point
      } // end while looking for the right insertion point
      if (index == albums.size() || comparison < 0) {
        albums.add(index, albumToStore);
        albumsByYear.add(albumToStore);
        album = albums.get(index);
      } 
      else {
        albums.get(index).merge(albumToStore);
        album = albums.get(index);
      }
      sortAlbumsByYear();
    } // end if we had to go looking for an insertion point
    
    album.setTunesArtist(this);
    
    return album;
  }
  
  private void sortAlbumsByYear() {
    boolean sorted = false;
    TunesAlbum prior;
    TunesAlbum next;
    while (! sorted) {
      sorted = true;
      for (int i = 1; i < albumsByYear.size(); i++) {
        prior = albumsByYear.get(i - 1);
        next  = albumsByYear.get(i);
        if (next.getYear() < prior.getYear()) {
          sorted = false;
          albumsByYear.set(i, prior);
          albumsByYear.set(i - 1, next);
        } // end if we need to swap entries
      } // end one pass through the list
    } // end while not yet sorted
  } // end method sortAlbumsByYear
  
  public void display() {
    
    System.out.print("  " + sortArtist.toString());
    if (! artist.equals(sortArtist.toString())) {
      System.out.print(" (" + artist + ")");
    }
    System.out.println("");
    
    for (index = 0; index < albums.size(); index++) {
      albums.get(index).display();
    }    
  }
  
  public void displayString(String label, String str) {
    System.out.println(label + ": " + str);
    for (int j = 0; j < str.length(); j++) {
      char c = str.charAt(j);
      int i = (int)c;
      System.out.println("  " + c + " = " + String.valueOf(i));
    }
  }
  
  /**
   Analyze this object and identify any anomalies. 
  */
  public void analyze(TunesCollection collection, TunesAnalysis analysis) {
    
    // Analyze this artist
    for (int libIndex = 0; libIndex < collection.getNumberOfLibraries(); libIndex++) {
      if (sources.isFromBoth(libIndex)) {
        // Everything is cool
      }
      else
      if (sources.isFromFolder(libIndex)) {
        collection.addAnomaly(libIndex, this,  
            TunesAnomaly.ON_DISK_NOT_IN_LIB, analysis);
      } 
      else
      if (sources.isFromLibrary(libIndex)) {
        collection.addAnomaly(libIndex, this,  
            TunesAnomaly.IN_LIB_NOT_ON_DISK, analysis);
      }
    } // end for each library
    
    // Now perform analysis for each album
    for (TunesAlbum album: albums) {
      album.analyze(collection, analysis);
    }
  }
  
  /**
   Export this object to an OPML file. 
  
   @param writer The OPML writer. 
  */
  public void exportToOPML(MarkupWriter writer) {
    writer.startOutline(artist);
    for (TunesAlbum album: albumsByYear) {
      album.exportToOPML(writer);
    }
    writer.endOutline();
  }

  /**
   Return a string to be used to identify the node in the anomaly tree. 
  
   @return A String representation of the object. 
  */
  @Override
  public String toString() {
    return "Artist: " + artist;
  }
  
  /**
   Add this object as a node in the anomaly tree.
  */
  public void addToAnomalyTree(TunesAnalysis analysis) {
    if (anomalyNode == null) {
      DefaultMutableTreeNode thisAnomaly = new DefaultMutableTreeNode(this);
      tunes.getAnomalyRoot().add(thisAnomaly);
      anomalyNode = thisAnomaly;
      if (analysis.getAttributesOption()) {
        TunesCollection.addToTree(thisAnomaly, "Sort Artist:   " + sortArtist);
        TunesCollection.addToTree(thisAnomaly, "Artist Folder: " + artistFolderName);
        TunesCollection.addToTree(thisAnomaly, 
            "Matching Key: " + commonName.toString());
      }
    }
  }
  
  /**
   Get the anomaly node that represents this object in the anomaly tree. 
  
   @return The anomaly node representing this object in the tree. 
  */
  public DefaultMutableTreeNode getAnomalyNode() {
    return anomalyNode;
  }
}
