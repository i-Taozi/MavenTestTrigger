/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.base;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings("LSC_LITERAL_STRING_COMPARISON")
public class CharSequencesTest {


  @Test
  public void testDistance() {
    Assert.assertEquals(3, CharSequences.distance("abc", "abcdef"));
    Assert.assertEquals(3, CharSequences.distance("def", "abcdef"));
    Assert.assertEquals(1, CharSequences.distance("abc", "bc"));
    Assert.assertEquals(3, CharSequences.distance("abc", "def"));
    Assert.assertEquals(1, CharSequences.distance("zoltran", "zoltan"));
  }

  @Test
  public void testDistance2() {
    Assert.assertEquals(3, CharSequences.distance("horse", "ros"));
  }

  @Test
  public void testDistance3() {
    Assert.assertEquals(27, CharSequences.distance("pneumonoultramicroscopicsilicovolcanoconiosis",
            "ultramicroscopically"));
  }


  @Test
  public void testLineNumbering() {
    CharSequence lineNumbered = CharSequences.toLineNumbered(0, "a\nbla\nc");
    Assert.assertEquals("/* 0 */ a\n/* 1 */ bla\n/* 2 */ c", lineNumbered.toString());
  }


  @Test
  public void testID() {
    Assert.assertFalse(JavaUtils.isJavaIdentifier(""));
    Assert.assertFalse(JavaUtils.isJavaIdentifier(null));
    Assert.assertFalse(JavaUtils.isJavaIdentifier("12A"));
    Assert.assertTrue(JavaUtils.isJavaIdentifier("_a"));
    Assert.assertTrue(JavaUtils.isJavaIdentifier("a"));
    Assert.assertTrue(JavaUtils.isJavaIdentifier("a123FGH"));
  }


  @Test
  public void testCompare() {
    Assert.assertEquals(0, CharSequences.compare("blabla", 6, "blabla/cucu", 6));
  }

  @Test
  public void testCompare2() {
    Assert.assertEquals(0, CharSequences.compare("cacablabla", 4, 6, "ablabla/cucu", 1, 6));
  }

  @Test
  public void testCompare3() {
    Assert.assertEquals("blabla123".compareTo("blabla"),
            CharSequences.compare("cacablabla123", 4, 9, "ablabla/cucu", 1, 6));
  }

  @Test
  public void testCompare4() {
    Assert.assertEquals("bla".compareTo("lab"),
            CharSequences.compare("cacablabla123", 4, 3, "ablabla/cucu", 2, 3));
  }

  @Test
  public void testCompare5() {
    Assert.assertEquals("bla".compareTo("labl"),
            CharSequences.compare("cacablabla123", 4, 3, "ablabla/cucu", 2, 4));
  }

  @Test
  public void testCotains() {
    Assert.assertTrue(CharSequences.containsIgnoreCase("asdgafsdHgas", ""));
    Assert.assertTrue(CharSequences.containsIgnoreCase("asdgafsdHgas", "sdh"));
    Assert.assertFalse(CharSequences.containsIgnoreCase("asdgafsdHgas", "sdhf"));
  }

  @Test
  public void testUnsignedIntegerParsing() {
    int val = CharSequences.parseUnsignedInt("  1234  ", 10, 2);
    Assert.assertEquals(1234, val);
    val = CharSequences.parseUnsignedInt("  " + Integer.MAX_VALUE + "  ", 10, 2);
    Assert.assertEquals(Integer.MAX_VALUE, val);
    try {
      CharSequences.parseUnsignedInt("  " + Integer.MAX_VALUE + "1  ", 10, 2);
      Assert.fail();
    } catch (NumberFormatException ex) {
      // expected
    }
  }

  @Test
  public void testUnsignedLongParsing() {
    long val = CharSequences.parseUnsignedLong("  1234  ", 10, 2);
    Assert.assertEquals(1234L, val);
    val = CharSequences.parseUnsignedLong("  " + Long.MAX_VALUE + "  ", 10, 2);
    Assert.assertEquals(Long.MAX_VALUE, val);
    try {
      CharSequences.parseUnsignedLong("  " + Long.MAX_VALUE + "1  ", 10, 2);
      Assert.fail();
    } catch (NumberFormatException ex) {
      // expected
    }
  }

  @Test(expected = NumberFormatException.class)
  public void testUnsignedInvalidLongParsing() {
    CharSequences.parseUnsignedLong("abc", 10, 0);
  }

  @Test
  public void testOccurenceCount() {
    Assert.assertEquals(3, CharSequences.countIgnoreCase(" aab Aab aaab", "aab"));
    Assert.assertEquals(0, CharSequences.countIgnoreCase("", "aab"));
    Assert.assertEquals(0, CharSequences.countIgnoreCase(" ", "aab"));
    Assert.assertEquals(0, CharSequences.countIgnoreCase(" ", ""));
  }


}
