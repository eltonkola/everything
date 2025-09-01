package com.eltonkola.everything

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform