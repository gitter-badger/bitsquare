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

package io.bitsquare.gui.util.validation;

import io.bitsquare.locale.BSResources;
import io.bitsquare.locale.CurrencyUtil;
import io.bitsquare.user.User;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FiatNumberValidator for validating fiat values.
 * <p>
 * That class implements just what we need for the moment. It is not intended as a general purpose library class.
 */
public final class OptionalFiatValidator extends NumberValidator {
    private static final Logger log = LoggerFactory.getLogger(OptionalFiatValidator.class);

    //TODO Find appropriate values - depends on currencies
    public static final double MIN_FIAT_VALUE = 0.01; // usually a cent is the smallest currency unit
    public static final double MAX_FIAT_VALUE = 1000000;
    private String currencyCode = "Fiat";


    @Inject
    public OptionalFiatValidator(User user) {
        if (user != null) {
            if (user.currentBankAccountProperty().get() == null)
                setFiatCurrencyCode(CurrencyUtil.getDefaultCurrency().getCurrencyCode());
            else if (user.currentBankAccountProperty().get() != null)
                setFiatCurrencyCode(user.currentBankAccountProperty().get().getCurrency().getCurrencyCode());

            user.currentBankAccountProperty().addListener((ov, oldValue, newValue) -> {
                if (newValue != null)
                    setFiatCurrencyCode(newValue.getCurrency().getCurrencyCode());
            });
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Public methods
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public ValidationResult validate(String input) {
        ValidationResult result = validateIfNotEmpty(input);

        // we accept empty input
        if (!result.isValid)
            return new ValidationResult(true);

        if (result.isValid) {
            input = cleanInput(input);
            result = validateIfNumber(input);
        }

        if (result.isValid) {
            result = validateIfNotZero(input);
            if (result.isValid) {
                result = validateIfNotNegative(input)
                        .and(validateIfNotExceedsMinFiatValue(input))
                        .and(validateIfNotExceedsMaxFiatValue(input));
            }
            else {
                // we accept zero input
                return new ValidationResult(true);
            }
        }

        return result;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Setter
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void setFiatCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////////////////////////////////////////

    private ValidationResult validateIfNotExceedsMinFiatValue(String input) {
        double d = Double.parseDouble(input);
        if (d < MIN_FIAT_VALUE)
            return new ValidationResult(false, BSResources.get("validation.fiat.toSmall", currencyCode));
        else
            return new ValidationResult(true);
    }

    private ValidationResult validateIfNotExceedsMaxFiatValue(String input) {
        double d = Double.parseDouble(input);
        if (d > MAX_FIAT_VALUE)
            return new ValidationResult(false, BSResources.get("validation.fiat.toLarge", currencyCode));
        else
            return new ValidationResult(true);
    }
}
