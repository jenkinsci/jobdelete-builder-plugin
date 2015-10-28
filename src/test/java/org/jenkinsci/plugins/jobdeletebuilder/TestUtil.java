package org.jenkinsci.plugins.jobdeletebuilder;


import hudson.model.*;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public  class  TestUtil {
    static public FreeStyleProject createJobDeleteProject(JenkinsRule j, String target) throws IOException {
        FreeStyleProject project = j.createFreeStyleProject("delete-job-project");
        JobDeleteBuilder job = new JobDeleteBuilder(target);
        project.getBuildersList().add(job);
        return project;
    }

    static public List<FreeStyleProject> createJDeleteTargetJobs(JenkinsRule j, String jobPrefix, int num) throws IOException {
        List<FreeStyleProject> l = new ArrayList<FreeStyleProject>();
        for (int i = 0; i < num; i++) {
            l.add(j.createFreeStyleProject(jobPrefix + Integer.valueOf(i+1)));
        }
        return l;
    }

    static public ParametersAction createParam(FreeStyleProject project,String paramName, String paramValue) throws IOException, ExecutionException, InterruptedException {
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition(
                        paramName,
                        "",
                        "Description"
                )
        ));

        return new ParametersAction(
                new StringParameterValue(paramName, paramValue)
        );
    }

    static public Build runBuildAsync(FreeStyleProject project, ParametersAction param) throws InterruptedException, ExecutionException {
        return project.scheduleBuild2(
                project.getQuietPeriod(),
                new Cause.UserIdCause(),
                param
        ).get();

    }

    static public Result getResult(Build build) throws InterruptedException {
        while(build.isBuilding()) {
            Thread.sleep(10);
        }
        return build.getResult();
    }

}
