package com.example.myktactil

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnClickListener
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myktactil.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private val controlPoints = mutableListOf<Pair<Float, Float>>()
    private var isDrawingBezier = false
    private var isBezierMode = false
    lateinit var mBitmap: Bitmap
    lateinit var mCanvas: Canvas
    lateinit var mPaint: Paint
    private lateinit var bezierPaint: Paint
    private var lastX: Float = -1f
    private var lastY: Float = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mBitmap = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mBitmap)
        mPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2F
            isAntiAlias = true
        }
        bezierPaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 4F
            isAntiAlias = true
        }

        // color del imageView
        mCanvas.drawColor(Color.WHITE)
        binding.myImg.setImageBitmap(mBitmap)

        drawAxes()

        val displayMetrics = DisplayMetrics().also {
            windowManager.defaultDisplay.getMetrics(it)
        }

        binding.myImg.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                var proporcionancho = binding.myImg.width
                var proporcionalto = binding.myImg.height
                var x = e.x * 500 / proporcionancho
                var y = e.y * 500 / proporcionalto

                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (isBezierMode) {
                            if (!isDrawingBezier) {
                                controlPoints.clear()
                                isDrawingBezier = true
                            }
                            controlPoints.add(Pair(x, y))
                            drawControlPoint(x, y)
                        } else {
                            // Normal drawing mode
                            lastX = x
                            lastY = y
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (!isBezierMode) {
                            // Normal drawing mode
                            if (lastX != -1f && lastY != -1f) {
                                mCanvas.drawLine(lastX, lastY, x, y, mPaint)
                                binding.myImg.setImageBitmap(mBitmap)
                            }
                            lastX = x
                            lastY = y
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isBezierMode && controlPoints.size == 4) {
                            drawBezierCurve()
                            isDrawingBezier = false
                            controlPoints.clear()
                        } else if (!isBezierMode) {
                            lastX = -1f
                            lastY = -1f
                        }
                    }
                }

                // Update position labels
                var mensaje1 = "($x, $y)"
                binding.lblposicion.text = mensaje1
                var mensaje2 = "(${x - 250}, ${y - 250})"
                binding.lblcoordenada.text = mensaje2

                return true
            }
        })

        binding.btnrojo.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                mPaint.color = Color.RED
            }
        })
        binding.btnverde.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                mPaint.color = Color.GREEN
            }
        })
        binding.btnazul.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                mPaint.color = Color.BLUE
            }
        })
        binding.bttncambiar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                if(binding.chkcambiartamano.isChecked){
                    var valor:String = binding.txtsize.getText().toString()
                    var tamano:Float = valor.toFloat()
                    mPaint.strokeWidth = tamano

                    // Toast: Mandar mensaje emergente
                    var mensaje:String = "Grosor cambiado"
                    val myToast = Toast.makeText(applicationContext,mensaje,Toast.LENGTH_LONG)
                    myToast.setGravity(Gravity.LEFT, 200, 200) // No es necesario
                    myToast.show()
                }
            }

        })

        binding.switchBezier.setOnClickListener {
            isBezierMode = !isBezierMode
            if (isBezierMode) {
                Toast.makeText(this, "Bezier mode activated. Tap 4 points to draw a curve.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Normal drawing mode activated.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnClear.setOnClickListener {
            clearScreen()
        }

        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
    }

    private fun drawControlPoint(x: Float, y: Float) {
        val tempPaint = Paint(mPaint).apply {
            style = Paint.Style.FILL
        }
        mCanvas.drawCircle(x, y, 5f, tempPaint)
        binding.myImg.setImageBitmap(mBitmap)
    }

    private fun drawBezierCurve() {
        if (controlPoints.size != 4) return

        val path = Path()
        path.moveTo(controlPoints[0].first, controlPoints[0].second)

        for (t in 0..100) {
            val tt = t / 100f
            val x = bezierPoint(tt, controlPoints[0].first, controlPoints[1].first, controlPoints[2].first, controlPoints[3].first)
            val y = bezierPoint(tt, controlPoints[0].second, controlPoints[1].second, controlPoints[2].second, controlPoints[3].second)
            path.lineTo(x, y)
        }

        bezierPaint.color = mPaint.color  // Use the current color from the palette
        mCanvas.drawPath(path, bezierPaint)

        // Erase control points
        val erasePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        for (point in controlPoints) {
            mCanvas.drawCircle(point.first, point.second, 6f, erasePaint)
        }

        binding.myImg.setImageBitmap(mBitmap)
    }

    private fun bezierPoint(t: Float, p0: Float, p1: Float, p2: Float, p3: Float): Float {
        val u = 1 - t
        return u * u * u * p0 +
               3 * u * u * t * p1 +
               3 * u * t * t * p2 +
               t * t * t * p3
    }

    private fun clearScreen() {
        mCanvas.drawColor(Color.WHITE)
        drawAxes()
        binding.myImg.setImageBitmap(mBitmap)
    }

    private fun drawAxes() {
        val axesPaint = Paint().apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        mCanvas.drawLine(0f, mCanvas.height / 2f, mCanvas.width.toFloat(), mCanvas.height / 2f, axesPaint)
        mCanvas.drawLine(mCanvas.width / 2f, 0f, mCanvas.width / 2f, mCanvas.height.toFloat(), axesPaint)
    }
}