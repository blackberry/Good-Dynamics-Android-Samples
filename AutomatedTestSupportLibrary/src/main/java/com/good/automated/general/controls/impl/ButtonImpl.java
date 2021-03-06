/* Copyright (c) 2017 - 2020 BlackBerry Limited.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*/

package com.good.automated.general.controls.impl;

import com.good.automated.general.controls.Button;

public class ButtonImpl extends ControlBase implements Button {

    private ButtonImpl(ControlWrapper control) {
        super(control, ButtonImpl.class.getCanonicalName());
    }

    @Override
    public boolean click() {
        return control.click();
    }

    @Override
    public String getResourceID() {
        return control.getResourceID();
    }

    @Override
    public String getClassName() {
        return control.getClassName();
    }

    @Override
    public String getPackage() {
        return control.getPackage();
    }

    @Override
    public boolean isSelected() {
        return control.isSelected();
    }

    @Override
    public boolean isEnabled() {
        return control.isEnabled();
    }

    @Override
    public boolean isClickable() {
        return control.isClickable();
    }

    @Override
    public boolean isFocusable() {
        return control.isFocusable();
    }

    @Override
    public boolean isAvailable() {
        return control.isAvailable();
    }

    @Override
    public String getText() {
        return super.getText();
    }

    public static Button getByID(String packageID, String id, int delay) {
        return new ButtonImpl(new ControlWrapper().getControlWrapperObject(packageID, id, delay));
    }

    public static Button getByID(String packageID, String id) {
        return new ButtonImpl(new ControlWrapper().getControlWrapperObject(packageID, id));
    }
}
