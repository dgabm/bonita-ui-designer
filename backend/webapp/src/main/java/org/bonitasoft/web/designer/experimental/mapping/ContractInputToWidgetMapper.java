/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.bonitasoft.web.designer.experimental.mapping;

import static com.google.common.base.Joiner.on;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Inject;
import javax.inject.Named;

import com.google.common.collect.Lists;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.AbstractParametrizedWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonAction;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ButtonWidget;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Labeled;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParameterType;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.ParametrizedWidgetFactory;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Requirable;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.Valuable;
import org.bonitasoft.web.designer.experimental.parametrizedWidget.WidgetContainer;
import org.bonitasoft.web.designer.model.JacksonObjectMapper;
import org.bonitasoft.web.designer.model.contract.Contract;
import org.bonitasoft.web.designer.model.contract.ContractInput;
import org.bonitasoft.web.designer.model.contract.NodeContractInput;
import org.bonitasoft.web.designer.model.page.Component;
import org.bonitasoft.web.designer.model.page.Container;
import org.bonitasoft.web.designer.model.page.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
public class ContractInputToWidgetMapper {

    private static final Logger logger = LoggerFactory.getLogger(ParametrizedWidgetFactory.class);

    public static final String ITEM_ITERATOR = "$item";
    static final String FORM_INPUT_DATA = "formInput";
    static final String FORM_OUTPUT_DATA = "formOutput";
    private ParametrizedWidgetFactory parametrizedWidgetFactory;
    private DimensionFactory dimensionFactory;

    private JacksonObjectMapper objectMapperWrapper;

    @Inject
    public ContractInputToWidgetMapper(DimensionFactory dimensionFactory, JacksonObjectMapper objectMapperWrapper) {
        this.dimensionFactory = dimensionFactory;
        this.objectMapperWrapper = objectMapperWrapper;
        parametrizedWidgetFactory = new ParametrizedWidgetFactory();
    }

    public Element toElement(ContractInput contractInput, List<List<Element>> rows) {
        return contractInput.isMultiple() ? toMultipleComponent(contractInput, rows) : toSimpleComponent(contractInput);
    }

    private Container toMultipleComponent(ContractInput contractInput, List<List<Element>> rows) {
        Container container = toMultipleContainer(contractInput);
        rows.add(Collections.<Element>singletonList(parametrizedWidgetFactory.createTitle(contractInput).toComponent(dimensionFactory)));
        AbstractParametrizedWidget component = parametrizedWidgetFactory.createParametrizedWidget(contractInput);
        if (component instanceof Labeled) {
            component.setLabel("");
            ((Labeled) component).setLabelWidth(0);
        }
        if (component instanceof Valuable) {
            ((Valuable) component).setValue(ITEM_ITERATOR);
        }
        if (component instanceof Requirable) {
            ((Requirable) component).setRequired(false);
        }
        container.getRows().add(Lists.<Element>newArrayList(component.toComponent(dimensionFactory), createRemoveButton()));
        return container;
    }

    private String multipleInputValue(ContractInput contractInput) {
        return contractInput.getParent() != null ? on(".").join(ITEM_ITERATOR, contractInput.getName()) : ITEM_ITERATOR;
    }

    private Container toMultipleContainer(ContractInput contractInput) {
        WidgetContainer multipleContainer = parametrizedWidgetFactory.createWidgetContainer();
        multipleContainer.setRepeatedCollection(isParentMultiple(contractInput) ? multipleInputValue(contractInput) : buildPathForInputValue(contractInput));
        return multipleContainer.toContainer(dimensionFactory);
    }

    private boolean isParentMultiple(ContractInput contractInput) {
        return contractInput.getParent() != null && contractInput.getParent().isMultiple();
    }

    private Container toSimpleContainer(NodeContractInput nodeContractInput) {
        return parametrizedWidgetFactory.createWidgetContainer().toContainer(dimensionFactory);
    }

    private Component toSimpleComponent(ContractInput contractInput) {
        AbstractParametrizedWidget widget = parametrizedWidgetFactory.createParametrizedWidget(contractInput);
        if (widget instanceof Valuable) {
            ((Valuable) widget).setValue(isParentMultiple(contractInput) ? multipleInputValue(contractInput) : buildPathForInputValue(contractInput));
        }
        return widget.toComponent(dimensionFactory);
    }

    public Container toContainer(NodeContractInput nodeContractInput, List<List<Element>> rows) {
        rows.add(Collections.<Element>singletonList(parametrizedWidgetFactory.createTitle(nodeContractInput).toComponent(dimensionFactory)));
        return nodeContractInput.isMultiple() ? toMultipleContainer(nodeContractInput) : toSimpleContainer(nodeContractInput);
    }

    public Component createSubmitButton(Contract contract, ButtonAction actionType) {
        ButtonWidget submitButton = parametrizedWidgetFactory.createSubmitButton(contract, actionType);
        submitButton.setDataToSend(FORM_OUTPUT_DATA);
        submitButton.setTargetUrlOnSuccess("/bonita");
        submitButton.setPropertyValue("disabled", ParameterType.EXPRESSION, "$form.$invalid");
        return submitButton.toComponent(dimensionFactory);
    }

    private String buildPathForInputValue(ContractInput contractInput) {
        List<String> pathNames = newArrayList();
        pathNames.add(contractInput.getName());
        ContractInput pInput = contractInput.getParent();
        while (pInput != null) {
            if (pInput.isMultiple()) {
                pathNames.add(ITEM_ITERATOR);
                break;
            } else {
                pathNames.add(pInput.getName());
                pInput = pInput.getParent();
            }
        }
        if (pathNames.isEmpty()) {
            return null;
        } else if (pInput == null) {
            pathNames.add(FORM_INPUT_DATA);
        }
        return on(".").join(reverse(pathNames));
    }

    public boolean canCreateComponent(ContractInput contractInput) {
        return parametrizedWidgetFactory.isSupported(contractInput);
    }

    public Component createRemoveButton() {
        ButtonWidget removeButton = parametrizedWidgetFactory.createRemoveButton();
        return removeButton.toComponent(dimensionFactory);
    }

    public Component createAddButton(ContractInput contractInput) {
        ButtonWidget addButton = parametrizedWidgetFactory.createAddButton();
        addButton.setCollectionToModify(isParentMultiple(contractInput) ? multipleInputValue(contractInput) : buildPathForInputValue(contractInput));
        if (contractHasInput(contractInput)) {
            addButton.setValueToAdd(getValueToAddFromContract(contractInput));
        }
        return addButton.toComponent(dimensionFactory);
    }

    private boolean contractHasInput(ContractInput contractInput) {
        return contractInput instanceof NodeContractInput
                && contractInput.getInput() != null;
    }

    private String getValueToAddFromContract(ContractInput contractInput) {
        try {
            FormInputVisitor visitor = new FormInputVisitor(objectMapperWrapper);
            for (ContractInput input : contractInput.getInput()) {
                input.accept(visitor);
            }
            return visitor.toJson();
        } catch (IOException e) {
            logger.warn("Impossible to set valueToAdd from ContractInput", e);
        }
        return null;
    }

}
