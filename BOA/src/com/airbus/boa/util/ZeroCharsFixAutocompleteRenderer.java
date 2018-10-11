/*
 * ------------------------------------------------------------------------
 * Class : ZeroCharsFixAutocompleteRenderer
 * Copyright 2016 by AIRBUS France
 * ------------------------------------------------------------------------
 */

package com.airbus.boa.util;

import javax.faces.component.UIComponent;

import org.richfaces.component.AbstractAutocomplete;
import org.richfaces.renderkit.html.AutocompleteRenderer;

/**
 * Allows rich:autocomplete to specify 'minChars="0"'. This is useful if you
 * want to be able to press the drop-down button ('showButton="true") and have
 * the drop down display the whole, unfiltered list.
 */
public class ZeroCharsFixAutocompleteRenderer extends AutocompleteRenderer {


    @Override
    protected int getMinCharsOrDefault(UIComponent component) {
      int value = 0;
      if (component instanceof AbstractAutocomplete) {
          value = ((AbstractAutocomplete) component).getMinChars();
          if (value < 0) {
              value = 0;
          }
      }

      return value;
    }
}