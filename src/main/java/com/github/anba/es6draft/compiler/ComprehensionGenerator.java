/**
 * Copyright (c) André Bargull
 * Alle Rechte vorbehalten / All Rights Reserved.  Use is subject to license terms.
 *
 * <https://github.com/anba/es6draft>
 */
package com.github.anba.es6draft.compiler;

import static com.github.anba.es6draft.compiler.BindingInitializationGenerator.BindingInitialization;
import static com.github.anba.es6draft.semantics.StaticSemantics.BoundNames;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.anba.es6draft.ast.Comprehension;
import com.github.anba.es6draft.ast.ComprehensionFor;
import com.github.anba.es6draft.ast.ComprehensionIf;
import com.github.anba.es6draft.ast.ComprehensionQualifier;
import com.github.anba.es6draft.ast.Expression;
import com.github.anba.es6draft.ast.Node;
import com.github.anba.es6draft.ast.scope.BlockScope;
import com.github.anba.es6draft.ast.scope.Name;
import com.github.anba.es6draft.compiler.Labels.TempLabel;
import com.github.anba.es6draft.compiler.StatementGenerator.Completion;
import com.github.anba.es6draft.compiler.assembler.Jump;
import com.github.anba.es6draft.compiler.assembler.MethodName;
import com.github.anba.es6draft.compiler.assembler.MutableValue;
import com.github.anba.es6draft.compiler.assembler.Type;
import com.github.anba.es6draft.compiler.assembler.Variable;
import com.github.anba.es6draft.runtime.DeclarativeEnvironmentRecord;
import com.github.anba.es6draft.runtime.LexicalEnvironment;
import com.github.anba.es6draft.runtime.internal.ScriptIterator;

/**
 * <h1>12 ECMAScript Language: Expressions</h1><br>
 * <h2>12.2 Primary Expression</h2>
 * <ul>
 * <li>Array Comprehension
 * </ul>
 */
abstract class ComprehensionGenerator extends DefaultCodeGenerator<Void> {
    private static final class Methods {
        // class: Iterator
        static final MethodName Iterator_hasNext = MethodName.findInterface(Types.Iterator, "hasNext",
                Type.methodType(Type.BOOLEAN_TYPE));

        static final MethodName Iterator_next = MethodName.findInterface(Types.Iterator, "next",
                Type.methodType(Types.Object));

        // class: IteratorOperations
        static final MethodName IteratorOperations_iterate = MethodName.findStatic(Types.IteratorOperations, "iterate",
                Type.methodType(Types.ScriptIterator, Types.Object, Types.ExecutionContext));
    }

    private Iterator<Node> elements;
    private Iterator<Variable<ScriptIterator<?>>> iterators;

    protected ComprehensionGenerator(CodeGenerator codegen) {
        super(codegen);
    }

    @Override
    protected Void visit(Node node, CodeVisitor mv) {
        throw new IllegalStateException(String.format("node-class: %s", node.getClass()));
    }

    /**
     * Runtime Semantics: ComprehensionEvaluation
     * <p>
     * ComprehensionTail : AssignmentExpression
     */
    @Override
    protected abstract Void visit(Expression node, CodeVisitor mv);

    /**
     * Runtime Semantics: ComprehensionEvaluation
     */
    @Override
    public Void visit(Comprehension node, CodeVisitor mv) {
        ArrayList<Node> list = new ArrayList<>(node.getList().size() + 1);
        list.addAll(node.getList());
        list.add(node.getExpression());
        elements = list.iterator();

        // Create variables early so they'll appear next to each other in the local variable map.
        ArrayList<Variable<ScriptIterator<?>>> iters = new ArrayList<>();
        for (ComprehensionQualifier e : node.getList()) {
            if (e instanceof ComprehensionFor) {
                Variable<ScriptIterator<?>> iter = mv.newVariable("iter", ScriptIterator.class).uncheckedCast();
                iters.add(iter);
            }
        }
        iterators = iters.iterator();

        // Start generating code.
        elements.next().accept(this, mv);

        return null;
    }

    /**
     * Runtime Semantics: ComprehensionComponentEvaluation
     * <p>
     * ComprehensionIf : if ( AssignmentExpression )
     */
    @Override
    public Void visit(ComprehensionIf node, CodeVisitor mv) {
        Jump ifFalse = new Jump();

        /* steps 1-4 */
        testExpressionBailout(node.getTest(), ifFalse, mv);
        /* steps 5-6 */
        elements.next().accept(this, mv);
        mv.mark(ifFalse);

        return null;
    }

    /**
     * Runtime Semantics: ComprehensionComponentEvaluation
     * <p>
     * ComprehensionFor : for ( ForBinding of AssignmentExpression )
     */
    @Override
    public Void visit(ComprehensionFor node, CodeVisitor mv) {
        Jump lblTest = new Jump(), lblLoop = new Jump();
        Variable<ScriptIterator<?>> iter = iterators.next();

        /* steps 1-2 */
        expressionBoxed(node.getExpression(), mv);

        /* steps 3-4 */
        mv.loadExecutionContext();
        mv.lineInfo(node.getExpression());
        mv.invoke(Methods.IteratorOperations_iterate);
        mv.store(iter);

        /* step 5 (not applicable) */

        /* step 6 */
        mv.nonDestructiveGoTo(lblTest);

        /* steps 6.d-e */
        mv.mark(lblLoop);
        mv.load(iter);
        mv.lineInfo(node);
        mv.invoke(Methods.Iterator_next);

        /* steps 6.f-j */
        BlockScope scope = node.getScope();
        if (scope.isPresent()) {
            mv.enterVariableScope();
            Variable<LexicalEnvironment<DeclarativeEnvironmentRecord>> env = mv
                    .newVariable("env", LexicalEnvironment.class).uncheckedCast();
            Variable<DeclarativeEnvironmentRecord> envRec = mv.newVariable("envRec",
                    DeclarativeEnvironmentRecord.class);

            // stack: [nextValue] -> []
            Variable<Object> nextValue = mv.newVariable("nextValue", Object.class);
            mv.store(nextValue);

            newDeclarativeEnvironment(scope, mv);
            mv.store(env);
            getEnvRec(env, envRec, mv);

            for (Name name : BoundNames(node.getBinding())) {
                BindingOp<DeclarativeEnvironmentRecord> op = BindingOp.of(envRec, name);
                op.createMutableBinding(envRec, name, false, mv);
            }
            BindingInitialization(codegen, envRec, node.getBinding(), nextValue, mv);

            mv.load(env);
            pushLexicalEnvironment(mv);

            mv.exitVariableScope();
        } else {
            // stack: [nextValue] -> []
            mv.pop();
        }

        /* step 6.k */
        mv.enterScope(node);
        new IterationGenerator<ComprehensionFor>(codegen) {
            @Override
            protected Completion iterationBody(ComprehensionFor node, Variable<ScriptIterator<?>> iterator,
                    CodeVisitor mv) {
                elements.next().accept(ComprehensionGenerator.this, mv);
                return Completion.Normal;
            }

            @Override
            protected MutableValue<Object> enterIteration(ComprehensionFor node, CodeVisitor mv) {
                return mv.enterIteration();
            }

            @Override
            protected List<TempLabel> exitIteration(ComprehensionFor node, CodeVisitor mv) {
                return mv.exitIteration();
            }
        }.generate(node, iter, mv);
        mv.exitScope();

        /* steps 6.l-m */
        if (scope.isPresent()) {
            popLexicalEnvironment(mv);
        }

        /* steps 6.a-c */
        mv.mark(lblTest);
        mv.load(iter);
        mv.lineInfo(node);
        mv.invoke(Methods.Iterator_hasNext);
        mv.ifne(lblLoop);

        return null;
    }
}
