
import BuildTools.bakeDestDirPath
import BuildTools.bakeSrcPath
import BuildTools.createCnameFile
import BuildTools.pushPages
import Constants.TASK_BAKE
import Constants.TASK_PUBLISH_SITE
import Constants.TASK_PUSH_PAGES

plugins { id("org.jbake.site") }

tasks.register(TASK_PUBLISH_SITE) {
    group = "managed"
    description = "Publish site online."
    dependsOn(TASK_BAKE)
    finalizedBy(TASK_PUSH_PAGES)
    jbake {
        srcDirName = bakeSrcPath
        destDirName = bakeDestDirPath
    }
    doFirst { createCnameFile() }
}

tasks.register(TASK_PUSH_PAGES) {
    group = "managed"
    description = "Push pages to repository."
    doFirst { pushPages() }
}
