package org.helpberkeley.memberdata;

import java.util.*;

public class MockJob implements Job {
    private Priority priority = Priority.MEDIUM;
    private List<JobDependency> dependencies = Arrays.asList(JobDependency.MEMBERDATA_RAW);

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
        System.out.println("Executing job with dependencies: " + dependencies);
    }
}