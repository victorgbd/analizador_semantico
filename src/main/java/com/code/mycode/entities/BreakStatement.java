package com.code.mycode.entities;

/**
 * A break statement.
 */
public class BreakStatement extends Statement {

    @Override
    public void analyze(AnalysisContext context) {
        if (!context.isInLoop()) {
            context.error("break_not_in_loop");
        }
    }
}
