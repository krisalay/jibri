package org.jitsi.capture.ffmpeg.executors

data class FfmpegExecutorParams(
        val resolution: String = "1280x720",
        val framerate: Int = 30,
        val videoEncodePreset: String = "veryfast",
        val queueSize: Int = 4096,
        val videoInputDevice: String = "FaceTime HD Camera",
        val audioInputDevice: String = "Built-in Microphone",
        val streamingMaxBitrate: Int = 2976,
        val streamingBufSize: Int = streamingMaxBitrate * 2,
        // The range of the CRF scale is 0–51, where 0 is lossless,
        // 23 is the default, and 51 is worst quality possible. A lower value
        // generally leads to higher quality, and a subjectively sane range is
        // 17–28. Consider 17 or 18 to be visually lossless or nearly so;
        // it should look the same or nearly the same as the input but it
        // isn't technically lossless.
        // https://trac.ffmpeg.org/wiki/Encode/H.264#crf
        val h264ConstantRateFactor: Int = 25,
        // TODO: if we want to make some of these nullable, we can still
        // assign a default by doing something here like:
        // val gopSize: Int = framerate?.times( 2) ?: 30 * 2,
        val gopSize: Int = framerate * 2,
        val display: String = ":0"
)