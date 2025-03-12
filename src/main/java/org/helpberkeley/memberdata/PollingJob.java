package org.helpberkeley.memberdata;

import org.quartz.*;

import java.util.*;

public class PollingJob implements Job {
    public static final String RUNTIME_DEPENDENCY = "runtime dependency";
//    private Integer priority;
//    private List<JobDependency> dependencies = Arrays.asList(JobDependency.MEMBERDATA_RAW);
//
//    @Override
//    public void setPriority(Integer priority) {
//        this.priority = priority;
//        System.out.println("Job priority set to: " + priority);
//    }
//
//    @Override
//    public List<JobDependency> getDependencies() {
//        return dependencies;
//    }

    public PollingJob() {}

    @Override
    public void execute(JobExecutionContext context) {
        Random random = new Random();
        int rand = random.nextInt(2);
        if (rand == 0) {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            String runtimeDependency = data.getString(RUNTIME_DEPENDENCY);
            System.out.println("Found a work-request. Executing with runtime dependency: " + runtimeDependency);
            int rand2 = random.nextInt(2);
            if (rand2 == 0) {
                context.setResult("one-kitchen workflow");
                System.out.println("context result set to one-kitchen workflow");
            } else {
                context.setResult("regular workflow");
                System.out.println("context result set to regular workflow");
            }
        } else {
            System.out.println("No work-requests found.");
        }
    }

}