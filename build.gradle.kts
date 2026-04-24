plugins {
    id("java")
}

group = "me.baldo3000"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val vertx = "5.0.11"
    val jUnit = "6.0.3"
    implementation("io.vertx:vertx-core:$vertx")
    testImplementation("io.vertx:vertx-unit:$vertx")

    testImplementation(platform("org.junit:junit-bom:$jUnit"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}