package com.windmill312.smtp.client.multiplexed.statemachine.processes;

import com.windmill312.smtp.client.multiplexed.enums.Condition;
import com.windmill312.smtp.client.multiplexed.enums.Step;
import com.windmill312.smtp.client.multiplexed.statemachine.Process;

import javax.annotation.Nonnull;

public class ProcessFactory {

    private final ConnectProcess connectProcess;
    private final ConnectReadProcess connectReadProcess;
    private final HeloWriteProcess heloWriteProcess;
    private final HeloReadProcess heloReadProcess;
    private final MailFromWriteProcess mailFromWriteProcess;
    private final MailFromReadProcess mailFromReadProcess;
    private final RcptToWriteProcess rcptToWriteProcess;
    private final RcptToReadProcess rcptToReadProcess;
    private final DataRequestWriteProcess dataRequestWriteProcess;
    private final DataRequestReadProcess dataRequestReadProcess;
    private final DataWriteProcess dataWriteProcess;
    private final DataReadProcess dataReadProcess;
    private final QuitWriteProcess quitWriteProcess;
    private final QuitReadProcess quitReadProcess;
    private final FinalProcess finalProcess;

    public ProcessFactory() {
        connectProcess = new ConnectProcess();
        connectReadProcess = new ConnectReadProcess();
        heloWriteProcess = new HeloWriteProcess();
        heloReadProcess = new HeloReadProcess();
        mailFromWriteProcess = new MailFromWriteProcess();
        mailFromReadProcess = new MailFromReadProcess();
        rcptToWriteProcess = new RcptToWriteProcess();
        rcptToReadProcess = new RcptToReadProcess();
        dataRequestWriteProcess = new DataRequestWriteProcess();
        dataRequestReadProcess = new DataRequestReadProcess();
        dataWriteProcess = new DataWriteProcess();
        dataReadProcess = new DataReadProcess();
        quitWriteProcess = new QuitWriteProcess();
        quitReadProcess = new QuitReadProcess();
        finalProcess = new FinalProcess();
    }

    public Process getProcess(@Nonnull Step step, @Nonnull Condition condition) {
        switch (step) {
            case ATTACH:
                return condition == Condition.READ ? connectReadProcess : connectProcess;
            case HELO:
                return condition == Condition.READ ? heloReadProcess : heloWriteProcess;
            case MAIL_FROM:
                return condition == Condition.READ ? mailFromReadProcess : mailFromWriteProcess;
            case RCPT_TO:
                return condition == Condition.READ ? rcptToReadProcess : rcptToWriteProcess;
            case DATA_REQUEST:
                return condition == Condition.READ ? dataRequestReadProcess : dataRequestWriteProcess;
            case DATA:
                return condition == Condition.READ ? dataReadProcess : dataWriteProcess;
            case QUIT:
                return condition == Condition.READ ? quitReadProcess : quitWriteProcess;
            default:
                return finalProcess;
        }
    }
}
