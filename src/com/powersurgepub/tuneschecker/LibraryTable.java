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

  import java.util.*;
  import javax.swing.table.*;

/**
 A table containing info about iTunes libraries. 

 @author Herb Bowie
 */
public class LibraryTable
    extends AbstractTableModel {
  
  public static final String[] COLUMN_NAMES = {
    "Library", "File/Folder Name", "Tracks"
  };
  
  public static final int[] COLUMN_WIDTH = { 70, 250, 30 };
  
  public static final String[] ROW_NAMES = {
    "Library File 1", "Media Music Folder 1", "Library File "
  };
  
  private ArrayList<TunesLibrary> libraries; 
  
  public LibraryTable() {
    libraries = new ArrayList<TunesLibrary>();
  }
  
  /**
   Add another library to the table. 
  
   @param library The library to be added. 
  
   @return The index at which the library was added, or -1 if the library could
           not be added. 
  */
  public int add(TunesLibrary library) {
    if (libraries.size() < TunesSources.MAX_LIBS) {
      libraries.add(library);
      return (libraries.size() - 1);
    } else {
      return -1;
    }
  }
  
  /**
   Do we have any libraries?
  
   @return True if the table is empty; false otherwise. 
  */
  public boolean isEmpty() {
    return (libraries.isEmpty());
  }
  
  /**
   Return the number of libraries stored in the table. 
  
   @return The number of libraries stored in the table. 
  */
  public int size() {
    return libraries.size();
  }
  
  public TunesLibrary get(int libIndex) {
    return libraries.get(libIndex);
  }
  
  /**
   Get the name of the column, to go at the top of each column. 
  
   @param col The column index. 
  
   @return The name for the given column. 
  */
  @Override
  public String getColumnName(int col) {
    return COLUMN_NAMES[col];
  }
  
  public int getColumnWidth(int col) {
    return COLUMN_WIDTH[col];
  }
  
  /**
   Get the number of rows in the table. Note that each library has two rows,
   one for the library file, and one for the media music folder. 
  
   @return The maximum number of libraries times 2.
  */
  @Override
  public int getRowCount() { 
    return TunesSources.MAX_LIBS * 2; 
  }
  
  /**
   Get the number of columns in the table. 
  
   @return The number of columns in the table. 
  */
  @Override
  public int getColumnCount() { 
    return COLUMN_NAMES.length; 
  }
  
  /**
   Retrieve the class of the objects in this column. 
  
   @param c
  @return 
  */
  @Override
  public Class getColumnClass(int col) {
    return getValueAt(0, col).getClass();
  }
  
  /**
   Get the value to be displayed for the specified row and column. 
  
   @param row The row index of the desired cell. 
   @param col The column index of the desired cell. 
  
   @return The value to be displayed. 
  */
  @Override
  public Object getValueAt(int row, int col) {
    
    int libIndex = row / 2;
    int libOrMedia = row % 2;
    if (col == 0 && row < getRowCount()) {
      if (libOrMedia == 0) {
        return "Library File " + String.valueOf(libIndex + 1);
      } else {
        return "Music Folder " + String.valueOf(libIndex + 1);
      }
    }
    else
    if (row < 0 || libIndex >= libraries.size()) {
      return "";
    } else {
      switch (col) {
        case 1:
          if (libOrMedia == 0) {
            return libraries.get(libIndex).getLibraryFile().toString();
          } else {
            return libraries.get(libIndex).getMusicFolder().toString();
          }
        case 2:
          return libraries.get(libIndex).getCountAsInteger
              (libOrMedia, TunesLibrary.TRACKS);
        default:
          return "";
      } // end column index switch
    } // end if we have a valid row count
  } // end method getValueAt

}
