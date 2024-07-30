import jenkins.model.Jenkins
import com.cloudbees.hudson.plugins.folder.Folder
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject
import org.jenkinsci.plugins.workflow.job.WorkflowJob
import java.text.SimpleDateFormat

def call(String rootFolderName) {
    def rootFolder = Jenkins.instance.getItemByFullName(rootFolderName)
    if (!rootFolder) {
        throw new IllegalArgumentException("Root folder '$rootFolderName' not found.")
    }

    def jobs = rootFolder.getItems()
    jobs.each { job ->
        if (job instanceof Folder) {
            call(job.fullName)
        } else if (job instanceof WorkflowMultiBranchProject) {
            job.getItems().each { branchJob ->
                printLastCompletedBuildDetails(branchJob)
            }
        } else {
            printLastCompletedBuildDetails(job)
        }
    }
}

def printLastCompletedBuildDetails(job) {
    def lastBuild = job.getLastBuild()
    if (lastBuild && lastBuild.result != null) {
        def jobName = job.fullName
        def branchName = (job instanceof WorkflowJob) ? job.parent.displayName : ""
        def buildStatus = lastBuild.result
        def buildNumber = lastBuild.number
        def timestamp = lastBuild.timestamp

        def dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss")
        def formattedTimestamp = dateFormat.format(timestamp.getTime())

        println "Job Name: ${jobName}, Branch Name: ${branchName}, Build Number: ${buildNumber}, Build Status: ${buildStatus}, Timestamp: ${formattedTimestamp}"
    }
}
