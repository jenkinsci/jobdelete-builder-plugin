package org.jenkinsci.plugins.jobdeletebuilder;

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
            String targetName = "job1";
            TestUtil.createJDeleteTargetJobs(j, "job", 1);

            FreeStyleProject project = TestUtil.createJobDeleteProject(j, targetName);
            Build build = TestUtil.runBuildAsync(project, null);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetName), nullValue()
            );
            project.delete();
        }
        // 2. String Direct 0  FAILURE
        {
            String targetName = "job1";

            FreeStyleProject project = TestUtil.createJobDeleteProject(j, targetName);
            Build build = TestUtil.runBuildAsync(project, null);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete no job test",
                    result, is(Result.FAILURE)
            );
            project.delete();
        }
        // 3. Regexp Direct 1  SUCCESS
        {
            String targetName = "job1";
            TestUtil.createJDeleteTargetJobs(j, "job", 1);

            FreeStyleProject project = TestUtil.createJobDeleteProject(j, targetName);
            Build build = TestUtil.runBuildAsync(project, null);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetName), nullValue()
            );
            project.delete();
        }

        // 4. Regexp Direct 10 SUCCESS
        {
            String targetName = "job";
            TestUtil.createJDeleteTargetJobs(j,"job", 10);

            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "job.*");
            Build build = TestUtil.runBuildAsync(project, null);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete 10 jobs test",
                    result, is(Result.SUCCESS)
            );
            for(int i = 0; i < 10; i++) {
                assertThat(
                        "Check targets are deleted",
                        Jenkins.getInstance().getItem(targetName + Integer.valueOf(i)), nullValue()
                );
            }
            project.delete();
        }
        // 5. String Param  1  SUCCESS
        {

            String targetName = "job1";
            TestUtil.createJDeleteTargetJobs(j, "job", 1);

            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "${TARGET}");
            ParametersAction param = TestUtil.createParam(project, "TARGET", "job1");
            Build build = TestUtil.runBuildAsync(project, param);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetName), nullValue()
            );
            project.delete();
        }
        // 6. Regexp Param  1  SUCCESS
        {
            String targetName = "job1";
            TestUtil.createJDeleteTargetJobs(j, "job", 1);

            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "${TARGET}");
            ParametersAction param = TestUtil.createParam(project, "TARGET", "job.*");
            Build build = TestUtil.runBuildAsync(project, param);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetName), nullValue()
            );
            project.delete();
        }
        // 7. Empty  Direct 1  FAILURE
        {
            String targetName = "job1";
            FreeStyleProject target = TestUtil.createJDeleteTargetJobs(j, "job", 1).get(0);

            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "");
            Build build = TestUtil.runBuildAsync(project, null);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.FAILURE)
            );
            project.delete();
            target.delete();
        }
        // 8. Empty  Param  1  FAILURE
        {
            FreeStyleProject target = TestUtil.createJDeleteTargetJobs(j, "job", 1).get(0);
            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "${TARGET}");
            ParametersAction param = TestUtil.createParam(project, "TARGET", "");

            Build build = TestUtil.runBuildAsync(project, param);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run target name is empty test",
                    result, is(Result.FAILURE)
            );
            project.delete();
            target.delete();
        }
        // 9. Empty  Param  0  FAILURE
        {
            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "${TARGET}");
            ParametersAction param = TestUtil.createParam(project, "TARGET", "");

            Build build = TestUtil.runBuildAsync(project, param);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run target name is empty test",
                    result, is(Result.FAILURE)
            );
            project.delete();
        }
        //10. String Direct only own job FAILURE
        {
            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "delete-job-project");

            Build build = TestUtil.runBuildAsync(project, null);
            Result result = TestUtil.getResult(build);

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
            String targetName = "delete1";
            TestUtil.createJDeleteTargetJobs(j,"delete", 1);
            FreeStyleProject project = TestUtil.createJobDeleteProject(j, "delete.*");

            Build build = TestUtil.runBuildAsync(project, null);
            Result result = TestUtil.getResult(build);

            assertThat(
                    "Run delete a job test",
                    result, is(Result.SUCCESS)
            );
            assertThat(
                    "Check target is deleted",
                    Jenkins.getInstance().getItem(targetName), nullValue()
            );
            assertThat(
                    "Check delete-job-project is not deleted",
                    Jenkins.getInstance().getItem("delete-job-project"), notNullValue()
            );
            project.delete();
        }
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