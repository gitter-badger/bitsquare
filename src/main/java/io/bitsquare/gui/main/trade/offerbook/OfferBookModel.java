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

package io.bitsquare.gui.main.trade.offerbook;

import io.bitsquare.bank.BankAccount;
import io.bitsquare.gui.UIModel;
import io.bitsquare.gui.util.BSFormatter;
import io.bitsquare.locale.Country;
import io.bitsquare.locale.CurrencyUtil;
import io.bitsquare.offer.Direction;
import io.bitsquare.offer.Offer;
import io.bitsquare.settings.Preferences;
import io.bitsquare.trade.TradeManager;
import io.bitsquare.user.User;

import org.bitcoinj.core.Coin;
import org.bitcoinj.utils.ExchangeRate;
import org.bitcoinj.utils.Fiat;

import com.google.inject.Inject;

import java.util.Comparator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * It holds the scope specific domain data for either a buy or sell UI screen.
 */
class OfferBookModel extends UIModel {
    private static final Logger log = LoggerFactory.getLogger(OfferBookModel.class);

    private final User user;
    private final OfferBook offerBook;
    private final Preferences preferences;
    private final BSFormatter formatter;
    private final TradeManager tradeManager;

    private final FilteredList<OfferBookListItem> filteredItems;
    private final SortedList<OfferBookListItem> sortedItems;
    // private OfferBookInfo offerBookInfo;
    private ChangeListener<BankAccount> bankAccountChangeListener;

    private final ObjectProperty<Coin> amountAsCoin = new SimpleObjectProperty<>();
    private final ObjectProperty<Fiat> priceAsFiat = new SimpleObjectProperty<>();
    private final ObjectProperty<Fiat> volumeAsFiat = new SimpleObjectProperty<>();

    final StringProperty restrictionsInfo = new SimpleStringProperty();
    final StringProperty fiatCode = new SimpleStringProperty();
    final StringProperty btcCode = new SimpleStringProperty();
    final ObjectProperty<Country> bankAccountCountry = new SimpleObjectProperty<>();
    final ObjectProperty<Comparator<OfferBookListItem>> comparator = new SimpleObjectProperty<>();
    private Direction direction;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    OfferBookModel(User user,
                   TradeManager tradeManager,
                   OfferBook offerBook,
                   Preferences preferences,
                   BSFormatter formatter) {
        this.tradeManager = tradeManager;
        this.user = user;
        this.offerBook = offerBook;
        this.preferences = preferences;
        this.formatter = formatter;

        filteredItems = new FilteredList<>(offerBook.getOfferBookListItems());
        sortedItems = new SortedList<>(filteredItems);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initialize() {
        bankAccountChangeListener = (observableValue, oldValue, newValue) -> setBankAccount(newValue);

        super.initialize();
    }

    @Override
    public void activate() {
        super.activate();

        amountAsCoin.set(null);
        priceAsFiat.set(null);
        volumeAsFiat.set(null);

        offerBook.addClient();
        user.currentBankAccountProperty().addListener(bankAccountChangeListener);
        btcCode.bind(preferences.btcDenominationProperty());

        setBankAccount(user.getCurrentBankAccount());
        applyFilter();
    }

    @Override
    public void deactivate() {
        super.deactivate();

        offerBook.removeClient();
        user.currentBankAccountProperty().removeListener(bankAccountChangeListener);
        btcCode.unbind();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void terminate() {
        super.terminate();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////////////////////

    void removeOffer(Offer offer) {
        tradeManager.removeOffer(offer);
    }

    void calculateVolume() {
        try {
            if (priceAsFiat.get() != null &&
                    amountAsCoin.get() != null &&
                    !amountAsCoin.get().isZero() &&
                    !priceAsFiat.get().isZero()) {
                volumeAsFiat.set(new ExchangeRate(priceAsFiat.get()).coinToFiat(amountAsCoin.get()));
            }
        } catch (Throwable t) {
            // Should be never reached
            log.error(t.toString());
        }
    }

    void calculateAmount() {
        try {
            if (volumeAsFiat.get() != null &&
                    priceAsFiat.get() != null &&
                    !volumeAsFiat.get().isZero() &&
                    !priceAsFiat.get().isZero()) {
                // If we got a btc value with more then 4 decimals we convert it to max 4 decimals
                amountAsCoin.set(formatter.reduceTo4Decimals(new ExchangeRate(priceAsFiat.get()).fiatToCoin
                        (volumeAsFiat.get())));
            }
        } catch (Throwable t) {
            // Should be never reached
            log.error(t.toString());
        }
    }

    boolean isTradable(Offer offer) {
        // if user has not registered yet we display all
        if (user.getCurrentBankAccount() == null)
            return true;

        boolean countryResult = offer.getAcceptedCountries().contains(user.getCurrentBankAccount().getCountry());
        // for IRC test version deactivate the check
        countryResult = true;
        if (!countryResult)
            restrictionsInfo.set("This offer requires that the payments account resides in one of those countries:\n" +
                    formatter.countryLocalesToString(offer.getAcceptedCountries()) +
                    "\n\nThe country of your payments account (" + user.getCurrentBankAccount().getCountry().getName() +
                    ") is not included in that list." +
                    "\n\n Do you want to edit your preferences now?");


        // TODO Not so clear how the restrictions will be handled
        // we might get rid of languages (handles viy arbitrators)
        /*
        // disjoint returns true if the two specified collections have no elements in common.
        boolean languageResult = !Collections.disjoint(preferences.getAcceptedLanguageLocales(),
                offer.getAcceptedLanguageLocales());
        if (!languageResult)
            restrictionsInfo.set("This offer requires that the payments account resides in one of those languages:\n" +
                    BSFormatter.languageLocalesToString(offer.getAcceptedLanguageLocales()) +
                    "\n\nThe country of your payments account (" + user.getCurrentBankAccount().getCountry().getName() +
                    ") is not included in that list.");

        boolean arbitratorResult = !Collections.disjoint(preferences.getAcceptedArbitrators(),
                offer.getArbitrators());*/

        return countryResult;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Setters
    ///////////////////////////////////////////////////////////////////////////////////////////

    void setDirection(Direction direction) {
        this.direction = direction;
    }

    void setAmount(Coin amount) {
        amountAsCoin.set(amount);
        applyFilter();
    }

    void setPrice(Fiat price) {
        priceAsFiat.set(price);
        applyFilter();
    }

    void setVolume(Fiat volume) {
        volumeAsFiat.set(volume);
        applyFilter();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////

    SortedList<OfferBookListItem> getOfferList() {
        return sortedItems;
    }

    boolean isRegistered() {
        return user.getAccountId() != null;
    }

    boolean isMyOffer(Offer offer) {
        return offer.getMessagePublicKey() != null && offer.getMessagePublicKey().equals(user.getMessagePublicKey());
    }

    Coin getAmountAsCoin() {
        return amountAsCoin.get();
    }

    ObjectProperty<Coin> amountAsCoinProperty() {
        return amountAsCoin;
    }

    Fiat getPriceAsFiat() {
        return priceAsFiat.get();
    }

    ObjectProperty<Fiat> priceAsFiatProperty() {
        return priceAsFiat;
    }

    Fiat getVolumeAsFiat() {
        return volumeAsFiat.get();
    }

    ObjectProperty<Fiat> volumeAsFiatProperty() {
        return volumeAsFiat;
    }

    Direction getDirection() {
        return direction;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void setBankAccount(BankAccount bankAccount) {
        if (bankAccount != null) {
            fiatCode.set(bankAccount.getCurrency().getCurrencyCode());
            bankAccountCountry.set(bankAccount.getCountry());
            sortedItems.stream().forEach(e -> e.setBankAccountCountry(bankAccount.getCountry()));
        }
        else {
            fiatCode.set(CurrencyUtil.getDefaultCurrency().getCurrencyCode());
        }
    }

    void applyFilter() {
        filteredItems.setPredicate(offerBookListItem -> {
            Offer offer = offerBookListItem.getOffer();

            boolean directionResult = offer.getDirection() != direction;

            boolean amountResult = true;
            if (amountAsCoin.get() != null && amountAsCoin.get().isPositive())
                amountResult = amountAsCoin.get().compareTo(offer.getAmount()) <= 0 &&
                        amountAsCoin.get().compareTo(offer.getMinAmount()) >= 0;

            boolean priceResult = true;
            if (priceAsFiat.get() != null && priceAsFiat.get().isPositive()) {
                if (offer.getDirection() == Direction.SELL)
                    priceResult = priceAsFiat.get().compareTo(offer.getPrice()) >= 0;
                else
                    priceResult = priceAsFiat.get().compareTo(offer.getPrice()) <= 0;
            }

            return directionResult && amountResult && priceResult;
        });
    }

}
