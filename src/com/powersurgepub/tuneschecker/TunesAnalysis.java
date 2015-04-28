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

/**
 An object used to perform analysis of a tunes collection. 

 @author Herb Bowie
 */
public class TunesAnalysis {
  
  private boolean attributesOption = false;
  private int     minTracks = 2;
  
  public TunesAnalysis() {
    
  }
  
  public void setAttributesOption(boolean attributesOption) {
    this.attributesOption = attributesOption;
  }
  
  public boolean getAttributesOption() {
    return attributesOption;
  }
  
  public void setMinTracks(int minTracks) {
    this.minTracks = minTracks;
  }
  
  public int getMinTracks() {
    return minTracks;
  }

}
