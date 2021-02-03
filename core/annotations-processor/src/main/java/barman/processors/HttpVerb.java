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
package barman.processors;

import javax.lang.model.element.ExecutableElement;

enum HttpVerb
{
  GET("doGet", "unhandledGet") {
    @Override String getPath(final ExecutableElement method)
    {
      final var metadata = method.getAnnotation(barman.web.GET.class);
      if (metadata == null) {
        return null;
      } else {
        return metadata.value();
      }
    }
  },
  POST("doPost", "unhandledPost") {
    @Override String getPath(final ExecutableElement method)
    {
      final var metadata = method.getAnnotation(barman.web.POST.class);
      if (metadata == null) {
        return null;
      } else {
        return metadata.value();
      }
    }
  },
  PUT("doPut", "unhandledPut") {
    @Override String getPath(final ExecutableElement method)
    {
      final var metadata = method.getAnnotation(barman.web.PUT.class);
      if (metadata == null) {
        return null;
      } else {
        return metadata.value();
      }
    }
  },
  DELETE("doDelete", "unhandledDelete") {
    @Override String getPath(final ExecutableElement method)
    {
      final var metadata = method.getAnnotation(barman.web.DELETE.class);
      if (metadata == null) {
        return null;
      } else {
        return metadata.value();
      }
    }
  };

  final String handler;
  final String unhandled;

  HttpVerb(
      final String handler,
      final String unhandled)
  {
    this.handler = handler;
    this.unhandled = unhandled;
  }

  abstract String getPath(ExecutableElement method);
}
