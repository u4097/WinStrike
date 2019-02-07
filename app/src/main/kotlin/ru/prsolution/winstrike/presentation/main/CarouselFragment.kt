package ru.prsolution.winstrike.presentation.main

/*
 * Created by oleg on 01.02.2018.
 */

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.facebook.drawee.view.SimpleDraweeView
import ru.prsolution.winstrike.R
import ru.prsolution.winstrike.domain.models.RoomSeatType
import ru.prsolution.winstrike.domain.models.SeatCarousel
import ru.prsolution.winstrike.presentation.utils.custom.ChooseSeatLinearLayout
import timber.log.Timber

class CarouselFragment : Fragment() {

    lateinit var mListener: OnSeatClickListener
    private var itemSeat: View? = null
    private var mSeat: SeatCarousel? = null
    private var mMainActivity: MainActivity? = null

    interface OnSeatClickListener {

        fun onSeatClick(seat: SeatCarousel?)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnSeatClickListener) {
            mListener = context
        } else {
            throw ClassCastException(
                "$context must implements OnChoosePlaceButtonsClickListener "
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            mSeat = arguments?.getSerializable("room") as SeatCarousel
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        this.mMainActivity = activity as MainActivity?
        if (container == null) {
            return null
        }
        itemSeat = inflater.inflate(R.layout.item_seats, container, false)

        val seatTitle = itemSeat!!.findViewById<TextView>(R.id.seat_title)

        val thumbnail = itemSeat!!.findViewById<SimpleDraweeView>(R.id.seat_image)

        val uri = Uri.parse(mSeat?.imageUrl)
        thumbnail.setImageURI(uri)

        if (mSeat?.type == RoomSeatType.COMMON) {
            seatTitle.text = getString(R.string.common_hall)
        } else {
            seatTitle.text = getString(R.string.vip_hall)
        }

        val root = itemSeat!!.findViewById<ChooseSeatLinearLayout>(R.id.root)
        val scale = this.arguments!!.getFloat("scale")
        root.setScaleBoth(scale)
        thumbnail.setOnClickListener {
                        mListener.onSeatClick(mSeat)
        }
        return itemSeat
    }

    companion object {

        fun newInstance(activity: FragmentActivity?, room: SeatCarousel): Fragment {
            val bundle = Bundle()
            bundle.putSerializable("room", room)
            return Fragment.instantiate(activity!!, CarouselFragment::class.java.name, bundle)
        }
    }
}
