/*
 * Copyright (C) 2021, 2022, THL A29 Limited, a Tencent company. All rights reserved.
 * Copyright (c) 2024 Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package compiler.vectorapi;

import java.lang.foreign.MemorySegment;
import jdk.incubator.vector.*;
import java.nio.ByteOrder;

/*
 * @test
 * @enablePreview
 * @bug 8262998
 * @summary Vector API intrinsincs should not modify IR when bailing out
 * @modules jdk.incubator.vector
 * @run main/othervm -Xbatch -XX:+IgnoreUnrecognizedVMOptions -XX:UseAVX=1
 *                   -XX:-TieredCompilation compiler.vectorapi.TestIntrinsicBailOut
 */

/*
 * @test
 * @enablePreview
 * @bug 8317299
 * @summary Vector API intrinsincs should handle JVM state correctly whith late inlining when compiling with -InlineUnsafeOps
 * @modules jdk.incubator.vector
 * @requires vm.cpu.features ~= ".*avx512.*"
 * @run main/othervm -Xcomp -XX:+UnlockDiagnosticVMOptions -XX:-InlineUnsafeOps -XX:+IgnoreUnrecognizedVMOptions -XX:UseAVX=3
 *                   -XX:CompileCommand=compileonly,compiler.vectorapi.TestIntrinsicBailOut::test -XX:CompileCommand=quiet
 *                   -XX:-TieredCompilation compiler.vectorapi.TestIntrinsicBailOut
 */


public class TestIntrinsicBailOut {
  static final VectorSpecies<Double> SPECIES256 = DoubleVector.SPECIES_256;
  static byte[] a = new byte[512];
  static byte[] r = new byte[512];
  static MemorySegment msa = MemorySegment.ofArray(a);
  static MemorySegment msr = MemorySegment.ofArray(r);

  static void test() {
    DoubleVector av = DoubleVector.fromMemorySegment(SPECIES256, msa, 0, ByteOrder.BIG_ENDIAN);
    av.intoMemorySegment(msr, 0, ByteOrder.BIG_ENDIAN);

    DoubleVector bv = DoubleVector.fromMemorySegment(SPECIES256, msa, 32, ByteOrder.LITTLE_ENDIAN);
    bv.intoMemorySegment(msr, 32, ByteOrder.LITTLE_ENDIAN);
  }

  public static void main(String[] args) {
    for (int i = 0; i < 15000; i++) {
      test();
    }
    System.out.println(r[0] + r[32]);
  }
}
