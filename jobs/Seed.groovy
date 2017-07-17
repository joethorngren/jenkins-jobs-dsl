
job('seed') {
    scm {
        git {
            branch(branchName)
            remote {
                url("git@gitlab.com:dayFun/jenkins-jobs-dsl.git")
                credentials("JTPrivateGitlab")
            }
        }
    }

    steps {
        shell("cp /home/jenkins/android.jks ${JENKINS_HOME}/")

        gradle 'clean'
        dsl {
            external 'jobs/**/*Jobs.groovy'
            additionalClasspath 'src/main/groovy'
        }
    }
    publishers {
        archiveJunit 'build/test-results/**/*.xml'
    }
}