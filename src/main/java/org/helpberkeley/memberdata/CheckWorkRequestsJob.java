package org.helpberkeley.memberdata;

import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class CheckWorkRequestsJob implements Job {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckWorkRequestsJob.class);
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

    public CheckWorkRequestsJob() {}

    @Override
    public void execute(JobExecutionContext context) {
        Random random = new Random();
        int rand = random.nextInt(2);
        if (rand == 0) {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            String runtimeDependency = data.getString(RUNTIME_DEPENDENCY);
            LOGGER.info("Found a work-request. Executing with runtime dependency: {}", runtimeDependency);
            int rand2 = random.nextInt(2);
            if (rand2 == 0) {
                context.setResult("one-kitchen workflow");
                LOGGER.info("context result set to one-kitchen workflow");
            } else {
                context.setResult("regular workflow");
                LOGGER.info("context result set to regular workflow");
            }
        } else {
            LOGGER.info("No work-requests found.");
        }
    }

}