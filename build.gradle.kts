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
    implementation("com.github.mwiede:jsch:0.2.19")
    implementation("org.apache.sshd:sshd-core:2.13.2")
    implementation("org.apache.sshd:sshd-common:2.13.2")
    implementation("org.apache.sshd:sshd-sftp:2.13.2")

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
}
