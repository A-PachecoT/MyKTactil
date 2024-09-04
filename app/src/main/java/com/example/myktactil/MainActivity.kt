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
import kotlin.math.pow
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.example.myktactil.algorithms.DrawingAlgorithms

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private val controlPoints = mutableListOf<Pair<Float, Float>>()
    private var isDrawingBezier = false
    private var isBezierMode = false
    private var isBSplineMode = false
    private val bSplinePoints = mutableListOf<Pair<Float, Float>>()
    lateinit var mBitmap: Bitmap
    lateinit var mCanvas: Canvas
    lateinit var mPaint: Paint
    private lateinit var bezierPaint: Paint
    private var lastX: Float = -1f
    private var lastY: Float = -1f
    private var currentAlgorithm = "Normal"

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

        val algorithms = arrayOf("Normal", "Bezier", "B-Spline")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, algorithms)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerAlgorithm.adapter = adapter

        binding.spinnerAlgorithm.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                currentAlgorithm = algorithms[position]
                when (currentAlgorithm) {
                    "Normal" -> {
                        isBezierMode = false
                        isBSplineMode = false
                    }
                    "Bezier" -> {
                        isBezierMode = true
                        isBSplineMode = false
                    }
                    "B-Spline" -> {
                        isBezierMode = false
                        isBSplineMode = true
                    }
                }
                Toast.makeText(applicationContext, "$currentAlgorithm mode activated", Toast.LENGTH_SHORT).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

        binding.myImg.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View, e: MotionEvent): Boolean {
                var proporcionancho = binding.myImg.width
                var proporcionalto = binding.myImg.height
                var x = e.x * 500 / proporcionancho
                var y = e.y * 500 / proporcionalto

                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        when (currentAlgorithm) {
                            "Normal" -> {
                                lastX = x
                                lastY = y
                            }
                            "Bezier" -> {
                                if (controlPoints.size < 4) {
                                    controlPoints.add(Pair(x, y))
                                    drawControlPoint(x, y)
                                }
                                if (controlPoints.size == 4) {
                                    DrawingAlgorithms.drawBezierCurve(mCanvas, mPaint, controlPoints)
                                    binding.myImg.setImageBitmap(mBitmap)
                                    controlPoints.clear()
                                }
                            }
                            "B-Spline" -> {
                                bSplinePoints.add(Pair(x, y))
                                drawControlPoint(x, y)
                                if (bSplinePoints.size >= 4) {
                                    DrawingAlgorithms.drawBSpline(mCanvas, mPaint, bSplinePoints)
                                    binding.myImg.setImageBitmap(mBitmap)
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (currentAlgorithm == "Normal") {
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
                        if (currentAlgorithm == "Normal") {
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

    private fun clearScreen() {
        mCanvas.drawColor(Color.WHITE)
        drawAxes()
        binding.myImg.setImageBitmap(mBitmap)
        bSplinePoints.clear()
        controlPoints.clear()
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