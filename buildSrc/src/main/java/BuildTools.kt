@file:Suppress(
    "GrazieInspection",
    "MemberVisibilityCanBePrivate",
    "SpellCheckingInspection"
)

import Constants.CNAME
import Constants.origin
import Constants.remote
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.Git.init
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.transport.PushResult
import org.eclipse.jgit.transport.URIish
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider
import org.gradle.api.Project
import java.io.File
import java.lang.System.getProperty
import java.nio.charset.StandardCharsets.UTF_8
import java.util.*

/*=================================================================================*/
object BuildTools {

    private val sep: String by lazy { getProperty("file.separator") }
    private val mapper: ObjectMapper by lazy {
        ObjectMapper(YAMLFactory()).apply {
            disable(WRITE_DATES_AS_TIMESTAMPS)
            registerKotlinModule()
        }
    }
    private val Project.localConf: SiteConfiguration
        get() = mapper.readValue(
            File("${rootDir}$sep${properties["managed_config_path"]}"),
            SiteConfiguration::class.java
        )
    val Project.bakeSrcPath: String get() = localConf.bake.srcPath
    val Project.bakeDestDirPath: String get() = localConf.bake.destDirPath

    fun Project.createCnameFile() {
        when {
            localConf.bake.cname != "" -> file(
                "${project.buildDir.absolutePath}$sep${
                    localConf.bake.destDirPath
                }$sep$CNAME"
            ).run {
                when {
                    exists() && isDirectory -> assert(deleteRecursively())
                    exists() -> assert(delete())
                }
                assert(!exists())
                assert(createNewFile())
                @Suppress("USELESS_ELVIS")
                appendText(localConf.bake.cname ?: "", UTF_8)
                assert(exists() && !isDirectory)
            }
        }
    }

    private fun Project.createRepoDir(path: String): File = File(path).apply {
        if (exists() && !isDirectory) assert(delete())
        if (exists()) assert(deleteRecursively())
        assert(!exists())
        if (!exists()) assert(mkdir())
    }

    private fun Project.copyBakedFilesToRepo(
        bakeDirPath: String,
        repoDir: File
    ) = File(bakeDirPath).run {
        assert(copyRecursively(repoDir, true))
        assert(deleteRecursively())
    }


    private fun Project.initAddCommit(
        repoDir: File,
        conf: SiteConfiguration,
    ): RevCommit {
        //3) initialiser un repo dans le dossier cvs
        init()
            .setDirectory(repoDir)
            .call().run {
                assert(!repository.isBare)
                assert(repository.directory.isDirectory)
                // add remote repo:
                remoteAdd().apply {
                    setName(origin)
                    setUri(URIish(conf.pushPage.repo.repository))
                    // you can add more settings here if needed
                }.call()
                //4) ajouter les fichiers du dossier cvs à l'index
                add().addFilepattern(".").call()
                //5) commit
                return commit().setMessage(conf.pushPage.message).call()
            }
    }

    private fun Project.push(
        repoDir: File,
        conf: SiteConfiguration,
    ): MutableIterable<PushResult>? {
        Git(FileRepositoryBuilder()
            .setGitDir(File("${repoDir.absolutePath}$sep.git"))
            .readEnvironment()
            .findGitDir()
            .setMustExist(true)
            .build()
            .apply {
                config.apply {
                    getString(
                        remote,
                        origin,
                        conf.pushPage.repo.repository
                    )
                    save()
                }
                assert(isBare)
            }).run {
            // push to remote:
            return push().apply {
                setCredentialsProvider(
                    UsernamePasswordCredentialsProvider(
                        conf.pushPage.repo.credentials.username,
                        conf.pushPage.repo.credentials.password
                    )
                )
                //you can add more settings here if needed
                remote = origin
                isForce = true
            }.call()
        }
    }

    fun Project.pushPages() {
        //1) créer un dossier cvs
        createRepoDir(
            "${buildDir.absolutePath}$sep${localConf.pushPage.to}"
        ).run {
            //2) déplacer le contenu du dossier jbake dans le dossier cvs
            copyBakedFilesToRepo("${buildDir.absolutePath}$sep$bakeDestDirPath", this)
            //3) initialiser un repo dans le dossier cvs
            // 4 & 5) ajouter les fichiers du dossier cvs à l'index et commit
            initAddCommit(this, localConf)
            //6) push
            push(this, localConf)
            deleteRecursively()
            File("${buildDir.absolutePath}$sep$bakeDestDirPath")
                .deleteRecursively()
        }
    }
}