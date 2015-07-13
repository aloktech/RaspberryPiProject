/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.sample.pi;

import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * This example code demonstrates how to perform simple state control of a GPIO
 * pin on the Raspberry Pi.
 *
 * @author Robert Savage
 */
public class PythonTemperatureSensor {

    SMTPMailSample sample = new SMTPMailSample();
    int detectionCount = 0;
    Calendar cal = GregorianCalendar.getInstance();
    boolean recording, sendMail, canSendMail;
    long startTime, currentTime, timeInterval, waitingForMail, mailStartTime, moduleStartTime;
    StringBuilder fileName = new StringBuilder();
    String videoFileName;
    List<String> timeStampList = new ArrayList<>();

    public void controlGpio() throws InterruptedException {

        System.out.println("<--Pi4J--> PythonTemperatureSensor ... started.");
        
        pythonTemperatureSensor();
    }

    private void start(final GpioPinDigitalOutput led, final GpioController gpio) throws InterruptedException {

    }

    public void pythonTemperatureSensor() {

        try {
            String cmd = "sudo python /home/pi/Adafruit_Python_DHT/examples/AdafruitDHT.py 22 4";
            int count = 0;
            JSONArray array = new JSONArray();
            int dayOfMonth = 0;
            cal.setTime(new Date());
            dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            while (true) {
                Process p = Runtime.getRuntime().exec(cmd);
                p.waitFor();

                StringBuilder output = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                String result = output.toString(), tempStr;
                double temp, humid;
                if (result != null && !result.trim().isEmpty()) {
                    tempStr = result.substring(result.indexOf("Humid"));
                    result = result.substring(result.indexOf("=") + 1, result.indexOf("C") - 1);
                    temp = Double.parseDouble(result);
                    result = tempStr;
                    result = result.substring(result.indexOf("=") + 1, result.indexOf("%"));
                    humid = Double.parseDouble(result);

                    JSONObject data = new JSONObject();
                    data.put("temp", temp);
                    data.put("humid", humid);
                    data.put("time", new Date().getTime());

                    array.put(data);
                }

                Thread.sleep(60000);
                count++;
                if (count == 60) {
                    cal.setTime(new Date());
                    StringBuilder builder = new StringBuilder();
                    builder.append(cal.get(Calendar.DAY_OF_MONTH));
                    builder.append("-");
                    builder.append(cal.get(Calendar.MONTH));
                    builder.append("-");
                    builder.append(cal.get(Calendar.YEAR));
                    builder.append("-");
                    builder.append(cal.get(Calendar.HOUR_OF_DAY));
                    builder.append("_");
                    builder.append(cal.get(Calendar.MINUTE));
                    String name = builder.toString();
                    Logger.getLogger(PiMainFile.class.getName()).log(Level.INFO, "{0} recorded", name);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(name + "_data.json"))) {
                        writer.append(array.toString());
                    } catch (IOException ex) {

                    }
                    System.out.println(builder.toString());
                    count = 0;
                    array = new JSONArray();
                    if (dayOfMonth != cal.get(Calendar.DAY_OF_MONTH)) {
                        builder = new StringBuilder();
                        builder.append(cal.get(Calendar.DAY_OF_MONTH));
                        builder.append("-");
                        builder.append(cal.get(Calendar.MONTH));
                        builder.append("-");
                        builder.append(cal.get(Calendar.YEAR));
                        String dirName = builder.toString();
                        File newDir = new File("./" + dirName);
                        newDir.mkdir();

                        File files = new File("./");
                        for (File file : files.listFiles()) {
                            if (file.getName().endsWith(".json")) {
                                file.renameTo(new File("./" + dirName +File.separator+ file.getName()));
                            }
                        }

                        dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    }
                }
            }
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(PythonTemperatureSensor.class.getName()).log(Level.SEVERE, null, ex);
        }
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
