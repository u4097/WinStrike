package ru.prsolution.winstrike.ui.main;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.DatePicker;
import com.applandeo.materialcalendarview.builders.DatePickerBuilder;
import com.applandeo.materialcalendarview.listeners.OnSelectDateListener;
import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.jakewharton.rxbinding.view.RxView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.prsolution.winstrike.R;
import ru.prsolution.winstrike.WinstrikeApp;
import ru.prsolution.winstrike.common.datetimeweels.datetimepicker.DateTimeWheel.TimeWheel.TimePickerPopWin;
import ru.prsolution.winstrike.common.entity.SeatModel;
import ru.prsolution.winstrike.common.utils.TinyDB;
import ru.prsolution.winstrike.mvp.apimodels.RoomLayoutFactory;
import ru.prsolution.winstrike.mvp.apimodels.Rooms;
import ru.prsolution.winstrike.mvp.models.TimeDataModel;
import ru.prsolution.winstrike.mvp.presenters.ChoosePresenter;
import ru.prsolution.winstrike.mvp.views.ChooseView;
import ru.prsolution.winstrike.networking.Service;
import ru.prsolution.winstrike.ui.common.BackButtonListener;
import ru.prsolution.winstrike.ui.common.MapInfoSingleton;
import ru.prsolution.winstrike.ui.common.RouterProvider;
import timber.log.Timber;


public class ChooseScreenFragment extends MvpAppCompatFragment implements ChooseView, BackButtonListener {

    private boolean isDebug = false;
    private static final String EXTRA_NAME = "extra_name";
    private static final String EXTRA_NUMBER = "extra_number";
    private float dpHeight;

    public ProgressDialog mProgressDialog;
    private TinyDB tinyDB;
    private Boolean isDataSelected;
    private String selectedDate;

    private onMapShowProcess listener;


    @BindView(R.id.seat_title)
    TextView seat_title;

    @BindView(R.id.head_image)
    ImageView ivSeatImg;

    @BindView(R.id.tv_date_news)
    TextView tv_date;

    @BindView(R.id.v_date_tap)
    View vDateTap;

    @BindView(R.id.v_time_tap)
    View vTimeTap;

    @BindView(R.id.next_button)
    View showMapButton;

    @BindView(R.id.tv_time_left)
    TextView tv_time;

    @BindView(R.id.iv_arr_time)
    ImageView iv_arr_time;

    @BindView(R.id.iv_arr_date)
    ImageView iv_arr_date;

    @BindView(R.id.cpu)
    TextView tvCpu;

    @BindView(R.id.ram)
    TextView tvRam;

    @BindView(R.id.gpu)
    TextView tvGpu;

    @BindView(R.id.monitor)
    TextView tvMonitor;


    @InjectPresenter
    public ChoosePresenter presenter;

    @Inject
    public Service service;

    @ProvidePresenter
    ChoosePresenter provideMainScreenPresenter() {
        return new ChoosePresenter(service,
                ((RouterProvider) getParentFragment()).getRouter()
        );
    }


    /**
     * route show map to main presenter in MainScreenActivity
     */
    public interface onMapShowProcess {
        void onMapShow();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onMapShowProcess) {
            listener = (onMapShowProcess) context;
        } else {
            throw new ClassCastException(context.toString() + " must implements onMapShowProcess");
        }
    }


    public static ChooseScreenFragment getNewInstance(String name, int number) {
        ChooseScreenFragment fragment = new ChooseScreenFragment();
        Bundle arguments = new Bundle();
        arguments.putString(EXTRA_NAME, name);
        arguments.putInt(EXTRA_NUMBER, number);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        WinstrikeApp.INSTANCE.getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frm_choose, container, false);
        ButterKnife.bind(this, view);

        SeatModel seat = MapInfoSingleton.getInstance().getSeat();
        dpHeight = WinstrikeApp.getInstance().getDisplayHeightDp();
        Timber.tag("map").d("DpHeight: %s", dpHeight);


        // TODO: 13/05/2018 Make model for that cases:
        if (seat != null && seat.getType().contains("VIP")) {
            ivSeatImg.setImageResource(R.drawable.vip);
            seat_title.setText("Вы выбрали: VIP место");

            tvCpu.setText("CPU: intel i7-8700k");
            tvRam.setText("RAM: 64GB, SSD HDD 500gb");
            tvGpu.setText("GPU: Geforce GTX 1080ti 11GB");
            tvMonitor.setText("Монитор: LG 32GK850G");

        } else {
            ivSeatImg.setImageResource(R.drawable.simple);
            seat_title.setText("Вы выбрали: Обычное место");

            tvCpu.setText("CPU: intel i5-8400k");
            tvRam.setText("RAM: 64GB, SSD HDD 500gb");
            tvGpu.setText("GPU: Geforce GTX 1070ti 8GB");
            tvMonitor.setText("Монитор: LG 24GM79G");
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tinyDB = new TinyDB(getContext());
        isDataSelected = false;
        initMapShowButton();

        initDateSelectDialog();

        initTimeSelectDialog();
    }



    private void initMapShowButton() {
        setShowMapBtnEnable(showMapButton, false);

        RxView.clicks(showMapButton).subscribe(
                it -> {
                    if (TimeDataModel.INSTANCE.isDateValid()) {
                        presenter.getActivePid();
                    } else {
                        Toast.makeText(getActivity(), "Не правильно выбрана дата", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * First get active pid for room
     *
     * @param roomsResponse
     */
    @Override
    public void onGetActivePidResponseSuccess(Rooms roomsResponse) {
        Timber.d("Success get map data from server: %s", roomsResponse);
        /**
         *  data for active room pid successfully get from server.
         *  save pid and get map for selected time period
         */
        String activePid = roomsResponse.getRoom().getActiveLayoutPid();

        String timeFrom, timeTo;
        if (isDebug == true) {
            timeFrom = tinyDB.getString("timeFrom");
            timeTo = tinyDB.getString("timeTo");
        } else {
            timeFrom = TimeDataModel.INSTANCE.getStart();
            timeTo = TimeDataModel.INSTANCE.getEnd();
        }


        Map<String, String> time = new HashMap<>();
        time.put("start_at", timeFrom);
        time.put("end_at", timeTo);
        presenter.getArenaByTimeRange(activePid, time);
    }

    /**
     * Active pid success get, so now we  get map for seats
     *
     * @param roomLayoutFactory
     */
    @Override
    public void onGetArenaByTimeResponseSuccess(RoomLayoutFactory roomLayoutFactory) {
        Timber.d("Success get layout data from server: %s", roomLayoutFactory);
        /**
         * data for seat mapping successfully get from sever.
         * save map data in singleton and call MapScreenFragment from main presenter
         */
        MapInfoSingleton.getInstance().setRoomLayout(roomLayoutFactory.getRoomLayout());
        if (MapInfoSingleton.getInstance().getRoomLayout() != null) {
            listener.onMapShow();
        }
    }

    /**
     * Something go wrong with map pid request
     *
     * @param appErrorMessage
     */
    @Override
    public void onGetAcitivePidFailure(String appErrorMessage) {
        Timber.d("Failure get map from server: %s", appErrorMessage);
    }

    /**
     * Something go wrong with map request
     *
     * @param appErrorMessage
     */
    @Override
    public void onGetArenaByTimeFailure(String appErrorMessage) {
        Timber.d("Failure get layout from server: %s", appErrorMessage);
        if (appErrorMessage.contains("416")) toast("Выбран не рабочий диапазон времени");
    }


    /**
     * Select date
     */
    private void initDateSelectDialog() {
        String date = "Выберите дату";
        String time = "00:00 - 00:00";
        tv_date.setText(date);
        tv_time.setText(time);


        OnSelectDateListener listener = calendar -> {
            TimeDataModel.INSTANCE.setSelectDate(calendar.get(0).getTime());
            selectedDate = TimeDataModel.INSTANCE.getSelectDate();
            tv_date.setText(selectedDate);
        };


        DatePickerBuilder builder = new DatePickerBuilder(getActivity(), listener)
                .pickerType(CalendarView.ONE_DAY_PICKER)
                .headerColor(R.color.color_black)
                .pagesColor(R.color.color_black)
                .abbreviationsBarColor(R.color.color_black)
                .daysLabelsColor(R.color.color_primary)
                .abbreviationsLabelsColor(R.color.color_grey)
                .date(Calendar.getInstance())
                .todayLabelColor(R.color.color_accent);

        DatePicker datePicker = builder.build();

        RxView.clicks(vDateTap).subscribe(
                it -> {
                    isDataSelected = true;
                    datePicker.show();
                }
        );
    }


    /**
     *  Check before time select that date is already selected.
     */
    private void initTimeSelectDialog() {
        RxView.clicks(vTimeTap).subscribe(
                it -> {
                    if (isDataSelected) {
                        openTimePickerDialog();
                    } else {
                        Toast.makeText(getActivity(), "Сначала выберите дату!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * Show time picker dialog.
     */

    private void openTimePickerDialog() {
        TimePickerPopWin pickerPopWin = new TimePickerPopWin.Builder(getActivity(), (hour, min, timeDesc, timeFromData, timeToData) -> {

            String time = timeFromData + " - " + timeToData;
            tv_time.setText(time);
            tinyDB.putString("time", time);


            /**
             *  Save date data from timepicker (start and end).
             */
            TimeDataModel.INSTANCE.setStartAt(String.valueOf(timeFromData));
            TimeDataModel.INSTANCE.setEndAt(String.valueOf(timeToData));
            /**
             * save in time data in tinydb (for test).
             */
            tinyDB.putString("timeFrom", TimeDataModel.INSTANCE.getStart());
            tinyDB.putString("timeTo", TimeDataModel.INSTANCE.getEnd());

            Timber.d("startAt: %s", TimeDataModel.INSTANCE.getStart());
            Timber.d("endAt: %s", TimeDataModel.INSTANCE.getEnd());

            Timber.d("dateStart: %s", TimeDataModel.INSTANCE.getStartDate());
            Timber.d("dateEnd: %s", TimeDataModel.INSTANCE.getEndDate());

            Timber.d("isDateValid: %s", TimeDataModel.INSTANCE.isDateValid());


            /**
             * Fix time dialog for difrent screen size
             */
            int bntTextSize = 20;
            int viewTextSize = 50;

            if (dpHeight > 700) {
                bntTextSize = 20;
                viewTextSize = 50;
            }
            if (dpHeight < 700) {
                bntTextSize = 10;
                viewTextSize = 30;
            }

        }).textConfirm("Продолжить") //text of confirm button
                .textCancel("CANCEL") //text of cancel button
                .btnTextSize(20) // button text size
                .viewTextSize(50) // pick view text size
                .colorCancel(Color.parseColor("#999999")) //color of cancel button
                .colorConfirm(Color.parseColor("#ffffffff"))//color of confirm button
                .build();

        setShowMapBtnEnable(showMapButton, true);

        pickerPopWin.showPopWin(getActivity());
    }


    protected void toast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void setShowMapBtnEnable(View v, Boolean isEnable) {
        if (isEnable) {
            v.setAlpha(1f);
            v.setClickable(true);
        } else {
            v.setAlpha(.5f);
            v.setClickable(false);
        }
    }

    /**
     * show progress on seats loading
     */
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this.getActivity());
            mProgressDialog.setMessage("Загрузка мест...");
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    public void showWait() {
        showProgressDialog();
    }

    @Override
    public void removeWait() {
        hideProgressDialog();
    }

    // TODO: 13/05/2018 Fix BAG with that function!!!
    @Override
    public boolean onBackPressed() {
        presenter.onBackPressed();
        return true;
    }


    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }


}
