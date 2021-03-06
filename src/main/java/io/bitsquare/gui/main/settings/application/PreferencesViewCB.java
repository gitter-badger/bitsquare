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

package io.bitsquare.gui.main.settings.application;

import io.bitsquare.gui.CachedViewCB;

import java.net.URL;

import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.fxml.FXML;
import javafx.scene.control.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This UI is not cached as it is normally only needed once.
 */
public class PreferencesViewCB extends CachedViewCB<PreferencesPM> {

    private static final Logger log = LoggerFactory.getLogger(PreferencesViewCB.class);

    @FXML ComboBox<String> btcDenominationComboBox;
    @FXML CheckBox useAnimationsCheckBox, useEffectsCheckBox;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    private PreferencesViewCB(PreferencesPM presentationModel) {
        super(presentationModel);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        super.initialize(url, rb);
    }

    @Override
    public void activate() {
        super.activate();

        btcDenominationComboBox.setItems(presentationModel.getBtcDenominationItems());
        btcDenominationComboBox.getSelectionModel().select(presentationModel.btcDenomination().get());

        useAnimationsCheckBox.selectedProperty().bindBidirectional(presentationModel.useAnimations());
        useEffectsCheckBox.selectedProperty().bindBidirectional(presentationModel.useEffects());

    }

    @Override
    public void deactivate() {
        super.deactivate();

        useAnimationsCheckBox.selectedProperty().unbind();
        useEffectsCheckBox.selectedProperty().unbind();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void terminate() {
        super.terminate();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // UI Handlers
    ///////////////////////////////////////////////////////////////////////////////////////////

    @FXML
    void onSelectBtcDenomination() {
        presentationModel.btcDenomination().set(btcDenominationComboBox.getSelectionModel().getSelectedItem());
    }
}
