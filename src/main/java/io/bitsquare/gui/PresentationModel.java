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

package io.bitsquare.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PresentationModel<T extends UIModel> {
    private static final Logger log = LoggerFactory.getLogger(PresentationModel.class);

    protected T model;

    public PresentationModel(T model) {
        this.model = model;
    }

    public PresentationModel() {
    }

    public void initialize() {
        log.trace("Lifecycle: initialize " + this.getClass().getSimpleName());
        if (model != null)
            model.initialize();
    }

    public void activate() {
        log.trace("Lifecycle: activate " + this.getClass().getSimpleName());
        if (model != null)
            model.activate();
    }

    public void deactivate() {
        log.trace("Lifecycle: deactivate " + this.getClass().getSimpleName());
        if (model != null)
            model.deactivate();
    }

    public void terminate() {
        log.trace("Lifecycle: terminate " + this.getClass().getSimpleName());
        if (model != null)
            model.terminate();
    }
}
