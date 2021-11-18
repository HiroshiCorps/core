/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.receiver;

import fr.redline.pms.connect.linker.thread.Client;
import fr.redxil.api.common.utils.FilePercentageReceiver;

public class DataActualise implements FilePercentageReceiver {

    Client client;

    public DataActualise(Client client) {
        this.client = client;
    }

    @Override
    public void changePercentage(double percent) {
        sendInfo("Download percentage: " + percent);
    }

    @Override
    public void noPercentage() {
        sendInfo("Download: No percentage available");
    }

    @Override
    public void fileSize(int size) {
        sendInfo("File size to download: " + size);
    }

    @Override
    public void sendInfo(String data) {

        /*
        client.write(data);
        client.read();
         */
    }

    public void infoStop() {
        /*
        client.write(data);
        client.read();
        */
    }

}
