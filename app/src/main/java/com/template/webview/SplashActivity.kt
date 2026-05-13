package PACKAGE_PLACEHOLDER

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.ComponentActivity

class SplashActivity : ComponentActivity() {

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        val image =
            ImageView(this)

        image.setImageResource(
            R.drawable.splash_image
        )

        image.scaleType =
            ImageView.ScaleType.CENTER_CROP

        setContentView(image)

        Handler(
            Looper.getMainLooper()
        ).postDelayed({

            startActivity(

                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()

        }, 2000)
    }
}