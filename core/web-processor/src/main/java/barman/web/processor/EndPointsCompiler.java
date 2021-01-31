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
package barman.web.processor;

import barman.web.Endpoint;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import org.slf4j.Logger;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("barman.web.Endpoint")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedOptions("environment")
public final class EndPointsCompiler
    extends AnnotationProcessor
{
  private final RoutersCodeBuilder routerBuilder;
  private TypeMirror loggerType;

  public EndPointsCompiler()
  {
    this(LocalDate.now().toString(), new RoutersCodeBuilder());
  }

  EndPointsCompiler(
      final String today,
      final RoutersCodeBuilder routerBuilder)
  {
    super(today);
    this.routerBuilder = routerBuilder;
  }

  @Override public boolean process(
      final Set<? extends TypeElement> annotations,
      final RoundEnvironment roundEnvironment)
  {
    final var isDevelopmentEnvironment = !"production".equals(option("environment"));

    for (final var endpointClass : getEndpoints(roundEnvironment)) {
      final var declarations = EndPointSpec.builder(ClassName.get(endpointClass), this.today);
      final var interpreter = new RoutesReader(messager(), declarations);

      if (interpreter.buildRoutesFor(endpointClass)) {
        final String impl = endpointClass.getQualifiedName().toString() + "__barmanImpl";
        declarations.implClass(impl);
        declarations.loggerDefined(hasLogger(endpointClass));
        final EndPointSpec routes = declarations.build();
        generateJavaCode(routes, isDevelopmentEnvironment);
      }
    }
    return true;
  }

  private boolean hasLogger(final TypeElement aClass)
  {
    final var methods = ElementFilter.methodsIn(elements().getAllMembers(aClass));
    for (final ExecutableElement method : methods) {
      final String methodName = method.getSimpleName().toString();
      final Set<Modifier> modifiers = method.getModifiers();
      if ("logger".equals(methodName) && modifiers.contains(Modifier.PROTECTED) && isLoggerType(method.getReturnType())) {
        return true;
      }
    }
    return false;
  }

  private boolean isLoggerType(final TypeMirror aType)
  {
    return types().isSameType(loggerType(), aType);
  }

  private TypeMirror loggerType()
  {
    if (loggerType == null) {
      loggerType = elements().getTypeElement(Logger.class.getCanonicalName()).asType();
    }
    return loggerType;
  }

  private List<TypeElement> getEndpoints(final RoundEnvironment roundEnvironment)
  {
    final Set<? extends Element> endpoints = roundEnvironment.getElementsAnnotatedWith(Endpoint.class);
    if (endpoints.isEmpty()) {
      return List.of();
    } else {
      var result = new java.util.ArrayList<TypeElement>();
      for (final Element e : endpoints) {
        final TypeElement endpoint = (TypeElement) e;
        if (endpoint.getKind() != ElementKind.CLASS) {
          error(e, "only classes can be marked as @barman.endpoint");
          result = null;
        } else {
          if (result != null) {
            result.add(endpoint);
          }
        }
      }
      if (result == null) {
        return List.of();
      }
      result.trimToSize();
      return result;
    }
  }

  private void generateJavaCode(
      final EndPointSpec routes,
      boolean isDevelopmentEnvironment)
  {
    final var routerCode = this.routerBuilder.buildJavaCode(routes, isDevelopmentEnvironment);
    try {
      routerCode.writeTo(filer());
    } catch (final IOException e) {
      error("could not write WebApp code, reason: " + e.getMessage());
    }
  }
}
