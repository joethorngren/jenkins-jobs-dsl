import org.kohsuke.github.GHBranch
import org.kohsuke.github.GitHub
import org.kohsuke.github.GitHubBuilder

GitHub gitHub = GitHubBuilder.fromCredentials().build()
def repo = gitHub.getRepository("LoopLabsInc/android-app")

repo.branches.forEach { String branchName, GHBranch githubBranch ->

    def matches = branchName.matches("^(?!=(master|((spikes|wip)/))).*?")
    def properties = githubBranch.properties.toMapString()
    print "githubBranch properties toMapString() = \n $properties"
    println("branchName = $branchName\n")
    println("matches \"^(?!=master|(spikes|wip)/).*\" = $matches")

    /*githubBranch properties toMapString() =
         [SHA1:27111641f5469cdae5574d8dfe530c86f2bc5a9c,
         protection:false,
         owner:GHRepository@6399551e [
             description=Android app,
             homepage=<null>,
             name=android-app,
             license=<null>,
             fork=false,
             size=120018,
             milestones={},
             language=Java,
             commits={},
             source=<null>,
             parent=<null>,
             url=https://api.github.com/repos/LoopLabsInc/android-app,id=39092395],
             class:class org.kohsuke.github.GHBranch,
             protectionUrl:null, root:org.kohsuke.github.GitHub@5d5f10b2,
             protected:false,
             apiRoute:/repos/LoopLabsInc/android-app/branches/master,
             name:master
         ]
         branchName = master

        matches "^(?!=master|(spikes|wip)/).*" = true <------ WHY?
    */

    println()
    println()

    // Only build develop while getting the Jobs DSL figured out...
    if (matches && branchName == "develop") {
        // TODO: Replace this line after: https://github.com/jenkinsci/job-dsl-plugin/commit/5c26b1808fd7857805942cf3aa95b7dc78ded113 gets merged/updated
        branchName = branchName.replace("/", "-")
        job(branchName) {
            scm {
                git {
                    branch(branchName)
                    remote {
                        url("git@github.com:LoopLabsInc/android-app.git")
                        credentials("MasterJenkinsGithubSSH")
                    }
                }
            }

            triggers {
                scm('H/15 * * * *')
            }

            steps {
                shell('/opt/Android/Sdk/platform-tools/adb start-server')
                shell('/opt/Android/Sdk/platform-tools/adb uninstall com.getnotion.android.alpha')
                shell('/opt/Android/Sdk/platform-tools/adb uninstall com.getnotion.android.staging')
                shell('/opt/Android/Sdk/platform-tools/adb uninstall com.getnotion.android')

                gradle('clean check connectedCheck checkstyle build')
            }

            publishers {
                archiveArtifacts {
                    pattern('app/build/outputs/apk/*(?!Test).apk')
                    onlyIfSuccessful()
                }
            }
        }

        job("$branchName-NIGHTLY") {
            scm {
                git {
                    branch(branchName)
                    remote {
                        url("git@github.com:LoopLabsInc/android-app.git")
                        credentials("JenkinsGithubSSH")
                    }
                }
            }

            triggers {
                // Should run daily at 5AM
                // Test to see if it will run at 8:15AM
                cron('30 8 * * *')
            }

            steps {
                gradle('clean check connectedCheck checkstyle build')
            }
        }
    }
}



