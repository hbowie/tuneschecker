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
 A Sort Key stored as a string, but with some special comparison logic to 
 take file names into account. 

 @author Herb Bowie
 */
public class TunesSortKey {
  
  private String sortKey = "";
  
  public TunesSortKey() {
    
  }
  
  public TunesSortKey(String sortKey) {
    this.sortKey = TunesCollection.makeSortName(sortKey);
  }
  
  public boolean equals(TunesSortKey sortKey2) {
    return (compareTo(sortKey2) == 0);
  }
  
  /**
   Compare the two strings, one character at a time. An underscore will be
   considered a match for any punctuation. An ampersand will be considered 
   a match for the word 'and'. Leading articles (a, an, the) will be ignored. 
  
   @param sortKey2 The second sort key. 
  
   @return Zero if the keys match, -1 if this one is lower, + 1 if this key
           is higher. 
  */
  public int compareTo(TunesSortKey sortKey2) {
    int result =  0;
    int i = TunesCollection.findSortNameStart(sortKey);
    int j = TunesCollection.findSortNameStart(sortKey2.getSortKey());
    char c1;
    char c2;
    
    while (i < sortKey.length() 
        && j < sortKey2.getSortKey().length()
        && result == 0) {
      c1 = Character.toLowerCase(sortKey.charAt(i));
      c2 = Character.toLowerCase(sortKey2.getSortKey().charAt(j));
     
      if (c1 == '_' && (! Character.isWhitespace(c2)) && (! Character.isLetterOrDigit(c2))) {
        // Consider this a match
        i++;
        j++;
      }
      else
      if (c2 == '_' && (! Character.isWhitespace(c1)) && (! Character.isLetterOrDigit(c1))) {
        // Consider this a match
        i++;
        j++;
      } 
      else
      if (c1 == '&'
          && subStr(sortKey2.getSortKey(), j, j+4).equalsIgnoreCase("and ")) {
        // Consider this a match
        i++;
        j = j + 3;
      }
      else
      if (c2 == '&'
          && subStr(sortKey, i, i+4).equalsIgnoreCase("and ")) {
        // Consider this a match
        j++;
        i = j + 3;
      }
      else
      if (c1 < c2) {
        result = -1;
      }
      else
      if (c1 > c2) {
        result = 1;
      }
      else {
        i++;
        j++;
      }
    }
    if (result == 0) {
      if (sortKey.length() > sortKey2.getSortKey().length()) {
        result = 1;
      }
      else
      if (sortKey.length() < sortKey2.getSortKey().length()) {
        result = -1;
      }
    }
    return result;
  }
  
  /**
   Return as much of the requested substring as is available. If either the 
   beginning or ending index is out of bounds, then returns as much of the 
   requested substring as is available, instead of throwing an exception. 
  
   @param str   The full string. 
   @param begin The beginning index. 
   @param end   The ending index. 
  
   @return As much of the requested substring is available from the full string. 
  */
  private String subStr(String str, int begin, int end) {
    if (begin < 0 || begin >= str.length()) {
      return "";
    } else {
      int e = end;
      if (e > str.length()) {
        e = str.length();
      }
      return str.substring(begin, e);
    }
  }
  
  public int length() {
    return sortKey.length();
  }
  
  public void setSortKey(String sortKey) {
    this.sortKey = TunesCollection.makeSortName(sortKey);
  }
  
  public String getSortKey() {
    return sortKey;
  }
  
  public String getFileName() {
    StringBuilder fileName = new StringBuilder(sortKey);
    for (int i = 0; i < fileName.length(); i++) {
      char c = fileName.charAt(i);
      if (Character.isWhitespace(c)) {
        // Leave it as-is
      }
      else
      if (Character.isLetterOrDigit(c)) {
        // Leave it alone
      }
      else
      if (c == '_' || c == '&' || c == '-') {
        // Let it be
      }
      else {
        fileName.replace(i, i+1, "_");
      }
    }
    return fileName.toString();
  }
  
  public String toString() {
    return sortKey;
  }

}
