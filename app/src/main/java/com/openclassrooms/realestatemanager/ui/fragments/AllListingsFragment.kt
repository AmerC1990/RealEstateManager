package com.openclassrooms.realestatemanager.ui.fragments

import android.app.DatePickerDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.transition.Slide
import android.transition.TransitionManager
import android.view.*
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.PopupWindow
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.material.slider.RangeSlider
import com.google.gson.Gson
import com.openclassrooms.realestatemanager.R
import com.openclassrooms.realestatemanager.Utils
import com.openclassrooms.realestatemanager.adapters.RecyclerViewAdapter
import com.openclassrooms.realestatemanager.data.cache.ListingEntity
import com.openclassrooms.realestatemanager.databinding.FilterScreenBinding
import com.openclassrooms.realestatemanager.filter.FilterParams
import com.openclassrooms.realestatemanager.viewmodels.ListingsViewModel
import kotlinx.android.synthetic.main.fragment_all_listings.*
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import java.util.*


class AllListingsFragment : Fragment() {
    lateinit var recyclerViewAdapter: RecyclerViewAdapter
    private val viewModel: ListingsViewModel by inject()
    private var binding: FilterScreenBinding? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        setHasOptionsMenu(true)
        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar!!
        actionBar.setDisplayHomeAsUpEnabled(false)
        val view = inflater.inflate(R.layout.filter_screen, null)
        binding = FilterScreenBinding.bind(view)
        return inflater.inflate(R.layout.fragment_all_listings, container, false)
    }

    override fun onStart() {
        super.onStart()
        overrideOnBackPressed()
        val listingFormFragment = ListingFormFragment()
        if (Utils.isOnline(requireContext())) {
            addListingButton.setOnClickListener {
                changeFragment(listingFormFragment)
            }
        } else if (!Utils.isOnline(requireContext())) {
            addListingButton.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val filter = sharedPrefs.getString("filter", null)
        val filterParams = sharedPrefs.getString("filterParams", null)
        if (filter.isNullOrEmpty()) {
            viewModel.fetchListings()
        } else {
            val objectMapper = ObjectMapper()
            val filterParamsObject = objectMapper.readValue(filterParams, FilterParams::class.java)
            viewModel.filter(filterParams = filterParamsObject)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initRecyclerview()
        attachObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        val filterIcon = menu.findItem(R.id.action_filter)
        filterIcon?.setOnMenuItemClickListener {

            val popupWindow = PopupWindow(
                    binding?.root, // Custom view to show in popup window
                    LinearLayout.LayoutParams.MATCH_PARENT, // Width of popup window
                    LinearLayout.LayoutParams.MATCH_PARENT // Window height
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                popupWindow.elevation = 10.0F
            }


            // If API level 23 or higher then execute the code
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // Create a new slide animation for popup window enter transition
                val slideIn = Slide()
                slideIn.slideEdge = Gravity.TOP
                popupWindow.enterTransition = slideIn

                // Slide animation for popup window exit transition
                val slideOut = Slide()
                slideOut.slideEdge = Gravity.RIGHT
                popupWindow.exitTransition = slideOut
            }


            val types = resources.getStringArray(R.array.type_of_listing_spinner)
            val statuses = resources.getStringArray(R.array.status_of_property_spinner)

            val typeAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, types)
            binding?.typeOfPropertySpinnerFilter?.adapter = typeAdapter


            val statusAdapter = ArrayAdapter(requireContext(),
                    android.R.layout.simple_spinner_dropdown_item, statuses)
            binding?.statusOfPropertySpinnerFilter?.adapter = statusAdapter

            popupWindow.isFocusable = true
            popupWindow.update()
            binding?.buttonClose?.setOnClickListener {
                // Dismiss the popup window
                popupWindow.dismiss()
            }

            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            binding?.beenOnMarketSinceDatePicker?.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
                            binding?.beenOnMarketDateTextview?.visibility = View.VISIBLE
                            var myMonth = (Month + 1).toString()
                            var myDay = Day.toString()
                            if (myMonth.length < 2) {
                                myMonth = "0$myMonth"
                            }
                            if (myDay.length < 2) {
                                myDay = "0$myDay"
                            }
                            binding?.beenOnMarketDateTextview?.text = "   $myMonth/$myDay/$Year"
                        },
                        year,
                        month,
                        day
                )
                datePickerDialog.show()
            }

            binding?.beenSoldSinceDatePicker?.setOnClickListener {
                val datePickerDialog = DatePickerDialog(
                        requireContext(),
                        DatePickerDialog.OnDateSetListener { view, Year, Month, Day ->
                            binding?.beenSoldSinceTextview?.visibility = View.VISIBLE
                            var myMonth = (Month + 1).toString()
                            var myDay = Day.toString()
                            if (myMonth.length < 2) {
                                myMonth = "0$myMonth"
                            }
                            if (myDay.length < 2) {
                                myDay = "0$myDay"
                            }
                            binding?.beenSoldSinceTextview?.text = "   $myMonth/$myDay/$Year"
                        },
                        year,
                        month,
                        day
                )
                datePickerDialog.show()
            }
            binding?.priceSlider?.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                binding?.priceMinTextview?.text = rangeSlider.values[0].toString() + "0"
                binding?.priceMaxTextview?.text = rangeSlider.values[1].toString() + "0"
            }
            binding?.surfaceAreaSlider?.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                binding?.surfaceMinTextview?.text = rangeSlider.values[0].toString().substringBefore(".")
                binding?.surfaceMaxTextview?.text = rangeSlider.values[1].toString().substringBefore(".")
            }
            binding?.numberOfPhotosSlider?.addOnChangeListener { rangeSlider: RangeSlider, fl: Float, b: Boolean ->
                binding?.photosMinTextview?.text = rangeSlider.values[0].toString().substringBefore(".")
                binding?.photosMaxTextview?.text = rangeSlider.values[1].toString().substringBefore(".")
            }

            binding?.buttonApply?.setOnClickListener {
                if (viewModel.uiState.value is ListingsViewModel.ListingState.Success) {

                    val minimumPhotos = binding?.photosMinTextview?.text.toString().substringBefore(".").toInt()
                    val maximumPhotos = binding?.photosMaxTextview?.text.toString().substringBefore(".").toInt()

                    var typeOfListingText = binding?.typeOfPropertySpinnerFilter?.selectedItem.toString()

                    var statusOfPropertyText = binding?.statusOfPropertySpinnerFilter?.selectedItem.toString()

                    if (typeOfListingText.contains("Select")) {
                        typeOfListingText = ""
                    }
                    if (statusOfPropertyText.contains("Select")) {
                        statusOfPropertyText = ""
                    }

                    val filterParams = FilterParams(minPrice = binding?.priceMinTextview?.text.toString().toDouble(),
                            maxPrice = binding?.priceMaxTextview?.text.toString().toDouble(),
                            minSurfaceArea = binding?.surfaceMinTextview?.text.toString().toDouble(),
                            maxSurfaceArea = binding?.surfaceMaxTextview?.text.toString().toDouble(),
                            minNumberOfPhotos = minimumPhotos.toDouble(),
                            maxNumberOfPhotos = maximumPhotos.toDouble(),
                            status = statusOfPropertyText,
                            type = typeOfListingText,
                            onMarketSince = binding?.beenOnMarketDateTextview?.text.toString(),
                            soldSince = binding?.beenSoldSinceTextview?.text.toString(),
                            pointsOfInterest = getCheckedCategories(),
                            location = binding?.editTextEnterLocation?.text.toString())

                    viewModel.filter(filterParams = filterParams)
                    val objectMapper = ObjectMapper()
                    val filterParamsAsString = objectMapper.writeValueAsString(filterParams)
                    val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPrefs.edit()
                    editor.apply {
                        putString("filter", "filter")
                        putString("filterParams", filterParamsAsString)
                    }
                            .apply()
                }
                popupWindow.dismiss()
            }
            binding?.resetFiltersTextview?.setOnClickListener {
                viewModel.fetchListings()
                val sharedPrefs = requireContext().getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
                val editor = sharedPrefs.edit()
                editor.apply {
                    remove("filter")
                    remove("filterParams")
                }
                        .apply()
                popupWindow.dismiss()
            }

            TransitionManager.beginDelayedTransition(allListingsConstraintLayout)
            popupWindow.showAtLocation(
                    allListingsConstraintLayout, // Location to display popup window
                    Gravity.CENTER, // Exact position of layout to display popup
                    0, // X offset
                    0 // Y offset
            )
            true
        }

        val searchItem = menu.findItem(R.id.action_search)
        val searchView: androidx.appcompat.widget.SearchView = searchItem?.actionView as androidx.appcompat.widget.SearchView
        searchView.queryHint = "Search Listings"
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (viewModel.uiState.value is ListingsViewModel.ListingState.Success) {
                    val filteredData =
                            (viewModel.uiState.value as ListingsViewModel.ListingState.Success).listing.filter {
                                it.descriptionOfListing.contains(
                                        searchView.query,
                                        ignoreCase = true)
                                        ||
                                        it.address.contains(searchView.query,
                                                ignoreCase = true)
                                        || it.pointsOfInterest.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.typeOfListing.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.price.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.photoDescription.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.surfaceArea.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.status.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.realEstateAgent.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.dateOnMarket.contains(searchView.query,
                                        ignoreCase = true)
                                        || it.saleDate.contains(searchView.query,
                                        ignoreCase = true)
                            }
                    recyclerViewAdapter.setListData(filteredData as ArrayList<ListingEntity>)
                    recyclerViewAdapter.notifyDataSetChanged()
                    when (searchView.query.toString()) {
                        "" -> {
                            recyclerViewAdapter.setListData((viewModel.uiState.value as ListingsViewModel.ListingState.Success).listing as ArrayList<ListingEntity>)
                            recyclerViewAdapter.notifyDataSetChanged()
                        }
                    }
                }
                return false
            }
        }
        )
    }

    private fun attachObservers() {
        lifecycleScope.launchWhenCreated {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is ListingsViewModel.ListingState.Loading -> {
//                        allListingsProgressBar.visibility = View.VISIBLE
                    }
                    is ListingsViewModel.ListingState.Success -> {
                        recyclerViewAdapter.setListData(uiState.listing as ArrayList<ListingEntity>)
                        recyclerViewAdapter.notifyDataSetChanged()
//                        allListingsProgressBar.visibility = View.GONE
                    }
                    is ListingsViewModel.ListingState.Error -> {
//                        allListingsProgressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun changeFragment(fragment: Fragment) {
//        allListingsProgressBar.visibility = View.VISIBLE
        activity?.supportFragmentManager?.beginTransaction()?.apply {
            replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
//            allListingsProgressBar.visibility = View.GONE
            commit()
        }
    }

    private fun initRecyclerview() {
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            recyclerViewAdapter = RecyclerViewAdapter()
            adapter = recyclerViewAdapter
            val divider = DividerItemDecoration(requireContext(), LinearLayout.VERTICAL)
            divider.setDrawable(context.resources.getDrawable(R.drawable.list_divider))
            addItemDecoration(divider)
        }
    }

    private fun overrideOnBackPressed() {
        activity?.onBackPressedDispatcher?.addCallback {
        }
    }

    private fun getCheckedCategories(): List<String> = listOfNotNull(
            "Bar".takeIf { binding!!.barCheckbox.isChecked },
            "School".takeIf { binding!!.schoolCheckbox.isChecked },
            "Park".takeIf { binding!!.parkCheckbox.isChecked },
            "Restaurant".takeIf { binding!!.restaurantCheckbox.isChecked },
            "Hospital".takeIf { binding!!.hospitalCheckbox.isChecked }
    )
}