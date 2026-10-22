
package controller;

import service.WorkflowSignalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workflow-signals")
@CrossOrigin(origins = "*")
public class WorkflowSignalController {

    private final WorkflowSignalService workflowSignalService;

    public WorkflowSignalController(
            WorkflowSignalService workflowSignalService) {
        this.workflowSignalService = workflowSignalService;
    }

    @GetMapping
    public Object getAllSignals() {
        return workflowSignalService.getAllSignals();
    }

    @GetMapping("/drone/{droneId}")
    public Object getSignalsByDrone(@PathVariable Long droneId) {
        return workflowSignalService.getSignalsByDrone(droneId);
    }
}
