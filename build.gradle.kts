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
    val rxJava = "4.0.0-alpha-13"
    implementation("io.vertx:vertx-core:$vertx")
    implementation("io.reactivex.rxjava4:rxjava:$rxJava")

    testImplementation("io.vertx:vertx-unit:$vertx")
    testImplementation(platform("org.junit:junit-bom:$jUnit"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}