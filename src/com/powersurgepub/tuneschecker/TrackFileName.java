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
 A string containing a file trackName for a track, with get methods to 
 pull out various pieces of the track name. 

 @author Herb Bowie
 */
public class TrackFileName {
  
  private     String              artistName = "";
  private     String              albumName = "";
  private     int                 trackNumber = 0;
  private     String              trackName = "";
  private     String              sortName = "";
  private     String              fileName = "";
  private     String              extension = "";
  
  /**
   Constructor with a track file trackName. 
  
   @param trackFileName The string containing the track file trackName. 
  
  */
  public TrackFileName(String inString) {
    
    // If it's a URL, convert it to a file path
    String trackFileName = inString;
    if (inString.startsWith("file:")) {
      try {
        URL fileURL = new URL(inString);
        URI fileURI = fileURL.toURI();
        File workFile = new File(fileURI.getSchemeSpecificPart());
        trackFileName = workFile.toString();
      } catch (MalformedURLException e) {
        // Leave it alone
      }
      catch (URISyntaxException e) {
        // Leave it alone
      }
    }
    
    // Find the start of the actual name of the file (following any path info)
    char slash = '/';
    int slashIndex = trackFileName.lastIndexOf("/");
    if (slashIndex < 0) {
      slashIndex = trackFileName.lastIndexOf("\\");
    }
    if (slashIndex >= 0) {
      slash = trackFileName.charAt(slashIndex);
    }
    int start = 0;
    if (slashIndex >= 0) {
      start = slashIndex + 1;
    }
    
    int periodIndex = trackFileName.length() - 1;
    int trackNameEnd = trackFileName.length();
    int trackNameStart = start;
    int dashIndex = start;
    int trackNumberStart = start;
    int trackNumberEnd = -1;

    // Find start of file extension
    while (periodIndex > 0
        && trackFileName.charAt(periodIndex) != '.') {
      periodIndex--;
    }
    if (periodIndex > 0) {
      trackNameEnd = periodIndex;
    } else {
      periodIndex = trackFileName.length();
    }
    
    // Capture the file name
    fileName = trackFileName.substring(start, trackNameEnd);

    // Find end of disc number, if it's there
    while (dashIndex < trackFileName.length()
        && Character.isDigit(trackFileName.charAt(dashIndex))) {
      dashIndex++;
    }
    if (dashIndex < trackFileName.length()
        && dashIndex < 4
        && trackFileName.charAt(dashIndex) == '-') {
      trackNameStart = dashIndex + 1;
      trackNumberStart = dashIndex + 1;
    } else {
      dashIndex = -1;
    }

    // Find end of track number, if it's there
    trackNumberEnd = trackNumberStart;
    while (trackNumberEnd < trackFileName.length()
        && Character.isDigit(trackFileName.charAt(trackNumberEnd))) {
      trackNumberEnd++;
    }

    // Find start of track number, if it's there
    if (trackNumberEnd < trackFileName.length()
        && trackFileName.charAt(trackNumberEnd) == ' '
        && trackNumberEnd == (trackNumberStart + 2)) {
      trackNameStart = trackNumberEnd + 1;
    } else {
      trackNumberStart = -1;
      trackNumberEnd = -1;
    }

    if (trackNumberStart >= 0
        && trackNumberEnd > 0) {
      setTrackNumber
          (trackFileName.substring(trackNumberStart, trackNumberEnd));
    }

    if (trackNameStart >= 0
        && trackNameEnd > trackNameStart) {
      trackName = trackFileName.substring(trackNameStart, trackNameEnd);
    }
    
    sortName = TunesCollection.makeSortName(trackName);
    
    if (periodIndex > 0 && periodIndex < trackFileName.length()) {
      extension = trackFileName.substring(periodIndex + 1);
    }
    
    if (slashIndex > 0) {
      int albumSlashIndex = trackFileName.lastIndexOf(slash, slashIndex - 1);
      if (albumSlashIndex > 0) {
        albumName = trackFileName.substring(albumSlashIndex + 1, slashIndex);
        int artistSlashIndex = trackFileName.lastIndexOf(slash, albumSlashIndex - 1);
        if (artistSlashIndex >= 0) {
          artistName = trackFileName.substring(artistSlashIndex + 1, albumSlashIndex);
        }
      }
    }
  }
  
  private void setTrackNumber(String trackNumber) {
    try {
      this.trackNumber = Integer.parseInt(trackNumber);
    } catch (NumberFormatException e) {
      System.out.println("Trouble converting track number to an integer");
    }
  }
  
  public boolean hasFileName() {
    return (fileName != null && fileName.length() > 0);
  }
  
  /**
   Get the file name, without extension and without folder/path info. 
  
   @return The name of the file. 
  */
  public String getFileName() {
    return fileName;
  }
  
  public boolean hasTrackNumber() {
    return (trackNumber > 0);
  }
  
  public int getTrackNumber() {
    return trackNumber;
  }
  
  public boolean hasTrackName() {
    return (trackName != null && trackName.length() > 0);
  }
  
  public String getTrackName() {
    return trackName;
  }
  
  public boolean hasSortName() {
    return (sortName != null && sortName.length() > 0);
  }
  
  public String getSortName() {
    return sortName;
  }
  
  public boolean hasExtension() {
    return (extension != null && extension.length() > 0);
  }
  
  public String getExtension() {
    return extension;
  }
  
  public boolean hasAlbumName() {
    return (albumName != null && albumName.length() > 0);
  }
  
  public String getAlbumName() {
    return albumName;
  }
  
  public boolean hasArtistName() {
    return (artistName != null && artistName.length() > 0);
  }
  
  public String getArtistName() {
    return artistName;
  }

}
