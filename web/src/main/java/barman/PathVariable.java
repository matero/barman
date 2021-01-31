/*
 The MIT License

 Copyright 2021 Juan José GIL (matero _at_ gmail _dot_ com)

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
package barman;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

public final class PathVariable<T> extends RequestValueReader<T>
{
  public static final class NotDefined extends RuntimeException
  {
    public final HttpServletRequest request;

    NotDefined(final String parameterName, final HttpServletRequest request)
    {
      super(parameterName);
      this.request = request;
    }
  }

  PathVariable(final String name, final ValueInterpreter<T> interpretValue) { super(name, interpretValue); }

  @Override public final boolean isDefinedAt(final HttpServletRequest request)
  {
    final Enumeration<String> attributeNames = request.getAttributeNames();
    while (attributeNames.hasMoreElements()) {
      final String attributeName = attributeNames.nextElement();
      if (name.equals(attributeName)) {
        return true;
      }
    }
    return false;
  }

  @Override protected final T valueUndefined(final HttpServletRequest request) { throw new NotDefined(name, request); }

  @Override protected final String read(final HttpServletRequest request) { return (String) request.getAttribute(name); }
}
