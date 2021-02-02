/*
 *
 *  The MIT License
 *
 *  Copyright (c) 2021 Juan J. GIL (matero _at_ gmail _dot_ com)
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 */
package barman.web.processor;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class RoutesProcessingTest
{
  static final String GENERATION_DATE = "2017-02-23";

  final Compiler compiler = javac().withProcessors(new EndPointsCompiler(GENERATION_DATE, new RoutersCodeBuilder()));

  @Test void should_be_able_to_generate_barmanImpl_for_API_endpoints()
  {
    final Compilation compilation = compiler.compile(JavaFileObjects.forResource("Tasks.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("test.Tasks__barmanImpl")
        .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/endpoints/Tasks__barmanImpl.java"));
  }

  @Test void should_be_able_to_generate_barmanImpl_for_ADMIN_endpoints()
  {
    final Compilation compilation = compiler.compile(JavaFileObjects.forResource("AdminTasks.java"));
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("test.Tasks__barmanImpl")
        .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/endpoints/AdminTasks__barmanImpl.java"));
  }

  @Test void should_be_able_to_generate_barmanImpl_for_restricted_roles_endpoint()
  {
    final Compilation compilation = compiler.compile(
        JavaFileObjects.forResource("Roles.java")
    );
    assertThat(compilation).succeeded();
    assertThat(compilation)
        .generatedSourceFile("test.Tasks__barmanImpl")
        .hasSourceEquivalentTo(JavaFileObjects.forResource("generated/endpoints/Roles__barmanImpl.java"));
  }
}
