package hu.bme.aut.t4xgko.DataDoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import hu.bme.aut.t4xgko.DataDoro.adapter.DayAdapter
import hu.bme.aut.t4xgko.DataDoro.data.AppDatabase
import hu.bme.aut.t4xgko.DataDoro.data.Day
import hu.bme.aut.t4xgko.DataDoro.data.TestData
import hu.bme.aut.t4xgko.DataDoro.databinding.FragmentDayListBinding
import hu.bme.aut.t4xgko.DataDoro.touch.DayRecyclerTouchCallback
import java.time.LocalDate

class DayListFragment : Fragment() {

    private var _binding: FragmentDayListBinding? = null
    private val binding get() = _binding!!
    private lateinit var dayAdapter: DayAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDayListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.circularProgressBar.visibility = View.VISIBLE
        binding.tvLoading.visibility = View.VISIBLE

        dayAdapter = DayAdapter.getInstance(requireContext())
        binding.recyclerDay.adapter = dayAdapter

        Thread {
            fun updateProgressBar(add: Int) {
                requireActivity().runOnUiThread {
                    binding.circularProgressBar.progress += add
                }
            }

            TestData.insert(requireContext(), ::updateProgressBar)

            AppDatabase.getInstance(requireContext()).dayDao().insertDay(Day(
                YearMonthDay = LocalDate.now().toString(),
                dayText = "",
                TimeStudiedSec = 0,
                GoalTimeSec = 30 * 60 // 30 min
            ))
            updateProgressBar(10)

            val dayList = AppDatabase.getInstance(requireContext()).dayDao().getAllDays()
            requireActivity().runOnUiThread {
                dayAdapter.dayItems = dayList.toMutableList()
                dayAdapter.notifyDataSetChanged()

                val touchCallbakList = DayRecyclerTouchCallback(dayAdapter)
                val itemTouchHelper = ItemTouchHelper(touchCallbakList)
                itemTouchHelper.attachToRecyclerView(binding.recyclerDay)

                binding.circularProgressBar.visibility = View.GONE
                binding.tvLoading.visibility = View.GONE
                binding.recyclerDay.visibility = View.VISIBLE
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
