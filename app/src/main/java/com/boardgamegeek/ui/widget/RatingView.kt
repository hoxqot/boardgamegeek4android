package com.boardgamegeek.ui.widget

import android.content.Context
import android.support.v4.app.FragmentActivity
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.boardgamegeek.R
import com.boardgamegeek.extensions.*
import com.boardgamegeek.ui.dialog.NumberPadDialogFragment
import kotlinx.android.synthetic.main.widget_rating.view.*
import java.text.DecimalFormat

class RatingView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0)
    : ForegroundLinearLayout(context, attrs) {
    private val hideWhenZero: Boolean
    private var isEditMode: Boolean = false
    private var activity: FragmentActivity? = null
    private var listener: Listener? = null

    interface Listener {
        fun onRatingChanged(rating: Double)
    }

    init {
        LayoutInflater.from(getContext()).inflate(R.layout.widget_rating, this, true)

        visibility = View.GONE
        gravity = Gravity.CENTER_VERTICAL
        minimumHeight = resources.getDimensionPixelSize(R.dimen.edit_row_height)
        orientation = LinearLayout.VERTICAL
        setSelectableBackgroundBorderless()

        val a = getContext().obtainStyledAttributes(attrs, R.styleable.RatingView, defStyleAttr, 0)
        try {
            hideWhenZero = a.getBoolean(R.styleable.RatingView_hideWhenZero, false)
        } finally {
            a.recycle()
        }

        setOnClickListener { _ ->
            var output = RATING_EDIT_FORMAT.format(ratingView.tag as Double)
            if ("0" == output) output = ""
            val fragment = NumberPadDialogFragment.newInstance(context.getString(R.string.rating), output)
            fragment.setMinValue(1.0)
            fragment.setMaxValue(10.0)
            fragment.setMaxMantissa(6)
            fragment.setOnDoneClickListener {
                rating = it.toDouble()
                listener?.onRatingChanged(rating)
            }
            activity?.showAndSurvive(fragment)
        }
    }

    private var rating: Double
        get() {
            return ratingView.tag as Double? ?: return 0.0
        }
        set(rating) {
            val constrainedRating = rating.coerceIn(0.0, 10.0)
            ratingView.text = constrainedRating.asPersonalRating(context)
            ratingView.tag = constrainedRating
            ratingView.setTextViewBackground(constrainedRating.toColor(ratingColors))
        }

    fun setOnChangeListener(activity: FragmentActivity, listener: Listener) {
        this.activity = activity
        this.listener = listener
    }

    fun setContent(rating: Double, timestamp: Long) {
        this.rating = rating
        timestampView.timestamp = timestamp
        setEditMode()
    }

    fun enableEditMode(enable: Boolean) {
        isEditMode = enable
        setEditMode()
    }

    private fun setEditMode() {
        if (isEditMode) {
            isClickable = true
            visibility = View.VISIBLE
        } else {
            isClickable = false
            visibility = if (hideWhenZero && rating == 0.0) View.GONE else View.VISIBLE
        }
    }

    companion object {
        private val RATING_EDIT_FORMAT = DecimalFormat("0.#")
    }
}