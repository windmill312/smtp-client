package com.windmill312.smtp.client.multiplexed.statemachine;

import com.google.common.collect.Table;
import com.windmill312.smtp.client.multiplexed.enums.Condition;
import com.windmill312.smtp.client.multiplexed.enums.Step;

public class StateMachine {
    private Table<Step, Condition, Process> table;

    StateMachine setTable(Table<Step, Condition, Process> table) {
        this.table = table;
        return this;
    }

    public void raise(Step step, Condition status, StateMachineScopeHolder contextHolder) {
        new StateMachineScopeImpl(contextHolder).enhance(step, status);
    }

    private StateMachineScopeImpl createContext(StateMachineScopeHolder contextHolder) {
        return new StateMachineScopeImpl(contextHolder);
    }

    private class StateMachineScopeImpl
            implements StateMachineScope {

        private final StateMachineScopeHolder contextHolder;

        private StateMachineScopeImpl(StateMachineScopeHolder contextHolder) {
            this.contextHolder = contextHolder;
        }

        @Override
        public void enhance(Step step, Condition condition) {
            final Process process = table.get(step, condition);
            process.execute(createContext(contextHolder));
        }

        @Override
        public StateMachineScopeHolder getContextHolder() {
            return contextHolder;
        }
    }
}
