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
 An object (artist, album, track, etc.) within an iTunes library. 

 @author Herb Bowie
 */
public interface TunesObject {
  
  /**
   Return a string to be used to identify the node in the anomaly tree. 
  
   @return A String representation of the object. 
  */
  @Override
  public String toString();
  
  /**
   Add this object as a node in the anomaly tree.
  */
  public void addToAnomalyTree(TunesAnalysis analysis);
  
  /**
   Get the anomaly node that represents this object in the anomaly tree. 
  
   @return The anomaly node representing this object in the tree. 
  */
  public DefaultMutableTreeNode getAnomalyNode();
  
}



