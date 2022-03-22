package com.example.mathwiz.ui.statistics

import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.mathwiz.MathWiz
import com.example.mathwiz.databinding.FragmentStatisticsBinding
import com.google.firebase.firestore.FirebaseFirestore


class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val statisticsViewModel =
            ViewModelProvider(this)[StatisticsViewModel::class.java]

        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textStatistics
        statisticsViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        val db = FirebaseFirestore.getInstance()
        val categoryNames: ArrayList<String> = ArrayList()
        val grade = MathWiz.userData?.grade
        val name = MathWiz.userData?.name
        val spinner = binding.categoriesSpinner
        val pieChart = binding.statsProgressbar
        val pieChartText = binding.pieChartText

        db.collection("categories").document(grade.toString()).get().addOnCompleteListener{ task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document != null) {
                    val count = document.getLong("count")
                    for (i in 1..count!!){
                        val categoryName = document.getString("category$i")
                        categoryNames.add(categoryName.toString())
                    }
                    spinner.adapter = activity?.applicationContext?.let { ArrayAdapter(it, R.layout.simple_spinner_item, categoryNames) } as SpinnerAdapter
                } else {
                    Log.d("TAG", "No such document")
                }
            } else {
                Log.d("TAG", "Error ", task.exception)
            }}


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                db.collection("users").document(name.toString()).collection("statistics").document(categoryNames[position]).get().addOnCompleteListener{ task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document != null) {
                            val correct = document.getDouble("correct")
                            val total = document.getDouble("total")
                            pieChart.progress =  ((correct!! / total !!) * 100).toInt()
                            pieChartText.text = "$correct / $total"
                        } else {
                            Log.d("TAG", "No such document")
                        }
                    } else {
                        Log.d("TAG", "Error ", task.exception)
                    }
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        // Add a Pie Chart



        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}