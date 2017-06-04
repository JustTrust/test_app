package com.player.receiver;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;


/**
 * Created by Test-Gupta on 4/7/2017.
 */

public class MyPhoneStateListener extends PhoneStateListener {

    SignalChangeListener listener;

    public MyPhoneStateListener(SignalChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);

        if (signalStrength.isGsm()) {
            int strength = signalStrength.getGsmSignalStrength();

            if(strength == 99) {
                strength = 0;
            }

            int percent = (strength * 100) / 31;
            if (listener != null) listener.onSignalChange(percent);
        }
        else {
            final int snr = signalStrength.getEvdoSnr();
            final int cdmaDbm = signalStrength.getCdmaDbm();
            final int cdmaEcio = signalStrength.getCdmaEcio();
            int levelDbm;
            int levelEcio;
            int level = 0;

            if (snr == -1) {
                if (cdmaDbm >= -75) levelDbm = 4;
                else if (cdmaDbm >= -85) levelDbm = 3;
                else if (cdmaDbm >= -95) levelDbm = 2;
                else if (cdmaDbm >= -100) levelDbm = 1;
                else levelDbm = 0;

                // Ec/Io are in dB*10
                if (cdmaEcio >= -90) levelEcio = 4;
                else if (cdmaEcio >= -110) levelEcio = 3;
                else if (cdmaEcio >= -130) levelEcio = 2;
                else if (cdmaEcio >= -150) levelEcio = 1;
                else levelEcio = 0;

                level = (levelDbm < levelEcio) ? levelDbm : levelEcio;
            } else {
                if (snr == 7 || snr == 8) level =4;
                else if (snr == 5 || snr == 6 ) level =3;
                else if (snr == 3 || snr == 4) level = 2;
                else if (snr ==1 || snr ==2) level =1;
            }

            int percent = (level * 100) / 4;
            if (listener != null) listener.onSignalChange(percent);
        }
    }

    public interface SignalChangeListener{
        void onSignalChange(int percent);
    }
}
