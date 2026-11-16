package com.lazy_durtles.wk.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.lazy_durtles.wk.GlobalSettings
import com.lazy_durtles.wk.WkApplication
import com.lazy_durtles.wk.db.model.MnemonicImage
import com.lazy_durtles.wk.db.model.Subject
import com.lazy_durtles.wk.util.MnemonicImageUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * The WaniKani mnemonic background drawing for a subject, if the optional data
 * pack is enabled and the SVG has been downloaded. The artwork is copyright
 * WaniKani / Tofugu. Rendered with Coil's SVG decoder from the local file.
 */
@Composable
fun MnemonicImageSection(subject: Subject, modifier: Modifier = Modifier) {
    if (!GlobalSettings.MnemonicImages.getEnabled()) {
        return
    }
    var imageFile by remember(subject.getId()) { mutableStateOf<File?>(null) }
    LaunchedEffect(subject.getId()) {
        imageFile = withContext(Dispatchers.IO) {
            val record: MnemonicImage? = WkApplication.getDatabase().mnemonicImageDao().getBySubjectId(subject.getId())
            if (record != null) MnemonicImageUtil.getExistingFile(record) else null
        }
    }
    val file = imageFile ?: return
    val context = LocalContext.current
    val imageLoader = remember(context) {
        coil.ImageLoader.Builder(context)
            .components { add(coil.decode.SvgDecoder.Factory()) }
            .build()
    }
    coil.compose.AsyncImage(
        model = file,
        contentDescription = "WaniKani mnemonic image (copyright WaniKani / Tofugu)",
        imageLoader = imageLoader,
        contentScale = ContentScale.FillWidth,
        modifier = modifier.fillMaxWidth(),
    )
}
