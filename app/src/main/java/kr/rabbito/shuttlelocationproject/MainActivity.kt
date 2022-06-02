package kr.rabbito.shuttlelocationproject

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.RelativeLayout
import android.widget.Toast
import android.widget.ZoomControls
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kr.rabbito.shuttlelocationproject.data.Location
import kr.rabbito.shuttlelocationproject.data.tuk_route
import kr.rabbito.shuttlelocationproject.data.tuk_route_down
import kr.rabbito.shuttlelocationproject.data.tuk_route_up
import kr.rabbito.shuttlelocationproject.databinding.ActivityMainBinding
import kr.rabbito.shuttlelocationproject.function.hashSHA256
import kr.rabbito.shuttlelocationproject.function.setChildEventListener
import kr.rabbito.shuttlelocationproject.function.showRoute

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mBinding: ActivityMainBinding? = null
    private val binding get() = mBinding!!

    private lateinit var map: GoogleMap
    private val postList = mutableListOf<Location>()

    private val FINISH_INTERVAL_TIME: Long = 2000
    private var backPressedTime: Long = 0

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        overridePendingTransition(0, 0)

        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//비밀번호 firebase에 올리기
            Firebase.database.getReference("Manager").child("1").setValue("TEST1234".hashSHA256())
            Firebase.database.getReference("Manager").child("2").setValue("S1L2P3".hashSHA256())
            Firebase.database.getReference("Manager").child("3").setValue("1P12L23S3".hashSHA256())
            Firebase.database.getReference("Manager").child("4").setValue("T1E2ST".hashSHA256())
            Firebase.database.getReference("Manager").child("5").setValue("TEST".hashSHA256())
            Firebase.database.getReference("Manager").child("6").setValue("mm0k211".hashSHA256())


        // 오류 무시
        val zoomControls = mapFragment.view!!.findViewById<View>(0x1)

        if (zoomControls != null && zoomControls.layoutParams is RelativeLayout.LayoutParams) {
            // ZoomControl is inside of RelativeLayout
            val params_zoom = zoomControls.layoutParams as RelativeLayout.LayoutParams

            params_zoom.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            params_zoom.addRule(RelativeLayout.CENTER_VERTICAL)

            val margin = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 16f,
                resources.displayMetrics
            ).toInt()

            val topMargin = resources.displayMetrics.heightPixels / 2 - 40
            params_zoom.setMargins(margin, topMargin, margin, margin)
        }

        binding.mainIconSbMenu.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            startActivity(intent)
        }

        binding.mainBtnToCommunity.setOnClickListener {
            val intent = Intent(this, CommunityActivity::class.java)
            startActivity(intent)
        }

        binding.mainBtnToPrefernces.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 나침판
        map.uiSettings.isCompassEnabled = false
        // 지도 화면 회전
        map.uiSettings.isRotateGesturesEnabled = false
        // 확대 축소(+-) 버튼
        map.uiSettings.isZoomControlsEnabled = true

        val start_loc = LatLng(37.345417, 126.738568)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(start_loc, 14.5f))
        // Add a marker in Sydney and move the camera
        //val yeouido = LatLng(37.521814, 126.923596)
        //map.addMarker(MarkerOptions().position(yeouido).title("Marker in Yeouido"))
        //map.moveCamera(CameraUpdateFactory.newLatLng(yeouido))


        showRoute(map, tuk_route_up, tuk_route_down)
        setChildEventListener(postList, map, "Driver/tuk")
    }

    // 뒤로가기 두 번 연속 터치 시 종료
    override fun onBackPressed() {
        val tempTime = System.currentTimeMillis()
        val intervalTime: Long = tempTime - backPressedTime
        if (0 <= intervalTime && FINISH_INTERVAL_TIME >= intervalTime) {
            finish()
        } else {
            backPressedTime = tempTime
            Toast.makeText(applicationContext, "한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun finish() {
        super.finish()
        overridePendingTransition(0, 0)
    }
}

