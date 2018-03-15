package org.jitsi.jibri.util

import java.lang.reflect.Field
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

/**
 * Mimic the "pid" member of Java 9's [Process].  This can't be
 * an extension function as it gets called from a Java context
 * (which wouldn't see the extension function as a normal
 * member)
 */
fun pid(p: Process): Long {
    var pid: Long = -1

    try {
        if (p.javaClass.name.equals("java.lang.UNIXProcess")) {
            val field: Field = p.javaClass.getDeclaredField("pid")
            field.isAccessible = true
            pid = field.getLong(p)
            field.isAccessible = false
        }
    } catch (e: Exception ) {
        pid = -1
    }
    return pid
}

fun stopProcess(p: Process?, name: String, logger: Logger) {
    p?.let {
        val pid = pid(it)
        logger.info("Sending SIGINT to $name proc $pid")
        Runtime.getRuntime().exec("kill -s SIGINT $pid")
    } ?: run {
        logger.info("Ffmpeg had already exited")
    }
    p?.waitFor(10, TimeUnit.SECONDS)
    p?.isAlive.let {
        // This isn't great, as killing ffmpeg this way will corrupt
        // the entire recording (from what I've seen)
        p?.destroyForcibly()
    }
    logger.info("$name exited with ${p?.exitValue()}")
}
