import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.AppOpsManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.content.getSystemService
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.softwareupdate.R
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import io.github.rupinderjeet.kprogresshud.KProgressHUD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

fun Context.showToastShort(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Context.showToastLong(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

private fun View.showSnackbar(message: String, duration: Int) {
    val snackbar = Snackbar.make(this, message, duration)
    snackbar.setAction(context.getString(android.R.string.cancel)) {
        snackbar.dismiss()
    }
    snackbar.show()
}

private const val LONG_SNACKBAR_DURATION = 5000

fun View.showSnackbarShort(message: String) = showSnackbar(message, Snackbar.LENGTH_SHORT)
fun View.showSnackbarLong(message: String) = showSnackbar(message, LONG_SNACKBAR_DURATION)

inline fun <T> Flow<T>.launchAndCollect(
    owner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) {
    owner.launchAndRepeat(state) {
        collect {
            action(it)
        }
    }
}

inline fun <T> Flow<T>.launchAndCollectLatest(
    owner: LifecycleOwner,
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.(T) -> Unit
) {
    owner.launchAndRepeat(state) {
        collectLatest {
            action(it)
        }
    }
}

inline fun LifecycleOwner.launchAndRepeat(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline action: suspend CoroutineScope.() -> Unit
) {
    this.lifecycleScope.launch {
        this@launchAndRepeat.lifecycle.repeatOnLifecycle(state) {
            action()
        }
    }
}

fun <T> List<T>.getSafe(index: Int): T? {
    return if (index in indices) this[index] else null
}

val Context.activity: Activity?
    get() {
        var context = this
        while (true) {
            when (context) {
                is Activity -> return context
                is ContextWrapper -> context = context.baseContext
                else -> return null
            }
        }
    }

fun View.hide() {
    visibility = View.GONE
}

fun View.show() {
    visibility = View.VISIBLE
}

fun View.makeInvisible() {
    visibility = View.INVISIBLE
}

private val originalDimensions = mutableMapOf<Int, Pair<Int, Int>>()
/**
 * Hide a view by setting its layout params to 0x0
 */
fun View.hideViewByLayoutParams() {
    if (!originalDimensions.containsKey(this.id)) {
        originalDimensions[this.id] = Pair(layoutParams.width, layoutParams.height)
    }

    when (val view = this) {
        is ConstraintLayout -> {
            val layoutParams = ConstraintLayout.LayoutParams(0, 0)
            view.layoutParams = layoutParams
        }

        is LinearLayout -> {
            val layoutParams = LinearLayout.LayoutParams(0, 0)
            view.layoutParams = layoutParams
        }

        is FrameLayout -> {
            val layoutParams = FrameLayout.LayoutParams(0, 0)
            view.layoutParams = layoutParams
        }

        is RelativeLayout -> {
            val layoutParams = RelativeLayout.LayoutParams(0, 0)
            view.layoutParams = layoutParams
        }

        is Toolbar -> {
            val layoutParams = Toolbar.LayoutParams(0, 0)
            view.layoutParams = layoutParams
        }

        is ViewGroup -> {
            val layoutParams = ViewGroup.LayoutParams(0, 0)
            view.layoutParams = layoutParams
        }

        else -> {
            val params = view.layoutParams
            params.width = 0
            params.height = 0
            view.layoutParams = params
        }
    }
}

fun View.showViewByLayoutParams() {
    val original = originalDimensions[this.id] ?: return

    when (this) {
        is ConstraintLayout -> {
            layoutParams = ConstraintLayout.LayoutParams(original.first, original.second)
        }
        is LinearLayout -> {
            layoutParams = LinearLayout.LayoutParams(original.first, original.second)
        }
        is FrameLayout -> {
            layoutParams = FrameLayout.LayoutParams(original.first, original.second)
        }
        is RelativeLayout -> {
            layoutParams = RelativeLayout.LayoutParams(original.first, original.second)
        }
        is Toolbar -> {
            layoutParams = Toolbar.LayoutParams(original.first, original.second)
        }
        is ViewGroup -> {
            layoutParams = ViewGroup.LayoutParams(original.first, original.second)
        }
        else -> {
            val params = layoutParams
            params.width = original.first
            params.height = original.second
            layoutParams = params
        }
    }

    // Remove from cache after restoring
//    originalDimensions.remove(this.id)
}

fun Context.isServiceRunning(serviceClass: Class<*>): Boolean {
    val manager = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
    for (service in manager?.getRunningServices(Integer.MAX_VALUE) ?: emptyList()) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun Context.startService(clazz: Class<*>) {
    if (isServiceRunning(clazz)) return

    Intent(this, clazz).also { service ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, service)
        } else {
            startService(service)
        }
    }
}

fun Context.getDrawableIdFromName(resName: String): Int {
    // noinspection DiscouragedApi
    return resources.getIdentifier(resName, "drawable", packageName)
}

fun Context.getColorIdFromName(resName: String?): Int {
    // noinspection DiscouragedApi
    return resources.getIdentifier(resName, "color", packageName)
}

fun Activity.navigateToActivity(clazz: Class<*>, finishCurrentActivity: Boolean = false, finishAllActivities: Boolean = false) {
    Intent(this, clazz).apply {
        startActivity(this)
        if (finishAllActivities) {
            finishAffinity()
        } else {
            if (finishCurrentActivity) {
                finish()
            }
        }
    }
}

fun Activity.navigateToActivity(intent: Intent, finishCurrentActivity: Boolean = false, finishAllActivities: Boolean = false) {
    startActivity(intent)
    if (finishAllActivities) {
        finishAffinity()
    } else {
        if (finishCurrentActivity) {
            finish()
        }
    }
}

@SuppressLint("ObsoleteSdkInt")
@Suppress("DEPRECATION")
fun Window.expandContentToStatusBar() {
    if (Build.VERSION.SDK_INT in 19..20) {
        addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
    }
    if (Build.VERSION.SDK_INT >= 19) {
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    }
    if (Build.VERSION.SDK_INT >= 21) {
        clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        statusBarColor = Color.TRANSPARENT
    }
}

fun View.containTouchRippleInViewBounds() {
    clipToOutline = true
}

/**
 * This method converts dp unit to equivalent pixels, depending on device
 * density.
 *
 * @param value A value in dp(density independent pixels) unit. Which we need
 * to convert into pixels
 *
 * @return An integer value to represent px equivalent to dp
 */
fun Context.convertDpToPixels(value: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
        .toInt()
}

/**
 * This method converts sp unit to equivalent pixels, depending on device
 * density.
 *
 * @param value A value in sp(scalable independent pixels) unit. Which we need
 * to convert into pixels
 *
 * @return An integer value to represent px equivalent to sp
 */
fun Context.convertSpToPixels(value: Float): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
        .toInt()
}

/**
 * This method converts pixels to equivalent dp, depending on device density.
 *
 * @param value A value in pixels (px) unit. Which we need to convert into dp
 *
 * @return A float value to represent dp equivalent to px
 */
fun Context.convertPixelsToDp(value: Float): Float {
    val density = resources.displayMetrics.density
    return value / density
}

fun Context.getColorFromId(@ColorRes resId: Int): Int = ContextCompat.getColor(this, resId)

fun Fragment.getColor(@ColorRes resId: Int): Int = requireContext().getColorFromId(resId)

fun Fragment.getDrawable(@DrawableRes resId: Int): Drawable? = AppCompatResources.getDrawable(requireContext(), resId)

fun TextView.updateTextStyleAtSpecificPosition(
    startIndex: Int,
    endIndex: Int,
    @ColorRes color: Int,
    @FontRes font: Int,
    style: Int = Typeface.NORMAL,
    underline: Boolean = false,
    applyClickSpan: Boolean = false,
    action: (() -> Unit)? = null
) {
    try {
        val spannableString = if (text is Spannable) {
            text as Spannable
        } else {
            SpannableString(text)
        }

        // Apply color span
        spannableString.setSpan(
            ForegroundColorSpan(ContextCompat.getColor(context, color)),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply font span
        spannableString.setSpan(object : TypefaceSpan(null) {
            override fun updateDrawState(ds: TextPaint) {
                try {
                    ds.typeface = Typeface.create(
                        ResourcesCompat.getFont(context, font) ?: Typeface.DEFAULT,
                        style
                    )
                } catch (ex: Resources.NotFoundException) {
//                    Firebase.crashlytics.recordException(ex)
                    ex.printStackTrace()
                } catch (ex: Exception) {
//                    Firebase.crashlytics.recordException(ex)
                    ex.printStackTrace()
                }
            }
        }, startIndex, endIndex, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)

        if (underline) {
            spannableString.setSpan(
                UnderlineSpan(),
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        if (applyClickSpan) {
            // Set click span
            spannableString.setSpan(
                object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        action?.invoke()
                    }
                },
                startIndex,
                endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

            movementMethod = LinkMovementMethod.getInstance()
        }

        text = spannableString
    } catch (ex: Resources.NotFoundException) {
//        Firebase.crashlytics.recordException(ex)
        ex.printStackTrace()
    } catch (ex: Exception) {
//        Firebase.crashlytics.recordException(ex)
        ex.printStackTrace()
    }
}

fun EditText.afterTextChanged(): Flow<String> = callbackFlow {
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(editable: Editable?) {
            trySend(editable.toString())
        }
    }

    addTextChangedListener(textWatcher)

    awaitClose { removeTextChangedListener(textWatcher) }
}

fun Context.getProgressBar(message: String = ""): KProgressHUD =
    KProgressHUD.create(this)
        .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
        .setCancellable(false)
        .setAnimationSpeed(1)
        .setDimAmount(0.5f)
        .setDetailsLabel(message)

val Context.isNotificationPermissionGranted: Boolean
    get() {
        val isNotificationPermissionGranted =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true


        return isNotificationPermissionGranted
    }

fun Context.copyTextToClipBoard(text: String) {
    val clipboardManager = getSystemService<ClipboardManager>()
    val clip = ClipData.newPlainText(getString(R.string.copied_text), text)
    clipboardManager?.setPrimaryClip(clip)
    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        // Deprecated in API 26
        vibrator.vibrate(200)
    }
//    showToastShort(getString(R.string.copied_to_clipboard))
}

fun Context.getCopiedTextFromClipboard(): String {
    val clipboardManager = getSystemService<ClipboardManager>()

    if (clipboardManager?.hasPrimaryClip() == true) {
        clipboardManager.primaryClip?.let { clip ->
            return clip.getItemAt(0).text.toString()
        } ?: run {
            showToastShort(getString(R.string.clipboard_is_empty))
        }
    } else {
        showToastShort(getString(R.string.clipboard_might_be_empty))
    }

    return ""
}

fun View.getNavigationBarHeight(): Int {
    val windowInsets = ViewCompat.getRootWindowInsets(this)
    return windowInsets?.getInsets(WindowInsetsCompat.Type.navigationBars())?.bottom ?: 0
}

fun View.isKeyboardOpened(): Boolean {
    val windowInsets = ViewCompat.getRootWindowInsets(this)
    val imeHeight = windowInsets?.getInsets(WindowInsetsCompat.Type.ime())?.bottom ?: 0
    return imeHeight > 0
}

fun Context.getNavigationBarHeight(): Int {
    // noinspection InternalInsetResource
    val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
    if (resourceId > 0) {
        return resources.getDimensionPixelSize(resourceId)
    }
    return 0
}

fun Context.convertUriToTempFile(uri: Uri): File? {
    var file: File? = null

    contentResolver.openInputStream(uri)?.use { inputStream ->
        file = File(cacheDir, "temp_image.jpg")
        file?.outputStream()?.use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    return file
}

fun <T> SharedPreferences.writeList(gson: Gson, key: String, data: List<T>) {
    val json = gson.toJson(data)
    edit { putString(key, json) }
}

inline fun <reified T> SharedPreferences.readList(gson: Gson, key: String): List<T> {
    val json = getString(key, "[]") ?: "[]"
    val type = object : TypeToken<List<T>>() {}.type

    return try {
        gson.fromJson(json, type)
    } catch (e: JsonSyntaxException) {
        emptyList()
    }
}

fun RadioButton.observeCheckedState(): Flow<Boolean> = callbackFlow {
    setOnCheckedChangeListener { _, isChecked ->
        trySend(isChecked)
    }

    awaitClose {  }
}

fun Context.getDefaultBrowserPackageName(): String? {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfo?.activityInfo?.packageName
}

fun AppOpsManager.isUsageAccessPermissionGranted(packageName: String): Boolean {
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
    } else {
        checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
    }

    return mode == AppOpsManager.MODE_ALLOWED
}

fun View.updateDrawableCornerRadius(radiusInDP: Float) {
    val background = background

    if (background is GradientDrawable) {
        background.cornerRadius = context.convertDpToPixels(radiusInDP).toFloat()
    } else if (background is ColorDrawable) {
        val drawable = GradientDrawable()
        drawable.setColor(background.color)
        drawable.cornerRadius = context.convertDpToPixels(radiusInDP).toFloat()
        this.background = drawable
    }
}

fun View.updateDrawableCornerRadiusAndColor(
    radiusInDP: Float,
    @ColorRes color: Int? = null
) {
    when (val background = background) {
        is GradientDrawable -> {
            background.cornerRadius = context.convertDpToPixels(radiusInDP).toFloat()
            color?.let { background.setColor(ContextCompat.getColor(context, it)) }
        }
        is ColorDrawable -> {
            val drawable = GradientDrawable().apply {
                setColor(color ?: background.color)
                cornerRadius = context.convertDpToPixels(radiusInDP).toFloat()
            }
            this.background = drawable
        }
        else -> {
            color?.let {
                val drawable = GradientDrawable().apply {
                    setColor(ContextCompat.getColor(context, it))
                    cornerRadius = context.convertDpToPixels(radiusInDP).toFloat()
                }
                this.background = drawable
            } ?: run {
                val drawable = GradientDrawable().apply {
                    cornerRadius = context.convertDpToPixels(radiusInDP).toFloat()
                }
                this.background = drawable
            }
        }
    }
}

fun View.updateDrawableSolidColor(@ColorRes color: Int) {
    when (val background = background) {
        is GradientDrawable -> {
            background.setColor(ContextCompat.getColor(context, color))
        }
        is ColorDrawable -> {
            val drawable = GradientDrawable().apply {
                setColor(ContextCompat.getColor(context, color))
            }
            this.background = drawable
        }
        else -> {
            val drawable = GradientDrawable().apply {
                setColor(ContextCompat.getColor(context, color))
            }
            this.background = drawable
        }
    }
}

fun View.updateDrawableGradientColors(@ColorRes colors: IntArray) {
    when (val background = background) {
        is GradientDrawable -> {
            val colorInts = colors.map { ContextCompat.getColor(context, it) }.toIntArray()
            background.colors = colorInts
        }
        else -> {
            val drawable = GradientDrawable().apply {
                this.colors = colors.map { ContextCompat.getColor(context, it) }.toIntArray()
                // Set default orientation if none exists
                if (this.orientation == null) {
                    this.orientation = GradientDrawable.Orientation.LEFT_RIGHT
                }
            }
            this.background = drawable
        }
    }
}

fun Long.formatBytes(): String {
    val bytes = this

    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024

    return when {
        bytes >= gb -> String.format(Locale.ENGLISH, "%.2f GB", bytes / gb)
        bytes >= mb -> String.format(Locale.ENGLISH, "%.2f MB", bytes / mb)
        bytes >= kb -> String.format(Locale.ENGLISH, "%.2f KB", bytes / kb)
        else -> "$bytes B"
    }
}
