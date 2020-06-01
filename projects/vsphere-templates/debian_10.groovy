freeStyleJob('vsphere_template_debian_10') {
    displayName('vsphere-template-debian-10')
    description('Build the debian-10 vSphere Template')
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
        shell("packer build -force debian-10.json")
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
