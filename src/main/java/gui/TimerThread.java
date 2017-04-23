package gui;


import javafx.application.Platform;
import javafx.scene.control.Label;

/**
 * Created by Helen on 18.04.2017.
 */
public class TimerThread extends Thread implements Runnable {
    private Label timer;
    private boolean recordingBoolean;
    private boolean pauseBoolean;
    private long time;

    public TimerThread(Label timer, long time) {
        this.timer = timer;
        this.time = time;
    }

    public void setRecordingBoolean(boolean recordingBoolean) {
        this.recordingBoolean = recordingBoolean;
    }

    public void setPauseBoolean(boolean pauseBoolean) {
        this.pauseBoolean = pauseBoolean;
    }

    public boolean isPauseBoolean() {
        return pauseBoolean;
    }

    @Override
    public void run() {
        while (recordingBoolean) {
            long time2 = System.currentTimeMillis() - time;
            String timerTextNotFinal = "";
            long time5 = System.currentTimeMillis();
            //hours
            if ((time2 / 3600000) < 10) {
                timerTextNotFinal = ("0" + (time2 / 3600000));
            } else {
                timerTextNotFinal = ("" + (time2 / 3600000));
            }
            //minutes
            if ((time2 / 60000) % 60 < 10) {
                timerTextNotFinal += (":0" + (time2 / 60000));
            } else {
                timerTextNotFinal += (":" + (time2 / 60000));
            }
            //seconds
            if ((time2 / 1000) % 60 < 10) {
                timerTextNotFinal += ":0" + (time2 / 1000) % 60;
            } else {
                timerTextNotFinal += ":" + (time2 / 1000) % 60;
            }
            //Milliseconds
            //timerTextNotFinal += ":" + ((time2 % 1000) / 100 + "0");
            final String timerText = timerTextNotFinal;
            Platform.runLater(() -> timer.setText(timerText));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (pauseBoolean) {
                long time3 = 0;
                while (pauseBoolean) {
                    try {
                        Thread.sleep(100);
                        time3 += 100;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                time = time + time3;
            }

        }
        Platform.runLater(() -> timer.setText("00:00:00"));
    }
}
