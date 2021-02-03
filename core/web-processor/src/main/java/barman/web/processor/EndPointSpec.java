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
package barman.web.processor;

import barman.web.RouterServlet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class EndPointSpec
{
  final List<Route> routes;
  final Map<HttpVerb, List<Route>> routesByVerb;
  final String paths;
  final String date;
  final ClassName superClass;
  final String routerClass;
  final String path;
  final boolean noLoggerDefined;

  @SuppressWarnings("checkstyle:parameterNumber") EndPointSpec(
      final String path,
      final List<Route> routes,
      final Map<HttpVerb, List<Route>> routesByVerb,
      final String paths,
      final String date,
      final String routerClass,
      final ClassName superClass,
      final boolean noLoggerDefined)
  {
    this.path = path;
    this.routes = routes;
    this.routesByVerb = routesByVerb;
    this.paths = paths;
    this.date = date;
    this.superClass = superClass;
    this.routerClass = routerClass;
    this.noLoggerDefined = noLoggerDefined;
  }

  static TypeName routerClass()
  {
    return ClassName.get(RouterServlet.class);
  }

  static Builder builder(
      final ClassName endpointClass,
      final String today)
  {
    return new Builder(new LinkedList<>(),
        new EnumMap<>(HttpVerb.class),
        new StringBuilder(),
        today,
        endpointClass);
  }

  boolean hasRoleConstraints()
  {
    return routes.stream().anyMatch(Route::hasRoleConstrains);
  }

  ClassName routerClassName()
  {
    return ClassName.bestGuess(routerClass);
  }

  static class Builder
  {
    final List<Route> routes;
    final Map<HttpVerb, List<Route>> routesByVerb;
    final StringBuilder paths;
    final String today;
    private final ClassName endpointClass;
    private String implClass;
    private String path;
    private boolean loggerDefined;

    Builder(
        final List<Route> routes,
        final Map<HttpVerb, List<Route>> routesByVerb,
        final StringBuilder paths,
        final String today,
        final ClassName endpointClass)
    {
      this.routes = routes;
      this.routesByVerb = routesByVerb;
      this.paths = paths;
      this.today = today;
      this.endpointClass = endpointClass;
    }

    void addRoute(final Route route)
    {
      if (route != null) {
        routes.add(route);
        routesByVerb.putIfAbsent(route.verb, new LinkedList<>());
        routesByVerb.get(route.verb).add(route);
      }
    }

    void implClass(final String value)
    {
      implClass = value;
    }

    void path(final String value)
    {
      path = value;
    }

    EndPointSpec build()
    {
      return new EndPointSpec(path, routes, routesByVerb, paths.toString(), today, implClass, endpointClass, !loggerDefined);
    }

    void loggerDefined(final boolean value)
    {
      loggerDefined = value;
    }
  }
}
