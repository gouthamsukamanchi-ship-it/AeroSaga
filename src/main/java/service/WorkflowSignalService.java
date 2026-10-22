
package service;

import entity.WorkflowSignal;
import repository.WorkflowSignalRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkflowSignalService {

    private final WorkflowSignalRepository workflowSignalRepository;

    public WorkflowSignalService(
            WorkflowSignalRepository workflowSignalRepository) {
        this.workflowSignalRepository = workflowSignalRepository;
    }

    @Transactional
    public WorkflowSignal logSignal(
            Long droneId,
            String signalType,
            String signalStatus,
            String message) {

        if (droneId == null) {
            throw new IllegalArgumentException("Drone ID is required");
        }

        if (signalType == null || signalType.isBlank()) {
            throw new IllegalArgumentException("Signal type is required");
        }

        if (signalStatus == null || signalStatus.isBlank()) {
            throw new IllegalArgumentException("Signal status is required");
        }

        WorkflowSignal signal = new WorkflowSignal();
        signal.setDroneId(droneId);
        signal.setSignalType(signalType);
        signal.setSignalStatus(signalStatus);
        signal.setMessage(message);

        return workflowSignalRepository.save(signal);
    }

    public List<WorkflowSignal> getSignalsByDrone(Long droneId) {
        return workflowSignalRepository.findByDroneId(droneId);
    }

    public List<WorkflowSignal> getAllSignals() {
        return workflowSignalRepository.findAll();
    }
}
