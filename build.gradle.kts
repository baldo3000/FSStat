plugins {
    id("java")
}

group = "me.baldo3000"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

private val vertx = "5.0.10"

dependencies {
    implementation("io.vertx:vertx-core:$vertx")
    testImplementation("io.vertx:vertx-unit:$vertx")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}