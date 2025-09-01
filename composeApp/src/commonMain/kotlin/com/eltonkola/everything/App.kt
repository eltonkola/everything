package com.eltonkola.everything

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.compose.setSingletonImageLoaderFactory
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.eltonkola.everything.ui.AppNavigation
import com.eltonkola.everything.ui.theme.AppTheme
import okio.FileSystem
import org.jetbrains.compose.ui.tooling.preview.Preview

fun getAsyncImageLoader(context: PlatformContext) =
    ImageLoader.Builder(context)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .memoryCache {
            MemoryCache.Builder()
                .maxSizePercent(context, 0.3)
                .strongReferencesEnabled(true)
                .build()
        }.crossfade(true)
        .logger(DebugLogger())
        .diskCachePolicy(CachePolicy.ENABLED)
        .diskCache { newDiskCache() }
        .build()

fun newDiskCache(): DiskCache {
    return DiskCache.Builder().directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
        .maxSizeBytes(512L * 1024 * 1024) // 512MB
        .build()
}

@Composable
@Preview
fun App() {
    setSingletonImageLoaderFactory { context ->
        getAsyncImageLoader(context)
    }
    AppTheme{
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            AppNavigation()
        }
    }
}
