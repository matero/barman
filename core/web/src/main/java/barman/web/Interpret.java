/*
The MIT License

Copyright (c) 2021 Juan J. GIL (matero _at_ gmail _dot_ com)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package barman.web;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public final class Interpret
{

  private Interpret()
  {
    throw new UnsupportedOperationException();
  }

  public static boolean asPrimitiveBoolean(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return Boolean.parseBoolean(raw);
    }
  }

  public static byte asPrimitiveByte(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return Byte.parseByte(raw);
    }
  }

  public static short asPrimitiveShort(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return Short.parseShort(raw);
    }
  }

  public static int asPrimitiveInt(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return Integer.parseInt(raw);
    }
  }

  public static long asPrimitiveLong(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return Long.parseLong(raw);
    }
  }

  public static char asPrimitiveChar(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return raw.charAt(0);
    }
  }

  public static float asPrimitiveFloat(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return Float.parseFloat(raw);
    }
  }

  public static double asPrimitiveDouble(final String raw)
  {
    if (raw == null) {
      throw new NullPointerException("raw");
    } else {
      return Double.parseDouble(raw);
    }
  }

  public static Boolean asBoolean(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return Boolean.valueOf(raw);
    }
  }

  public static Byte asByte(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return Byte.valueOf(raw);
    }
  }

  public static Short asShort(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return Short.valueOf(raw);
    }
  }

  public static Integer asInteger(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return Integer.valueOf(raw);
    }
  }

  public static Long asLong(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return Long.valueOf(raw);
    }
  }

  public static Character asCharacter(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return raw.charAt(0);
    }
  }

  public static Float asFloat(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return Float.valueOf(raw);
    }
  }

  public static Double asDouble(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return Double.valueOf(raw);
    }
  }

  public static String asString(final String raw)
  {
    return raw;
  }

  public static String asTrimmedString(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      return raw.trim();
    }
  }

  public static URL asUrl(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      try {
        return new URL(raw);
      } catch (final MalformedURLException e) {
        throw new IllegalArgumentException("malformed URL", e);
      }
    }
  }

  public static List<String> asStringList(final String raw)
  {
    if (raw == null) {
      return null;
    } else {
      final var strings = raw.split(",");
      return List.of(strings);
    }
  }
}
