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

  import java.io.*;
  import java.net.*;
  import javax.swing.tree.*;

/**
 
 @author Herb Bowie
 */
public class TunesFile 
  implements Comparable<TunesFile>,
             TunesObject {
  
  private     TunesTrack          tunesTrack = null;
  
  private     TunesSources        sources = new TunesSources();
  
  private     File                location = null;
  private     String              kind = "";
  private     long                size = 0;
  private     int                 bitRate = 0;
  private     int                 sampleRate = 0;
  
  private     boolean             fileExists = false;
  
  private DefaultMutableTreeNode  anomalyNode = null;
  
  public TunesFile() {
    
  }
  
  public void setTunesTrack(TunesTrack tunesTrack) {
    this.tunesTrack = tunesTrack;
  }
  
  public TunesTrack getTunesTrack() {
    return tunesTrack;
  }
  
  public TunesSources getSources() {
    return sources;
  }
  
  public boolean hasKey() {
    return (location != null && location.length() > 0);
  }
  
  /**
   Does this file have the same sort key as another?
  
   @param file2 The second file.
  
   @return True if equal, false otherwise. 
  */
  public boolean equals(TunesFile file2) {
    return (compareTo(file2) == 0);
  }
  
  /**
   Compare this file to another instance of TunesFile to determine
   their sort order. 
  
   @param file2 The second file, to which this one will be compared. 
  
   @return a negative integer if this file is less than the second,
           a positive integer if this file is greater than the second, or
           zero if the two TunesFile instances represent the same file. 
  */
  @Override
  public int compareTo(TunesFile file2) {
    int result =  0;
    result = location.compareTo(file2.getLocation());
    return result;
  }
  
  public void merge(TunesFile file2) {
    if (file2.getKind().length() > kind.length()) {
      setKind(file2.getKind());
    }
    if (file2.getSize() > 0) {
      setSize(file2.getSize());
    }
    if (file2.getBitRate() > 0) {
      setBitRate(file2.getBitRate());
    }
    if (file2.getSampleRate() > 0) {
      setSize(file2.getSampleRate());
    }
    sources.merge(file2.getSources());
  }
  
  public void setKind(String kind) {
    this.kind = kind;
  }
  
  public String getKind() {
    return kind;
  }
  
  public void setLocation(String inString) {
    File workLocation = null;
    try {
      URL locationURL = new URL(inString);
      URI locationURI = locationURL.toURI();
      workLocation = new File(locationURI.getSchemeSpecificPart());
    } catch (MalformedURLException e) {
      workLocation = new File(inString);
    }
    catch (URISyntaxException e) {
      workLocation = new File(inString);
    }
    setLocation(workLocation);
  }
  
  public void setLocation(File location) {
    this.location = location;
  }
  
  public boolean hasLocation() {
    return (location != null && location.length() > 0);
  }
  
  public File getLocation() {
    return location;
  }
  
  public void setSize(long size) {
    this.size = size;
  }
  
  public long getSize() {
    return size;
  }
  
  public void setBitRate(int bitRate) {
    this.bitRate = bitRate;
  }
  
  public int getBitRate() {
    return bitRate;
  }
  
  public void setSampleRate(int sampleRate) {
    this.sampleRate = sampleRate;
  }
  
  public int getSampleRate() {
    return sampleRate;
  }
  
  public void setFileExists() {
    setFileExists(true);
  }
  
  public void setFileExists(boolean fileExists) {
    this.fileExists = fileExists;
  }
  
  public boolean getFileExists() {
    return fileExists;
  }
  
  public boolean fileExists() {
    return fileExists;
  }
  
  public void display() {
    System.out.println("        " + location);
  }
  
  /**
   Return a string to be used to identify the node in the anomaly tree. 
  
   @return A String representation of the object. 
  */
  @Override
  public String toString() {
    return "File: " + location.toString();
  }
  
  /**
   Add this object as a node in the anomaly tree.
  */
  @Override
  public void addToAnomalyTree(TunesAnalysis analysis) {
    if (anomalyNode == null) {
      DefaultMutableTreeNode thisAnomaly = new DefaultMutableTreeNode(this);
      if (tunesTrack.getAnomalyNode() == null) {
        tunesTrack.addToAnomalyTree(analysis);
      }
      tunesTrack.getAnomalyNode().add(thisAnomaly);
      anomalyNode = thisAnomaly;
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
