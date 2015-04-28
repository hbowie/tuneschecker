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
 Keep track of the sources of our information. Note that each iTunes library
 has an associated Music Folder, and that info stored in TunesChecker can 
 come from the library file, from the music folder, or from both sources. 

 @author Herb Bowie
 */
public class TunesSources {
  
  public static final int        MAX_LIBS = 2;
  
  private boolean[] sources;
  
  public TunesSources() {
    sources = new boolean[MAX_LIBS * 2];
    for (int i = 0; i < MAX_LIBS; i = i + 2) {
      sources[i] = false;
      sources[i + 1] = false;
    }
  }
  
  public void merge(TunesSources sources2) {
    for (int i = 0; i < MAX_LIBS; i++) {
      if (sources2.isFromLibrary(i)) {
        setFromLibrary(i);
      }
      if (sources2.isFromFolder(i)) {
        setFromFolder(i);
      }
    }
  }
  
  public boolean isFromBoth(int libIndex) {
    return (isFromLibrary(libIndex) && isFromFolder(libIndex));
  }
  
  public void setFromLibrary(int libIndex) {
    setFromLibrary(true, libIndex);
  }
  
  public void setFromLibrary(boolean fromLibrary, int libIndex) {
    if (libIndex >= 0 && libIndex < MAX_LIBS) {
      sources[libIndex * 2] = fromLibrary;
    }
  }
  
  public boolean isFromLibrary(int libIndex) {
    if (libIndex >= 0 && libIndex < MAX_LIBS) {
      return sources[libIndex * 2];
    } else {
      return false;
    }
  }
  
  public void setFromFolder(int libIndex) {
    setFromFolder(true, libIndex);
  }
  
  public void setFromFolder(boolean fromFolder, int libIndex) {
    if (libIndex >= 0 && libIndex < MAX_LIBS) {
      sources[(libIndex * 2) + 1] = fromFolder;
    }
  }
  
  public boolean isFromFolder(int libIndex) {
    if (libIndex >= 0 && libIndex < MAX_LIBS) {
      return sources[(libIndex * 2) + 1];
    } else {
      return false;
    }
  }

}
