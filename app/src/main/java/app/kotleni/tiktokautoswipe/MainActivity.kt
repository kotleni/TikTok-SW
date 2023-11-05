package app.kotleni.tiktokautoswipe

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.MotionEvent.PointerProperties
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import app.kotleni.tiktokautoswipe.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: SharedPreferences
    private var isEnabled = false
    private var score = 0
    private var lastScoreUpdate = 0L

    inner class MyWebViewClient: WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)

            println("onPageFinished, injecting js")

            view?.evaluateJavascript("""
                // CHINA ASS HACK
                
                console.log("in js")
                App.onScriptLoaded()
                
                function startScript() {
                let video = document.getElementsByTagName("video")[0]
                
                video.addEventListener("ended", (event) => {
                    console.log("video is ended")
                    App.onVideoEnded()
                })

                video.addEventListener("playing", (event) => {
                    console.log("video is playing")
                    App.onVideoPlaying()
                })
                }
                
                setTimeout(startScript, 100)
            """.trimIndent(), null)
        }

        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            println(request?.url?.path)
            if(request != null && request.url.path != null && request.url.path!!.contains("oauth2")) {
                val launchBrowser = Intent(Intent.ACTION_VIEW, request.url)
                startActivity(launchBrowser)
                return true
            }

            return super.shouldOverrideUrlLoading(view, request)
        }
    }

    inner class MyChromeClient: WebChromeClient() {

    }

    inner class JSInterface {
        @android.webkit.JavascriptInterface
        fun onScriptLoaded() {
            // Toast.makeText(this@MainActivity, "OK", Toast.LENGTH_SHORT).show()
        }

        @android.webkit.JavascriptInterface
        fun onVideoPlaying() {
            println("onVideoPlaying()")
        }

        @android.webkit.JavascriptInterface
        fun onVideoEnded() {
            println("onVideoEnded()")

            if(System.currentTimeMillis() - lastScoreUpdate > 1_000) {
                updateCounter()
                lastScoreUpdate = System.currentTimeMillis()
            }

            binding.webView.post {
                if(isEnabled) {
                    simulateSwipe()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("main", Context.MODE_PRIVATE)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.webView.webViewClient = MyWebViewClient()
        binding.webView.webChromeClient = MyChromeClient()

        CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true);

        binding.webView.settings.apply {
            javaScriptEnabled = true
            this.allowContentAccess = true
            this.allowFileAccess = true
            this.domStorageEnabled = true
            userAgentString = "Mozilla/5.0 (iPhone; CPU iPhone OS 15_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) CriOS/98.0.4758.85 Mobile/15E148 Safari/604.1"
        }

        val jsInterface = JSInterface()
        binding.webView.addJavascriptInterface(jsInterface, "App")

        binding.webView.loadUrl("https://tiktok.com/")

        binding.toggleButton.text = if(isEnabled) "ВЫКЛ" else "ВКЛ"
        binding.toggleButton.setOnClickListener {
            isEnabled = !isEnabled
            binding.toggleButton.text = if(isEnabled) "ВЫКЛ" else "ВКЛ"
        }

        binding.swipeButton.setOnClickListener {
            simulateSwipe()
        }

        updateCounterView()
    }

    override fun onBackPressed() {
        if(binding.webView.canGoBack())
            binding.webView.goBack()
        else
            super.onBackPressed()
    }

    private fun updateCounter() = GlobalScope.launch(Dispatchers.Main) {
        val totalCount = prefs.getInt("count", 0) + 1
        prefs.edit {
            putInt("count", totalCount)
        }
        score += 1
        updateCounterView()
    }

    private fun updateCounterView() {
        val totalCount = prefs.getInt("count", 0)
        binding.videosScore.text = "${score}/${totalCount}"
    }

    private fun simulateSwipe() = GlobalScope.launch(Dispatchers.Main) {
        var x = 140f
        val height = binding.webView.contentHeight
        val startY = height - 100f
        val endY = (height / 2f) + 100f
        val endY2 = (height / 2f)
        val endY3 = (height / 2f) - 100f

        val downTime = SystemClock.uptimeMillis()
        val eventTime = SystemClock.uptimeMillis()
        val properties = arrayOfNulls<PointerProperties>(1)
        val pp1 = PointerProperties()
        pp1.id = 0
        pp1.toolType = MotionEvent.TOOL_TYPE_FINGER
        properties[0] = pp1
        val pointerCoords = arrayOfNulls<PointerCoords>(1)
        val pc1 = PointerCoords()
        pc1.x = x.toFloat()
        pc1.y = startY
        pc1.pressure = 1f
        pc1.size = 1f
        pointerCoords[0] = pc1

        var motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_DOWN, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        dispatchTouchEvent(motionEvent)

        delay(40)

        pc1.y = endY
        motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_MOVE, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        binding.webView.dispatchTouchEvent(motionEvent)

        delay(40)

        pc1.y = endY2
        motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_MOVE, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        binding.webView.dispatchTouchEvent(motionEvent)

        delay(40)

        pc1.y = endY3
        motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_MOVE, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        binding.webView.dispatchTouchEvent(motionEvent)

        delay(40)

        motionEvent = MotionEvent.obtain(
            downTime, eventTime,
            MotionEvent.ACTION_UP, 1, properties,
            pointerCoords, 0, 0, 1f, 1f, 0, 0, 0, 0
        )
        binding.webView.dispatchTouchEvent(motionEvent)
    }
}