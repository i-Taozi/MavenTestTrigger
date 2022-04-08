/*
 * Copyright (c) 2001-2017, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Additionally licensed with:
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.spf4j.zel.vm;

import com.google.common.base.Strings;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Function;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import org.spf4j.base.CharSequences;
import org.spf4j.base.Pair;
import org.spf4j.base.Throwables;
import org.spf4j.base.TimeSource;
import org.spf4j.zel.instr.Instruction;
import org.spf4j.zel.instr.var.ARRAY;
import org.spf4j.zel.instr.var.DECODE;
import org.spf4j.zel.instr.var.INT;
import org.spf4j.zel.instr.var.LOG;
import org.spf4j.zel.instr.var.MAX;
import org.spf4j.zel.instr.var.MIN;
import org.spf4j.zel.instr.var.NVL;
import org.spf4j.zel.instr.var.OUT;
import org.spf4j.zel.instr.var.RANDOM;
import org.spf4j.zel.instr.var.SQRT;
import org.spf4j.zel.vm.ParsingContext.Location;
import org.spf4j.zel.instr.SymbolRef;
import org.spf4j.zel.instr.var.LIST;
import org.spf4j.zel.instr.var.MAP;

/**
 * <p>
 * A ZEL program (function)</p>
 *
 * This is a Turing machine a Program will always be pretty much an array of operations (instructions).
 *
 * @author zoly
 * @version 1.0
 *
 */
@Immutable
public final class Program implements Serializable {

  private static final long serialVersionUID = 1L;

  private static final MemoryBuilder ZEL_GLOBAL_FUNC;

  private static volatile boolean terminated = false;

  static {
    ZEL_GLOBAL_FUNC = new MemoryBuilder();
    ZEL_GLOBAL_FUNC.addSymbol("out", OUT.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("sqrt", SQRT.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("int", INT.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("log", LOG.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("log10", LOG.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("min", MIN.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("max", MAX.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("array", ARRAY.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("list", LIST.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("map", MAP.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("random", RANDOM.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("channel", Channel.Factory.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("EOF", Channel.EOF);
    ZEL_GLOBAL_FUNC.addSymbol("decode", DECODE.INSTANCE);
    ZEL_GLOBAL_FUNC.addSymbol("nvl", NVL.INSTANCE);
  }

  public enum Type {
    DETERMINISTIC, NONDETERMINISTIC
  }

  public enum ExecutionType {
    SYNC,
    ASYNC
  }

  private final Type type;
  private final ExecutionType execType;
  private final int id; // program ID, unique ID identifying the program

  private final Instruction[] instructions;
  private final Location[] debug;
  private final String source;
  private final boolean hasDeterministicFunctions;
  private final Object[] globalMem;
  private final int localMemSize;
  private final Map<String, Integer> localSymbolTable;
  private final Map<String, Integer> globalSymbolTable;
  private final String name;
  private final String[] parameterNames;

//CHECKSTYLE:OFF
  Program(final String name, final Map<String, Integer> globalTable, final Object[] globalMem,
          final Map<String, Integer> localTable,
          @Nonnull final Instruction[] objs, final Location[] debug,
          final String source, @Nonnegative final int start,
          @Nonnegative final int end, final Type progType, final ExecutionType execType,
          final boolean hasDeterministicFunctions, final String... parameterNames) throws CompileException {
    //CHECKSTYLE:ON
    this(name, globalTable, globalMem, buildLocalSymTable(objs, parameterNames, end - start, globalTable, localTable),
            java.util.Arrays.copyOfRange(objs, start, end),
            debug, source, progType, execType, hasDeterministicFunctions, parameterNames);
  }

  //CHECKSTYLE:OFF
  Program(final String name, final Map<String, Integer> globalTable, final Object[] globalMem,
          final Map<String, Integer> localTable,
          @Nonnull final Instruction[] instructions, final Location[] debug, final String source,
          final Type progType, final ExecutionType execType,
          final boolean hasDeterministicFunctions, final String... parameterNames) {
    //CHECKSTYLE:ON
    this.globalMem = globalMem;
    this.instructions = instructions;
    this.type = progType;
    this.id = ProgramBuilder.generateID();
    this.execType = execType;
    this.hasDeterministicFunctions = hasDeterministicFunctions;
    this.localSymbolTable = localTable;
    this.localMemSize = localSymbolTable.size();
    this.globalSymbolTable = globalTable;
    this.debug = debug;
    this.source = source;
    this.name = name;
    this.parameterNames = parameterNames;
  }

  public Program async() {
    return new Program(name, globalSymbolTable, globalMem,
            localSymbolTable, instructions, debug, source, type, ExecutionType.ASYNC,
            hasDeterministicFunctions, parameterNames);
  }

  public static MemoryBuilder getGlobalMemoryBuilder() {
    return ZEL_GLOBAL_FUNC.copy();
  }

  Location[] getDebug() {
    return debug;
  }

  public String getSource() {
    return source;
  }

  public String getName() {
    return name;
  }

  public String[] getParameterNames() {
    return parameterNames.clone();
  }

  String[] getParameterNamesInternal() {
    return parameterNames.clone();
  }

  private static Map<String, Integer> buildLocalSymTable(final Instruction[] instructions,
          final String[] parameterNames1,
          final int length, final Map<String, Integer> globalTable,
          final Map<String, Integer> addTo) throws CompileException {
    final int addToSize = addTo.size();
    Map<String, Integer> symbolTable = new HashMap<>(addToSize + parameterNames1.length);
    symbolTable.putAll(addTo);
    // allocate program params
    int i = addToSize;
    for (String param : parameterNames1) {
      Integer existing = symbolTable.put(param, i++);
      if (existing != null) {
        throw new CompileException("Duplicate parameter defined: " + param);
      }
    }
    // allocate variables used in Program
    for (int j = 0; j < length; j++) {
      Instruction code = instructions[j];
      if (code instanceof SymbolRef) {
        String ref = ((SymbolRef) code).getSymbol();
        Integer idxr = symbolTable.get(ref);
        if (idxr == null) {
          idxr = globalTable.get(ref);
          if (idxr == null) {
            idxr = i++;
            symbolTable.put(ref, idxr);
          }
        }
      }
    }
    return symbolTable;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Map<String, Integer> getGlobalSymbolTable() {
    return globalSymbolTable;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Map<String, Integer> getLocalSymbolTable() {
    return localSymbolTable;
  }

  public int getLocalMemSize() {
    return localMemSize;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Object[] getGlobalMem() {
    return globalMem;
  }

  @Override
  @CheckReturnValue
  public boolean equals(final Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Program other = (Program) obj;
    return (this.id == other.id);
  }

  @Override
  @CheckReturnValue
  public int hashCode() {
    return this.id;
  }

  public boolean hasDeterministicFunctions() {
    return hasDeterministicFunctions;
  }

  /**
   * @param i - inst address.
   * @return the instruction.
   */
  @CheckReturnValue
  public Instruction get(final int i) {
    return instructions[i];
  }

  @CheckReturnValue
  public Instruction[] getCode() {
    return instructions.clone();
  }

  @CheckReturnValue
  Instruction[] getCodeInternal() {
    return instructions;
  }

  @CheckReturnValue
  Location[] getDebugInfoInternal() {
    return debug;
  }

  @CheckReturnValue
  public Location[] getDebugInfo() {
    return debug.clone();
  }

  @CheckReturnValue
  public int size() {
    return instructions.length;
  }

  public ExecutionType getExecType() {
    return execType;
  }

  @Nonnull
  public static Program compile(@Nonnull final String zExpr, @Nonnull final String... varNames)
          throws CompileException {
    return compile("anonFunc", "String", new StringReader(zExpr), varNames);
  }

  @Nonnull
  public static Program compile(@Nonnull final String source,
          @Nonnull final String name,
          @Nonnull final Reader zExpr, @Nonnull final String... varNames)
          throws CompileException {

    ParsingContext cc = new CompileContext(ZEL_GLOBAL_FUNC.copy());
    try {
      ZCompiler.compile(source, zExpr, cc);
    } catch (TokenMgrError | ParseException err) {
      throw new CompileException(err);
    }
    return RefOptimizer.INSTANCE.apply(cc.getProgramBuilder().toProgram(name, source, varNames));
  }

  @Nonnull
  public static <T> ZelPredicate<T> compilePredicate(@Nonnull final CharSequence zExpr, @Nonnull final String varName)
          throws CompileException {
    ParsingContext cc = new CompileContext(ZEL_GLOBAL_FUNC.copy());
    try {
      ZCompiler.compilePredicate("CharSquence", CharSequences.reader(zExpr), cc);
    } catch (TokenMgrError | ParseException err) {
      throw new CompileException(err);
    }
    Program result = RefOptimizer.INSTANCE.apply(cc.getProgramBuilder().toProgram("anonPredicate",
            "CharSquence", varName));
    return result.toPredicate(zExpr.toString());
  }

  public static Program compile(@Nonnull final String zExpr,
          final Map<String, Integer> localTable,
          final Object[] globalMem,
          final Map<String, Integer> globalTable,
          @Nonnull final String... varNames)
          throws CompileException {
    return compile("String", "anonFunc", new StringReader(zExpr), localTable, globalMem, globalTable, varNames);
  }

  public static Program compile(@Nonnull final String source,
          @Nonnull final String name,
          @Nonnull final Reader zExpr,
          final Map<String, Integer> localTable,
          final Object[] globalMem,
          final Map<String, Integer> globalTable,
          @Nonnull final String... varNames)
          throws CompileException {

    ParsingContext cc = new CompileContext(new MemoryBuilder(
            new ArrayList<>(Arrays.asList(globalMem)), globalTable));
    try {
      ZCompiler.compile(source, zExpr, cc);
    } catch (TokenMgrError | ParseException err) {
      throw new CompileException(err);
    }
    return cc.getProgramBuilder().toProgram(name, source, varNames, localTable);
  }

  public Object execute() throws ExecutionException, InterruptedException {
    return execute(ProcessIOStreams.DEFAULT);
  }

  public Object execute(final Object... args) throws ExecutionException, InterruptedException {
    return execute(ProcessIOStreams.DEFAULT, args);
  }

  public <T> ZelPredicate<T> toPredicate(final String toString) {
    if (parameterNames.length != 1) {
      throw new UnsupportedOperationException("Not a predicate " + this);
    }
    String paramName = parameterNames[0];
    return new ZelPredicate<T>() {
      @Override
      public boolean test(final T arg) {
        try {
          return (Boolean) execute((Object) arg);
        } catch (ExecutionException | InterruptedException ex) {
          throw new RuntimeException(ex);
        }
      }

      @Override
      public String toString() {
        return toString;
      }

      @Override
      public String getZelExpression() {
        return toString;
      }

      @Override
      public String getParameterId() {
        return paramName;
      }
    };
  }

  public Object execute(@Nonnull final ExecutorService execService,
          final Object... args) throws ExecutionException, InterruptedException {
    return execute(new VMExecutor(execService), ProcessIOStreams.DEFAULT, args);
  }

  public Object executeSingleThreaded(final Object... args) throws ExecutionException, InterruptedException {
    return execute(null, ProcessIOStreams.DEFAULT, args);
  }

  public Object execute(@Nullable final VMExecutor execService,
          @Nullable final ProcessIO io,
          final Object... args)
          throws ExecutionException, InterruptedException {
    Object[] localMem = allocMem(args);
    final ExecutionContext ectx = new ExecutionContext(this, globalMem, localMem, io, execService);
    return execute(ectx);
  }

  Object[] allocMem(final Object... args) {
    Object[] localMem;
    final int lms = this.getLocalMemSize();
    if (args.length == lms) {
      localMem = args;
    } else {
      localMem = new Object[lms];
      System.arraycopy(args, 0, localMem, 0, args.length);
    }
    return localMem;
  }

  public Pair<Object, ExecutionContext> execute(@Nullable final VMExecutor execService,
          @Nullable final ProcessIO io,
          final ResultCache resultCache,
          final Object... args)
          throws ExecutionException, InterruptedException {
    Object[] localMem = allocMem(args);
    final ExecutionContext ectx = new ExecutionContext(this, globalMem, localMem,
            resultCache, io, execService);
    return Pair.of(execute(ectx), ectx);
  }

  public static Object executeSync(@Nonnull final ExecutionContext ectx) throws
          ExecutionException, InterruptedException {
    try {
      return ectx.call();
    } catch (SuspendedException ex) {
      throw new ExecutionException("Suspension not supported in sync calls " + ectx, ex);
    }
  }

  public static Object execute(@Nonnull final ExecutionContext ectx)
          throws ExecutionException, InterruptedException {
    Object result = ectx.executeSyncOrAsync();
    if (result instanceof Future) {
      return ((Future<Object>) result).get();
    } else {
      return result;
    }
  }

  public Object execute(final ProcessIO io, final Object... args)
          throws ExecutionException, InterruptedException {
    if (execType == ExecutionType.SYNC) {
      return execute((VMExecutor) null, io, args);
    } else {
      return execute(VMExecutor.Lazy.DEFAULT, io, args);
    }
  }

  /**
   *
   * This allows to run ZEL in an interactive mode
   *
   * @param args
   */
  @SuppressWarnings("checkstyle:regexp")
  public static void main(final String[] args) throws IOException, InterruptedException {
    System.out.println("ZEL Shell");
    Map<String, Integer> localSymTable = Collections.emptyMap();
    Pair<Object[], Map<String, Integer>> gmemPair = ZEL_GLOBAL_FUNC.build();
    Map<String, Integer> globalSymTable = gmemPair.getSecond();
    Object[] mem = new Object[]{};
    Object[] gmem = gmemPair.getFirst();
    ResultCache resCache = new SimpleResultCache();
    InputStreamReader inp = new InputStreamReader(System.in, StandardCharsets.UTF_8);
    BufferedReader br = new BufferedReader(inp);
    org.spf4j.base.Runtime.queueHookAtBeginning(new Runnable() {
      @Override
      public void run() {
        terminated = true;
        try {
          System.in.close();
        } catch (IOException ex) {
          // ignore.
        }
      }
    });
    System.out.println("zel>\n");
    while (!terminated) {
      String line = br.readLine();
      if (line == null) {
        break;
      }
      line = line.trim();
      if ("quit".equals(line)) {
        terminated = true;
      } else {
        try {
          final Program prog = Program.compile(line, localSymTable, gmem, globalSymTable).async();
          localSymTable = prog.getLocalSymbolTable();
          globalSymTable = prog.getGlobalSymbolTable();
          gmem = prog.getGlobalMem();
          long startTime = TimeSource.nanoTime();
          Pair<Object, ExecutionContext> res = prog.execute(
                  VMExecutor.Lazy.DEFAULT, ProcessIOStreams.DEFAULT, resCache, mem);
          long elapsed = TimeSource.nanoTime() - startTime;
          final Object result = res.getFirst();
          System.out.println("result> " + result);
          System.out.println("type> " + (result == null ? "none" : result.getClass()));
          System.out.println("executed in> " + elapsed + " ns");

          final ExecutionContext execCtx = res.getSecond();
          mem = execCtx.getMem();
          resCache = execCtx.getResultCache();
        } catch (CompileException ex) {
          System.out.println("Syntax Error:");
          Throwables.writeTo(ex, System.out, Throwables.PackageDetail.SHORT);
          System.out.println();
        } catch (ExecutionException ex) {
          System.out.println("Execution Error:");
          Throwables.writeTo(ex, System.out, Throwables.PackageDetail.SHORT);
          System.out.println();
        }
        System.out.println("zel>");
      }
    }
  }

  public String toAssemblyString() {
    StringBuilder result = new StringBuilder();
    result.append("Program: \n");
    int toPad = Integer.toString(instructions.length).length();
    for (int i = 0; i < instructions.length; i++) {
      Object obj = instructions[i];
      result.append(Strings.padEnd(Integer.toString(i), toPad, ' '));
      result.append(':');
      result.append(obj);
      result.append(',');
    }
    result.append("execType = ").append(this.execType).append('\n');
    result.append("type = ").append(this.type).append('\n');
    return result.toString();
  }

  @Override
  public String toString() {
    return name;
  }

  /**
   * @return the type
   */
  public Program.Type getType() {
    return type;
  }

  public boolean contains(final Class<? extends Instruction> instr) {
    Boolean res = itterate(new HasClass(instr));
    if (res == null) {
      return false;
    }
    return res;
  }

  @Nullable
  public <T> T itterate(final Function<Object, T> func) {
    for (Instruction code : instructions) {
      T res = func.apply(code);
      if (res != null) {
        return res;
      }
      for (Object param : code.getParameters()) {
        res = func.apply(param);
        if (res != null) {
          return res;
        }
        if (param instanceof Program) {
          res = ((Program) param).itterate(func);
        }
        if (res != null) {
          return res;
        }
      }
    }
    return null;
  }

  Instruction[] getInstructions() {
    return instructions;
  }

  static final class HasClass implements Function<Object, Boolean> {

    private final Class<? extends Instruction> instr;

    HasClass(final Class<? extends Instruction> instr) {
      this.instr = instr;
    }

    @Override
    @SuppressFBWarnings({"TBP_TRISTATE_BOOLEAN_PATTERN", "NP_BOOLEAN_RETURN_NULL"})
    @Nullable
    public Boolean apply(@Nonnull final Object input) {
      if (input.getClass() == instr) {
        return Boolean.TRUE;
      }
      return null;
    }
  }

}
