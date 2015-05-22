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

/**
  A Sort Key stored as a lowest common denominator string, without punctuation 
  or spaces, without insignificant words, and in lower case. 

  @author Herb Bowie
 */
public class TunesCommonName {
  
  private static final Map <String, String> dict = new HashMap<String, String>();
  
  private StringBuilder name = new StringBuilder();
  private             int     nameType = 0;
  public static final int       GENERIC_NAME = 0;
  public static final int       ARTIST_NAME = 1;
  public static final int       TRACK_NAME = 3;
  
  public static final String    AND                  = "a";
  public static final String    BAND_EXPECTED        = "b";
  public static final String    DIGIT_EXPECTED       = "d";
  public static final String    JUNIOR               = "j";
  public static final String    FIRST_NAME_OF_BAND   = "f";
  public static final String    PERSON_BAND          = "p";
  public static final String    SECOND_NAME_OF_BAND  = "s";
  public static final String    TWO_WORD_LAST_NAME   = "v";
  public static final String    TRIVIAL              = "t";
  
  /**
   Static Initialization Block. 
  */
  static {
    dict.put("a", TRIVIAL);
    dict.put("an", TRIVIAL);
    
    dict.put("the", BAND_EXPECTED);
    
    dict.put("and", AND);
    dict.put("&", AND);
    
    dict.put("variation", DIGIT_EXPECTED);
    
    dict.put("arcade", FIRST_NAME_OF_BAND);
    dict.put("box", FIRST_NAME_OF_BAND);
    dict.put("civil", FIRST_NAME_OF_BAND);
    dict.put("electric", FIRST_NAME_OF_BAND);
    dict.put("fairport", FIRST_NAME_OF_BAND);
    dict.put("grateful", FIRST_NAME_OF_BAND);
    dict.put("howling", FIRST_NAME_OF_BAND);
    dict.put("lovin", FIRST_NAME_OF_BAND);
    dict.put("muddy", FIRST_NAME_OF_BAND);
    dict.put("rolling", FIRST_NAME_OF_BAND);
    dict.put("royal", FIRST_NAME_OF_BAND);
    dict.put("steps", FIRST_NAME_OF_BAND);
    dict.put("trigger", FIRST_NAME_OF_BAND);
    dict.put("trombone", FIRST_NAME_OF_BAND);
    dict.put("unknown", FIRST_NAME_OF_BAND);
    dict.put("various", FIRST_NAME_OF_BAND);
    dict.put("velvet", FIRST_NAME_OF_BAND);
    dict.put("we", FIRST_NAME_OF_BAND);
    
    dict.put("airplane", SECOND_NAME_OF_BAND);
    dict.put("boys", SECOND_NAME_OF_BAND);
    dict.put("brothers", SECOND_NAME_OF_BAND);
    dict.put("chicks", SECOND_NAME_OF_BAND);
    dict.put("creek", SECOND_NAME_OF_BAND);
    dict.put("gentlemen", SECOND_NAME_OF_BAND);
    dict.put("heads", SECOND_NAME_OF_BAND);
    dict.put("sessions", SECOND_NAME_OF_BAND);
    dict.put("sisters", SECOND_NAME_OF_BAND);
    
    dict.put("band", PERSON_BAND);
    dict.put("experience", PERSON_BAND);
    dict.put("group", PERSON_BAND);
    dict.put("orchestra", PERSON_BAND);
    dict.put("quartet", PERSON_BAND);
    dict.put("quintet", PERSON_BAND);
    dict.put("revue", PERSON_BAND);
    
    dict.put("van", TWO_WORD_LAST_NAME);
    
    dict.put("jr", JUNIOR);
    dict.put("iii", JUNIOR);
  }
  
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
    setName(in, GENERIC_NAME);
  }
  
  public void setName(String in, int nameType) {
    this.nameType = nameType;
    name = new StringBuilder();
    
    // Convert everything to lower case
    String work = in.toLowerCase();
    
    // Go through the input name, examining one word at a time
    int i = 0;
    boolean trailingDigitOK = false;
    boolean bandName = false;
    boolean notAPerson = false;
    int wordCount = 0;
    int lastNameStart = -1;
    int juniorStart = -1;
    int wordsInName = 2;
    boolean skipRemaining = false;
    int commaCount = 0;
    while (i < work.length()) {
      
      // Skip past any white space or punctuation symbols
      while (i < work.length()
          && (! isWordCharacter(work.charAt(i)))) {
        if (work.charAt(i) == '#') {
          trailingDigitOK = true;
        }
        i++;
      }
      
      // Now look for end of next word
      int wordStart = i;
      while (i < work.length() 
          && (isWordCharacter(work.charAt(i)))) {
        i++;
      } // end while looking for end of word
      String word = work.substring(wordStart, i);
      if (i < work.length() && work.charAt(i) == ',') {
        commaCount++;
      }
      
      // Now process next word
      String wordType = dict.get(word);
      if (wordType == null) {
        wordType = " ";
      }
      
      if (wordType.equals(TRIVIAL)) {
        // drop common insignificant words
      }
      else
      if (wordType.equals(BAND_EXPECTED)) {
        if (wordCount == 0 && nameType == ARTIST_NAME) {
          bandName = true;
        }
        // drop common insignificant words
      }
      else
      if (wordType.equals(AND)) {
        if (nameType == ARTIST_NAME
            && wordCount == (wordsInName)
            && (! bandName)) {
          skipRemaining = true;
          // System.out.println("Name with And: " + in);
        }
        else
        if (nameType == ARTIST_NAME
            && wordCount == 1) {
          bandName = true;
        }
        // drop common insignificant words
      }
      else
      if (wordType.equals(PERSON_BAND)
          && nameType == ARTIST_NAME
          && wordCount == wordsInName
          && (! notAPerson)) {
        bandName = false;
      }
      else
      if (nameType == TRACK_NAME
          && (! trailingDigitOK)
          && i >= work.length()
          && word.length() == 1
          && Character.isDigit(word.charAt(0))) {
        // Skip a single digit at end of a track name
      } 
      else
      if (skipRemaining) {
        // Skip everything else
      } else {
        if (wordCount == 1) {
          lastNameStart = name.length();
        }
        if (wordType.equals(JUNIOR)
            && wordCount == (wordsInName - 1)) {
          wordsInName++;
          juniorStart = name.length();
        } 
        name.append(word);
        wordCount++;
        if (nameType == ARTIST_NAME) {
          if (wordCount == 1
              && wordType.equals(FIRST_NAME_OF_BAND)) {
            bandName = true;
            notAPerson = true;
          }
          else
          if (wordCount == 2
              && wordType.equals(SECOND_NAME_OF_BAND)) {
            bandName = true;
            notAPerson = true;
          }
          else
          if (wordCount == 2
              && wordType.equals(TWO_WORD_LAST_NAME)) {
            wordsInName++;
          }
          else
          if (wordCount == 2
              && name.length() == 2) {
            wordsInName++;
            lastNameStart = name.length();
          }
        }
        if (wordType.equals(DIGIT_EXPECTED)
            && nameType == TRACK_NAME) {
          trailingDigitOK = true;
        } else {
          trailingDigitOK = false;
        }
      }
      
      // Skip past character that ended the word
      i++;
    } // end while scanning input string
    
    // If this appears to be a person's name, then put last name first
    if (nameType == ARTIST_NAME 
        && wordCount == (wordsInName) 
        && lastNameStart > 0
        && (! bandName)
        && commaCount == 0) {
      int lastNameEnd = name.length();
      if (juniorStart > 0) {
        lastNameEnd = juniorStart;
      }
      String lastName = name.substring(lastNameStart, lastNameEnd);
      name.delete(lastNameStart, lastNameEnd);
      name.insert(0, lastName);
    }
    
  } // end method setName
  
  public static boolean isWordCharacter(char c) {
    return (c == '&'
        || Character.isLetter(c)
        || Character.isDigit(c));
  }
  
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
