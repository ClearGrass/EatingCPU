package com.cleargrass.app.eattingcpu

import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.IOException

class CpuUtil {
    companion object {
        fun getMaxCpuFreq(cpu: Int): Int {
            var result = ""
            val cmd: ProcessBuilder
            try {
                val args = arrayOf("/system/bin/cat", "/sys/devices/system/cpu/cpu$cpu/cpufreq/cpuinfo_max_freq")
                cmd = ProcessBuilder(*args)
                val process = cmd.start()
                val `in` = process.inputStream
                val re = ByteArray(24)
                while (`in`.read(re) !== -1) {
                    result = result + String(re)
                }
                `in`.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
                result = "-1"
            }

            return result.trim { it <= ' ' }.toInt()
        }

        fun getMinCpuFreq(cpu: Int): Int {
            var result = ""
            val cmd: ProcessBuilder
            try {
                val args = arrayOf("/system/bin/cat", "/sys/devices/system/cpu/cpu$cpu/cpufreq/cpuinfo_min_freq")
                cmd = ProcessBuilder(*args)
                val process = cmd.start()
                val `in` = process.inputStream
                val re = ByteArray(24)
                while (`in`.read(re) !== -1) {
                    result = result + String(re)
                }
                `in`.close()
            } catch (ex: IOException) {
                ex.printStackTrace()
                result = "-1"
            }

            return result.trim { it <= ' ' }.toInt()
        }
        fun getCurCpuFreq(cpu: Int): Int {
            var result = "-1"
            try {
                val fr = FileReader(
                        "/sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_cur_freq")
                val br = BufferedReader(fr)
                val text = br.readLine()
                result = text.trim({ it <= ' ' })
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                result = "-1"
            } catch (e: IOException) {
                e.printStackTrace()
                result = "-1"
            }

            return result.toInt()
        }
    }
}