package com.example.client_1.service.procedure;

import java.util.Map;

/**
 * Имя процедуры задаётся через @Component("procedureName").
 */
public interface ProcedureExecutor {

    /**
     * Выполняет процедуру с заданными параметрами
     *
     * @param parameters параметры процедуры из ProcedureRequest
     * @return результат выполнения процедуры
     */
    Map<String, Object> execute(Map<String, Object> parameters);
}
