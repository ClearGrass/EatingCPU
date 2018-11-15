package com.cleargrass.app.eattingcpu

import java.math.BigDecimal

class Cpu (val min: Int, val max: Int, var current: Int) {
    var percent = current.toDouble() / max
        get() = current.toDouble() / max


    override fun toString(): String {
        return "Min: ${min/1000}K\nMax: ${max/1000}K\nNow: ${current/1000}K\n${(percent * 100).toInt()}%"
    }
}