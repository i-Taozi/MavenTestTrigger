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
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import javax.annotation.Nonnull;
//CHECKSTYLE:OFF
import sun.nio.cs.ArrayDecoder;
import sun.nio.cs.ArrayEncoder;
//CHECKSTYLE:ON
import java.nio.charset.StandardCharsets;

/**
 *
 * @author zoly
 */
@SuppressFBWarnings("IICU_INCORRECT_INTERNAL_CLASS_USE")
public final class Strings {


  public static final String EOL = System.getProperty("line.separator", "\n");


  private static final boolean LENIENT_CODING = Boolean.getBoolean("spf4j.encoding.lenient");

  private static final ThreadLocal<CharsetDecoder> UTF8_DECODER = new ThreadLocal<CharsetDecoder>() {

    @Override
    protected CharsetDecoder initialValue() {
      return createUtf8Decoder();
    }

  };

  private static final ThreadLocal<CharsetEncoder> UTF8_ENCODER = new ThreadLocal<CharsetEncoder>() {

    @Override
    protected CharsetEncoder initialValue() {
      return createUtf8Encoder();
    }

  };

  private Strings() {
  }

  /**
   * @deprecated use CharSequences.distance instead.
   */
  @Deprecated
  public static int distance(@Nonnull final String s1, @Nonnull final String s2) {
    return CharSequences.distance(s1, s2);
  }



  /**
   * @deprecated use containsAnyChars instead.
   */
  @Deprecated
  public static boolean contains(final String string, final char... chars) {
    return containsAnyChars(string, chars);
  }

  public static boolean containsAnyChars(final String string, final char... chars) {
    for (char c : chars) {
      if (string.indexOf(c) >= 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * @deprecated use CharSequences.containsAnyChar instead.
   */
  @Deprecated
  public static boolean contains(final CharSequence string, final char... chars) {
    return CharSequences.containsAnyChar(string, chars);
  }

  public static String withFirstCharLower(final String str) {
    if (str.isEmpty()) {
      return str;
    }
    char fc = str.charAt(0);
    if (Character.isLowerCase(fc)) {
      return str;
    }
    int l = str.length();
    StringBuilder result = new StringBuilder(l);
    result.append(Character.toLowerCase(fc));
    for (int i = 1; i < l; i++) {
      result.append(str.charAt(i));
    }
    return result.toString();
  }

  /**
   * A get/set/is prefixed method name to attribute name converter.
   * @param prefix
   * @param str
   * @return
   */
  public static String methodToAttribute(final String prefix, final String str) {
    int length = str.length();
    int pl = prefix.length();
    StringBuilder result = new StringBuilder(length - pl);
    char fc = str.charAt(pl);
    if (Character.isLowerCase(fc)) {
      result.append(str, pl, length);
    } else {
      result.append(Character.toLowerCase(fc));
      result.append(str, pl + 1, length);
    }
    return result.toString();
  }

  /**
   * A attribute to get/set/is method name converter.
   * @param prefix
   * @param str
   * @return
   */
  public static String attributeToMethod(final String prefix, final String str) {
    int length = str.length();
    StringBuilder result = new StringBuilder(length + prefix.length());
    result.append(prefix);
    char fc = str.charAt(0);
    if (Character.isLowerCase(fc)) {
      result.append(Character.toUpperCase(fc));
      result.append(str, 1, length);
    } else {
      result.append(str);
    }
    return result.toString();
  }



  public static void writeReplaceWhitespaces(final String str, final char replacement, final Appendable writer)
          throws IOException {
    for (char c : steal(str)) {
      if (Character.isWhitespace(c)) {
        writer.append(replacement);
      } else {
        writer.append(c);
      }
    }
  }


  /**
   * Steal the underlying character array of a String.
   *
   * @param str
   * @return
   */
  public static char[] steal(final String str) {
    return UnsafeString.steal(str);
  }

  /**
   * Create a String based on the provided character array. No copy of the array is made.
   *
   * @param chars
   * @return
   */
  public static String wrap(final char[] chars) {
    return UnsafeString.wrap(chars);
  }

  public static CharsetEncoder createUtf8Encoder() {
    if (LENIENT_CODING) {
      return StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPLACE)
              .onUnmappableCharacter(CodingErrorAction.REPLACE);
    } else {
      return StandardCharsets.UTF_8.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
              .onUnmappableCharacter(CodingErrorAction.REPORT);
    }
  }


  public static CharsetDecoder createUtf8Decoder() {
    if (LENIENT_CODING) {
      return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPLACE)
              .onUnmappableCharacter(CodingErrorAction.REPLACE);
    } else {
      return StandardCharsets.UTF_8.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
              .onUnmappableCharacter(CodingErrorAction.REPORT);
    }
  }

  public static CharsetEncoder getUTF8CharsetEncoder() {
    return UTF8_ENCODER.get();
  }

  public static CharsetDecoder getUTF8CharsetDecoder() {
    return UTF8_DECODER.get();
  }

  @SuppressFBWarnings("SUA_SUSPICIOUS_UNINITIALIZED_ARRAY")
  public static byte[] encode(final CharsetEncoder ce, final char[] ca, final int off, final int len) {
    if (len == 0) {
      return Arrays.EMPTY_BYTE_ARRAY;
    }
    byte[] ba = TLScratch.getBytesTmp(getmaxNrBytes(ce, len));
    int nrBytes = encode(ce, ca, off, len, ba);
    return java.util.Arrays.copyOf(ba, nrBytes);
  }

  public static int getmaxNrBytes(final CharsetEncoder ce, final int nrChars) {
    return (int) (nrChars * (double) ce.maxBytesPerChar());
  }

  public static int encode(final CharsetEncoder ce, final char[] ca, final int off, final int len,
          final byte[] targetArray) {
    if (len == 0) {
      return 0;
    }
    if (ce instanceof ArrayEncoder) {
      return ((ArrayEncoder) ce).encode(ca, off, len, targetArray);
    } else {
      ce.reset();
      ByteBuffer bb = ByteBuffer.wrap(targetArray);
      CharBuffer cb = CharBuffer.wrap(ca, off, len);
      try {
        CoderResult cr = ce.encode(cb, bb, true);
        if (!cr.isUnderflow()) {
          cr.throwException();
        }
        cr = ce.flush(bb);
        if (!cr.isUnderflow()) {
          cr.throwException();
        }
      } catch (CharacterCodingException x) {
        throw new InternalError("Should never throw a CharacterCodingException, probably a JVM issue", x);
      }
      return bb.position();
    }
  }

  @SuppressFBWarnings("SUA_SUSPICIOUS_UNINITIALIZED_ARRAY")
  public static String decode(final CharsetDecoder cd, final byte[] ba, final int off, final int len) {
    if (len == 0) {
      return "";
    }
    int en = (int) (len * (double) cd.maxCharsPerByte());
    char[] ca = TLScratch.getCharsTmp(en);
    if (cd instanceof ArrayDecoder) {
      int clen = ((ArrayDecoder) cd).decode(ba, off, len, ca);
      return new String(ca, 0, clen);
    }
    cd.reset();
    ByteBuffer bb = ByteBuffer.wrap(ba, off, len);
    CharBuffer cb = CharBuffer.wrap(ca);
    try {
      CoderResult cr = cd.decode(bb, cb, true);
      if (!cr.isUnderflow()) {
        cr.throwException();
      }
      cr = cd.flush(cb);
      if (!cr.isUnderflow()) {
        cr.throwException();
      }
    } catch (CharacterCodingException x) {
      throw new UncheckedIOException(x);
    }
    return new String(ca, 0, cb.position());
  }

  /**
   * Optimized UTF8 decoder.
   *
   * Here is a benchmark comparison with the JDK implementation (see EncodingBenchmark.java in the benchmark project):
   *
   * EncodingBenchmark.stringDecode thrpt 10 16759798.463 ± 343505.144 ops/s EncodingBenchmark.fastStringDecode thrpt 10
   * 17413298.464 ± 301756.867 ops/s
   *
   *
   * @param bytes
   * @return
   */
  public static String fromUtf8(final byte[] bytes) {
    return decode(UTF8_DECODER.get(), bytes, 0, bytes.length);
  }

  public static String fromUtf8(final byte[] bytes, final int startIdx, final int length) {
    return decode(UTF8_DECODER.get(), bytes, startIdx, length);
  }

  /**
   * Optimized UTF8 string encoder.
   *
   * comparison with the stock JDK implementation (see EncodingBenchmark.java in the benchmark project):
   *
   * EncodingBenchmark.stringEncode thrpt 10 9481668.776 ± 252543.135 ops/s EncodingBenchmark.fastStringEncode thrpt 10
   * 22469383.612 ± 898677.892 ops/s
   *
   * @param str
   * @return
   */
  public static byte[] toUtf8(final String str) {
    final char[] chars = steal(str);
    return encode(UTF8_ENCODER.get(), chars, 0, chars.length);
  }

  /**
   * @deprecated use CharSequences.compare
   */
  @Deprecated
  public static int compareTo(@Nonnull final CharSequence s, @Nonnull final CharSequence t) {
    return CharSequences.compareTo(s, t);
  }

  @Deprecated
  public static boolean equals(@Nonnull final CharSequence s, @Nonnull final CharSequence t) {
    return CharSequences.equals(s, t);
  }

  @Deprecated
  public static int hashcode(final CharSequence cs) {
    return CharSequences.hashcode(cs);
  }

  @Deprecated
  public static CharSequence subSequence(final CharSequence seq, final int startIdx, final int endIdx) {
    return CharSequences.subSequence(seq, startIdx, endIdx);
  }

  @Deprecated
  public static boolean endsWith(final CharSequence qc, final CharSequence with) {
    return CharSequences.endsWith(qc, with);
  }

  /**
   * Utility method to escape java strings to json strings.
   *
   * @param toEscape - the java string to escape.
   * @param jsonString - the destination json String builder.
   */
  @Deprecated
  public static void escapeJsonString(@Nonnull final String toEscape, final StringBuilder jsonString) {
    AppendableUtils.escapeJsonString(toEscape, jsonString);
  }

  @Deprecated
  public static void escapeJsonString(@Nonnull final String toEscape, final Appendable jsonString)
          throws IOException {
    AppendableUtils.escapeJsonString(toEscape, jsonString);
  }

  @Deprecated
  public static void appendJsonStringEscapedChar(final char c, final StringBuilder jsonString) {
    AppendableUtils.appendJsonStringEscapedChar(c, jsonString);
  }

  @Deprecated
  public static void appendJsonStringEscapedChar(final char c, final Appendable jsonString) throws IOException {
    AppendableUtils.appendJsonStringEscapedChar(c, jsonString);
  }

  @Deprecated
  public static void appendUnsignedString(final StringBuilder sb, final long nr, final int shift) {
    AppendableUtils.appendUnsignedString(sb, nr, shift);
  }

  @Deprecated
  public static void appendUnsignedString(final StringBuilder sb, final int nr, final int shift) {
    AppendableUtils.appendUnsignedString(sb, nr, shift);
  }

  @Deprecated
  public static void appendUnsignedStringPadded(final StringBuilder sb, final int nr, final int shift,
          final int padTo) {
    AppendableUtils.appendUnsignedStringPadded(sb, nr, shift, padTo);
  }

  @Deprecated
  public static void appendUnsignedStringPadded(final Appendable sb, final int nr, final int shift,
          final int padTo) throws IOException {
    AppendableUtils.appendUnsignedStringPadded(sb, nr, shift, padTo);
  }

  @Deprecated
  public static void appendSpaces(final Appendable to, final int nrSpaces) throws IOException {
    AppendableUtils.appendSpaces(to, nrSpaces);
  }

  @Deprecated
  public static void appendSpaces(final StringBuilder to, final int nrSpaces) {
    AppendableUtils.appendSpaces(to, nrSpaces);
  }

  /**
   * @deprecated use CharSequences.regionMatches.
   */
  @Deprecated
  public static boolean regionMatches(final CharSequence t, final int toffset,
          final CharSequence other, final int ooffset, final int plen) {
    return CharSequences.regionMatches(t, toffset, other, ooffset, plen);
  }

  public static String truncate(@Nonnull final String value, final int length) {
    if (value.length() > length) {
      return value.substring(0, length);
    } else {
      return value;
    }
  }

  @Nonnull
  public static String commonPrefix(@Nonnull final CharSequence... strs) {
    if (strs.length <= 0) {
      throw new IllegalArgumentException("Must have at least 1 string " + java.util.Arrays.toString(strs));
     }
    CharSequence common = strs[0];
    for (int i = 1; i < strs.length; i++) {
      common = com.google.common.base.Strings.commonPrefix(common, strs[i]);
    }
    return common.toString();
  }

}
