package com.rishi.codepath.taskkeeper.model;

import com.rishi.codepath.taskkeeper.R;

/**
 * Enum to hold the priorities of the task
 * TODO Usage could be improved
 * Created by rishi on 9/23/16.
 */
public enum TaskPriority {

    HIGH_PRIORITY(2, R.drawable.high_priority, R.id.radio_high),
    MEDIUM_PRIORITY(1, R.drawable.medium_priority, R.id.radio_medium),
    LOW_PRIORITY(0, R.drawable.low_priority, R.id.radio_low);

    private int drawable;
    private int value;
    private int radioOptionId;

    /**
     * Constructor with required values
     * @param value
     * @param drawable
     * @param radio
     */
    TaskPriority(int value, int drawable, int radio){
        this.value = value;
        this.drawable = drawable;
        this.radioOptionId = radio;
    }

    /**
     * Returns radio option ID for value
     * @param value
     * @return
     */
    public static int getRadioOptionForValue(int value){
        for (TaskPriority priority: values()){
            if(priority.getValue() == value){
                return priority.getRadioOptionId();
            }
        }
        return 0;
    }

    /**
     * Returns drawable icon for value
     * @param value
     * @return
     */
    public static int getIconForValue(int value) {
        for(TaskPriority priority: values()){
            if(priority.getValue() == value){
                return priority.getDrawableIcon();
            }
        }
        return 0;
    }

    /**
     * Returns value of priority for radio option ID
     * @param radioOptionId
     * @return
     */
    public static int getValueForOption(int radioOptionId) {
        for(TaskPriority priority: values()){
            if(priority.getRadioOptionId() == radioOptionId){
                return priority.getValue();
            }
        }
        return 0;
    }

    /**
     * Returns radio option ID
     * @return
     */
    public int getRadioOptionId(){
        return this.radioOptionId;
    }

    /**
     * Returns drawable icon
     * @return
     */
    public int getDrawableIcon(){
        return this.drawable;
    }

    /**
     * Returns value
     * @return
     */
    public int getValue(){
        return this.value;
    }
}
