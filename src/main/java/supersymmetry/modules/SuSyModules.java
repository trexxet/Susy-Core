package supersymmetry.modules;

import gregtech.api.modules.IModuleContainer;
import supersymmetry.Supersymmetry;

public class SuSyModules implements IModuleContainer {

    public static final String MODULE_BDSAndM = "bdsandm_integration";
    public static final String MODULE_BUBBLES = "baubles_integration";

    @Override
    public String getID() {
        return Supersymmetry.MODID;
    }
}
