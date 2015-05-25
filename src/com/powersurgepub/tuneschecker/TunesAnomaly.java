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

  import javax.swing.tree.*;

/**
 
 @author Herb Bowie
 */
public class TunesAnomaly
    implements TunesObject {
  
  private int         libIndex = -1;
  private TunesObject object = null;
  private AnomalyType anomalyType = null;
  private int         trackNumber = -1;
  
  private DefaultMutableTreeNode  anomalyNode = null;
  
  public TunesAnomaly(
      int libIndex, 
      TunesObject object, 
      AnomalyType anomalyType) {
    
    this.libIndex = libIndex;
    this.object = object;
    this.anomalyType = anomalyType;
  }
  
  public TunesAnomaly(
      int libIndex, 
      TunesObject object, 
      AnomalyType anomalyType,
      int trackNumber) {
    
    this.libIndex = libIndex;
    this.object = object;
    this.anomalyType = anomalyType;
    this.trackNumber = trackNumber;
  }
  
  /**
   Return a string to be used to identify the node in the anomaly tree. 
  
   @return A String representation of the object. 
  */
  @Override
  public String toString() {
    if (trackNumber < 1) {
      return "Anomaly: " + anomalyType.getMessage();
    } else {
      return "Anomaly: " + anomalyType.getMessage() + String.valueOf(trackNumber);
    }
  }
  
  /**
   Add this object as a node in the anomaly tree.
  */
  @Override
  public void addToAnomalyTree(TunesAnalysis analysis) {
    // Method provided for consistency with interface, but anomalies are 
    // actually added elsewhere. 
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
