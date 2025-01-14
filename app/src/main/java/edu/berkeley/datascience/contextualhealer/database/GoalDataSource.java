package edu.berkeley.datascience.contextualhealer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import edu.berkeley.datascience.contextualhealer.R;
import edu.berkeley.datascience.contextualhealer.activity.ActivityType;
import edu.berkeley.datascience.contextualhealer.model.ActivitySample;
import edu.berkeley.datascience.contextualhealer.model.ActivitySummary;
import edu.berkeley.datascience.contextualhealer.model.Goal;
import edu.berkeley.datascience.contextualhealer.model.GoalCompletion;
import edu.berkeley.datascience.contextualhealer.utils.CommonUtil;

public class GoalDataSource {

    private static final String TAG = "UPDATE_DB";
    private Context mContext;
    private GoalSQLiteHelper mGoalSQLiteHelper;
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();
    private final Lock r = rwl.readLock();
    private final Lock w = rwl.writeLock();

    public GoalDataSource(Context context){
        mContext = context;
        //mGoalSQLiteHelper = new GoalSQLiteHelper(context);
        //Create from singleton
        mGoalSQLiteHelper = GoalSQLiteHelper.getInstance(context);


        //For initial seeding
        //SQLiteDatabase database = mGoalSQLiteHelper.getReadableDatabase();
        //database.close();
    }

    private SQLiteDatabase  open(){
        return mGoalSQLiteHelper.getWritableDatabase();
    }

    private  void close(SQLiteDatabase database){
        database.close();
    }


    //Read Operation
    public ArrayList<Goal> readActiveGoals(){
        //return  getMockedActiveGoals();



        SQLiteDatabase database = open();
        Cursor cursor = database.query(
                GoalSQLiteHelper.GOALS_TABLE,
                new String[] {BaseColumns._ID,
                        GoalSQLiteHelper.COLUMN_GOAL_TITLE,
                        GoalSQLiteHelper.COLUMN_GOAL_TYPE,
                        GoalSQLiteHelper.COLUMN_GOAL_DURATION_IN_MINUTES,
                        GoalSQLiteHelper.COLUMN_GOAL_REPEAT_TYPE,
                        GoalSQLiteHelper.COLUMN_GOAL_REPEAT_PATTERN,
                        GoalSQLiteHelper.COLUMN_GOAL_START_TIME,
                        GoalSQLiteHelper.COLUMN_GOAL_END_TIME,
                        GoalSQLiteHelper.COLUMN_GOAL_SET_DATE,
                        GoalSQLiteHelper.COLUMN_IS_GOAL_CURRENTLY_TRACKED,
                        GoalSQLiteHelper.COLUMN_IS_GOAL_DELETED
                },
                null, // selection
                null, // selection args
                null, // group by
                null, // having
                null); //order
        ArrayList<Goal> activeGoals = new ArrayList<Goal>();
        if(cursor.moveToFirst()){
            do{
                int GoalID = getIntFromColumnName(cursor, BaseColumns._ID);
                Goal goal = new Goal(getIntFromColumnName(cursor, BaseColumns._ID),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_TITLE),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_TYPE),
                        getIntFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_DURATION_IN_MINUTES),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_REPEAT_TYPE),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_REPEAT_PATTERN),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_START_TIME),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_END_TIME),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_GOAL_SET_DATE),
                        getIntFromColumnName(cursor, GoalSQLiteHelper.COLUMN_IS_GOAL_CURRENTLY_TRACKED),
                        getIntFromColumnName(cursor, GoalSQLiteHelper.COLUMN_IS_GOAL_DELETED));

                //TODO: WORK ON COMPLETION PERCENTAGE
                //As per current GoalID and Current Date, the Completion Percentage

                //goal.setCompletedPercentage(60);
                String currentDate = CommonUtil.GetCurrentDateString();
                GoalCompletion goalCompletionData = readGoalCompletionIDDateWise(GoalID, currentDate);
                if (goalCompletionData == null) {
                    goal.setCompletedPercentage(0.0f);
                }
                else{
                    goal.setCompletedPercentage(CommonUtil.round(goalCompletionData.getGoalCompletionPercentage(),1));
                }

                activeGoals.add(goal);

            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
        return activeGoals;
    }


    //Create a new goal
    public void create(Goal goal){

        SQLiteDatabase database = open();

        database.beginTransaction();

        ContentValues goalValues = new ContentValues();
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_TITLE, goal.getGoalTitle());
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_TYPE, goal.getGoalType());
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_DURATION_IN_MINUTES, goal.getGoalDurationInMinutes());
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_REPEAT_TYPE, goal.getGoalRepeatType());
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_REPEAT_PATTERN, goal.getGoalRepeatPattern());
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_START_TIME, goal.getGoalStartTime());
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_END_TIME, goal.getGoalEndTime());
        goalValues.put(GoalSQLiteHelper.COLUMN_GOAL_SET_DATE, goal.getGoalSetDate());
        goalValues.put(GoalSQLiteHelper.COLUMN_IS_GOAL_CURRENTLY_TRACKED, goal.getIsGoalCurrentlyTracked());
        goalValues.put(GoalSQLiteHelper.COLUMN_IS_GOAL_DELETED, goal.getIsGoalDeleted());

        long goalID = database.insert(GoalSQLiteHelper.GOALS_TABLE,null,goalValues);

        database.setTransactionSuccessful();
        database.endTransaction();
        //close(database);  //TODO : Check if it is required to close it , if using singleton

    }

    //Update Operation
    public void update(Goal goal){
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues updateGoalValues = new ContentValues();

        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_TITLE, goal.getGoalTitle());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_TYPE, goal.getGoalType());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_DURATION_IN_MINUTES, goal.getGoalDurationInMinutes());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_REPEAT_TYPE, goal.getGoalRepeatType());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_REPEAT_PATTERN, goal.getGoalRepeatPattern());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_START_TIME, goal.getGoalStartTime());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_END_TIME, goal.getGoalEndTime());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_GOAL_SET_DATE, goal.getGoalSetDate());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_IS_GOAL_CURRENTLY_TRACKED, goal.getIsGoalCurrentlyTracked());
        updateGoalValues.put(GoalSQLiteHelper.COLUMN_IS_GOAL_DELETED, goal.getIsGoalDeleted());
        database.update(GoalSQLiteHelper.GOALS_TABLE,
                updateGoalValues,
                String.format("%s=%d", BaseColumns._ID, goal.getGoalID()),null);

        database.setTransactionSuccessful();
        database.endTransaction();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
    }

    public void delete(int goalId){
        SQLiteDatabase database = open();
        database.beginTransaction();

        // delete goal
        database.delete(GoalSQLiteHelper.GOALS_TABLE,
                String.format("%s=%s", BaseColumns._ID, String.valueOf(goalId)), null);


        database.setTransactionSuccessful();
        database.endTransaction();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
    }

    public ArrayList<ActivitySummary> readActivitySummary(){

       String query =  "SELECT " +  GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_ACTIVITY_TYPE + ", " +
                "SUM(" +   GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_DURATION_IN_MILLI_SECS +
                 " / (60.0 * 1000.0))  " + GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_TOTAL_DURATION_IN_MINS  +" from "+ GoalSQLiteHelper.ACTIVITY_SAMPLES_TABLE + " group by " +
               GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_ACTIVITY_TYPE ;

        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery(query, null);
        ArrayList<ActivitySummary> activitySummaries = new ArrayList<ActivitySummary>();

        if(cursor.moveToFirst()){
            do{
                String activityType = getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_ACTIVITY_TYPE);
                float TotalDurationInMinutes = getFloatFromColumnName(cursor, GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_TOTAL_DURATION_IN_MINS);
                Log.v(TAG, "activity type : "+ activityType + " Total duration :" + TotalDurationInMinutes );

                ActivitySummary summary = new ActivitySummary(
                        activityType,
                        TotalDurationInMinutes);
                activitySummaries.add(summary);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
        return activitySummaries;
    }

    public ArrayList<ActivitySample> readActivitySamples(int duration){

        String query =  "SELECT " +  GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_ACTIVITY_TYPE + ", " +
                  GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_END_TIME_STAMP +
                 " from "+ GoalSQLiteHelper.ACTIVITY_SAMPLES_TABLE + " where " + GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_ACTIVITY_TYPE +
                 " not like 'unknown'";

        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery(query, null);
        ArrayList<ActivitySample> activitySamples = new ArrayList<ActivitySample>();

        if(cursor.moveToFirst()){
            do{

                ActivitySample sample = new ActivitySample();
                sample.setActivityType(getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_ACTIVITY_TYPE));
                String endTimeStamp = getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_END_TIME_STAMP);
                sample.setEndTimeStamp(endTimeStamp);
                sample.setEndTimeStampInDate(CommonUtil.ParseTimeStampString(endTimeStamp));
                if(CommonUtil.IsBetweenLastOneHour(CommonUtil.ParseTimeStampString(endTimeStamp))){

                    // If timestamp is between last one hour
                    activitySamples.add(sample);
                }

            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton

        //Now the activities are available : Let's work and sort it in descending order
        return CommonUtil.GetActivitySampleForTimeLine(activitySamples);

    }

    public int readGoalsSetCount(){

        int setGoalsCount = 0;
        String query =  "SELECT count(*) Total_Count from "+ GoalSQLiteHelper.GOALS_COMPLETION_TABLE ;
        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery(query, null);
        ArrayList<ActivitySummary> activitySummaries = new ArrayList<ActivitySummary>();

        if(cursor.moveToFirst()){
            do{
                setGoalsCount = getIntFromColumnName(cursor, "Total_Count");
            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
        return setGoalsCount;
    }

    public float readGoalsAverageCompletionPercentage(){

        float avgCompletion = 0.0f;
        String query =  "SELECT avg(" +  GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_COMPLETION_PERCENTAGE  + ") Avg_Completion from "+ GoalSQLiteHelper.GOALS_COMPLETION_TABLE ;
        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery(query, null);
        ArrayList<ActivitySummary> activitySummaries = new ArrayList<ActivitySummary>();

        if(cursor.moveToFirst()){
            do{
                avgCompletion = getFloatFromColumnName(cursor, "Avg_Completion");
            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
        return avgCompletion;
    }

    public ArrayList<GoalCompletion> readGoalCompletionByType(){

        String query =  "select GOAL_TYPE, avg(GOAL_COMPLETION_PERCENTAGE) Avg_Completion from GOALS_COMPLETION Group by GOAL_TYPE" ;

        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery(query, null);
        ArrayList<GoalCompletion> result = new ArrayList<GoalCompletion>();

        if(cursor.moveToFirst()){
            do{

                GoalCompletion summary = new GoalCompletion(
                        getFloatFromColumnName(cursor,"Avg_Completion"),
                        getStringFromColumnName(cursor, GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_TYPE)
                        );
                result.add(summary);
            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
        return result;
    }

    public boolean IsGoalCompletionRowInDB(GoalCompletion goalCompletion){

        int goalCompletionCount = 0;
        String query =  "SELECT count(*) Total_Count from "+ GoalSQLiteHelper.GOALS_COMPLETION_TABLE +
                " WHERE " + GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_ID + " == " + goalCompletion.getGoalID() +
                " AND " + GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_DATE + " LIKE '" + goalCompletion.getGoalDate() + "'";

        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery(query, null);


        if(cursor.moveToFirst()){
            do{
                goalCompletionCount = getIntFromColumnName(cursor, "Total_Count");
            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
        return (goalCompletionCount > 0);

    }

    public GoalCompletion readGoalCompletionIDDateWise(int goalID, String goalDate){

        int goalCompletionCount = 0;
        String query =  "SELECT * from "+ GoalSQLiteHelper.GOALS_COMPLETION_TABLE +
                " WHERE " + GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_ID + " == " + goalID +
                " AND " + GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_DATE + " LIKE '" + goalDate + "'";

        SQLiteDatabase database = open();
        Cursor cursor = database.rawQuery(query, null);
        GoalCompletion goalCompletion = null;

        if(cursor.moveToFirst()){
            do{
                goalCompletion = new GoalCompletion(
                  getIntFromColumnName(cursor, GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_ID),
                        getStringFromColumnName(cursor,GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_DATE),
                        getFloatFromColumnName(cursor, GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_COMPLETION_PERCENTAGE),
                        getStringFromColumnName(cursor,GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_TYPE)
                );

            }while(cursor.moveToNext());
        }
        cursor.close();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
        return goalCompletion;
    }

    //Update Operation
    public void updateGoalCompletionPercentage(GoalCompletion goalCompletion){
        SQLiteDatabase database = open();
        database.beginTransaction();

        ContentValues updateGoalCompletionValues = new ContentValues();

        updateGoalCompletionValues.put(GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_COMPLETION_PERCENTAGE, goalCompletion.getGoalCompletionPercentage());

        database.update(GoalSQLiteHelper.GOALS_COMPLETION_TABLE,
                updateGoalCompletionValues,
                String.format("%s=%d", GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_ID, goalCompletion.getGoalID()),null);

        database.setTransactionSuccessful();
        database.endTransaction();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
    }

    //Insert
    public void insertActivitySample(ActivitySample sample){
        SQLiteDatabase database = open();

        database.beginTransaction();

        ContentValues sampleValues = new ContentValues();
        sampleValues.put(GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_START_TIME_STAMP, sample.getStartTimeStamp());
        sampleValues.put(GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_END_TIME_STAMP, sample.getEndTimeStamp());
        sampleValues.put(GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_DURATION_IN_MILLI_SECS, sample.getDurationInMilliSecs());
        sampleValues.put(GoalSQLiteHelper.COLUMN_ACTIVITY_SAMPLES_ACTIVITY_TYPE,sample.getActivityType());

        database.insert(GoalSQLiteHelper.ACTIVITY_SAMPLES_TABLE,null,sampleValues);

        database.setTransactionSuccessful();
        database.endTransaction();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
    }

    //Insert
    public void insertGoalCompletion(GoalCompletion goalCompletion){

        SQLiteDatabase database = open();

        database.beginTransaction();

        ContentValues sampleValues = new ContentValues();
        sampleValues.put(GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_ID, goalCompletion.getGoalID());
        sampleValues.put(GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_DATE, goalCompletion.getGoalDate());
        sampleValues.put(GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_COMPLETION_PERCENTAGE, goalCompletion.getGoalCompletionPercentage());
        sampleValues.put(GoalSQLiteHelper.COLUMN_COMPLETION_TABLE_GOAL_TYPE,goalCompletion.getGoalType());

        database.insert(GoalSQLiteHelper.GOALS_COMPLETION_TABLE,null,sampleValues);

        database.setTransactionSuccessful();
        database.endTransaction();
        //close(database);  //TODO : Check if it is required to close it , if using singleton
    }

    //Helpers
    private int getIntFromColumnName(Cursor cursor, String columnName){
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getInt(columnIndex);
    }

    private String getStringFromColumnName(Cursor cursor, String columnName){
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getString(columnIndex);
    }

    private float getFloatFromColumnName(Cursor cursor, String columnName){
        int columnIndex = cursor.getColumnIndex(columnName);
        return cursor.getFloat(columnIndex);
    }




}
