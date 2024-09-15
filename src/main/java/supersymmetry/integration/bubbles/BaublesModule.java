package supersymmetry.integration.bubbles;

import baubles.api.BaubleType;
import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.common.items.MetaItems;
import gregtech.integration.IntegrationSubmodule;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import supersymmetry.common.item.behavior.ArmorBaubleBehavior;
import supersymmetry.modules.SuSyModules;

import java.util.Collections;
import java.util.List;

@GregTechModule(
        moduleID = SuSyModules.MODULE_BUBBLES,
        containerID = GTValues.MODID,
        modDependencies = "baubles",
        name = "SuSy Baubles Integration",
        description = "Baubles Integration Module")
public class BaublesModule extends IntegrationSubmodule {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        MetaItems.SEMIFLUID_JETPACK.addComponents(new ArmorBaubleBehavior(BaubleType.BODY));
        MetaItems.ELECTRIC_JETPACK.addComponents(new ArmorBaubleBehavior(BaubleType.BODY));
        MetaItems.ELECTRIC_JETPACK_ADVANCED.addComponents(new ArmorBaubleBehavior(BaubleType.BODY));
    }

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(BaublesModule.class);
    }
}
