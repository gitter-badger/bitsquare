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

package io.bitsquare.gui.main.funds;

import io.bitsquare.gui.CachedViewCB;
import io.bitsquare.gui.Navigation;
import io.bitsquare.gui.ViewCB;
import io.bitsquare.gui.ViewLoader;

import java.net.URL;

import java.util.ResourceBundle;

import javax.inject.Inject;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.control.*;

public class FundsViewCB extends CachedViewCB {

    private Navigation.Listener navigationListener;
    private ChangeListener<Tab> tabChangeListener;
    private Tab currentTab;

    @FXML Tab withdrawalTab, transactionsTab;

    private final ViewLoader viewLoader;
    private final Navigation navigation;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    FundsViewCB(ViewLoader viewLoader, Navigation navigation) {
        super();
        this.viewLoader = viewLoader;
        this.navigation = navigation;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        navigationListener = navigationItems -> {
            if (navigationItems != null && navigationItems.length == 3
                    && navigationItems[1] == Navigation.Item.FUNDS)
                loadView(navigationItems[2]);
        };

        tabChangeListener = (ov, oldValue, newValue) -> {
            if (newValue == withdrawalTab)
                navigation.navigationTo(Navigation.Item.MAIN, Navigation.Item.FUNDS, Navigation.Item.WITHDRAWAL);
            else if (newValue == transactionsTab)
                navigation.navigationTo(Navigation.Item.MAIN, Navigation.Item.FUNDS, Navigation.Item.TRANSACTIONS);
        };

        super.initialize(url, rb);
    }

    @Override
    public void activate() {
        super.activate();

        ((TabPane) root).getSelectionModel().selectedItemProperty().addListener(tabChangeListener);
        navigation.addListener(navigationListener);

        if (((TabPane) root).getSelectionModel().getSelectedItem() == transactionsTab)
            navigation.navigationTo(Navigation.Item.MAIN, Navigation.Item.FUNDS, Navigation.Item.TRANSACTIONS);
        else
            navigation.navigationTo(Navigation.Item.MAIN, Navigation.Item.FUNDS, Navigation.Item.WITHDRAWAL);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        ((TabPane) root).getSelectionModel().selectedItemProperty().removeListener(tabChangeListener);
        navigation.removeListener(navigationListener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Navigation
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    protected Initializable loadView(Navigation.Item navigationItem) {
        super.loadView(navigationItem);

        // we want to get activate/deactivate called, so we remove the old view on tab change
        if (currentTab != null)
            currentTab.setContent(null);

        ViewLoader.Item loaded = viewLoader.load(navigationItem.getFxmlUrl());
        switch (navigationItem) {
            case WITHDRAWAL:
                currentTab = withdrawalTab;
                break;
            case TRANSACTIONS:
                currentTab = transactionsTab;
                break;
        }
        currentTab.setContent(loaded.view);
        ((TabPane) root).getSelectionModel().select(currentTab);
        Initializable childController = loaded.controller;
        ((ViewCB) childController).setParent(this);

        return childController;
    }

}

