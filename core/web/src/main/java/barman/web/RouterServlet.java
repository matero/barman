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

import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Serializable;
import java.util.regex.Pattern;

public abstract class RouterServlet
    extends javax.servlet.http.HttpServlet
{
  /** represents index path {@code "/"}. */
  @SuppressWarnings("checkstyle:constantName") protected static final Path indexPath = IndexPath.INSTANCE;

  /** Defined to avoid possible {@link EndPointServlet} anonymous construction. */
  protected RouterServlet()
  {
    // nothing more to do
  }

  /**
   * Creates an static (fixed) path representation.
   *
   * @param uri  Complete URI of the represented path.
   * @param path sub-path of the URI diferentiating this path from the others in the same endpoint.
   * @return an {@link StaticPath} representing the {@code uri} and {@code path}.
   */
  protected static Path path(
      final String uri,
      final String path)
  {
    return new StaticPath(uri, path);
  }

  protected static Path path(
      final String uri,
      final String pattern,
      final Pattern regex,
      final String... parameters)
  {
    return new ParameterizedPath(uri, pattern, regex, parameters);
  }

  protected static void notAuthorized(final HttpServletResponse response)
  {
    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
  }

  /**
   * @return the logger to be used at the controller.
   */
  protected abstract Logger logger();

  protected void unhandledGet(
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException, IOException
  {
    super.doGet(request, response);
  }

  protected void unhandledDelete(
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException, IOException
  {
    super.doDelete(request, response);
  }

  protected void unhandledPut(
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException, IOException
  {
    super.doPut(request, response);
  }

  protected void unhandledPost(
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException, IOException
  {
    super.doPost(request, response);
  }

  protected void unhandledHead(
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException, IOException
  {
    super.doHead(request, response);
  }

  protected void unhandledOptions(
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException, IOException
  {
    super.doOptions(request, response);
  }

  protected void unhandledTrace(
      final HttpServletRequest request,
      final HttpServletResponse response)
      throws ServletException, IOException
  {
    super.doTrace(request, response);
  }

  private enum IndexPath
      implements Path
  {
    INSTANCE;

    @Override public boolean matches(final HttpServletRequest request)
    {
      final var pathInfo = request.getPathInfo();
      return null == pathInfo || pathInfo.isEmpty() || "/".equals(pathInfo);
    }

    @Override public String toString()
    {
      return "Path('/')";
    }
  }

  protected interface Path
      extends Serializable
  {
    boolean matches(HttpServletRequest request);
  }

  private static final class StaticPath
      implements Path
  {
    private final String uri;
    private final String path;

    private StaticPath(
        final String uri,
        final String path)
    {
      this.uri = uri;
      this.path = path;
    }

    @Override public String toString()
    {
      return "Path('" + uri + "')";
    }

    @Override public int hashCode()
    {
      return uri.hashCode();
    }

    @Override public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o instanceof StaticPath) {
        return uri.equals(((StaticPath) o).uri);
      }
      return false;
    }

    @Override public boolean matches(final HttpServletRequest request)
    {
      return path.equals(request.getPathInfo());
    }
  }

  private static final class ParameterizedPath
      implements Path
  {
    private final String uri;
    private final String pattern;
    private final Pattern regex;
    private final String[] parameters;

    private ParameterizedPath(
        final String uri,
        final String pattern,
        final Pattern regex,
        final String[] parameters)
    {
      this.uri = uri;
      this.pattern = pattern;
      this.regex = regex;
      this.parameters = parameters;
    }

    @Override public String toString()
    {
      return "Path('" + pattern + "')";
    }

    @Override public int hashCode()
    {
      return pattern.hashCode();
    }

    @Override public boolean equals(final Object o)
    {
      if (this == o) {
        return true;
      }
      if (o instanceof ParameterizedPath) {
        return uri.equals(((ParameterizedPath) o).uri);
      }
      return false;
    }

    @Override public boolean matches(final HttpServletRequest request)
    {
      if (!IndexPath.INSTANCE.matches(request)) {
        final var matcher = regex.matcher(request.getPathInfo());
        if (matcher.matches()) {
          for (final var parameter : parameters) {
            request.setAttribute(parameter, matcher.group(parameter));
          }
          return true;
        }
      }
      return false;
    }
  }
}
