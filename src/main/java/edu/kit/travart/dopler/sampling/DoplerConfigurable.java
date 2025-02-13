package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;
import edu.kit.dopler.model.Dopler;

/** Implementation of {@link IConfigurable} for the {@link Dopler} model. */
class DoplerConfigurable implements IConfigurable {

    private final String name;
    private boolean selected;

    /**
     * Constructor of {@link DoplerConfigurable}.
     *
     * @param name     Name of the configurable
     * @param selected Boolean value that stores if the configurable is selected
     */
    DoplerConfigurable(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String getName() {
        return name;
    }
}
