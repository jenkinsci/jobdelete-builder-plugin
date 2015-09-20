package org.jenkinsci.plugins.jobdeletebuilder;

import com.thoughtworks.xstream.mapper.Mapper;
import hudson.model.*;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;


/*
  testPerform()
  1. String Direct 1  SUCCESS
  2. String Direct 0  FAILURE
  3. Regexp Direct 1  SUCCESS
  4. Regexp Direct 10 SUCCESS
  5. String Param  1  SUCCESS
  6. Regexp Param  1  SUCCESS
  7. Empty  Direct 1  FAILURE
  8. Empty  Param  1  FAILURE
  9. Empty  Param  0  FAILURE
  testDescriptorDoCheckTarget()
*/
public class JobDeleteBuilderTest {


    @Rule
    public JenkinsRule j = new JenkinsRule();

    private JobDeleteBuilder.DescriptorImpl getDescriptor() {
        return (JobDeleteBuilder.DescriptorImpl)new JobDeleteBuilder("").getDescriptor();
    }


    // delete a job test
    @Test
    public void testPerform() throws IOException, ExecutionException, InterruptedException {
        // 1. String Direct 1  SUCCESS
        {
            String targetJobName = "job01";
            FreeStyleProject target = j.createFreeStyleProject(targetJobName);
            FreeStyleProject project = createJobDeleteProject("delete-job-project", targetJobName);
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause()
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetJobName), nullValue()
            );
            project.delete();
        }
        // 2. String Direct 0  FAILURE
        {
            String targetJobName = "job02";
            FreeStyleProject project = createJobDeleteProject("delete-job-project", targetJobName);
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause()
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete no job test",
                    result, is(Result.FAILURE)
            );
            project.delete();
        }
        // 3. Regexp Direct 1  SUCCESS
        {
            String targetJobName = "job03";
            FreeStyleProject target = j.createFreeStyleProject(targetJobName);
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "job.*");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause()
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetJobName), nullValue()
            );
            project.delete();
        }

        // 4. Regexp Direct 10 SUCCESS
        {
            String targetJobName = "job";
            for (int i = 0; i < 10; i++) {
                j.createFreeStyleProject(targetJobName  + Integer.valueOf(i));
            }
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "job.*");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause()
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete 10 jobs test",
                    result, is(Result.SUCCESS)
            );
            for(int i = 0; i < 10; i++) {
                assertThat(
                        "Check targets are deleted",
                        Jenkins.getInstance().getItem(targetJobName + Integer.valueOf(i)), nullValue()
                );
            }
            project.delete();
        }
        // 5. String Param  1  SUCCESS
        {
            String targetJobName = "job01";
            FreeStyleProject target = j.createFreeStyleProject(targetJobName);
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "${TARGET}");
            ParametersAction param = createParam(project, "TARGET" ,"job01");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause(),
                    param
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetJobName), nullValue()
            );
            project.delete();
        }
        // 6. Regexp Param  1  SUCCESS
        {
            String targetJobName = "job01";
            FreeStyleProject target = j.createFreeStyleProject(targetJobName);
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "${TARGET}");
            ParametersAction param = createParam(project, "TARGET" ,"job.*");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause(),
                    param
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetJobName), nullValue()
            );
            project.delete();
        }
        // 7. Empty  Direct 1  FAILURE
        {
            String targetJobName = "job01";
            FreeStyleProject target = j.createFreeStyleProject(targetJobName);
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause()
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.FAILURE)
            );
            project.delete();
            target.delete();
        }
        // 8. Empty  Param  1  FAILURE
        {
            String targetJobName = "job01";
            FreeStyleProject target = j.createFreeStyleProject(targetJobName);
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "${TARGET}");
            ParametersAction param = createParam(project, "TARGET" ,"");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause(),
                    param
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run target name is empty test",
                    result, is(Result.FAILURE)
            );
            project.delete();
            target.delete();
        }
        // 9. Empty  Param  0  FAILURE
        {
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "${TARGET}");
            ParametersAction param = createParam(project, "TARGET" ,"");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause(),
                    param
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run target name is empty test",
                    result, is(Result.FAILURE)
            );
            project.delete();
        }
        //10. String Direct only own job FAILURE
        {
            FreeStyleProject project = createJobDeleteProject("delete-job-project", "delete-job-project");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause()
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete a own job test",
                    result, is(Result.FAILURE)
            );
            assertThat(
                    "Check delete-job-project is not deleted",
                    Jenkins.getInstance().getItem("delete-job-project"), notNullValue()
            );
            project.delete();
        }
        //11. Regexp Direct 1 +  own job SUCCESS
        {
            String targetJobName = "job01-project";
            FreeStyleProject target = j.createFreeStyleProject(targetJobName);
            FreeStyleProject project = createJobDeleteProject("delete-job-project", ".*-project");
            FreeStyleBuild build = project.scheduleBuild2(
                    project.getQuietPeriod(),
                    new Cause.UserIdCause()
            ).get();

            Result result = runJobDeleteBuilder(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetJobName), nullValue()
            );
            assertThat(
                    "Check delete-job-project is not deleted",
                    Jenkins.getInstance().getItem("delete-job-project"), notNullValue()
            );
            project.delete();
        }
    }

    private FreeStyleProject createJobDeleteProject(String jobName, String target) throws IOException {
        FreeStyleProject project = j.createFreeStyleProject(jobName);

        JobDeleteBuilder job = new JobDeleteBuilder(target);
        project.getBuildersList().add(job);
        return project;
    }

    private ParametersAction createParam(FreeStyleProject project,String paramName, String paramValue) throws IOException, ExecutionException, InterruptedException {
        project.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition(
                        paramName,
                        "",
                        "Description"
                )
        ));
        ParametersAction paramAction = new ParametersAction(
                new StringParameterValue(paramName, paramValue)
        );

        return paramAction;
    }

    private Result runJobDeleteBuilder (FreeStyleBuild build) throws InterruptedException {
        while(build.isBuilding())
        {
            Thread.sleep(10);
        }
        return build.getResult();
    }

    @Test
    public void testDescriptorDoCheckTarget() {
        JobDeleteBuilder.DescriptorImpl descriptor = getDescriptor();

        String stringJobName = "Sample-Job1";
        String regexpJobName = "Sample-Job*";
        String envJobName = "${TARGET_JOB}";
        String emptyJobName = "";

        {
            assertThat(
                    "Normal String",
                    descriptor.doCheckTarget(stringJobName).kind, is(FormValidation.Kind.OK)
            );

            assertThat(
                    "Regexp String",
                    descriptor.doCheckTarget(regexpJobName).kind, is(FormValidation.Kind.OK)
            );

            assertThat(
                    "Env String",
                    descriptor.doCheckTarget(envJobName).kind, is(FormValidation.Kind.OK)
            );

            assertThat(
                    "Empty String",
                    descriptor.doCheckTarget(emptyJobName).kind, is(FormValidation.Kind.ERROR)
            );
        }
    }
}