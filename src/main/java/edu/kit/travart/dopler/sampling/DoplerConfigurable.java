package edu.kit.travart.dopler.sampling;

import at.jku.cps.travart.core.common.IConfigurable;

public class DoplerConfigurable implements IConfigurable {

    private final String name;
    private boolean selected;

    public DoplerConfigurable(String name, boolean selected) {
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
