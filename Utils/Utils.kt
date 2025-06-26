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

private fun Context.showToast(message: String, duration: Int) =
    Toast.makeText(this, message, duration).show()

private fun View.showSnackbar(message: String, duration: Int) {
    val snackbar = Snackbar.make(this, message, duration)
    snackbar.setAction("Dismiss") {
        snackbar.dismiss()
    }
    snackbar.show()
}

private const val LONG_SNACKBAR_DURATION = 5000

fun View.showSnackbarShort(message: String) = showSnackbar(message, Snackbar.LENGTH_SHORT)
fun View.showSnackbarLong(message: String) = showSnackbar(message, LONG_SNACKBAR_DURATION)

fun Context.showToastShort(message: String) = showToast(message, Toast.LENGTH_SHORT)

fun Context.showToastLong(message: String) = showToast(message, Toast.LENGTH_LONG)

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.makeInvisible() {
    visibility = View.INVISIBLE
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

fun AppOpsManager.isUsageStatsPermissionGranted(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
    } else {
        checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
    }
}


fun getOverlayViewLayoutParams(): WindowManager.LayoutParams {
    val layoutType: Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } catch (ex: WindowManager.BadTokenException) {
            WindowManager.LayoutParams.TYPE_PHONE
        }
    } else {
        WindowManager.LayoutParams.TYPE_PHONE
    }

    val layoutFlags = (WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
            or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            or WindowManager.LayoutParams.FLAG_FULLSCREEN
            or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
            or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

    return WindowManager.LayoutParams(
        WindowManager.LayoutParams.MATCH_PARENT,
        WindowManager.LayoutParams.MATCH_PARENT,
        layoutType,
        layoutFlags,
        PixelFormat.TRANSLUCENT
    )
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

fun UsageStatsManager.getForegroundApp() = flow<String> {
    while (true) {
        val currentTime = System.currentTimeMillis()
        val interval = currentTime - 1000 * 60
        val usageStatsList = queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            interval,
            currentTime
        )

        var foregroundApp: String? = null
        if (usageStatsList != null && usageStatsList.isNotEmpty()) {
            var recentStats: UsageStats? = null
            for (usageStats in usageStatsList) {
                if (recentStats == null || usageStats.lastTimeUsed > recentStats.lastTimeUsed) {
                    recentStats = usageStats
                }
            }
            foregroundApp = recentStats?.packageName
        }
        foregroundApp?.let {
            emit(foregroundApp)
        }

        delay(100)
    }
}.distinctUntilChanged().flowOn(Dispatchers.IO)

fun CameraManager.isCameraAvailable(context: Context): Boolean {
    return try {
        cameraIdList.isNotEmpty() &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) &&
                Camera.getNumberOfCameras() > 0
    } catch (e: CameraAccessException) {
        false
    }
}

fun Context.getAppInfo(packageName: String): Int {
    return try {
        packageManager.getApplicationInfo(packageName, 0).icon
    } catch (e: PackageManager.NameNotFoundException) {
        R.drawable.bg_indicator
    }
}

@SuppressLint("InternalInsetResource", "DiscouragedApi")
fun Activity.getStatusBarHeight(): Int {
    fun getStatusBarHeightUsingLegacyMethod(): Int {
        return resources.getIdentifier("status_bar_height", "dimen", "android").let { resId ->
            if (resId > 0) resources.getDimensionPixelSize(resId) else resources.getDimensionPixelSize(
                com.intuit.sdp.R.dimen._24sdp
            )
        }
    }

    var statusBarHeight = 0
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        val insets = windowManager.currentWindowMetrics.windowInsets
        statusBarHeight = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
        if (statusBarHeight == 0) {
            statusBarHeight = getStatusBarHeightUsingLegacyMethod()
        }
    } else {
        statusBarHeight = getStatusBarHeightUsingLegacyMethod()
    }
    return statusBarHeight
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
        .roundToInt()
}

val Int.dp: Int get() =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics).roundToInt()

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
//    if (isAnyActivityRunning.get().not()) return

    if (isServiceRunning(clazz)) return

    Intent(this, clazz).also { service ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, service)
        } else {
            startService(service)
        }
    }
}

fun Context.stopService(clazz: Class<*>) {
    Intent(this, clazz).also(::stopService)
}

fun Context.getDrawableIdFromName(resName: String): Int {
    // noinspection DiscouragedApi
    return resources.getIdentifier(resName, "drawable", packageName)
}

fun Context.getColorIdFromName(resName: String?): Int {
    // noinspection DiscouragedApi
    return resources.getIdentifier(resName, "color", packageName)
}

fun EditText.afterTextChanged(): Flow<String> = callbackFlow {
    val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(s: Editable?) {
            trySend(s.toString())
        }
    }

    addTextChangedListener(textWatcher)

    awaitClose { removeTextChangedListener(textWatcher) }
}

fun Context.getDefaultBrowserPackageName(): String? {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://"))
    val resolveInfo = packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)

    return resolveInfo?.activityInfo?.packageName
}

suspend fun Context.getCompleteAddress(latitude: Double?, longitude: Double?): String = withContext(Dispatchers.IO) {
    if (latitude == null || longitude == null)
        return@withContext getString(R.string.unknown)

    val geocoder = Geocoder(this@getCompleteAddress)
    try {
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (addresses?.isNotEmpty() == true) {
            val address = addresses[0]
            address.getAddressLine(0)
        } else {
            getString(R.string.unknown)
        }
    } catch (ex: Exception) {
        coroutineContext.ensureActive()

        getString(R.string.unknown)
    }
}

suspend fun Context.getLocationFromAddress(location: String): LatLng? = withContext(Dispatchers.IO) {
    val geocoder = Geocoder(this@getLocationFromAddress)

    try {
        val addresses = geocoder.getFromLocationName(location, 1)
        if (addresses?.isNotEmpty() == true) {
            val address = addresses[0]
            LatLng(address.latitude, address.longitude)
        } else {
            null
        }
    } catch (ex: Exception) {
        coroutineContext.ensureActive()

        null
    }
}

fun EditText.autoCapitalizeLetters() {
    try {
        filters = arrayOf(InputFilter.AllCaps())
    } catch (ex: Exception) {
        Firebase.crashlytics.recordException(ex)
        println("autoCapitalizeLetters threw:\n${ex.javaClass.name}\n${ex.stackTraceToString()}")
    }
}

@SuppressLint("MissingPermission")
fun getLastLocation(lastLocation: (LatLng) -> Unit) {
    fusedLocationProviderClient.lastLocation.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            task.result?.let { location ->
                lastLocation.invoke(LatLng(location.latitude, location.longitude))
            } ?: lastLocation.invoke(LatLng(0.0, 0.0))
        } else {
            lastLocation.invoke(LatLng(0.0, 0.0))
        }
    }
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

fun Context.loadImageAndConvertToBitmap(imageUrl: String): Bitmap? {
    return try {
        Glide.with(this)
            .asBitmap()
            .load(imageUrl)
            .placeholder(R.drawable.ic_profile)
            .error(R.drawable.ic_profile)
            .circleCrop()
            .submit()
            .get()
    } catch (e: Exception) {
        Log.e("ImageLoadingError", "Failed to load image: $e")
        null // Or return a placeholder bitmap
    }
}

SuppressLint("InflateParams")
suspend fun Context.getMarkerBitmapFromView(imageUrl: String, isOnline: Boolean): Bitmap {
    val customMarkerView: View = (getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?)!!.inflate(
        R.layout.item_custom_marker_layout,
        null
    )
    Log.i("TAG", "getMarkerBitmapFromView: $isOnline")
//    if(!isOnline) {
//        val markerImageview: ImageView = customMarkerView.findViewById(R.id.markerImageview)
//        markerImageview.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_marker_offline))
//    }
    val markerImageView: ImageView = customMarkerView.findViewById(R.id.userImageview)
    val bitmap: Bitmap?
    withContext(Dispatchers.IO) {
        bitmap = loadImageAndConvertToBitmap(imageUrl)
        if (bitmap == null) {
            markerImageView.setImageDrawable(ContextCompat.getDrawable(this@getMarkerBitmapFromView, R.drawable.ic_profile))
        } else {
            markerImageView.setImageBitmap(bitmap)
        }
    }

    customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
    customMarkerView.layout(
        0,
        0,
        customMarkerView.measuredWidth,
        customMarkerView.measuredHeight
    )
    customMarkerView.buildDrawingCache()
    val returnedBitmap = Bitmap.createBitmap(
        customMarkerView.measuredWidth, customMarkerView.measuredHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(returnedBitmap)
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN)
    customMarkerView.draw(canvas)
    return returnedBitmap
}

fun Activity.restartApp() {
    val intent = packageManager.getLaunchIntentForPackage(packageName)
    val mainIntent = Intent.makeRestartActivityTask(intent?.component)
    // Required for API 34 and later
    // Ref: https://developer.android.com/about/versions/14/behavior-changes-14#safer-intents
    mainIntent.setPackage(packageName)
    startActivity(mainIntent)
    finishAffinity()
}

fun TextView.updateTextStyleAtSpecificPosition(
    startIndex: Int,
    endIndex: Int,
    @ColorRes color: Int,
    @FontRes font: Int,
    style: Int = Typeface.NORMAL,
    underline: Boolean = false,
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
            ForegroundColorSpan(resources.getColor(color, null)),
            startIndex,
            endIndex,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Apply font span
        spannableString.setSpan(object : TypefaceSpan(null) {
            override fun updateDrawState(ds: TextPaint) {
                try {
                    ds.typeface = Typeface.create(ResourcesCompat.getFont(context, font) ?: Typeface.DEFAULT, style)
                } catch (ex: NotFoundException) {
                    Firebase.crashlytics.recordException(ex)
                    ex.printStackTrace()
                } catch (ex: Exception) {
                    Firebase.crashlytics.recordException(ex)
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

        text = spannableString
    } catch (ex: NotFoundException) {
        Firebase.crashlytics.recordException(ex)
        ex.printStackTrace()
    } catch (ex: Exception) {
        Firebase.crashlytics.recordException(ex)
        ex.printStackTrace()
    }
}

fun Context.isGooglePlayServicesAvailable(): Boolean {
    val googleApiAvailability = GoogleApiAvailability.getInstance()
    val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this)

    return resultCode == ConnectionResult.SUCCESS
}
