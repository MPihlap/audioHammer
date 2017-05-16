package client;

/**
 * Created by Meelis on 09/05/2017.
 */
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import java.util.concurrent.BlockingQueue;

public class ListenGPIO implements Runnable{
    private final Client client;
    private boolean readyToRecord;
    private final BlockingQueue<String> commandQueue;

    public void setReadyToRecord(boolean readyToRecord) {
        this.readyToRecord = readyToRecord;
    }

    public ListenGPIO(Client client, BlockingQueue<String> commandQueue) {
        this.client = client;

        this.commandQueue = commandQueue;
    }

    @Override
    public void run() {

            // create gpio controller
            final GpioController gpio = GpioFactory.getInstance();

            // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
            final GpioPinDigitalInput startButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_DOWN);
            final GpioPinDigitalInput stopButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_03, PinPullResistance.PULL_DOWN);

            // set shutdown state for this input pin
            startButton.setShutdownOptions(true);
            stopButton.setShutdownOptions(true);

            stopButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    System.out.println("Stop button state change: "+event.getPin()+event.getState());
                    commandQueue.add("stop");
                }
            });
            // create and register gpio pin listener
            startButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    // display pin state on console
                    System.out.println(" --> Start STATE CHANGE: " + event.getPin() + " = " + event.getState());
                    if (event.getState() == PinState.HIGH && readyToRecord){
                        commandQueue.add("start");
                        System.out.println("Started");
                    }
                }

            });
            stopButton.addListener(new GpioPinListenerDigital() {
                @Override
                public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                    if (event.getState() == PinState.HIGH && readyToRecord) {
                        commandQueue.add("stop");
                        System.out.println("Stopped");
                    }
                }
            });

            // keep program running until user aborts (CTRL-C)
            while(true) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // stop all GPIO activity/threads by shutting down the GPIO controller
            // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
            // gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller
    }
}
