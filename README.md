# ToMiCompiler4J

> Copyright &copy; Tony's Studio 2023
>
> Java implementation of [ToMiCompiler](https://github.com/Lord-Turmoil/ToMiCompiler).

---

## DISCLAIMER

DO NOT COPY IT DIRECTLY! We invite your references, but don't copy any parts to your projects.

---

As the successor of [ToMiCompiler](https://github.com/Lord-Turmoil/ToMiCompiler), it completes LLVM generation and MIPS generation, and some basic optimizations. For the documentation, see that repository instead.

## About the Branches

For lab, see `task--` branches. For the final exam, see `final-exam` branch.

## About the Final Exam

Well, the final exam is quite easy, which only involves two modifications in grammar.

First, we can now declare variable in `ForInitStmt`.

```
ForStmt -> for '(' [ForInitStmt] ';' [Cond] ';' [ForStepStmt] ')' Stmt
ForInitStmt -> LVal '=' Exp
ForInitStmt -> BType VarDef
```

Second, we have a new wired binary operator `**`, that `a ** b = (a + b) ^ b`. And this is added to `MulExp`.

```
MulExp -> UnaryExp
MulExp -> MulExp ( '*' | '/' | '%' | '**' ) Unary
```

> One thing to notice is that, for this operator, `b` is guaranteed to be a compile-time constant, which makes it even simpler. Since our compiler will replace constants at compile time already, so it is not a problem, just throw an error if it is not deterministic.

So, every thing is clear. We have to modify our compiler in the following layers.

- Lexicography: Add support for symbol `**`.
- Syntax: Add support for grammar `ForInitStmt` and `MulExp`.
- Semantic: Add branch for `ForInitStmt`, and add support for `**` evaluation.
- IR: Add LLVM generation for `**` operator.

Because of the existence of IR, we don't need to modify our backend MIPS generator. Good luck. 🫡
