package com.AeroSaga;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface DroneMissionWorkflow {

    @WorkflowMethod
    String executeMission(String missionId);
}