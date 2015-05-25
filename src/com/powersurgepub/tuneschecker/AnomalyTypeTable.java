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
 A table containing info about types of anomalies. 

 @author Herb Bowie
 */
public class AnomalyTypeTable 
    extends AbstractTableModel {
  
  private static AnomalyTypeTable table = null;
  
  public static final String[] ANOMALY_TYPE = {
    "On disk, but not in library",
    "In library, but not on disk",
    "Missing track # "
  };
  public static final int ON_DISK_NOT_IN_LIB = 0;
  public static final int IN_LIB_NOT_ON_DISK = 1;
  public static final int MISSING_TRACK_NUMBER = 2;
  
  public static final String[] COLUMN_NAMES = {
    "Sel?", "ID", "Message"
  };
  
  public static final int[] COLUMN_WIDTH = { 20, 20, 120 };
  
  private ArrayList<AnomalyType> types;
  
  private AnomalyTypeTable() {
    types = new ArrayList<AnomalyType>();
    add(ON_DISK_NOT_IN_LIB, ANOMALY_TYPE[ON_DISK_NOT_IN_LIB]);
    add(IN_LIB_NOT_ON_DISK, ANOMALY_TYPE[IN_LIB_NOT_ON_DISK]);
    add(MISSING_TRACK_NUMBER, ANOMALY_TYPE[MISSING_TRACK_NUMBER]);
  }
  
  /**
   Get shared singleton instance of this class.
  
   @return The single shared instance of this class. 
  */
  public static AnomalyTypeTable getShared() {
    if (table == null) {
      table = new AnomalyTypeTable();
    }
    return table;
  }
  
  public int add(int id, String message) {
    AnomalyType newType = new AnomalyType(id, message);
    return add(newType);
  }
  
  /**
   Add another type to the table. 
  
   @param type The anomaly type to be added. 
  
   @return The index at which the type was added, or -1 if the type could
           not be added. 
  */
  public int add(AnomalyType newType) {
    types.add(newType);
    return (types.size() - 1);
  }
  
  /**
   Do we have any types?
  
   @return True if the table is empty; false otherwise. 
  */
  public boolean isEmpty() {
    return (types.isEmpty());
  }
  
  /**
   Return the number of types stored in the table. 
  
   @return The number of types stored in the table. 
  */
  public int size() {
    return types.size();
  }
  
  public AnomalyType get(int typeIndex) {
    return types.get(typeIndex);
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
   Get the number of rows in the table.  
  
   @return The number of rows in the table
  */
  @Override
  public int getRowCount() { 
    return types.size(); 
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
   Should the cell be editable?
  
   @param rowIndex
   @param columnIndex
  
   @return 
  */
  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 0;
  }
  
  /**
   Retrieve the class of the objects in this column. 
  
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
    
    if (row < 0 || row >= types.size()) {
      return null;
    } else {
      AnomalyType type = types.get(row);
      switch (col) {
        case 0:
          return type.isSelected();
        case 1:
          return type.getID();
        case 2:
          return type.getMessage();
        default:
          return null;
      }
    }
  } // end method getValueAt
  
  @Override
  public void setValueAt(Object value, int row, int col) {
    if (row < 0 || row >= types.size()) {
      // do nothing
    } else {
      AnomalyType type = types.get(row);
      switch (col) {
        case 0:
          if (value instanceof Boolean) {
            Boolean selected = (Boolean)value;
            type.setSelected(selected);
          }
          break;
        case 1:
          break;
        case 2:
          break;
        default:
          break;
      }
    }
  }

}
