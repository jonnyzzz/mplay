package com.jonnyzzz.mplay.agent.runtime


class MPlayRecorder(
//    val recordingClassName: String,
//    val configClassName: String,
//    val configClasspath: List<String>,
) {
    init {
        println("MPlayRecorder created")
    }


    companion object {
        /**
         * This method is executed in bytecode
         */
        @JvmStatic
        fun getInstance(
            recordingClassName: String,
            configClassName: String,
            configClasspath: List<String>,
        ): MPlayRecorder {
            return MPlayRecorder()
        }
    }
}
