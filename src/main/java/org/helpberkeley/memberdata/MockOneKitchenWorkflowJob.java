package org.helpberkeley.memberdata;

import java.util.Arrays;
import java.util.List;

public class MockOneKitchenWorkflowJob implements Job {
    private Priority priority = Priority.MEDIUM;
    private List<JobDependency> dependencies = Arrays.asList(JobDependency.ONEKITCHEN_WORKFLOW_REQUEST);

    @Override
    public void setPriority(Priority priority) {
        this.priority = priority;
        System.out.println("Job priority set to: " + priority);
    }

    @Override
    public List<JobDependency> getDependencies() {
        return dependencies;
    }

    @Override
    public void execute(List<CacheEntry> dependencies) {
        System.out.println("Executing oneKitchenWorkflow");
    }
}