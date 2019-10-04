# jenkins-dsl

[Jenkins DSLs][] that run my Jenkins instance and does regular maintenance tasks
like updating forks, etc.

This is pretty much wholesale taken from [jessfraz/jenkins-dsl][].

## Initial Setup

These jobs are seeded during Jenkins setup using an `init.groovy.d` script at:
`$JENKINS_HOME/init.groovy.d/seed_jobs.groovy` with the following:

```groovy
#!/usr/bin/env groovy

import hudson.model.FreeStyleProject;
import hudson.plugins.git.GitSCM;
import hudson.plugins.git.UserRemoteConfig;
import hudson.plugins.git.BranchSpec;
import hudson.triggers.SCMTrigger;
import javaposse.jobdsl.plugin.*;
import jenkins.model.Jenkins;

def jenkins = Jenkins.instance;

def seedJobsUrl = "git@github.com:nickcharlton/jenkins-dsl.git"
def jobName = "seed-jobs";
def branch = "*/master"

jenkins.items.findAll { job -> job.name == jobName }
  .each { job -> job.delete() }

gitTrigger = new SCMTrigger("H * * * *");
dslBuilder = new ExecuteDslScripts()

dslBuilder.setTargets("projects/**/*.groovy")
dslBuilder.setUseScriptText(false)
dslBuilder.setIgnoreExisting(false)
dslBuilder.setIgnoreMissingFiles(false)
dslBuilder.setRemovedJobAction(RemovedJobAction.DELETE)
dslBuilder.setRemovedViewAction(RemovedViewAction.DELETE)
dslBuilder.setLookupStrategy(LookupStrategy.SEED_JOB)

dslProject = new hudson.model.FreeStyleProject(jenkins, jobName);
dslProject.scm = new GitSCM(seedJobsUrl);
dslProject.scm.branches = [new BranchSpec(branch)];
dslProject.addTrigger(gitTrigger);
dslProject.createTransientActions();
dslProject.getPublishersList().add(dslBuilder);

jenkins.add(dslProject, jobName);

gitTrigger.start(dslProject, true);
```

This is then configured with all of the other Jenkins configuration (in my
case, using Ansible).

[Jenkins DSLs]: https://github.com/jenkinsci/job-dsl-plugin
[jessfraz/jenkins-dsl]: https://github.com/jessfraz/jenkins-dsl
