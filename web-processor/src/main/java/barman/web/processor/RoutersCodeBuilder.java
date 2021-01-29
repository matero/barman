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

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.processing.Generated;
import javax.lang.model.element.Modifier;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

class RoutersCodeBuilder {
  private static final ClassName LOGGER_CLASS = ClassName.get(Logger.class);
  private static final ClassName LOGGER_FACTORY_CLASS = ClassName.get(LoggerFactory.class);

  JavaFile buildJavaCode(
      final EndPointSpec declarations,
      boolean isDevelopmentEnvironment)
  {
    final var classname = declarations.routerClassName();
    final var router = TypeSpec.classBuilder(classname)
                               .superclass(declarations.superClass)
                               .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                               .addAnnotation(AnnotationSpec.builder(Generated.class)
                                                            .addMember("value", "$S", "barman/web-processor")
                                                            .addMember("comments", "$S", declarations.paths)
                                                            .addMember("date", "$S", declarations.date)
                                                            .build())
                               .addAnnotation(AnnotationSpec.builder(WebServlet.class)
                                                            .addMember("value", "$S", declarations.path)
                                                            .build());
    if (declarations.noLoggerDefined) {
      router.addField(FieldSpec.builder(LOGGER_CLASS, "LOGGER", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                               .initializer("$T.getLogger($S)", LOGGER_FACTORY_CLASS, declarations.superClass.toString())
                               .build());
      router.addMethod(MethodSpec.methodBuilder("logger")
                                 .addAnnotation(Override.class)
                                 .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                                 .returns(Logger.class)
                                 .addStatement("return LOGGER")
                                 .build());
    }
    addRouteFields(router, declarations);
    addRouteHandlers(router, declarations, isDevelopmentEnvironment);
    return JavaFile.builder(classname.packageName(), router.build()).skipJavaLangImports(true).build();
  }

  void addRouteFields(
      final TypeSpec.Builder router,
      final EndPointSpec declarations)
  {
    for (final HttpVerb httpVerb : HttpVerb.values()) {
      final var routes = declarations.routesByVerb.get(httpVerb);
      if (no(routes)) {
        continue;
      }

      String last = null;
      for (final var route : routes) {
        if (!route.pattern.equals(last)) {
          final var routeField = route.makeField();
          if (routeField != null) {
            router.addField(routeField);
            last = route.pattern;
          }
        }
      }
    }
  }

  void addRouteHandlers(
      final TypeSpec.Builder router,
      final EndPointSpec declarations,
      boolean isDevelopmentEnvironment)
  {
    for (final var httpVerb : HttpVerb.values()) {
      final var routes = declarations.routesByVerb.get(httpVerb);

      if (no(routes)) {
        if (isDevelopmentEnvironment) {
          overrideVerbHandlerOnDevelopmentEnvironment(httpVerb);
        }
      } else {
        router.addMethod(overrideVerbHandler(httpVerb, routes, isDevelopmentEnvironment));
      }
    }
  }

  MethodSpec overrideVerbHandlerOnDevelopmentEnvironment(final HttpVerb httpVerb)
  {
    final MethodSpec.Builder httpVerbHandler = MethodSpec
                                                   .methodBuilder(httpVerb.handler)
                                                   .addAnnotation(Override.class)
                                                   .addModifiers(Modifier.PUBLIC)
                                                   .addParameter(HttpServletRequest.class, "request", Modifier.FINAL)
                                                   .addParameter(HttpServletResponse.class, "response", Modifier.FINAL)
                                                   .addException(ServletException.class)
                                                   .addException(IOException.class);
    httpVerbHandler.addStatement("response.setHeader(\"Access-Control-Allow-Origin\", \"*\")");
    httpVerbHandler.addStatement("super.$L(request, response)", httpVerb.handler);
    return httpVerbHandler.build();
  }

  MethodSpec overrideVerbHandler(
      final HttpVerb httpVerb,
      final List<Route> routes,
      boolean isDevelopmentEnvironment)
  {
    final var httpVerbHandler = MethodSpec
                                    .methodBuilder(httpVerb.handler)
                                    .addAnnotation(Override.class)
                                    .addModifiers(Modifier.PUBLIC)
                                    .addParameter(HttpServletRequest.class, "request", Modifier.FINAL)
                                    .addParameter(HttpServletResponse.class, "response", Modifier.FINAL)
                                    .addException(ServletException.class)
                                    .addException(IOException.class);
    for (final var route : routes) {
      final var ifMatchesRoute = route.makeMatcher(httpVerbHandler);
      addHandle(ifMatchesRoute, route);
      httpVerbHandler.endControlFlow();
    }

    if (isDevelopmentEnvironment) {
      httpVerbHandler.addStatement("response.setHeader(\"Access-Control-Allow-Origin\", \"*\")");
    }
    httpVerbHandler.addStatement("$L(request, response)", httpVerb.unhandled);
    return httpVerbHandler.build();
  }

  boolean hasDynamicRoutes(final Iterable<Route> routes)
  {
    for (final Route r : routes) {
      if (r.isParameterized()) {
        return true;
      }
    }
    return false;
  }

  private String paramsInit(final Iterable<Route> routes)
  {
    final int maxParametersCount = maxParametersCountAt(routes);
    switch (maxParametersCount) {
    case 1:
      return "null";
    case 2:
      return "null, null";
    case 3:
      return "null, null, null";
    case 4:
      return "null, null, null, null";
    default: {
      final int capacity = 4 * maxParametersCount + 2 * (maxParametersCount - 1);
      final var params = new StringBuilder(capacity).append("null, null, null, null");
      for (int i = 4; i < maxParametersCount; i++) {
        params.append(", null");
      }
      return params.toString();
    }
    }
  }

  private int maxParametersCountAt(final Iterable<Route> routes)
  {
    int maxParametersCount = 0;
    for (final Route r : routes) {
      if (r.isParameterized()) {
        if (r.parametersCount() > maxParametersCount) {
          maxParametersCount = r.parametersCount();
        }
      }
    }
    return maxParametersCount;
  }

  void addHandle(
      final MethodSpec.Builder control,
      final Route route)
  {
    if (route.hasRoleConstrains()) {
      control.beginControlFlow("if (!" + route.rolesExpr() + ')', (Object[]) route.roles)
             .addStatement("notAuthorized(response)")
             .addStatement("return")
             .endControlFlow();
    }
    control.addStatement("$L(request, response)", route.handler);
    control.addStatement("return");
  }

  private boolean no(final List<Route> routes) {
    return routes == null || routes.isEmpty();
  }
}
