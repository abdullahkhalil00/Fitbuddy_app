package com.app.fitt_buddy

import android.content.Context
import android.media.RingtoneManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class WorkoutsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var tvWeek: TextView
    private lateinit var tvTitle: TextView
    private lateinit var rvExercises: RecyclerView
    private lateinit var llDaysContainer: LinearLayout

    // Timer Overlay UI
    private lateinit var clTimerOverlay: ConstraintLayout
    private lateinit var tvCountdown: TextView
    private lateinit var tvTimerExName: TextView
    private lateinit var pbTimerCircle: ProgressBar
    private lateinit var btnStopResume: Button
    private lateinit var btnCloseTimer: ImageButton

    private var currentWeek = 1
    private var maxWeeks = 4
    
    private val dateList = mutableListOf<Date>()
    private var selectedDateIndex = 0

    private val exerciseList = mutableListOf<WorkoutExercise>()
    private lateinit var adapter: WorkoutAdapter

    private var countDownTimer: CountDownTimer? = null
    private var millisRemaining: Long = 0
    private var totalMillis: Long = 0
    private var isTimerRunning = false
    private var activeExercise: WorkoutExercise? = null
    private var lastSavedSecs = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_recipes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        tvWeek = view.findViewById(R.id.tv_current_week)
        tvTitle = view.findViewById(R.id.tv_workout_title)
        rvExercises = view.findViewById(R.id.rv_exercises)
        llDaysContainer = view.findViewById(R.id.ll_days_container)

        // Timer Views
        clTimerOverlay = view.findViewById(R.id.cl_timer_overlay)
        tvCountdown = view.findViewById(R.id.tv_countdown)
        tvTimerExName = view.findViewById(R.id.tv_timer_exercise_name)
        pbTimerCircle = view.findViewById(R.id.pb_timer_circle)
        btnStopResume = view.findViewById(R.id.btn_stop_resume)
        btnCloseTimer = view.findViewById(R.id.btn_close_timer)

        setupRecyclerView()
        
        // Rolling dates starting from today
        generateDateList()
        fetchUserData()

        view.findViewById<View>(R.id.btn_next_week).setOnClickListener {
            if (currentWeek < maxWeeks) {
                currentWeek++
                updateWeekUI()
            } else {
                Toast.makeText(context, "Plan duration limit reached!", Toast.LENGTH_SHORT).show()
            }
        }

        btnStopResume.setOnClickListener {
            if (isTimerRunning) pauseTimer() else resumeTimer()
        }

        btnCloseTimer.setOnClickListener {
            saveProgress(isFinished = false)
            countDownTimer?.cancel()
            clTimerOverlay.visibility = View.GONE
        }
    }

    private fun generateDateList() {
        dateList.clear()
        val calendar = Calendar.getInstance()
        for (i in 0..6) {
            dateList.add(calendar.time)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    private fun setupRecyclerView() {
        adapter = WorkoutAdapter(exerciseList) { exercise ->
            startTimerOverlay(exercise)
        }
        rvExercises.layoutManager = LinearLayoutManager(requireContext())
        rvExercises.adapter = adapter
    }

    private fun startTimerOverlay(exercise: WorkoutExercise) {
        activeExercise = exercise
        totalMillis = exercise.duration * 60 * 1000L
        millisRemaining = totalMillis
        lastSavedSecs = 0
        
        tvTimerExName.text = exercise.name
        clTimerOverlay.visibility = View.VISIBLE
        pbTimerCircle.max = totalMillis.toInt()
        
        startTimer(millisRemaining)
    }

    private fun startTimer(millis: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(millis, 100) {
            override fun onTick(millisUntilFinished: Long) {
                millisRemaining = millisUntilFinished
                updateTimerUI()
            }
            override fun onFinish() {
                millisRemaining = 0
                updateTimerUI()
                playAlarm()
                saveProgress(isFinished = true)
                Toast.makeText(context, "Exercise Finished!", Toast.LENGTH_SHORT).show()
                clTimerOverlay.visibility = View.GONE
            }
        }.start()
        isTimerRunning = true
        btnStopResume.text = "STOP"
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnStopResume.text = "RESUME"
        saveProgress(isFinished = false)
    }

    private fun resumeTimer() {
        startTimer(millisRemaining)
    }

    private fun updateTimerUI() {
        val minutes = (millisRemaining / 1000) / 60
        val seconds = (millisRemaining / 1000) % 60
        tvCountdown.text = String.format("%02d:%02d", minutes, seconds)
        pbTimerCircle.progress = (totalMillis - millisRemaining).toInt()
    }

    private fun saveProgress(isFinished: Boolean) {
        val currentTotalSecsInThisSession = ((totalMillis - millisRemaining) / 1000).toInt()
        val deltaSecs = currentTotalSecsInThisSession - lastSavedSecs
        if (deltaSecs <= 0 && !isFinished) return
        
        lastSavedSecs = currentTotalSecsInThisSession
        
        val sharedPref = requireContext().getSharedPreferences("WorkoutCache", Context.MODE_PRIVATE)
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val totalSecsToday = sharedPref.getInt("${today}_completed_secs", 0)
        
        with(sharedPref.edit()) {
            val newTotalSecs = totalSecsToday + deltaSecs
            putInt("${today}_completed_secs", newTotalSecs)
            putInt("${today}_completed", newTotalSecs / 60)
            if (isFinished) putBoolean("${today}_${activeExercise?.name}", true)
            apply()
        }
    }

    private fun playAlarm() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            val r = RingtoneManager.getRingtone(requireContext(), uri)
            r.play()
        } catch (e: Exception) {}
    }

    private fun fetchUserData() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get().addOnSuccessListener { doc ->
            if (isAdded) {
                maxWeeks = doc.getLong("commitmentWeeks")?.toInt() ?: 4
                updateWeekUI()
            }
        }
    }

    private fun updateWeekUI() {
        tvWeek.text = "WEEK $currentWeek"
        setupDays()
        loadExercises()
    }

    private fun setupDays() {
        llDaysContainer.removeAllViews()
        val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        
        for (i in 0 until dateList.size) {
            val date = dateList[i]
            val dayView = LayoutInflater.from(requireContext()).inflate(R.layout.item_day_chip, llDaysContainer, false)
            val tvDay = dayView.findViewById<TextView>(R.id.tv_day_number)
            
            tvDay.text = dateFormat.format(date)
            
            if (i == selectedDateIndex) {
                dayView.setBackgroundResource(R.drawable.bg_capsule_selected)
                tvDay.setTextColor(resources.getColor(R.color.bg_dark, null))
            } else {
                dayView.setBackgroundResource(R.drawable.bg_card_unselected)
                tvDay.setTextColor(resources.getColor(R.color.white, null))
            }

            dayView.setOnClickListener {
                selectedDateIndex = i
                setupDays()
                loadExercises()
            }
            llDaysContainer.addView(dayView)
        }
    }

    private fun loadExercises() {
        val calendar = Calendar.getInstance()
        calendar.time = dateList[selectedDateIndex]
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        
        val category = when (dayOfWeek) {
            Calendar.MONDAY -> "Chest"
            Calendar.TUESDAY -> "Back"
            Calendar.WEDNESDAY -> "Shoulders"
            Calendar.THURSDAY -> "Biceps"
            Calendar.FRIDAY -> "Triceps"
            Calendar.SATURDAY -> "Legs"
            Calendar.SUNDAY -> "Abs"
            else -> "Full body"
        }
        tvTitle.text = category.uppercase()

        db.collection("workout").document(category).get().addOnSuccessListener { doc ->
            if (!isAdded) return@addOnSuccessListener
            exerciseList.clear()
            if (doc.exists()) {
                doc.data?.forEach { (name, durationValue) ->
                    val durationInt = durationValue.toString().replace(" min", "").trim().toIntOrNull() ?: 5
                    exerciseList.add(WorkoutExercise(name, durationInt, category))
                }
            }
            adapter.notifyDataSetChanged()
        }
    }
}