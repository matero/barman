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

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

public abstract class AnnotationProcessor
    extends AbstractProcessor
{
  /** text representation of the date when the code generation takes place. */
  protected final String today;

  protected AnnotationProcessor(final String today)
  {
    this.today = today;
  }

  protected final Messager messager()
  {
    return processingEnv.getMessager();
  }

  protected final Filer filer()
  {
    return processingEnv.getFiler();
  }

  protected final Elements elements()
  {
    return processingEnv.getElementUtils();
  }

  protected final Types types()
  {
    return processingEnv.getTypeUtils();
  }

  protected void error(
      final Element element,
      final Throwable failure)
  {
    message(Diagnostic.Kind.ERROR, message(failure), element);
  }

  protected void error(
      final Element element,
      final String errorMessage)
  {
    message(Diagnostic.Kind.ERROR, errorMessage, element);
  }

  protected final void info(
      final Element element,
      final String infoMessage,
      final Object... args)
  {
    info(element, String.format(infoMessage, args));
  }

  protected final void info(
      final String infoMessage,
      final Object... args)
  {
    message(Diagnostic.Kind.NOTE, String.format(infoMessage, args));
  }

  protected final void info(
      final Element element,
      final String infoMessage)
  {
    message(Diagnostic.Kind.NOTE, String.format("[%s] %s", getClass().getSimpleName(), infoMessage), element);
  }

  protected void error(final Throwable failure)
  {
    error(message(failure));
  }

  protected void error(final String message)
  {
    message(Diagnostic.Kind.ERROR, message);
  }

  /**
   Prints a message of the specified kind.

   @param kind the kind of message
   @param msg  the message, or an empty string if none
   */
  protected final void message(
      final Diagnostic.Kind kind,
      final CharSequence msg)
  {
    messager().printMessage(kind, msg);
  }

  /**
   Prints a message of the specified kind at the location of the element.

   @param kind the kind of message
   @param msg  the message, or an empty string if none
   @param e    the element to use as a position hint
   */
  protected final void message(
      final Diagnostic.Kind kind,
      final CharSequence msg,
      final Element e)
  {
    messager().printMessage(kind, msg, e);
  }

  /**
   Prints a message of the specified kind at the location of the annotation mirror of the annotated element.

   @param kind the kind of message
   @param msg  the message, or an empty string if none
   @param e    the annotated element
   @param a    the annotation to use as a position hint
   */
  protected final void message(
      final Diagnostic.Kind kind,
      final CharSequence msg,
      final Element e,
      final AnnotationMirror a)
  {
    messager().printMessage(kind, msg, e, a);
  }

  /**
   Prints a message of the specified kind at the location of the annotation value inside the annotation mirror of the annotated element.

   @param kind the kind of message
   @param msg  the message, or an empty string if none
   @param e    the annotated element
   @param a    the annotation containing the annotation value
   @param v    the annotation value to use as a position hint
   */
  protected final void message(
      final Diagnostic.Kind kind,
      final CharSequence msg,
      final Element e,
      final AnnotationMirror a,
      final AnnotationValue v)
  {
    messager().printMessage(kind, msg, e, a, v);
  }

  protected final String message(final Throwable t)
  {
    return t.getMessage() == null ? "unknown error" : t.getMessage();
  }

  protected String readSuperClassCannonicalName(final TypeMirror superClass)
  {
    return types().asElement(superClass).getSimpleName().toString();
  }

  protected final Object option(final String key)
  {
    if (key == null) {
      throw new NullPointerException("key");
    }
    if (processingEnv == null) {
      return null;
    }
    if (processingEnv.getOptions() == null) {
      return null;
    }
    return processingEnv.getOptions().get(key);
  }
}
