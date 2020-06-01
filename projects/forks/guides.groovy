freeStyleJob('update_fork_guides') {
    displayName('update-fork-guides')
    description('Rebase the primary branch (master) in nickcharlton/guides fork.')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/nickcharlton/guides')
        sidebarLinks {
            link('https://github.com/thoughtbot/guides', 'UPSTREAM: thoughtbot/guides', 'notepad.png')
        }
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:nickcharlton/guides.git')
                name('origin')
                credentials('github-ssh-key')
                refspec('+refs/heads/master:refs/remotes/origin/master')
            }
            remote {
                url('https://github.com/thoughtbot/guides.git')
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
