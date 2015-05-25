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
 A type of anomaly. 

 @author Herb Bowie
 */
public class AnomalyType {
  
  private Boolean selected  = new Boolean(true);;
  private Integer id        = new Integer(0);
  private String  message   = "";
  
  /**
   Constructor. 
  */
  public AnomalyType(int id, String message) {
    this.id = new Integer(id);
    this.message = message;
  }
  
  public Integer getID() {
    return id;
  }
  
  public String getMessage() {
    return message;
  }
  
  public void setSelected(boolean selected) {
    this.selected = new Boolean(selected);
  }
  
  public boolean isSelected() {
    return selected.booleanValue();
  }
  
  public Boolean getSelected() {
    return selected;
  }

}
