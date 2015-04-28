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

/**
 A single iTunes Library, consisting of a library xml file and an 
 associated music folder. 

 @author Herb Bowie
 */
public class TunesLibrary {
  
  public static final String MUSIC = "Music";
  public static final String LOCALHOST = "/localhost";
  public static final int LIB     = 0;
  public static final int MEDIA   = 1;
  
  public static final int TRACKS  = 0;
  
  private File libraryFile = null;
  private File musicFolder = null;
  
  private int[][] count = { {0}, {0} };
  
  public TunesLibrary() {
    
  }
  
  public void setLibraryFile(File libraryFile) {
    this.libraryFile = libraryFile;
  }
  
  public File getLibraryFile() {
    return libraryFile;
  }
  
  public void setMusicFolder(String inString) {

    File workFolder;
    try {
      URL folderURL = new URL(inString);
      URI folderURI = folderURL.toURI();
      workFolder = new File(folderURI.getSchemeSpecificPart());
      StringBuilder work = new StringBuilder(workFolder.toString());
      if (work.substring(0, LOCALHOST.length()).equalsIgnoreCase(LOCALHOST)) {
        work.delete(0, LOCALHOST.length());
        workFolder = new File(work.toString());
      }
    } catch (MalformedURLException e) {
      workFolder = new File(inString);
      System.out.println("  Malformed URL: " + inString);
    }
    catch (URISyntaxException e) {
      workFolder = new File(inString);
      System.out.println("  URI Syntax error: " + inString);
    }
    setMusicFolder(workFolder);
  }
  
  public void setMusicFolder(File musicFolder) {
    if (musicFolder.getName().equals(MUSIC)) {
      this.musicFolder = musicFolder;
    } else {
      this.musicFolder = new File(musicFolder, MUSIC);
    }
  }
  
  public File getMusicFolder() {
    return musicFolder;
  }
  
  public void setCount(int libOrMedia, int objectType, int count) {
    this.count[libOrMedia][objectType] = count;
  }
  
  public int getCount(int libOrMedia, int objectType) {
    return count[libOrMedia][objectType];
  }
  
  public String getCountAsString(int libOrMedia, int objectType) {
    return String.valueOf(count[libOrMedia][objectType]);
  }
  
  public Integer getCountAsInteger(int libOrMedia, int objectType) {
    return new Integer(count[libOrMedia][objectType]);
  }

}
