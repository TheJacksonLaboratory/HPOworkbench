package org.monarchinitiative.hpoworkbench.gui;

import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.scene.control.TextField;
import org.controlsfx.control.textfield.AutoCompletionBinding;

import java.util.Collection;

public class WidthAwareTextFields {

    /**
     * Create autocompletion binding between given {@link TextField} instance and Collection of possible suggestions.
     * Additionally, bind the minWidthProperty of suggestion box to widthProperty of textField.
     *
     * @param textField           TextField to which the suggestions will be offered.
     * @param possibleSuggestions Collection of all possible suggestions.
     * @param <T>                 type
     * @return the binding object
     */
    public static <T> AutoCompletionBinding<T> bindWidthAwareAutoCompletion(
            TextField textField, Collection<T> possibleSuggestions) {
        AutoCompletionTextFieldBinding<T> k = new AutoCompletionTextFieldBinding<>(textField,
                SuggestionProvider.create(possibleSuggestions));
        k.minWidthProperty().bind(textField.widthProperty());
        return k;
    }

}
