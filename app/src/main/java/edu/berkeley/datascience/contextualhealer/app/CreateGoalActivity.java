package edu.berkeley.datascience.contextualhealer.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.appeaser.sublimepickerlibrary.datepicker.SelectedDate;
import com.appeaser.sublimepickerlibrary.helpers.SublimeOptions;
import com.appeaser.sublimepickerlibrary.recurrencepicker.SublimeRecurrencePicker;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Text;

import java.util.Calendar;

import edu.berkeley.datascience.contextualhealer.R;
import edu.berkeley.datascience.contextualhealer.activity.ActivityType;
import edu.berkeley.datascience.contextualhealer.database.GoalDataSource;
import edu.berkeley.datascience.contextualhealer.fragment.SublimePickerFragment;
import edu.berkeley.datascience.contextualhealer.model.Goal;
import edu.berkeley.datascience.contextualhealer.utils.CommonUtil;
import info.hoang8f.android.segmented.SegmentedGroup;

public class CreateGoalActivity extends AppCompatActivity{

    private static final String TAG = CreateGoalActivity.class.getSimpleName();


    private Button btnSaveGoal,btnStartTime,btnDeleteGoal;
    private TextView txtGoalTitle;
    private TextView txtStartTime;
    private TextView txtEndTime;
    private TextView txtRepeatEvent;
    private TextView txtRepeatCustomRule;
    private Toolbar toolbar;
    private TimePickerDialog timePickerDialog ;
    private Calendar calendar ;
    private Calendar mEndCalender;
    private int CalendarHour, CalendarMinute;
    private RadioButton chkActivityJogging, chkActivityWalking, chkActivityStaircase;
    private SegmentedGroup mSelectActivityType;
    private SwitchCompat tvIsTrackingEnabled;
    private TextView txtDurationInMinutes;
    private String startTime, endTime;
    public static final String EXTRA_GOAL = "EXTRA_GOAL";
    public static final String EXTRA_GOAL_POSITION = "EXTRA_GOAL_POSITION";
    public static final String EXTRA_GOAL_ID = "EXTRA_GOAL_ID";
    private RelativeLayout mRoolayout;
    private LinearLayout layout_goal_title,layout_goal_start_time,layout_goal_end_time,layout_goal_duration,layout_goal_repeat;


    // Chosen values
    SelectedDate mSelectedDate;
    int mHour, mMinute;
    String mRecurrenceOption, mRecurrenceRule;
    private boolean IsNewGoal = true;
    private int mCurrentGoalID = -1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_goal);



        //Toolbar
        toolbar = (Toolbar) findViewById(R.id.createGoalToolBar);
        toolbar.setTitle("Set Goal");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Get Elements
        mRoolayout = (RelativeLayout) findViewById(R.id.rootLayout);
        btnSaveGoal = (Button) findViewById(R.id.btnSaveGoal);
        btnDeleteGoal = (Button) findViewById(R.id.btnDeleteGoal);
        txtGoalTitle = (TextView) findViewById(R.id.txtGoalTitle);
        txtStartTime = (TextView) findViewById(R.id.txtStartTime);
        txtEndTime = (TextView) findViewById(R.id.txtEndTime);
        txtRepeatEvent = (TextView) findViewById(R.id.txtRepeatDays);
        txtRepeatCustomRule = (TextView) findViewById(R.id.txtRepeatCustomRule);
        chkActivityJogging = (RadioButton) findViewById(R.id.chkActivityJogging);
        chkActivityWalking = (RadioButton) findViewById(R.id.chkActivityWalking);
        chkActivityStaircase = (RadioButton) findViewById(R.id.chkActivityStaircase);
        tvIsTrackingEnabled = (SwitchCompat) findViewById(R.id.tvIsTrackingEnabled);
        txtDurationInMinutes = (TextView) findViewById(R.id.txtDurationInMinutes);
        mSelectActivityType = (SegmentedGroup) findViewById(R.id.selectActivityType);
        layout_goal_title = (LinearLayout) findViewById(R.id.layout_goal_title);
        layout_goal_start_time = (LinearLayout) findViewById(R.id.layout_goal_start_time);
        layout_goal_end_time = (LinearLayout) findViewById(R.id.layout_goal_end_time);
        layout_goal_duration = (LinearLayout) findViewById(R.id.layout_goal_duration);
        layout_goal_repeat = (LinearLayout) findViewById(R.id.layout_goal_repeat);

        //Initialize
        calendar = Calendar.getInstance();
        mEndCalender = Calendar.getInstance();

        Intent intent = getIntent();
        if(intent.getParcelableExtra(EXTRA_GOAL) != null){
            IsNewGoal =false;

            // Edit Goal
            Goal goal = intent.getParcelableExtra(EXTRA_GOAL);

            //Get Goal details
            mCurrentGoalID = intent.getIntExtra(EXTRA_GOAL_ID,-1);
            // Set details as received by intent
            txtGoalTitle.setText(goal.getGoalTitle());
            txtStartTime.setText(goal.getGoalStartTime());
            txtEndTime.setText(goal.getGoalEndTime());
            txtDurationInMinutes.setText(String.valueOf(goal.getGoalDurationInMinutes()));
            txtRepeatEvent.setText(goal.getGoalRepeatType());
            txtRepeatCustomRule.setText(goal.getGoalRepeatPattern());

            switch (goal.getGoalType()){
               case "jogging":
                   chkActivityJogging.setChecked(true);
                   break;

                case "walking":
                    chkActivityWalking.setChecked(true);
                    break;
                case "staircase":
                    chkActivityStaircase.setChecked(true);
                    break;

                default:
                    chkActivityJogging.setChecked(true);

            }

            chkActivityJogging.setEnabled(false);
            chkActivityWalking.setEnabled(false);
            chkActivityStaircase.setEnabled(false);
            mSelectActivityType.setTintColor(R.color.calendar_disabled_text_color_dark, R.color.mdtp_white);



            if(goal.getIsGoalCurrentlyTracked() == 1){
                tvIsTrackingEnabled.setChecked(true);
            }
            else{
                tvIsTrackingEnabled.setChecked(false);
            }

            //Enable the delete button
            btnDeleteGoal.setEnabled(true);

        }
        else{
            //New Goal
            IsNewGoal = true;
            //Set default start and end time
            //Start Time
            int StartHour = calendar.get(Calendar.HOUR_OF_DAY);
            int StartMinute = calendar.get(Calendar.MINUTE);
            txtStartTime.setText(StringUtils.leftPad(Integer.toString(StartHour), 2,"0")+ ":" + StringUtils.leftPad(Integer.toString(StartMinute), 2,"0"));
            //End Time
            mEndCalender.add(Calendar.HOUR_OF_DAY,1);
            int EndHour = mEndCalender.get(Calendar.HOUR_OF_DAY);
            int EndMinute = mEndCalender.get(Calendar.MINUTE);
            txtEndTime.setText(StringUtils.leftPad(Integer.toString(EndHour), 2,"0")+ ":" + StringUtils.leftPad(Integer.toString(EndMinute), 2,"0"));


            //Set Default Repeat
            txtRepeatEvent.setText("DAILY");

            //Disable the Delete Button
            btnDeleteGoal.setEnabled(false);
        }


        btnDeleteGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mCurrentGoalID != -1 && IsNewGoal == false){
                    GoalDataSource dataSource = new GoalDataSource(CreateGoalActivity.this);
                    dataSource.delete(mCurrentGoalID);
                    //Snackbar.make(mRoolayout, "Goal removed from list", Snackbar.LENGTH_SHORT).show();
                    finish();
                }

            }
        });





        btnSaveGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Save the Goal
                //Create a Goal Object
                if(! txtGoalTitle.getText().toString().equals("")){
                    //Get Goal Title
                    String goalTitle = txtGoalTitle.getText().toString();
                    //Get Activity Type
                    String activityType = ActivityType.jogging.toString(); //default

                    if(chkActivityJogging.isChecked()){
                        activityType = ActivityType.jogging.toString();
                    }
                    if(chkActivityWalking.isChecked()){
                        activityType = ActivityType.walking.toString();
                    }
                    if(chkActivityStaircase.isChecked()){
                        activityType = ActivityType.staircase.toString();
                    }

                    //Duration
                    Integer duration =  60;
                    duration = Integer.parseInt(txtDurationInMinutes.getText().toString());


                    //Repeat
                    String repeatType = txtRepeatEvent.getText().toString();
                    String repeatPattern = txtRepeatCustomRule.getText().toString();
                    //Start Time
                    String startTime = txtStartTime.getText().toString();
                    //End Time
                    String endTime = txtEndTime.getText().toString();
                    //Goal Set Date
                    String goalSetDate = CommonUtil.GetCurrentDateString();


                    //IsTrackingEnabled
                    int isTrackingEnabled = 1;
                    if(tvIsTrackingEnabled.isChecked()){
                        isTrackingEnabled =1;
                    }
                    else{
                        isTrackingEnabled = 0;
                    }


                    GoalDataSource dataSource = new GoalDataSource(CreateGoalActivity.this);


                    Log.v(TAG, "IsNewGoal : " + IsNewGoal);
                    if(IsNewGoal){
                        //Create new
                        Goal goal = new Goal(goalTitle, activityType,duration, repeatType,repeatPattern ,startTime, endTime,goalSetDate, isTrackingEnabled,0);
                        dataSource.create(goal);
                    }
                    else{
                        //Update
                        Goal goal = new Goal(mCurrentGoalID, goalTitle, activityType,duration, repeatType, repeatPattern ,startTime, endTime,goalSetDate, isTrackingEnabled,0);
                        dataSource.update(goal);
                    }

                    finish();
                }


            }
        });


        //Goal Title
        layout_goal_title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetGoalTitleDialog();
            }
        });


        //Start Time
        layout_goal_start_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetTimeFromPicker(calendar, timePickerDialog, true);
                //Toast.makeText(CreateGoalActivity.this, "Time Not Selected", Toast.LENGTH_SHORT).show();
            }
        });

        //End Time
        layout_goal_end_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetTimeFromPicker(calendar, timePickerDialog, false);
                //Toast.makeText(CreateGoalActivity.this, "Time Not Selected", Toast.LENGTH_SHORT).show();
            }
        });

        //Goal Duration
        layout_goal_duration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSetGoalDurationDialog();
            }
        });

        //Repeat Information
        layout_goal_repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Open the Recurring events thing
                SublimePickerFragment pickerFrag = new SublimePickerFragment();
                pickerFrag.setCallback(mFragmentCallback);
                // Options
                Pair<Boolean, SublimeOptions> optionsPair = getOptions();
                // Valid options
                Bundle bundle = new Bundle();
                bundle.putParcelable("SUBLIME_OPTIONS", optionsPair.second);
                pickerFrag.setArguments(bundle);

                pickerFrag.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                pickerFrag.show(getSupportFragmentManager(), "SUBLIME_PICKER");
            }
        });





    }



    private SublimePickerFragment.Callback mFragmentCallback = new SublimePickerFragment.Callback() {
        @Override
        public void onCancelled() {
            //rlDateTimeRecurrenceInfo.setVisibility(View.GONE);
        }

        @Override
        public void onDateTimeRecurrenceSet(SelectedDate selectedDate,
                                            int hourOfDay, int minute,
                                            SublimeRecurrencePicker.RecurrenceOption recurrenceOption,
                                            String recurrenceRule) {

            //txtRepeatEvent.setText(recurrenceRule != null ? recurrenceRule : "None");

            mSelectedDate = selectedDate;
            mHour = hourOfDay;
            mMinute = minute;
            mRecurrenceOption = recurrenceOption != null ?
                    recurrenceOption.name() : "None";
            mRecurrenceRule = recurrenceRule != null ?
                    recurrenceRule : "";

            //Based on the selected output set the text
            String repeat = "";
            repeat = mRecurrenceOption + "" + mRecurrenceRule;
            String repeatCustomRule = "";

            switch (mRecurrenceOption){
                case "DOES_NOT_REPEAT":
                    repeat = "NEVER";
                    break;
                case "DAILY":
                    repeat = "DAILY";
                    break;
                case "WEEKLY":
                    repeat = "WEEKLY";
                    break;
                case "MONTHLY":
                    repeat = "MONTHLY";
                    break;
                case "YEARLY":
                    repeat = "YEARLY";
                    break;
                case "CUSTOM":
                    //TODO : WORK ON CUSTOM REPEAT : Till then Set it to DAILY
                    repeat = "DAILY";
                    //repeat = "CUSTOM";
                    //repeatCustomRule = mRecurrenceRule;
                    break;
                default:
                    repeat = "NEVER";
                    break;
            }

//            if(mRecurrenceOption == "DOES_NOT_REPEAT"){
//                repeat = "No Repeat";
//            }
//
//            else{
//                repeat = recurrenceOption.name() + " " + recurrenceRule.toString();
//            }
            txtRepeatEvent.setText(repeat);
            txtRepeatCustomRule.setText(repeatCustomRule);


        }
    };


    // Validates & returns SublimePicker options
    private Pair<Boolean, SublimeOptions> getOptions() {
        SublimeOptions options = new SublimeOptions();
        int displayOptions = 0;
        displayOptions |= SublimeOptions.ACTIVATE_RECURRENCE_PICKER;
        options.setPickerToShow(SublimeOptions.Picker.REPEAT_OPTION_PICKER);
        options.setDisplayOptions(displayOptions);
        // Enable/disable the date range selection feature
        options.setCanPickDateRange(true);

        // Example for setting date range:
        // Note that you can pass a date range as the initial date params
        // even if you have date-range selection disabled. In this case,
        // the user WILL be able to change date-range using the header
        // TextViews, but not using long-press.

        /*Calendar startCal = Calendar.getInstance();
        startCal.set(2016, 2, 4);
        Calendar endCal = Calendar.getInstance();
        endCal.set(2016, 2, 17);

        options.setDateParams(startCal, endCal);*/

        // If 'displayOptions' is zero, the chosen options are not valid
        return new Pair<>(displayOptions != 0 ? Boolean.TRUE : Boolean.FALSE, options);
    }


    private void GetTimeFromPicker(Calendar calendar, TimePickerDialog timePickerDialog, final Boolean IsStartTime){
        CalendarHour = calendar.get(Calendar.HOUR_OF_DAY);

        CalendarMinute = calendar.get(Calendar.MINUTE);
        timePickerDialog = TimePickerDialog.newInstance(null, CalendarHour, CalendarMinute, true);

        timePickerDialog.setThemeDark(false);

        timePickerDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialogInterface) {

                Toast.makeText(CreateGoalActivity.this, "Time Not Selected", Toast.LENGTH_SHORT).show();
            }
        });
        timePickerDialog.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute, int second) {
                String SelectedTime = "Selected Time is " + hourOfDay + " : " + minute ;
                if(IsStartTime){
                    startTime = SelectedTime;
                    txtStartTime.setText(StringUtils.leftPad(Integer.toString(hourOfDay), 2,"0")+ ":" + StringUtils.leftPad(Integer.toString(minute), 2,"0"));
                }
                else{
                    endTime = SelectedTime;
                    txtEndTime.setText(StringUtils.leftPad(Integer.toString(hourOfDay), 2,"0")+ ":" + StringUtils.leftPad(Integer.toString(minute), 2,"0"));
                }

                //Toast.makeText(CreateGoalActivity.this, SelectedTime, Toast.LENGTH_LONG).show();
            }
        });

        timePickerDialog.show(getFragmentManager(), "Material Design TimePicker Dialog");

    }


    //Dialogs
    protected void showSetGoalTitleDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(CreateGoalActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_set_title, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateGoalActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editTextDialogTitle);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        txtGoalTitle.setText(editText.getText());
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    protected void showSetGoalDurationDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(CreateGoalActivity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_set_duration, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CreateGoalActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.editTextDialogDuration);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        txtDurationInMinutes.setText(editText.getText());
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
