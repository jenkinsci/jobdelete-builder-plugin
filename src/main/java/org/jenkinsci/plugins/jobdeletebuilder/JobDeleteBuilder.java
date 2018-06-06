package org.jenkinsci.plugins.jobdeletebuilder;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Item;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class JobDeleteBuilder extends Builder {

    private final String target;

    @DataBoundConstructor
    public JobDeleteBuilder(String target) {
        this.target = target;
    }

    public String getTarget() {
        return target;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
                
        List<Item> allJobs = Jenkins.getInstance().getAllItems();

        // Get job name on this build
        String myJobName = build.getProject().getDisplayName();

        // Get delete Regexp
        String deleteRegexp = getDeleteRegexp(build.getEnvironment(listener));
        if (deleteRegexp.isEmpty()) {
            listener.getLogger().println("Error : " + Messages.JobDeleteBuilder_JobName_empty());
            return false;
        }
        listener.getLogger().println(Messages.JobDeleteBuilder_deleted_target() + " : " + deleteRegexp);

        // Get delete jobs
        List<Item> deleteJobs = getDeleteJobs(allJobs, deleteRegexp, myJobName);
        if (deleteJobs.isEmpty()) {
            listener.getLogger().println("Error : " + Messages.JobDeleteBuilder_JobName_notExists());
            return false;
        }

        // Delete jobs
        for (Item job : deleteJobs) {
            listener.getLogger().println(Messages.JobDeleteBuilder_delete_complete() + " : " + job.getFullName());
            job.delete();
        }

        return true;
    }

    private String getDeleteRegexp(EnvVars env){
        // Get Regexp
        if(StringUtils.isBlank(target)) {
            return "";
        }

        // Expand the variable expressions in job names.
        String exRegexp = env.expand(target);
        if(StringUtils.isBlank(exRegexp)) {
            return "";
        }
        return exRegexp;
    }

    private List<Item> getDeleteJobs(List<Item> allJobs, String deleteRegexp, String myJobName) {
        List<Item> deleteJobList = new ArrayList<Item>();

        for (Item job : allJobs) {
            String jobName = job.getFullName();

            if (jobName.equals(myJobName)) {
                continue;
            }

            if (Pattern.matches(deleteRegexp, jobName)) {
                deleteJobList.add(job);
            }
        }

        return deleteJobList;
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckTarget(@QueryParameter String value) {
            if (value.length() == 0)
                return FormValidation.error(Messages.JobDeleteBuilder_JobName_empty());
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            // Indicates that this builder can be used with all kinds of project types
            return true;
        }

        public String getDisplayName() {
            return Messages.JobDeleteBuilder_DisplayName();
        }
    }
}

