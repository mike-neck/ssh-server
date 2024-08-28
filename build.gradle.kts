import java.nio.file.Files

plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.sshd:sshd-core:2.13.2")
    implementation("org.apache.sshd:sshd-common:2.13.2")
    implementation("org.apache.sshd:sshd-sftp:2.13.2")
    implementation("net.i2p.crypto:eddsa:0.3.0") // <- ed25519 will

    runtimeOnly("ch.qos.logback:logback-classic:1.5.7")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:8.0")

    implementation("org.bouncycastle:bcprov-jdk18on:1.78")
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
dependencies.compileOnly("org.jetbrains:annotations:24.1.0")

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.example.ssh.Server")
}

tasks {
    run.configure {
        this.environment(
            "SSH_PORT" to "2020",
            "SSH_HOST_KEY" to "${buildDir}/ssh-keys/etc/ssh/ssh_host_ed25519_key",
            "SSH_AUTHORIZED_KEYS" to "${buildDir}/ssh-keys/server/authorized_keys",
            "SSH_USERNAME" to "user",
        )
    }

    distTar.configure {
        this.archiveFileName = "${project.name}.tar"
    }

    create("ssh-keygen") {
        group = "ssh-keygen"
        description = "Generates ssh keys(client/server)"
        dependsOn("client.ssh-keygen", "server.ssh-keygen")
    }

    create("prepare.ssh-keygen") {
        group = "ssh-keygen"
        description = "Prepares ssh directory for the key generation"
        val directories = listOf(
            layout.buildDirectory.dir("ssh-keys/etc/ssh"),
            layout.buildDirectory.dir("ssh-keys/user"),
            layout.buildDirectory.dir("ssh-keys/server"),
        )
        outputs.dirs(directories)
        doLast {
            directories.forEach {
                val dir = it.get().asFile.toPath()
                Files.createDirectories(dir)
            }
        }
    }

    val clientPublicKey = layout.buildDirectory.file("ssh-keys/user/ssh-key.pub")

    create("client.ssh-keygen", Exec::class) {
        group = "ssh-keygen"
        description = "Generates ssh keys(private/public) using ed25519 algorithm"
        dependsOn("prepare.ssh-keygen")
        outputs.files(layout.buildDirectory.file("ssh-keys/user/ssh-key"))
        outputs.files(clientPublicKey)
        commandLine = listOf(
            "ssh-keygen",
            "-b",
            "4096",
            "-t",
            "ed25519",
            "-f",
            "${buildDir}/ssh-keys/user/ssh-key",
            "-N",
            "",
        )
    }

    create("server.ssh-keygen", Exec::class) {
        group = "ssh-keygen"
        description = "Generates ssh keys(server host keys)"
        dependsOn("prepare.ssh-keygen")
        finalizedBy("authorized_keys")
        val keyFiles = objects.fileTree().from(layout.buildDirectory.dir("ssh-keys/etc/ssh"))
        outputs.files(keyFiles)
        commandLine = listOf(
            "ssh-keygen",
            "-A",
            "-b",
            "4096",
            "-f",
            "${buildDir}/ssh-keys",
        )
    }

    create("authorized_keys", Copy::class) {
        group = "ssh-keygen"
        description = "Copies client ssh public key to the server authorized_keys"
        dependsOn("client.ssh-keygen")

        from(clientPublicKey)
        into("${buildDir}/ssh-keys/server")
        rename { "authorized_keys" }
    }
}
