
import BuildTools.bakeDestDirPath
import BuildTools.bakeSrcPath
import BuildTools.createCnameFile
import BuildTools.pushPages

plugins { id("org.jbake.site") }

tasks.register("publishSite") {
    group = "managed"
    description = "Publish site online."
    dependsOn("bake")
    jbake {
        srcDirName = bakeSrcPath
        destDirName = bakeDestDirPath
    }
    doFirst { createCnameFile() }
    doLast { pushPages() }
}