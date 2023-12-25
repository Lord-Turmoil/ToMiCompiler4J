# ToMiCompiler4J

> Java implementation of [ToMiCompiler](https://github.com/Lord-Turmoil/ToMiCompiler). 😡

---

## DISCLAIMER

DO NOT COPY IT DIRECTLY! We invite your references, but don't copy any parts to your projects.

---

As the successor of [ToMiCompiler](https://github.com/Lord-Turmoil/ToMiCompiler), it completes LLVM generation and MIPS generation, and some basic optimizations. For the documentation, see that repository instead.

## About Branches

For lab, see `task--` branches. For the final exam, see `final-exam` branch.

## About Final Exam

Well, the final exam is quite easy, which only involve two modification in grammar.

First, we can now declare variable in `ForInitStmt`.

```
ForStmt -> for '(' [ForInitStmt] ';' [Cond] ';' [ForStepStmt] ')' Stmt
ForInitStmt -> LVal '=' Exp
ForInitStmt -> BType VarDef
```

Second, we have a new wired binary operator `**`, that $ a\space **\space b = (a + b) ^ b $. And this is added to `MulExp`.

```
MulExp -> UnaryExp
MulExp -> MulExp ( '*' | '/' | '%' | '**' ) Unary
```

So, every thing is clear. We have to modify our compiler in all layers.

- Lexicography: Add support for symbol `**`.
- Syntax: Add support for grammar `ForInitStmt` and `MulExp`.
- Semantic: Add branch for `ForInitStmt`, and add support for `**` evaluation.
- IR: Add LLVM generation for `**` operator.

Because of the existence of IR, we don't need to modify our backend MIPS generator.
