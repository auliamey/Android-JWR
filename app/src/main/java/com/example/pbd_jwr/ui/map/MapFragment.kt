//package com.example.pbd_jwr.ui.map
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.example.pbd_jwr.databinding.FragmentMapsBinding
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//
//class MapsFragment : Fragment(), OnMapReadyCallback {
//
//    private lateinit var mMap: GoogleMap
//
//    private var _binding: FragmentMapsBinding? = null
//    private val binding get() = _binding!!
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        _binding = FragmentMapsBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//
//        val mapFragment = childFragmentManager.findFragmentById(R.id.map_container) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        return root
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//
//    override fun onMapReady(googleMap: GoogleMap) {
//        mMap = googleMap
//
//        // Add a marker in a default location and move the camera
//        val defaultLocation = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(defaultLocation).title("Marker in Default Location"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation))
//    }
//}
