package com.example.common;

/**
 * Имя процедуры задаётся через @Component("procedureName").
 */
public interface ProcedureExecutor<P, R> {
    R execute(P parameters);
}