package hu.bme.aut.t4xgko.DataDoro.touch

interface DayTouchHelperCallback {
    fun onDismissed(position: Int)
    fun onItemMoved(fromPosition: Int, toPosition: Int)
}