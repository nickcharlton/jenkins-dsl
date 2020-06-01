freeStyleJob('vsphere_template_ubuntu_1804') {
    displayName('vsphere-template-ubuntu-1804')
    description('Build the ubuntu-1804 vSphere Template')
    label('linux')

    checkoutRetryCount(3)

    properties {
        githubProjectUrl('https://github.com/nickcharlton/esxi-infrastructure')
    }

    logRotator {
        numToKeep(100)
        daysToKeep(15)
    }

    scm {
        git {
            remote {
                url('git@github.com:nickcharlton/esxi-infrastructure.git')
                credentials('github-ssh-key')
            }
            branches('master')
            extensions {
                disableRemotePoll()
                wipeOutWorkspace()
                cleanAfterCheckout()
            }
        }
    }

    triggers {
        cron('H 12 1 1-12 *') // run every month, on the 1st, at 12am
    }

    wrappers {
      colorizeOutput()

      credentialsBinding { 
        usernamePassword('VSPHERE_USERNAME', 'VSPHERE_PASSWORD', 'vsphere_user')
      }
    }

    environmentVariables(
      VSPHERE_HOST: 'vsphere.nullgrid.net',
      ESXI_HOST: 'esxi.nullgrid.net',
      ESXI_DATASTORE: 'primary'
    )

    steps {
        shell("packer build -force ubuntu-1804.json")
    }

    publishers {
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
