package com.metrolist.music.ktorext.encoding

import io.ktor.client.plugins.compression.ContentEncodingConfig

fun ContentEncodingConfig.brotli(quality: Float? = null) {
    customEncoder(BrotliEncoder, quality)
}
