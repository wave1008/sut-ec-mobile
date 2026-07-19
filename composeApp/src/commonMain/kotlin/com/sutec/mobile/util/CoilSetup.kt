package com.sutec.mobile.util

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.memory.MemoryCache
import coil3.network.ktor3.KtorNetworkFetcherFactory
import coil3.request.crossfade

// Coil3 の共有 ImageLoader。KtorNetworkFetcherFactory はクラスパス上のエンジン
// (Android=OkHttp / iOS=Darwin, build.gradle で付与)を既定 HttpClient 経由で使用。
// App から setSingletonImageLoaderFactory { buildImageLoader(it) } で登録する。
fun buildImageLoader(context: PlatformContext): ImageLoader =
    ImageLoader.Builder(context)
        .components { add(KtorNetworkFetcherFactory()) }
        .crossfade(true)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.25)
                .build()
        }
        .build()
