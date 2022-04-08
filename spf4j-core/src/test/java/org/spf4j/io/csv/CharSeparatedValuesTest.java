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
package org.spf4j.io.csv;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Zoltan Farkas
 */
public class CharSeparatedValuesTest {


  @Test
  public void testCsvReaderEmpty() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.reader(new StringReader(""));
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.next());
  }

  @Test
  public void testCsvReaderEmpty2() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.reader(new StringReader("\r"));
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.next());
  }

  @Test
  public void testCsvReader() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.reader(new StringReader("a b c\nd e"));
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("a", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("b", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("c", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.current());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.current());
    Assert.assertEquals("d", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("e", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.next());
  }

  @Test
  public void testCsvReaderX() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.reader(new StringReader("a b c\nd e\n"));
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("a", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("b", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("c", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.current());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.current());
    Assert.assertEquals("d", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("e", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.next());
  }

   @Test
  public void testCsvReaderXX() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.readerILEL(new StringReader("a b c\r\nd e\r\n"));
    Assert.assertEquals(CsvReader.TokenType.START_DOCUMENT, reader.current());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("a", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("b", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("c", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.current());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.current());
    Assert.assertEquals("d", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("e", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.next());
  }

  @Test
  public void testCsvReaderXXX() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.readerILEL(new StringReader("a b c\n\rd e\n\r"));
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("a", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("b", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("c", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.current());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.current());
    Assert.assertEquals("d", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("e", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.next());
  }

  @Test
  public void testCsvReader2() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.reader(new StringReader(""));
    Assert.assertEquals(CsvReader.TokenType.START_DOCUMENT, reader.current());
    Assert.assertEquals(CsvReader.TokenType.ELEMENT, reader.next());
    Assert.assertEquals("", reader.getElement().toString());
    Assert.assertEquals(CsvReader.TokenType.END_ROW, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.next());
    Assert.assertEquals(CsvReader.TokenType.END_DOCUMENT, reader.current());
  }

  @Test
  public void testCsvReader3() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    CsvReader reader = csv.reader(new StringReader("a b c\nd e"));
    int nrElems = reader.skipRow();
    Assert.assertEquals(3, nrElems);
    nrElems = reader.skipRow();
    Assert.assertEquals(2, nrElems);
  }


  @Test
  public void testCsvWriter() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    StringWriter writer = new StringWriter();
    CsvWriter csvW = csv.writer(writer);
    csvW.writeElement("a");
    csvW.writeElement("b");
    csvW.writeElement("c");
    csvW.writeEol();
    csvW.writeElement("d");
    csvW.writeElement("e");
    Assert.assertEquals("a b c\nd e", writer.toString());
  }

  @Test
  public void testCsvWriter2() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    StringWriter writer = new StringWriter();
    CsvWriter csvW = csv.writer(writer);
    csvW.writeElement("");
    Assert.assertEquals("", writer.toString());
  }
  @Test
  public void testCsvWriter3() throws IOException, CsvParseException {
    CharSeparatedValues csv = new CharSeparatedValues(' ');
    StringWriter writer = new StringWriter();
    CsvWriter csvW = csv.writer(writer);
    csvW.writeElement("");
    csvW.writeElement("b ");
    Assert.assertEquals(" \"b \"", writer.toString());
  }

  @Test
  public void testRowParse() {
     CharSeparatedValues csv = new CharSeparatedValues(',');
     List<String> res = new ArrayList<>();
     for (CharSequence elem :csv.singleRow(new StringReader("a,b,c"))) {
       res.add(elem.toString());
     }
     Assert.assertEquals(Arrays.asList("a", "b", "c"), res);
  }

  @Test
  public void testRowParse2() {
     CharSeparatedValues csv = new CharSeparatedValues(',');
     List<String> res = new ArrayList<>();
     for (CharSequence elem :csv.singleRow(new StringReader(""))) {
       res.add(elem.toString());
     }
     Assert.assertEquals(Collections.singletonList(""), res);
  }

  @Test
  public void testRowParse3() {
     CharSeparatedValues csv = new CharSeparatedValues(',');
     List<String> res = new ArrayList<>();
     for (CharSequence elem :csv.singleRow(new StringReader("\na,b,c"))) {
       res.add(elem.toString());
     }
     Assert.assertEquals(Collections.singletonList(""), res);
  }


}
