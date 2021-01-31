/*
The MIT License

Copyright (c) Juan José GIL (matero _at_ gmail _dot_ com)

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

import javax.servlet.http.HttpServletRequest;

/**
 Defines how to read values ({@link PathVariable}s, {@link QueryParameter}s, {@link EndPointServlet.Header}s, etc) from requests.

 @param <T> Final type interpreted by the {@link RequestValueReader}. */
public abstract class RequestValueReader<T>
    implements java.io.Serializable
{
  /** name of the value to read. */
  protected final String name;
  /** how to interpret the value accessed from the request. */
  protected final ValueInterpreter<T> interpretValue;

  /**
   Builds a {@link RequestValueReader} defining the value name and interpreter.

   @param name        name used to identify the value.
   @param interpreter how to interpret the raw {@link String} at the request representing the value.
   */
  protected RequestValueReader(
      final String name,
      final ValueInterpreter<T> interpreter)
  {
    this.name = name;
    this.interpretValue = interpreter;
  }

  /**
   Checks if the value is defined in the request.

   @param request {@link HttpServletRequest} where to check if the value is defined or not.
   @return {@literal true} if the value is defined in the request; {@literal false} other way.
   */
  public abstract boolean isDefinedAt(HttpServletRequest request);

  /**
   Gets the interpreted value at some request.

   @param request {@link HttpServletRequest} where to get the value definition.
   @return the interpretation of the raw {@link String} defined in {@code request} representing this value.
   */
  public final T at(final HttpServletRequest request)
  {
    if (isDefinedAt(request)) {
      return valueDefined(request);
    } else {
      return valueUndefined(request);
    }
  }

  /**
   Gets the value to use when the value is defined in a request.

   @param request {@link HttpServletRequest} where to get the value definition.
   @return the value to use when this is defined under {@code request}.
   */
  protected T valueDefined(HttpServletRequest request)
  {
    return interpretValue.from(read(request));
  }

  /**
   Gets the value to use when the value is not defined in a request.
   <p>
   Can use the request as context or to look for another value.

   @param request {@link HttpServletRequest} where to get the value definition.
   @return the value to use when this is undefined under {@code request}.
   */
  protected abstract T valueUndefined(HttpServletRequest request);

  /**
   Gets the raw representation of a <em>defined value</em> at a {@link HttpServletRequest}.

   @param request {@link HttpServletRequest} where to get the value raw representation.
   @return raw representation of the value t the {@code request}.
   */
  protected abstract String read(HttpServletRequest request);

  /** Indicates that  required value is not defined under a request. */
  public static final class ValueNotDefined
      extends RuntimeException
  {
    /** request which doesn't hold the value. */
    public final HttpServletRequest request;

    /**
     Construct an instance of {@link ValueNotDefined} indicating the name of the undefined value and the request where it was undefined.

     @param name    name of the undefined value. It will be accessible as exception message.
     @param request {@link HttpServletRequest} where the value is undefined.
     */
    ValueNotDefined(
        final String name,
        final HttpServletRequest request)
    {
      super(name);
      this.request = request;
    }
  }
}
