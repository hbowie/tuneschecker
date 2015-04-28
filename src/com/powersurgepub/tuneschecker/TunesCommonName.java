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
  A Sort Key stored as a lowest common denominator string, without punctuation 
  or spaces, without insignificant words, and in lower case. 

  @author Herb Bowie
 */
public class TunesCommonName {
  
  private StringBuilder name = new StringBuilder();
  
  /**
   A constructor with no arguments. 
  */
  public TunesCommonName() {
    
  }
  
  /**
   A constructor with the string to be represented. 
  
   @param name The string to be represented in its 
               lowest common denominator form. 
  */
  public TunesCommonName (String in) {
    setName(in);
  }
  
  public void setName(String in) {
    setName(in, false);
  }
  
  public void setName(String in, boolean trackName) {
    name = new StringBuilder();
    String work = in.toLowerCase();
    int i = 0;
    boolean trailingDigitOK = false;
    while (i < work.length()) {
      
      // Skip past any white space or punctuation symbols
      while (i < work.length()
          && (! Character.isLetter(work.charAt(i))
          && (! Character.isDigit(work.charAt(i))))) {
        if (work.charAt(i) == '#') {
          trailingDigitOK = true;
        }
        i++;
      }
      
      // Now look for end of next word
      int wordStart = i;
      while (i < work.length() 
          && (Character.isLetter(work.charAt(i))
          || Character.isDigit(work.charAt(i)))) {
        i++;
      } // end while looking for end of word
      String word = work.substring(wordStart, i);
      
      // Now process next word
      if (word.equals("a")
          || word.equals("and")
          || word.equals("an")
          || word.equals("the")) {
        // drop common insignificant words
      }
      else
      if (trackName
          && (! trailingDigitOK)
          && i >= work.length()
          && word.length() == 1
          && Character.isDigit(word.charAt(0))) {
        // Skip a single digit at end of a track name
      } else {
        name.append(word);
        if (word.equals("variation")) {
          trailingDigitOK = true;
        } else {
          trailingDigitOK = false;
        }
      }
      
      // Skip past character that ended the word
      i++;
    } // end while scanning input string
    
  } // end method setName
  
  public int length() {
    return name.length();
  }
  
  public int compareTo(TunesCommonName name2) {
    return (getName().compareTo(name2.getName()));
  }
  
  public String getName() {
    return name.toString();
  }
  
  public String toString() {
    return name.toString();
  }

}
