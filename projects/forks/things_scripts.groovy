freeStyleJob('update_fork_things_scripts') {
    displayName('update-fork-things-scripts')
    description('Rebase the primary branch (master) in nickcharlton/things-scripts fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/nickcharlton/things-scripts')
        sidebarLinks {
            link('https://github.com/benjamineskola/things-scripts', 'UPSTREAM: benjamineskola/things-scripts', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:nickcharlton/things-scripts.git')
                name('origin')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('https://github.com/benjamineskola/things-scripts.git')
                name('upstream')
                refspec('+refs/heads/master:refs/remotes/upstream/master')
            }
            branches('master', 'upstream/master')
            extensions {
                disableRemotePoll()
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H H * * *')
    }

    wrappers { colorizeOutput() }

    steps {
        shell('git rebase upstream/master')
    }

    publishers {
        git {
            branch('origin', 'master')
            pushOnlyIfSuccess()
        }

        extendedEmail {
            recipientList('$DEFAULT_RECIPIENTS')
            contentType('text/plain')
            triggers {
                stillFailing {
                    attachBuildLog(true)
                }
            }
        }

        wsCleanup()
    }
}
