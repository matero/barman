/*
 * The MIT License
 *
 * Copyright (c) 2021 Juan José GIL.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package barman.web.processor;

import barman.endpoint;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static javax.lang.model.util.ElementFilter.methodsIn;

class RoutesReader {
  private static final String[] NO_ROLES = {};

  private static final String API = "/api";
  private static final String ADMIN = "/admin";

  private final Messager messager;

  private final EndPointSpec.Builder routes;

  private boolean success;

  RoutesReader(
      final Messager messager,
      final EndPointSpec.Builder routes)
  {
    this.messager = messager;
    this.routes = routes;
  }

  /**
   @param endpoint
   @return null if no processing done (reports errors on such case), the route add
   */
  boolean buildRoutesFor(final TypeElement endpoint)
  {
    success = true;

    final var baseUri = makeEndpointPath(getEndpointPathKind(endpoint), endpoint);
    routes.path(baseUri + "/*");
    for (final var method : methodsIn(endpoint.getEnclosedElements())) {
      for (final var httpVerb : HttpVerb.values()) {
        buildRoute(endpoint, baseUri, httpVerb, method);
      }
    }

    return success;
  }

  private String getEndpointPathKind(final TypeElement endpoint)
  {
    if (isAdmin(endpoint)) {
      return ADMIN;
    } else {
      return API;
    }
  }

  void buildRoute(
      final TypeElement endpoint,
      final String baseUri,
      final HttpVerb httpVerb,
      final ExecutableElement method)
  {
    final var path = httpVerb.getPath(method);
    if (path == null) {
      return;
    }
    final var handler = handlerPath(method, path);
    makeRoute(
        httpVerb,
        getUri(baseUri, handler),
        handler,
        method);
  }

  private String getUri(
      final String baseUri,
      final String handler)
  {
    return makeUri(baseUri, handler);
  }

  boolean isAdmin(final TypeElement endpoint) { return endpoint.getAnnotation(barman.endpoint.class).admin(); }

  private void makeRoute(
      final HttpVerb verb,
      final String endPointUri,
      final String handlerUri,
      final ExecutableElement method)
  {
    final var path = PathSpec.from(endPointUri, handlerUri);
    routes.addRoute(path.makeRoute(verb, method));
  }

  private void error(
      final String message,
      final Element e)
  {
    messager.printMessage(Diagnostic.Kind.ERROR, message, e);
    success = false;
  }

  private String makeUri(
      final String parentPath,
      final String childPath)
  {
    if (childPath.startsWith("/")) {
      if (parentPath.endsWith("/")) {
        return parentPath + childPath.substring(1);
      } else {
        return parentPath + childPath;
      }
    } else {
      if (parentPath.endsWith("/")) {
        return parentPath + childPath;
      } else {
        if ("".equals(childPath)) {
          return parentPath;
        } else {
          return parentPath + '/' + childPath;
        }
      }
    }
  }

  private String handlerPath(
      final ExecutableElement method,
      final String path)
  {
    if ("".equals(path)) {
      final String actionName = method.getSimpleName().toString();
      switch (actionName) {
        case "index":
        case "save":
        case "update":
        case "delete":
          return "";
        default:
          return '/' + actionName;
      }
    } else {
      return path.startsWith("/") ? path : '/' + path;
    }
  }

  private String makeEndpointPath(
      final String parentPath,
      final TypeElement endpoint)
  {
    final String path = getPath(endpoint);
    if (path == null) {
      error("@endpoint.path can't be null", endpoint);
    } else {
      if ("".equals(path)) {
        final var endpointClassName = endpoint.getSimpleName();
        final var classname = endpointClassName.toString();
        return makeUri(parentPath, classname.toLowerCase());
      } else if (path.startsWith("/")) {
        return path;
      } else {
        return makeUri(parentPath, path);
      }
    }
    return "";
  }

  private String getPath(final TypeElement endpoint) { return endpoint.getAnnotation(endpoint.class).value(); }
}
