/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.gui.main.settings.network;

import io.bitsquare.btc.BitcoinNetwork;
import io.bitsquare.network.ClientNode;

import java.net.URL;

import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

public class NetworkSettingsViewCB implements Initializable {

    private final String bitcoinNetworkValue;
    private final ClientNode clientNode;

    @FXML TextField bitcoinNetwork, connectionType, nodeAddress, bootstrapNodeAddress;

    @Inject
    public NetworkSettingsViewCB(BitcoinNetwork bitcoinNetwork, ClientNode clientNode) {
        this.bitcoinNetworkValue = bitcoinNetwork.toString();
        this.clientNode = clientNode;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        bitcoinNetwork.setText(bitcoinNetworkValue);
        connectionType.setText(clientNode.getConnectionType().toString());
        nodeAddress.setText(clientNode.getAddress().toString());
        bootstrapNodeAddress.setText(clientNode.getBootstrapNodeAddress().toString());
    }
}

