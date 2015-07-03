/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imos.sample.pi;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Alok
 */
public class PiMainFile {

    public static void main(String[] args) {

        boolean mail = false;
        if (mail) {
            SMTPMailSample sample = new SMTPMailSample();
            sample.connect("", "sample.txt", "sample.txt");
        } else {
            ControlGpioExample control = new ControlGpioExample();
            try {
                control.controlGpio();
            } catch (InterruptedException ex) {
                Logger.getLogger(PiMainFile.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
