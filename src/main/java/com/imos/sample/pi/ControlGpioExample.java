/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.sample.pi;

import java.util.Calendar;
import java.util.List;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListener;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This example code demonstrates how to perform simple state control of a GPIO
 * pin on the Raspberry Pi.
 *
 * @author Robert Savage
 */
public class ControlGpioExample {

    SMTPMailSample sample = new SMTPMailSample();
    int detectionCount = 0;
    Calendar cal = GregorianCalendar.getInstance();
    boolean recording, sendMail, canSendMail;
    long startTime, currentTime, timeInterval, waitingForMail, mailStartTime, moduleStartTime;
    StringBuilder fileName = new StringBuilder();
    String videoFileName;
    List<String> timeStampList = new ArrayList<>();

    public void controlGpio() throws InterruptedException {

        System.out.println("<--Pi4J--> GPIO Control Example ... started.");
        GpioController gpio = null;
        GpioPinDigitalOutput led = null;
        try {
            // create gpio controller
            gpio = GpioFactory.getInstance();

            // provision gpio pin #01 as an output pin and turn on
            led = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "MyLED", PinState.LOW);

            //ledBlink(led, gpio);
            motionSensor(gpio, led);
            //temperatureSensor(led, gpio);
            //distanceSensor(gpio, led);
        } catch (InterruptedException e) {
            led.low();
            // stop all GPIO activity/threads by shutting down the GPIO controller
            // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
            gpio.shutdown();
            throw e;
        } finally {
            led.low();
            // stop all GPIO activity/threads by shutting down the GPIO controller
            // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
            gpio.shutdown();
        }

        //sample.connect(String.valueOf(detectionCount), fileName.toString());
        System.out.println("done");
    }

    private void temperatureSensor(final GpioPinDigitalOutput led, final GpioController gpio) throws InterruptedException {
        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalInput sensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, "TemperatureSensor", PinPullResistance.PULL_DOWN);
        System.out.println("started");
        System.out.println(sensor.getMode().getValue());
        led.high();

        sensor.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            System.out.println(event.getState().getValue());
        });

        try {
            int count = 0;
            // keep program running until user aborts       
            for (;;) {
                Thread.sleep(500);
                count++;
                if (count > 200) {
                    break;
                }
            }
            System.out.println("completed");
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void ledBlink(final GpioPinDigitalOutput led, final GpioController gpio) throws InterruptedException {
        // set shutdown state for this pin
        led.setShutdownOptions(true, PinState.LOW);
        led.high();
        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(5000);

        // turn off gpio pin #01
        led.low();
        System.out.println("--> GPIO state should be: OFF");

        Thread.sleep(5000);

        // toggle the current state of gpio pin #01 (should turn on)
        led.toggle();
        System.out.println("--> GPIO state should be: ON");

        Thread.sleep(20000);

        // toggle the current state of gpio pin #01  (should turn off)
        led.toggle();
        System.out.println("--> GPIO state should be: OFF");

        Thread.sleep(5000);

        // turn on gpio pin #01 for 1 second and then off
        System.out.println("--> GPIO state should be: ON for only 1 second");
        led.pulse(1000, true); // set second argument to 'true' use a blocking call

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        //gpio.shutdown();
    }

    private void distanceSensor(final GpioController gpio, final GpioPinDigitalOutput led) {
        final GpioPinDigitalInput sensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_05, "DistanceSensor", PinPullResistance.PULL_DOWN);
        System.out.println("started");
        led.high();
        sensor.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {

            System.out.println(event.getState().getValue());
            if (event.getState().isHigh()) {

            }
        });

        int counter = 0;
        while (true) {
            if (counter > 120) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                Logger.getLogger(ControlGpioExample.class.getName()).log(Level.SEVERE, null, ex);
            }
            counter++;
        }
    }

    private void motionSensor(final GpioController gpio, final GpioPinDigitalOutput led) throws InterruptedException {
        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalInput sensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_04, "MotionSensor", PinPullResistance.PULL_DOWN);
        System.out.println("started");
        //led.high();
        moduleStartTime = startTime = currentTime = System.nanoTime();
        sensor.addListener((GpioPinListenerDigital) (GpioPinDigitalStateChangeEvent event) -> {
            System.out.println("in action : " + event.getState());

            if (event.getState().isHigh()) {
                led.high();
                System.out.println("Motion Detected!");
                if (timeInterval > 10) {
                    recording = false;
                }
                String value = calculateTime(System.nanoTime());
                //System.out.println(value);
                timeStampList.add(value);
                detectionCount++;
                currentTime = System.nanoTime();
                if (mailStartTime == 0) {
                    canSendMail = true;
                }
                if (!sendMail) {
                    sendMail = true;
                    waitingForMail = 0;
                    mailStartTime = currentTime;
                }
                if (!recording) {
                    startTime = currentTime;
                }
                if (startTime < currentTime) {
                    timeInterval = currentTime - startTime;
                    timeInterval = timeInterval / 1000000000;
                }
                if (!recording) {
                    System.out.println("recording started");
                    recording = true;
                    Process p;
                    try {
                        createFileName();
                        videoFileName = fileName.toString();
                        //p = Runtime.getRuntime().exec("raspivid -o /home/pi/NetBeansProjects/HelloRaspberryPi/dist/" + videoFileName + " -t 10000 -td 2500,5000");
                        //p = Runtime.getRuntime().exec("raspivid -s -o ./" + videoFileName + " -t 10000 -td 2000 3000");
                        p = Runtime.getRuntime().exec("raspivid -s -o ./" + videoFileName + " -t 10000");
                        p.waitFor();
//                        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//
//                        String line = "";
//                        while ((line = reader.readLine()) != null) {
//                            output.append(line + "\n");
//                        }

                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (event.getState().isLow()) {
                System.out.println("All is quiet...");
                led.low();
//                if (timeInterval > 11) {
//                    System.out.println("recording ends");
//                    recording = false;
//                    timeInterval = 0;
//                }
                System.out.println("recording ends");
                    recording = false;
                    timeInterval = 0;
                Process p;
                String cmd = null;
                StringBuilder output = new StringBuilder();
                try {
                    cmd = "pgrep raspivid";
                    System.out.println(cmd);
                    p = Runtime.getRuntime().exec(cmd);
                    p.waitFor();
                    
                    BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        output.append(line + "\n");
                    }
                    System.out.println("raspivid "+output.toString());
//                    cmd = "kill -USR1 "+output.toString().trim();
//                    System.out.println(cmd);
//                    p = Runtime.getRuntime().exec(cmd);
//                    //p = Runtime.getRuntime().exec("kill "+output.toString().trim());
//                    p.waitFor();
                    
//                    output = new StringBuilder();
//                    reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
//                    line = "";
//                    while ((line = reader.readLine()) != null) {
//                        output.append(line + "\n");
//                    }
//                    System.out.println("raspivid "+output.toString());
                    
                    cmd = "kill "+output.toString().trim();
                    System.out.println(cmd);
                    p = Runtime.getRuntime().exec(cmd);
                    //p = Runtime.getRuntime().exec("kill "+output.toString().trim());
                    p.waitFor();
                } catch (IOException ex) {
                    Logger.getLogger(ControlGpioExample.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ControlGpioExample.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        try {
            int count = 0;
            // keep program running until user aborts       
            for (;;) {
                Thread.sleep(500);
                count++;
                if (count > 180) {
                    break;
                }
                currentTime = System.nanoTime();
                if (startTime < currentTime) {
                    timeInterval = currentTime - moduleStartTime;
                    timeInterval = timeInterval / 1000000000;
                    System.out.println(timeInterval);
                    if (canSendMail) {
                        waitingForMail = currentTime - mailStartTime;
                        waitingForMail = waitingForMail / 1000000000;

                        if (waitingForMail > 60) {
                            recording = false;
                            timeInterval = 0;
                            startTime = currentTime;
                            waitingForMail = 0;
                            canSendMail = false;
                            mailStartTime = 0;
                            System.out.println("mail sent");
                            try (BufferedWriter writer = new BufferedWriter(new FileWriter("timeStamp.txt"))) {
                                for (String time : timeStampList) {
                                    writer.append(time);
                                    writer.newLine();
                                }
                            }
                            String str = creatMp4File();
                            //files = new File("/home/pi/NetBeansProjects/HelloRaspberryPi/dist/" + str);
                            File files = new File("./" + str);
                            StringBuilder builder = new StringBuilder();
                            builder.append(str);
                            if (files.exists()) {
                                new Thread(() -> {
                                    sample.connect(String.valueOf(detectionCount), builder.toString(), "timeStamp.txt");
                                }).start();
                            } else {
                                new Thread(() -> {
                                    sample.connect(String.valueOf(detectionCount), videoFileName, "timeStamp.txt");
                                }).start();
                            }

                            sendMail = false;
                        }
                    }
                }
            }
            System.out.println("loop completed");
        } catch (final Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String creatMp4File() throws IOException, InterruptedException {
        //File files = new File("/home/pi/NetBeansProjects/HelloRaspberryPi/dist");
        File files = new File("./");
        String cmd = "MP4Box";
        for (File file : files.listFiles()) {
            if (file.getName().endsWith("h264")) {
                System.out.println(file.getName());
                cmd += " -cat " + file.getName();
            }
        }
        String str = videoFileName;
        str = str.substring(0, str.lastIndexOf(".h264"));
        str = "new" + str + ".mp4";
        cmd += " -new " + str;
        System.out.println(cmd);
        Process p;
        p = Runtime.getRuntime().exec(cmd);
        p.waitFor();
        return str;
    }

    private void createFileName() {
        fileName = new StringBuilder();
        cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        fileName.append(cal.get(Calendar.HOUR_OF_DAY));
        fileName.append("_");
        fileName.append(cal.get(Calendar.MINUTE));
        fileName.append("_");
        fileName.append(cal.get(Calendar.SECOND));
        fileName.append("_");
        fileName.append(cal.get(Calendar.MILLISECOND));
        fileName.append(".h264");
    }

    String calculateTime(long timeInMilliSec) {
        StringBuilder timeStamp = new StringBuilder();
        cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        timeStamp.append(cal.get(Calendar.DAY_OF_MONTH));
        timeStamp.append("_");
        timeStamp.append(cal.get(Calendar.MONTH));
        timeStamp.append("_");
        timeStamp.append(cal.get(Calendar.YEAR));
        timeStamp.append("_");
        timeStamp.append(cal.get(Calendar.HOUR_OF_DAY));
        timeStamp.append("_");
        timeStamp.append(cal.get(Calendar.MINUTE));
        timeStamp.append("_");
        timeStamp.append(cal.get(Calendar.SECOND));
        timeStamp.append("_");
        timeStamp.append(cal.get(Calendar.MILLISECOND));
        return timeStamp.toString();
    }
}
