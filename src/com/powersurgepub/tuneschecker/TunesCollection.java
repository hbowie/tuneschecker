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
  import com.powersurgepub.psutils.*;
  import java.util.*;
  import javax.swing.*;
  import javax.swing.table.*;
  import javax.swing.tree.*;

/**
 
 @author Herb Bowie
 */
public class TunesCollection {
  
  private TunesSources           sources   = new TunesSources();
  
  private LibraryTable           libraries = new LibraryTable();
  
  private ArrayList<TunesArtist> artists = new ArrayList<TunesArtist>();
  private TunesArtist            artist = new TunesArtist();
  private int                    artistIndex = -1;
  
  private TunesArtist            compilations = new TunesArtist();
  
  private DefaultMutableTreeNode anomalyRoot 
      = new DefaultMutableTreeNode("Anomalies");
  private DefaultTreeModel anomalies;
  
  public TunesCollection() {
    compilations.setArtist("Compilations");
    libraries = new LibraryTable();
    anomalyRoot = new DefaultMutableTreeNode("Anomalies");
    anomalies = new DefaultTreeModel(anomalyRoot);
  }
  
  public TreeModel getAnomalies() {
    return anomalies;
  }
  
  public TunesArtist getCompilations() {
    return compilations;
  }
  
  public TunesSources getSources() {
    return sources;
  }
  
  public LibraryTable getLibraries() {
    return libraries;
  }
  
  public ArrayList<TunesArtist> getArtists() {
    return artists;
  }
  
  /**
   Add a new library to the collection. 
  
   @param library The library to be added. 
  */
  public int addLibrary(TunesLibrary library) {
    return libraries.add(library);
  }
  
  /**
   Return the index of the last library added. 
  
   @return The index position of the last library added. 
  */
  public int getLibIndex() {
    return (libraries.size() - 1);
  }
  
  /**
   Get one of the libraries. 
  
   @param libIndex The index to the desired library.
  
   @return The desired library, or null, if the index is out of range. 
  */
  public TunesLibrary getLibrary(int libIndex) {
    if (libIndex >= 0 && libIndex < libraries.size()) {
      return libraries.get(libIndex);
    } else {
      return null;
    }
  }
  
  /**
   Return the number of libraries in the collection. 
  
   @return The number of libraries in the collection. 
  */
  public int getNumberOfLibraries() {
    return libraries.size();
  }
  
  /**
   Return the library most recently added to the collection. 
  
   @return The library most recently added, if we have any libraries,
           or null, if we have no libraries. 
  */
  public TunesLibrary getLastLibrary() {
    if (libraries.isEmpty()) {
      return null;
    } else {
      return libraries.get(libraries.size() - 1);
    }
  }
  
  /** 
   Store an artist by adding to the existing list, or merging data with 
   an existing entry in the list. 
  
   @param artistToStore The artist with data to be stored. 
  
   @return The artist containing the stored data. 
  */
  public TunesArtist storeArtist(TunesArtist artistToStore) {
    
    if (artists.isEmpty()) {
      artists.add(artistToStore);
      artistIndex = 0;
      artist = artistToStore;
    }
    else
    if (artistIndex >= 0 && artistIndex < artists.size() && artist != null
        && artist.equals(artistToStore)) {
      artist.merge(artistToStore);
    }
    else {
      artistIndex = 0;
      int comparison = 1;
      while (artistIndex < artists.size() && comparison > 0) {
        comparison = artistToStore.compareTo(artists.get(artistIndex));
        if (comparison > 0) {
          artistIndex++;
        } // end if still looking for the right insertion point
      } // end while looking for the right insertion point
      if (artistIndex == artists.size() || comparison < 0) {
        artists.add(artistIndex, artistToStore);
        artist = artists.get(artistIndex);
      } 
      else {
        artists.get(artistIndex).merge(artistToStore);
        artist = artists.get(artistIndex);
      }
    } // end if we had to go looking for an insertion point
    
    artist.setCollection(this);
    
    return artist;
  }
  
  public void display() {
    System.out.println("TunesCollection.display");
    for (artistIndex = 0; artistIndex < artists.size(); artistIndex++) {
      artists.get(artistIndex).display();
    }
  }
  
  public void analyze(TunesAnalysis analysis) {

    for (TunesArtist nextArtist: artists) {
      nextArtist.analyze(this, analysis);
    }
    anomalies.nodeStructureChanged(anomalyRoot);
    
  }
  
  public void exportToOPML(MarkupWriter writer) {
    for (TunesArtist nextArtist: artists) {
      nextArtist.exportToOPML(writer);
    }
  }
  
  public DefaultMutableTreeNode getAnomalyRoot() {
    return anomalyRoot;
  }

  /**
   Create a new anomaly and add it to the tree. 
  
   @param libIndex    Identify the library with which the anomaly is associated. 
   @param object      The tunes object with the anomaly.  
   @param anomalyType Identifier for the type of anomaly. 
  
   @return The new node that was created. 
  */
  public DefaultMutableTreeNode addAnomaly(
      int libIndex, 
      TunesObject object, 
      int anomalyType,
      TunesAnalysis analysis) {
    
    // If the object is not already in the tree, then add it
    if (object.getAnomalyNode() == null) {
      object.addToAnomalyTree(analysis);
    }
    
    // Now let's create an anomaly object
    TunesAnomaly anomaly = new TunesAnomaly(libIndex, object, anomalyType);
    
    return addToTree(object.getAnomalyNode(), anomaly);

  }
  
  public DefaultMutableTreeNode addAnomaly(
      int libIndex, 
      TunesObject object, 
      int anomalyType,
      int trackNumber,
      TunesAnalysis analysis) {
    
    // If the object is not already in the tree, then add it
    if (object.getAnomalyNode() == null) {
      object.addToAnomalyTree(analysis);
    }
    
    // Now let's create an anomaly object
    TunesAnomaly anomaly = new TunesAnomaly
        (libIndex, object, anomalyType, trackNumber);
    
    return addToTree(object.getAnomalyNode(), anomaly);

  }
  
  /**
   Add a new node as a child to an existing parent node. 
  
   @param parent  The parent node. 
   @param payload The object to be carried in the new node. 
  
   @return The new node that was created. 
  */
  public static DefaultMutableTreeNode addToTree(DefaultMutableTreeNode parent, Object payload) {
    
    DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(payload);
    parent.add(newNode);
    return newNode;
  }
  
  /**
   Drop any leading articles (a, an, the) from the name. 
  
   @param name The full name. 
  
   @return A name that can be used for meaningful sorting. 
  */
  public static String makeSortName(String name) {
    int sortNameStart = findSortNameStart(name);
    return name.substring(sortNameStart);
  }
  
  public static int findSortNameStart(String name) {
    int sortNameStart = 0;
    while (sortNameStart < name.length()
        && name.charAt(sortNameStart) != ' ') {
      sortNameStart++;
    }
    String article = "";
    if (sortNameStart < name.length()) {
      article = name.substring(0, sortNameStart);
    }
    if ((sortNameStart < name.length())
      && ((article.equalsIgnoreCase("The")
          || article.equalsIgnoreCase("A")
          || article.equalsIgnoreCase("An")))) {
      sortNameStart++;
    } else {
      sortNameStart = 0;
    }
    return sortNameStart;
  }

}
