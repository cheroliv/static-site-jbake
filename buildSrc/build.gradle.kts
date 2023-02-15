plugins { `kotlin-dsl` }

repositories {
    google()
    mavenCentral()
}

dependencies {

    val jacksonVersion = "2.14.1"
    val jgitVersion = "6.4.0.202211300538-r"
    val commonsIoVersion = "2.11.0"
    val slf4jVersion = "1.7.36"
    val xzVersion = "1.9"

    implementation("org.slf4j:slf4j-simple:$slf4jVersion")
    implementation("commons-io:commons-io:$commonsIoVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation( "com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:$jacksonVersion")
    implementation( "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation( "org.eclipse.jgit:org.eclipse.jgit:$jgitVersion")
    implementation( "org.eclipse.jgit:org.eclipse.jgit.archive:$jgitVersion")
    implementation( "org.eclipse.jgit:org.eclipse.jgit.ssh.jsch:$jgitVersion")
    implementation( "org.tukaani:xz:$xzVersion")
}