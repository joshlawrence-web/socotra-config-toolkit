package com.socotra.platform.tools;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.util.SplittableRandom;

/**
 * Universally Unique Lexicographically Sortable Identifier
 *
 * <p>See https://github.com/ulid/spec for Specification
 */
@JsonSerialize(using = ULID.ULIDSerializer.class)
public class ULID implements Comparable<ULID> {
  private static final char[] ALPHABET_UPPERCASE = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J',
    'K', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'X', 'Y', 'Z'
  };
  private static final char[] ALPHABET_LOWERCASE = {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j',
    'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'v', 'w', 'x', 'y', 'z'
  };

  private static final long[] ALPHABET_VALUES = new long[128];

  public static final int RANDOM_BYTES = 10;
  public static final int ULID_BYTES_LENGTH = 16;
  public static final int ULID_CHARS_COUNT = 26;

  private final long msb;
  private final long lsb;

  public ULID(long msb, long lsb) {
    this.msb = msb;
    this.lsb = lsb;
  }

  public ULID(long time, byte[] random) {
    if ((time & 0xffff000000000000L) != 0) {
      throw new IllegalArgumentException("Invalid time value");
    }
    if (random == null || random.length != RANDOM_BYTES) {
      throw new IllegalArgumentException(
          "Random bytes cannot be null and should be " + RANDOM_BYTES + " in length");
    }

    long msb = 0;
    msb |= time << 16;
    msb |= (long) (random[0x0] & 0xff) << 8;
    msb |= random[0x1] & 0xff;

    long lsb = 0;
    lsb |= (long) (random[0x2] & 0xff) << 56;
    lsb |= (long) (random[0x3] & 0xff) << 48;
    lsb |= (long) (random[0x4] & 0xff) << 40;
    lsb |= (long) (random[0x5] & 0xff) << 32;
    lsb |= (long) (random[0x6] & 0xff) << 24;
    lsb |= (long) (random[0x7] & 0xff) << 16;
    lsb |= (long) (random[0x8] & 0xff) << 8;
    lsb |= random[0x9] & 0xff;
    this.msb = msb;
    this.lsb = lsb;
  }

  @Override
  public int compareTo(ULID o) {
    final long min = 0x8000000000000000L;

    final long a = this.msb + min;
    final long b = o.msb + min;

    if (a > b) return 1;
    else if (a < b) return -1;

    final long c = this.lsb + min;
    final long d = o.lsb + min;

    if (c > d) return 1;
    else if (c < d) return -1;

    return 0;
  }

  @Override
  public int hashCode() {
    final long bits = msb ^ lsb;
    return (int) (bits ^ (bits >>> 32));
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj instanceof ULID other) {
      return (lsb == other.lsb && msb == other.msb);
    }
    return false;
  }

  @Override
  public String toString() {
    return toString(ALPHABET_UPPERCASE);
  }

  public String toLowerString() {
    return toString(ALPHABET_LOWERCASE);
  }

  public long getMostSignificantBits() {
    return msb;
  }

  public long getLeastSignificantBits() {
    return lsb;
  }

  private String toString(char[] alphabet) {
    final char[] chars = new char[ULID_CHARS_COUNT];

    long time = this.msb >>> 16;
    long random0 = ((this.msb & 0xffffL) << 24) | (this.lsb >>> 40);
    long random1 = (this.lsb & 0xffffffffffL);

    chars[0x00] = alphabet[(int) (time >>> 45 & 0b11111)];
    chars[0x01] = alphabet[(int) (time >>> 40 & 0b11111)];
    chars[0x02] = alphabet[(int) (time >>> 35 & 0b11111)];
    chars[0x03] = alphabet[(int) (time >>> 30 & 0b11111)];
    chars[0x04] = alphabet[(int) (time >>> 25 & 0b11111)];
    chars[0x05] = alphabet[(int) (time >>> 20 & 0b11111)];
    chars[0x06] = alphabet[(int) (time >>> 15 & 0b11111)];
    chars[0x07] = alphabet[(int) (time >>> 10 & 0b11111)];
    chars[0x08] = alphabet[(int) (time >>> 5 & 0b11111)];
    chars[0x09] = alphabet[(int) (time & 0b11111)];

    chars[0x0a] = alphabet[(int) (random0 >>> 35 & 0b11111)];
    chars[0x0b] = alphabet[(int) (random0 >>> 30 & 0b11111)];
    chars[0x0c] = alphabet[(int) (random0 >>> 25 & 0b11111)];
    chars[0x0d] = alphabet[(int) (random0 >>> 20 & 0b11111)];
    chars[0x0e] = alphabet[(int) (random0 >>> 15 & 0b11111)];
    chars[0x0f] = alphabet[(int) (random0 >>> 10 & 0b11111)];
    chars[0x10] = alphabet[(int) (random0 >>> 5 & 0b11111)];
    chars[0x11] = alphabet[(int) (random0 & 0b11111)];

    chars[0x12] = alphabet[(int) (random1 >>> 35 & 0b11111)];
    chars[0x13] = alphabet[(int) (random1 >>> 30 & 0b11111)];
    chars[0x14] = alphabet[(int) (random1 >>> 25 & 0b11111)];
    chars[0x15] = alphabet[(int) (random1 >>> 20 & 0b11111)];
    chars[0x16] = alphabet[(int) (random1 >>> 15 & 0b11111)];
    chars[0x17] = alphabet[(int) (random1 >>> 10 & 0b11111)];
    chars[0x18] = alphabet[(int) (random1 >>> 5 & 0b11111)];
    chars[0x19] = alphabet[(int) (random1 & 0b11111)];

    return new String(chars);
  }

  /**
   * Generates a random ULID
   *
   * @return
   */
  public static ULID generate() {
    final SplittableRandom random = new SplittableRandom();
    return new ULID(
        System.currentTimeMillis() << 16 | (random.nextLong() & 0xffffL), random.nextLong());
  }

  /**
   * Constructs ULID for byte array representation
   *
   * @param bytes
   * @return
   */
  public static ULID from(byte[] bytes) {
    if (bytes == null || bytes.length != ULID_BYTES_LENGTH) {
      throw new IllegalArgumentException("Invalid ULID bytes"); // null or wrong length!
    }

    long msb = 0;
    long lsb = 0;

    msb |= (bytes[0x0] & 0xffL) << 56;
    msb |= (bytes[0x1] & 0xffL) << 48;
    msb |= (bytes[0x2] & 0xffL) << 40;
    msb |= (bytes[0x3] & 0xffL) << 32;
    msb |= (bytes[0x4] & 0xffL) << 24;
    msb |= (bytes[0x5] & 0xffL) << 16;
    msb |= (bytes[0x6] & 0xffL) << 8;
    msb |= (bytes[0x7] & 0xffL);

    lsb |= (bytes[0x8] & 0xffL) << 56;
    lsb |= (bytes[0x9] & 0xffL) << 48;
    lsb |= (bytes[0xa] & 0xffL) << 40;
    lsb |= (bytes[0xb] & 0xffL) << 32;
    lsb |= (bytes[0xc] & 0xffL) << 24;
    lsb |= (bytes[0xd] & 0xffL) << 16;
    lsb |= (bytes[0xe] & 0xffL) << 8;
    lsb |= (bytes[0xf] & 0xffL);

    return new ULID(msb, lsb);
  }

  /**
   * Returns time in milliseconds associated with ULID
   *
   * @return
   */
  @JsonIgnore
  public long getTime() {
    return this.msb >>> 16;
  }

  /**
   * Monotonically create a new ULID using existing one
   *
   * @return
   */
  public ULID increment() {

    long newMsb = this.msb;
    long newLsb = this.lsb + 1; // increment the LEAST significant bits

    if (newLsb == 0x0000000000000000L) {
      newMsb += 1; // increment the MOST significant bits
    }

    return new ULID(newMsb, newLsb);
  }

  public static boolean isValid(String value) {
    if (value == null || value.isBlank()) {
      return false;
    }

    if ((ALPHABET_VALUES[value.charAt(0)] & 0b11000) != 0) {
      return false;
    }
    if (value.length() != ULID_CHARS_COUNT) {
      return false;
    }
    for (int i = 0; i < value.length(); i++) {
      if (ALPHABET_VALUES[value.charAt(i)] == -1) {
        return false;
      }
    }
    return true;
  }

  @JsonCreator
  /** Constructs ULID from String representation */
  public static ULID from(String value) {
    if (!isValid(value)) {
      throw new IllegalArgumentException("Invalid ULID: '" + value + "'");
    }

    final char[] chars = value.toCharArray();
    long time = 0;
    long r0 = 0;
    long r1 = 0;

    time |= ALPHABET_VALUES[chars[0x00]] << 45;
    time |= ALPHABET_VALUES[chars[0x01]] << 40;
    time |= ALPHABET_VALUES[chars[0x02]] << 35;
    time |= ALPHABET_VALUES[chars[0x03]] << 30;
    time |= ALPHABET_VALUES[chars[0x04]] << 25;
    time |= ALPHABET_VALUES[chars[0x05]] << 20;
    time |= ALPHABET_VALUES[chars[0x06]] << 15;
    time |= ALPHABET_VALUES[chars[0x07]] << 10;
    time |= ALPHABET_VALUES[chars[0x08]] << 5;
    time |= ALPHABET_VALUES[chars[0x09]];

    r0 |= ALPHABET_VALUES[chars[0x0a]] << 35;
    r0 |= ALPHABET_VALUES[chars[0x0b]] << 30;
    r0 |= ALPHABET_VALUES[chars[0x0c]] << 25;
    r0 |= ALPHABET_VALUES[chars[0x0d]] << 20;
    r0 |= ALPHABET_VALUES[chars[0x0e]] << 15;
    r0 |= ALPHABET_VALUES[chars[0x0f]] << 10;
    r0 |= ALPHABET_VALUES[chars[0x10]] << 5;
    r0 |= ALPHABET_VALUES[chars[0x11]];

    r1 |= ALPHABET_VALUES[chars[0x12]] << 35;
    r1 |= ALPHABET_VALUES[chars[0x13]] << 30;
    r1 |= ALPHABET_VALUES[chars[0x14]] << 25;
    r1 |= ALPHABET_VALUES[chars[0x15]] << 20;
    r1 |= ALPHABET_VALUES[chars[0x16]] << 15;
    r1 |= ALPHABET_VALUES[chars[0x17]] << 10;
    r1 |= ALPHABET_VALUES[chars[0x18]] << 5;
    r1 |= ALPHABET_VALUES[chars[0x19]];

    final long msb = (time << 16) | (r0 >>> 24);
    final long lsb = (r0 << 40) | (r1 & 0xffffffffffL);

    return new ULID(msb, lsb);
  }

  static {
    for (int i = 0; i < ALPHABET_VALUES.length; i++) {
      ALPHABET_VALUES[i] = -1;
    }
    // Numbers
    ALPHABET_VALUES['0'] = 0x00;
    ALPHABET_VALUES['1'] = 0x01;
    ALPHABET_VALUES['2'] = 0x02;
    ALPHABET_VALUES['3'] = 0x03;
    ALPHABET_VALUES['4'] = 0x04;
    ALPHABET_VALUES['5'] = 0x05;
    ALPHABET_VALUES['6'] = 0x06;
    ALPHABET_VALUES['7'] = 0x07;
    ALPHABET_VALUES['8'] = 0x08;
    ALPHABET_VALUES['9'] = 0x09;
    // Lower case
    ALPHABET_VALUES['a'] = 0x0a;
    ALPHABET_VALUES['b'] = 0x0b;
    ALPHABET_VALUES['c'] = 0x0c;
    ALPHABET_VALUES['d'] = 0x0d;
    ALPHABET_VALUES['e'] = 0x0e;
    ALPHABET_VALUES['f'] = 0x0f;
    ALPHABET_VALUES['g'] = 0x10;
    ALPHABET_VALUES['h'] = 0x11;
    ALPHABET_VALUES['j'] = 0x12;
    ALPHABET_VALUES['k'] = 0x13;
    ALPHABET_VALUES['m'] = 0x14;
    ALPHABET_VALUES['n'] = 0x15;
    ALPHABET_VALUES['p'] = 0x16;
    ALPHABET_VALUES['q'] = 0x17;
    ALPHABET_VALUES['r'] = 0x18;
    ALPHABET_VALUES['s'] = 0x19;
    ALPHABET_VALUES['t'] = 0x1a;
    ALPHABET_VALUES['v'] = 0x1b;
    ALPHABET_VALUES['w'] = 0x1c;
    ALPHABET_VALUES['x'] = 0x1d;
    ALPHABET_VALUES['y'] = 0x1e;
    ALPHABET_VALUES['z'] = 0x1f;
    // Lower case OIL
    ALPHABET_VALUES['o'] = 0x00;
    ALPHABET_VALUES['i'] = 0x01;
    ALPHABET_VALUES['l'] = 0x01;
    // Upper case
    ALPHABET_VALUES['A'] = 0x0a;
    ALPHABET_VALUES['B'] = 0x0b;
    ALPHABET_VALUES['C'] = 0x0c;
    ALPHABET_VALUES['D'] = 0x0d;
    ALPHABET_VALUES['E'] = 0x0e;
    ALPHABET_VALUES['F'] = 0x0f;
    ALPHABET_VALUES['G'] = 0x10;
    ALPHABET_VALUES['H'] = 0x11;
    ALPHABET_VALUES['J'] = 0x12;
    ALPHABET_VALUES['K'] = 0x13;
    ALPHABET_VALUES['M'] = 0x14;
    ALPHABET_VALUES['N'] = 0x15;
    ALPHABET_VALUES['P'] = 0x16;
    ALPHABET_VALUES['Q'] = 0x17;
    ALPHABET_VALUES['R'] = 0x18;
    ALPHABET_VALUES['S'] = 0x19;
    ALPHABET_VALUES['T'] = 0x1a;
    ALPHABET_VALUES['V'] = 0x1b;
    ALPHABET_VALUES['W'] = 0x1c;
    ALPHABET_VALUES['X'] = 0x1d;
    ALPHABET_VALUES['Y'] = 0x1e;
    ALPHABET_VALUES['Z'] = 0x1f;
    // Upper case OIL
    ALPHABET_VALUES['O'] = 0x00;
    ALPHABET_VALUES['I'] = 0x01;
    ALPHABET_VALUES['L'] = 0x01;
  }

  static class ULIDSerializer extends StdSerializer<ULID> {

    public ULIDSerializer() {
      this(null);
    }

    public ULIDSerializer(Class<ULID> t) {
      super(t);
    }

    @Override
    public void serialize(ULID value, JsonGenerator gen, SerializerProvider provider)
        throws IOException {
      gen.writeString(value.toString());
    }
  }
}
